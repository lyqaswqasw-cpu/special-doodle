package com.example.data.xtreme

import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import com.example.data.model.XtremeAccount
import com.example.data.model.XtremeContentType
import com.example.data.model.XtremeItem
import com.example.data.model.SeriesInfo
import com.example.data.model.VodInfo
import com.example.data.model.XtremeEpisode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class XtremeApiClient {

    companion object {
        private const val TAG = "XtremeApiClient"
    }

    private val client: OkHttpClient by lazy {
        val dispatcher = Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 24
        }
        val pool = ConnectionPool(32, 5, TimeUnit.MINUTES)
        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(pool)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    /**
     * Fetches VOD movies and series directly from the Xtreme Codes server for the Library concurrently.
     * Uses ultra-fast stream parsing with JsonReader to minimize latency and memory footprint.
     * (Live streams are excluded from the Library as requested).
     */
    suspend fun fetchAccountContent(account: XtremeAccount): List<XtremeItem> = withContext(Dispatchers.IO) {
        val cleanHost = account.getCleanHost()
        val u = account.username.trim()
        val p = account.password.trim()

        if (cleanHost.isBlank() || u.isBlank() || p.isBlank()) {
            return@withContext emptyList()
        }

        coroutineScope {
            // Concurrent parallel download of Movies and Series for ultra-fast performance
            val vodDeferred = async {
                val list = mutableListOf<XtremeItem>()
                try {
                    val vodUrl = "$cleanHost/player_api.php?username=$u&password=$p&action=get_vod_streams"
                    executeStreamingGet(vodUrl) { reader ->
                        parseVodStreamsStreaming(reader, cleanHost, u, p, account, list)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error fetching VOD for ${account.name}: ${e.message}")
                }
                list
            }

            val seriesDeferred = async {
                val list = mutableListOf<XtremeItem>()
                try {
                    val seriesUrl = "$cleanHost/player_api.php?username=$u&password=$p&action=get_series"
                    executeStreamingGet(seriesUrl) { reader ->
                        parseSeriesStreaming(reader, cleanHost, u, p, account, list)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error fetching series for ${account.name}: ${e.message}")
                }
                list
            }

            val vodList = vodDeferred.await()
            val seriesList = seriesDeferred.await()
            vodList + seriesList
        }
    }

    suspend fun getSeriesInfo(account: XtremeAccount, seriesId: String): SeriesInfo? = withContext(Dispatchers.IO) {
        val cleanHost = account.getCleanHost()
        val u = account.username.trim()
        val p = account.password.trim()
        
        val url = "$cleanHost/player_api.php?username=$u&password=$p&action=get_series_info&series_id=$seriesId"
        try {
            val jsonString = executeGetRequest(url)
            if (jsonString != null) {
                val obj = JSONObject(jsonString)
                val seasons = mutableListOf<Int>()
                
                // Seasons array can sometimes be an array or object
                val seasonsArray = obj.optJSONArray("seasons")
                if (seasonsArray != null) {
                    for (i in 0 until seasonsArray.length()) {
                        val s = seasonsArray.optJSONObject(i)
                        if (s != null) {
                            val sNum = s.optInt("season_number", -1)
                            if (sNum != -1) seasons.add(sNum)
                        }
                    }
                }
                
                val episodesMap = mutableMapOf<Int, MutableList<XtremeEpisode>>()
                val epsObj = obj.optJSONObject("episodes")
                if (epsObj != null) {
                    val iter = epsObj.keys()
                    while (iter.hasNext()) {
                        val seasonKey = iter.next()
                        val seasonNum = seasonKey.toIntOrNull() ?: 1
                        val seasonEps = epsObj.optJSONArray(seasonKey)
                        if (seasonEps != null) {
                            val epList = episodesMap.getOrPut(seasonNum) { mutableListOf() }
                            for (i in 0 until seasonEps.length()) {
                                val ep = seasonEps.optJSONObject(i) ?: continue
                                val streamId = ep.optString("id").ifBlank { ep.optString("stream_id") }
                                if (streamId.isBlank()) continue
                                
                                val epNum = ep.optInt("episode_num", i + 1)
                                val title = ep.optString("title").ifBlank { "الحلقة $epNum" }
                                val icon = fixIconUrl(
                                    ep.optJSONObject("info")?.optString("movie_image")
                                        ?: ep.optString("info.movie_image")
                                        ?: ep.optString("movie_image", "")
                                )
                                val ext = ep.optString("container_extension", "mp4").ifBlank { "mp4" }
                                
                                epList.add(
                                    XtremeEpisode(
                                        id = "${account.id}_ep_$streamId",
                                        title = title,
                                        episodeNum = epNum,
                                        seasonNum = seasonNum,
                                        streamId = streamId,
                                        streamIcon = icon,
                                        streamUrl = "$cleanHost/series/$u/$p/$streamId.$ext",
                                        containerExtension = ext
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // Fallback if episodes is an array
                    val epsArray = obj.optJSONArray("episodes")
                    if (epsArray != null) {
                        for (i in 0 until epsArray.length()) {
                            val ep = epsArray.optJSONObject(i) ?: continue
                            val streamId = ep.optString("id").ifBlank { ep.optString("stream_id") }
                            if (streamId.isBlank()) continue
                            
                            val seasonNum = ep.optInt("season_num", ep.optInt("season", 1))
                            val epNum = ep.optInt("episode_num", i + 1)
                            val title = ep.optString("title").ifBlank { "الحلقة $epNum" }
                            val icon = fixIconUrl(
                                ep.optJSONObject("info")?.optString("movie_image")
                                    ?: ep.optString("movie_image", "")
                            )
                            val ext = ep.optString("container_extension", "mp4").ifBlank { "mp4" }
                            
                            val epList = episodesMap.getOrPut(seasonNum) { mutableListOf() }
                            epList.add(
                                XtremeEpisode(
                                    id = "${account.id}_ep_$streamId",
                                    title = title,
                                    episodeNum = epNum,
                                    seasonNum = seasonNum,
                                    streamId = streamId,
                                    streamIcon = icon,
                                    streamUrl = "$cleanHost/series/$u/$p/$streamId.$ext",
                                    containerExtension = ext
                                )
                            )
                        }
                    }
                }
                
                if (seasons.isEmpty()) {
                    seasons.addAll(episodesMap.keys.sorted())
                }
                if (seasons.isEmpty() && episodesMap.isNotEmpty()) {
                    seasons.add(1)
                }
                
                val infoObj = obj.optJSONObject("info")
                val name = infoObj?.optString("name", "") ?: ""
                val coverUrl = fixIconUrl(infoObj?.optString("cover", "") ?: infoObj?.optString("movie_image", ""))
                val plot = infoObj?.optString("plot", "") ?: ""
                val cast = infoObj?.optString("cast", "") ?: ""
                val rating = infoObj?.optString("rating", "") ?: ""
                val director = infoObj?.optString("director", "") ?: ""
                val genre = infoObj?.optString("genre", "") ?: ""
                val releaseDate = infoObj?.optString("releaseDate", "") ?: ""
                
                return@withContext SeriesInfo(
                    name = name,
                    coverUrl = coverUrl,
                    plot = plot,
                    cast = cast,
                    rating = rating,
                    director = director,
                    genre = genre,
                    releaseDate = releaseDate,
                    seasons = seasons.sorted().distinct(),
                    episodes = episodesMap
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching series info: ${e.message}")
        }
        return@withContext null
    }
    
    suspend fun getVodInfo(account: XtremeAccount, vodId: String): VodInfo? = withContext(Dispatchers.IO) {
        val cleanHost = account.getCleanHost()
        val u = account.username.trim()
        val p = account.password.trim()
        
        val url = "$cleanHost/player_api.php?username=$u&password=$p&action=get_vod_info&vod_id=$vodId"
        try {
            val jsonString = executeGetRequest(url)
            if (jsonString != null) {
                val obj = JSONObject(jsonString)
                val infoObj = obj.optJSONObject("info")
                val movieData = obj.optJSONObject("movie_data")
                
                val name = infoObj?.optString("name", "") ?: movieData?.optString("name", "") ?: ""
                val coverUrl = fixIconUrl(infoObj?.optString("movie_image", "") ?: infoObj?.optString("cover", ""))
                val plot = infoObj?.optString("plot", "") ?: infoObj?.optString("description", "") ?: ""
                val cast = infoObj?.optString("cast", "") ?: infoObj?.optString("actors", "") ?: ""
                val rating = infoObj?.optString("rating", "") ?: ""
                val director = infoObj?.optString("director", "") ?: ""
                val genre = infoObj?.optString("genre", "") ?: ""
                val releaseDate = infoObj?.optString("releasedate", "") ?: ""
                
                val ext = movieData?.optString("container_extension", "mp4") ?: "mp4"
                val streamUrl = "$cleanHost/movie/$u/$p/$vodId.$ext"

                return@withContext VodInfo(
                    name = name,
                    coverUrl = coverUrl,
                    plot = plot,
                    cast = cast,
                    rating = rating,
                    director = director,
                    genre = genre,
                    releaseDate = releaseDate,
                    streamUrl = streamUrl,
                    containerExtension = ext
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vod info: ${e.message}")
        }
        return@withContext null
    }

    private fun fixIconUrl(url: String?): String {
        if (url.isNullOrBlank()) return ""
        var fixedUrl = url.replace("\\/", "/")
        if (fixedUrl.startsWith("//")) fixedUrl = "http:$fixedUrl"
        return fixedUrl.trim()
    }

    private fun executeStreamingGet(url: String, onStream: (JsonReader) -> Unit): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "IPTVSmarters/2.0")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.let { body ->
                    body.charStream().use { charStream ->
                        JsonReader(charStream).apply { isLenient = true }.use { reader ->
                            onStream(reader)
                        }
                    }
                    true
                } ?: false
            } else {
                Log.w(TAG, "HTTP error ${response.code} for url $url")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "HTTP streaming call failed for url $url: ${e.message}")
            false
        }
    }

    private fun readSafeString(reader: JsonReader): String {
        return try {
            when (reader.peek()) {
                JsonToken.STRING, JsonToken.NUMBER -> reader.nextString().trim()
                JsonToken.BOOLEAN -> reader.nextBoolean().toString()
                JsonToken.NULL -> {
                    reader.nextNull()
                    ""
                }
                else -> {
                    reader.skipValue()
                    ""
                }
            }
        } catch (_: Exception) {
            try { reader.skipValue() } catch (_: Exception) {}
            ""
        }
    }

    private fun parseVodStreamsStreaming(
        reader: JsonReader,
        host: String,
        u: String,
        p: String,
        account: XtremeAccount,
        output: MutableList<XtremeItem>
    ) {
        if (reader.peek() != JsonToken.BEGIN_ARRAY) {
            Log.w(TAG, "parseVodStreamsStreaming: expected BEGIN_ARRAY, got ${reader.peek()}")
            return
        }
        reader.beginArray()
        while (reader.hasNext()) {
            if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                reader.skipValue()
                continue
            }
            reader.beginObject()
            var streamId = ""
            var name = "فيلم"
            var icon = ""
            var catId = ""
            var ext = "mp4"

            while (reader.hasNext()) {
                val fieldName = reader.nextName()
                when (fieldName) {
                    "stream_id", "id" -> {
                        val idVal = readSafeString(reader)
                        if (idVal.isNotBlank()) streamId = idVal
                    }
                    "name", "title" -> {
                        val n = readSafeString(reader)
                        if (n.isNotBlank()) name = n
                    }
                    "stream_icon", "cover" -> {
                        val ic = readSafeString(reader)
                        if (ic.isNotBlank()) icon = ic
                    }
                    "category_id" -> {
                        catId = readSafeString(reader)
                    }
                    "container_extension" -> {
                        val e = readSafeString(reader)
                        if (e.isNotBlank()) ext = e
                    }
                    else -> {
                        try {
                            reader.skipValue()
                        } catch (_: Exception) {
                            break
                        }
                    }
                }
            }
            try {
                reader.endObject()
            } catch (_: Exception) {}

            if (streamId.isNotBlank()) {
                output.add(
                    XtremeItem(
                        id = "${account.id}_vod_$streamId",
                        title = name,
                        streamId = streamId,
                        streamIcon = fixIconUrl(icon),
                        type = XtremeContentType.MOVIE,
                        categoryId = catId,
                        streamUrl = "$host/movie/$u/$p/$streamId.$ext",
                        accountName = account.name,
                        accountId = account.id,
                        containerExtension = ext
                    )
                )
            }
        }
        try {
            reader.endArray()
        } catch (_: Exception) {}
    }

    private fun parseSeriesStreaming(
        reader: JsonReader,
        host: String,
        u: String,
        p: String,
        account: XtremeAccount,
        output: MutableList<XtremeItem>
    ) {
        if (reader.peek() != JsonToken.BEGIN_ARRAY) {
            Log.w(TAG, "parseSeriesStreaming: expected BEGIN_ARRAY, got ${reader.peek()}")
            return
        }
        reader.beginArray()
        while (reader.hasNext()) {
            if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                reader.skipValue()
                continue
            }
            reader.beginObject()
            var seriesId = ""
            var name = "مسلسل"
            var icon = ""
            var catId = ""

            while (reader.hasNext()) {
                val fieldName = reader.nextName()
                when (fieldName) {
                    "series_id", "id" -> {
                        val idVal = readSafeString(reader)
                        if (idVal.isNotBlank()) seriesId = idVal
                    }
                    "name", "title" -> {
                        val n = readSafeString(reader)
                        if (n.isNotBlank()) name = n
                    }
                    "cover", "stream_icon" -> {
                        val ic = readSafeString(reader)
                        if (ic.isNotBlank()) icon = ic
                    }
                    "category_id" -> {
                        catId = readSafeString(reader)
                    }
                    else -> {
                        try {
                            reader.skipValue()
                        } catch (_: Exception) {
                            break
                        }
                    }
                }
            }
            try {
                reader.endObject()
            } catch (_: Exception) {}

            if (seriesId.isNotBlank()) {
                output.add(
                    XtremeItem(
                        id = "${account.id}_series_$seriesId",
                        title = name,
                        streamId = seriesId,
                        streamIcon = fixIconUrl(icon),
                        type = XtremeContentType.SERIES,
                        categoryId = catId,
                        streamUrl = "$host/series/$u/$p/$seriesId.mp4",
                        accountName = account.name,
                        accountId = account.id,
                        containerExtension = "mp4"
                    )
                )
            }
        }
        try {
            reader.endArray()
        } catch (_: Exception) {}
    }

    private fun executeGetRequest(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "IPTVSmarters/2.0")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "HTTP call failed for url $url: ${e.message}")
            null
        }
    }

    private fun parseLiveStreams(
        jsonString: String,
        host: String,
        u: String,
        p: String,
        account: XtremeAccount,
        output: MutableList<XtremeItem>
    ) {
        val ext = account.streamExtension.ifBlank { "m3u8" }
        parseLiveStreamsWithExt(jsonString, host, u, p, ext, account, emptyMap(), output)
    }

    suspend fun fetchLiveCategories(account: XtremeAccount): Map<String, String> = withContext(Dispatchers.IO) {
        val cleanHost = account.getCleanHost()
        val u = account.username.trim()
        val p = account.password.trim()
        if (cleanHost.isBlank() || u.isBlank() || p.isBlank()) return@withContext emptyMap()

        val map = mutableMapOf<String, String>()
        val url = "$cleanHost/player_api.php?username=$u&password=$p&action=get_live_categories"
        try {
            val json = executeGetRequest(url)
            if (json != null) {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val id = obj.optString("category_id")
                    val name = obj.optString("category_name")
                    if (id.isNotBlank() && name.isNotBlank()) {
                        map[id] = name
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching live categories: ${e.message}")
        }
        map
    }

    suspend fun fetchLiveStreams(account: XtremeAccount): List<XtremeItem> = withContext(Dispatchers.IO) {
        val cleanHost = account.getCleanHost()
        val u = account.username.trim()
        val p = account.password.trim()
        val ext = account.streamExtension.ifBlank { "m3u8" }

        if (cleanHost.isBlank() || u.isBlank() || p.isBlank()) {
            return@withContext emptyList()
        }

        val categoriesMap = fetchLiveCategories(account)
        val list = mutableListOf<XtremeItem>()
        val liveUrl = "$cleanHost/player_api.php?username=$u&password=$p&action=get_live_streams"

        try {
            executeStreamingGet(liveUrl) { reader ->
                parseLiveStreamsStreaming(reader, cleanHost, u, p, ext, account, categoriesMap, list)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Streaming live failed: ${e.message}, falling back to buffered JSON")
            try {
                val json = executeGetRequest(liveUrl)
                if (json != null) {
                    parseLiveStreamsWithExt(json, cleanHost, u, p, ext, account, categoriesMap, list)
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback live fetch failed: ${e2.message}")
            }
        }
        list
    }

    private fun parseLiveStreamsStreaming(
        reader: JsonReader,
        host: String,
        u: String,
        p: String,
        ext: String,
        account: XtremeAccount,
        categoriesMap: Map<String, String>,
        output: MutableList<XtremeItem>
    ) {
        if (reader.peek() != JsonToken.BEGIN_ARRAY) {
            return
        }
        reader.beginArray()
        while (reader.hasNext()) {
            if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                reader.skipValue()
                continue
            }
            reader.beginObject()
            var streamId = ""
            var name = "قناة مباشرة"
            var icon = ""
            var catId = ""

            while (reader.hasNext()) {
                val fieldName = reader.nextName()
                when (fieldName) {
                    "stream_id", "id" -> {
                        val idVal = readSafeString(reader)
                        if (idVal.isNotBlank()) streamId = idVal
                    }
                    "name", "title" -> {
                        val n = readSafeString(reader)
                        if (n.isNotBlank()) name = n
                    }
                    "stream_icon", "logo", "icon" -> {
                        val ic = readSafeString(reader)
                        if (ic.isNotBlank()) icon = ic
                    }
                    "category_id" -> {
                        catId = readSafeString(reader)
                    }
                    else -> {
                        try { reader.skipValue() } catch (_: Exception) { break }
                    }
                }
            }
            try { reader.endObject() } catch (_: Exception) {}

            if (streamId.isNotBlank()) {
                val catName = categoriesMap[catId] ?: ""
                val streamUrl = "$host/live/$u/$p/$streamId.$ext"
                output.add(
                    XtremeItem(
                        id = "${account.id}_live_$streamId",
                        title = name,
                        streamId = streamId,
                        streamIcon = fixIconUrl(icon),
                        type = XtremeContentType.LIVE,
                        categoryId = catId,
                        categoryName = catName,
                        streamUrl = streamUrl,
                        accountName = account.name,
                        accountId = account.id,
                        containerExtension = ext
                    )
                )
            }
        }
        try { reader.endArray() } catch (_: Exception) {}
    }

    private fun parseLiveStreamsWithExt(
        jsonString: String,
        host: String,
        u: String,
        p: String,
        ext: String,
        account: XtremeAccount,
        categoriesMap: Map<String, String>,
        output: MutableList<XtremeItem>
    ) {
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val streamId = obj.optString("stream_id").ifBlank { obj.optInt("stream_id").toString() }
            if (streamId.isBlank()) continue
            val name = obj.optString("name", "قناة مباشرة")
            val icon = fixIconUrl(obj.optString("stream_icon", ""))
            val catId = obj.optString("category_id", "")
            val catName = categoriesMap[catId] ?: ""
            val streamUrl = "$host/live/$u/$p/$streamId.$ext"

            output.add(
                XtremeItem(
                    id = "${account.id}_live_$streamId",
                    title = name,
                    streamId = streamId,
                    streamIcon = icon,
                    type = XtremeContentType.LIVE,
                    categoryId = catId,
                    categoryName = catName,
                    streamUrl = streamUrl,
                    accountName = account.name,
                    accountId = account.id,
                    containerExtension = ext
                )
            )
        }
    }

    private fun parseVodStreams(
        jsonString: String,
        host: String,
        u: String,
        p: String,
        account: XtremeAccount,
        output: MutableList<XtremeItem>
    ) {
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val streamId = obj.optString("stream_id").ifBlank { obj.optInt("stream_id").toString() }
            val name = obj.optString("name", "فيلم")
            val icon = fixIconUrl(obj.optString("stream_icon", ""))
            val catId = obj.optString("category_id", "")
            val ext = obj.optString("container_extension", "mp4").ifBlank { "mp4" }
            val streamUrl = "$host/movie/$u/$p/$streamId.$ext"

            output.add(
                XtremeItem(
                    id = "${account.id}_vod_$streamId",
                    title = name,
                    streamId = streamId,
                    streamIcon = icon,
                    type = XtremeContentType.MOVIE,
                    categoryId = catId,
                    streamUrl = streamUrl,
                    accountName = account.name,
                    accountId = account.id,
                    containerExtension = ext
                )
            )
        }
    }

    private fun parseSeries(
        jsonString: String,
        host: String,
        u: String,
        p: String,
        account: XtremeAccount,
        output: MutableList<XtremeItem>
    ) {
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val seriesId = obj.optString("series_id").ifBlank { obj.optInt("series_id").toString() }
            val name = obj.optString("name", "مسلسل")
            val icon = fixIconUrl(obj.optString("cover", ""))
            val catId = obj.optString("category_id", "")
            val streamUrl = "$host/series/$u/$p/$seriesId.mp4"

            output.add(
                XtremeItem(
                    id = "${account.id}_series_$seriesId",
                    title = name,
                    streamId = seriesId,
                    streamIcon = icon,
                    type = XtremeContentType.SERIES,
                    categoryId = catId,
                    streamUrl = streamUrl,
                    accountName = account.name,
                    accountId = account.id,
                    containerExtension = "mp4"
                )
            )
        }
    }
}
