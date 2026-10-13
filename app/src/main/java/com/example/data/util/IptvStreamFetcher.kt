package com.example.data.util

import android.util.Log
import com.example.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

/**
 * Utility to fetch and parse channels from M3U playlists and Xtream Codes IPTV accounts
 * for category publishing and direct streaming with intelligent quality clustering.
 */
object IptvStreamFetcher {

    private const val TAG = "IptvStreamFetcher"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    /**
     * Parses channels from an M3U playlist URL with quality deduplication.
     */
    suspend fun fetchM3uChannels(url: String, categoryName: String): List<Channel> = withContext(Dispatchers.IO) {
        try {
            M3uParser.parse(url.trim(), categoryName.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching M3U channels: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches live channels from an Xtream Codes IPTV account (host, username, password)
     * and automatically consolidates multiple quality variants into single unified channels.
     */
    suspend fun fetchXtreamLiveChannels(
        host: String,
        username: String,
        password: String,
        categoryName: String,
        streamExtension: String = "m3u8"
    ): List<Channel> = withContext(Dispatchers.IO) {
        val u = username.trim()
        val p = password.trim()
        val cat = categoryName.trim().ifBlank { "عام" }
        val ext = streamExtension.trim().lowercase().ifBlank { "m3u8" }

        var cleanHost = host.trim()
        if (!cleanHost.startsWith("http://", ignoreCase = true) && !cleanHost.startsWith("https://", ignoreCase = true)) {
            cleanHost = "http://$cleanHost"
        }
        cleanHost = cleanHost.trimEnd('/')

        if (cleanHost.isBlank() || u.isBlank() || p.isBlank()) {
            return@withContext emptyList()
        }

        val rawStreams = mutableListOf<ChannelClusteringEngine.RawStream>()

        // 1. Attempt Xtream Codes API (get_live_streams)
        try {
            val apiUrl = "$cleanHost/player_api.php?username=$u&password=$p&action=get_live_streams"
            val request = Request.Builder()
                .url(apiUrl)
                .header("User-Agent", "IPTVSmarters/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank() && body.trim().startsWith("[")) {
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val name = obj.optString("name").trim()
                        val streamId = obj.optString("stream_id").trim()
                        val icon = obj.optString("stream_icon").trim()

                        if (name.isNotBlank() && streamId.isNotBlank()) {
                            val streamUrl = "$cleanHost/live/$u/$p/$streamId.$ext"
                            rawStreams.add(
                                ChannelClusteringEngine.RawStream(
                                    rawName = name,
                                    streamUrl = streamUrl,
                                    logoUrl = icon,
                                    category = cat,
                                    streamType = if (ext == "ts") "ts" else "m3u8"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Xtream API call failed: ${e.message}, falling back to M3U endpoint")
        }

        // 2. If API returned no channels, fallback to get.php m3u_plus playlist
        if (rawStreams.isEmpty()) {
            try {
                val fallbackM3uUrl = "$cleanHost/get.php?username=$u&password=$p&type=m3u_plus&output=$ext"
                return@withContext M3uParser.parse(fallbackM3uUrl, cat)
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback to Xtream M3U endpoint also failed: ${e2.message}", e2)
            }
        }

        // Cluster and merge quality variants
        ChannelClusteringEngine.clusterRawStreams(rawStreams, cat)
    }
}
