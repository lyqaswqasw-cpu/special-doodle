package com.example.data.util

import android.util.Log
import com.example.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object M3uParser {

    private const val TAG = "M3uParser"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    suspend fun parse(url: String, categoryName: String): List<Channel> = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return@withContext emptyList()

        val fullUrl = if (!cleanUrl.startsWith("http://", ignoreCase = true) && !cleanUrl.startsWith("https://", ignoreCase = true)) {
            "http://$cleanUrl"
        } else {
            cleanUrl
        }

        val rawItems = mutableListOf<ChannelClusteringEngine.RawStream>()
        try {
            val request = Request.Builder()
                .url(fullUrl)
                .header("User-Agent", "IPTVSmarters/1.0 (Linux; Android)")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val reader = response.body?.charStream()?.buffered()
                if (reader != null) {
                    var currentName = ""
                    var currentLogo = ""
                    var currentGroup = categoryName.trim().ifBlank { "عام" }

                    val logoRegex = Regex("""(?:tvg-logo|logo)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))""", RegexOption.IGNORE_CASE)
                    val groupRegex = Regex("""(?:group-title)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))""", RegexOption.IGNORE_CASE)
                    val nameAttrRegex = Regex("""(?:tvg-name|name)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))""", RegexOption.IGNORE_CASE)

                    reader.useLines { lines ->
                        for (line in lines) {
                            val trimmed = line.trim()
                            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                                // Extract logo cleanly from any captured group
                                val logoMatch = logoRegex.find(trimmed)
                                val logo = logoMatch?.let { m ->
                                    m.groupValues[1].ifBlank { m.groupValues[2].ifBlank { m.groupValues[3] } }
                                }
                                if (!logo.isNullOrBlank()) {
                                    currentLogo = logo.trim()
                                }

                                // Extract group-title
                                val groupMatch = groupRegex.find(trimmed)
                                val parsedGroup = groupMatch?.let { m ->
                                    m.groupValues[1].ifBlank { m.groupValues[2].ifBlank { m.groupValues[3] } }
                                }
                                if (!parsedGroup.isNullOrBlank() && categoryName.isBlank()) {
                                    currentGroup = parsedGroup.trim()
                                }

                                // Extract name (prefer substring after last comma, fallback to tvg-name)
                                val namePart = trimmed.substringAfterLast(",", "").trim()
                                if (namePart.isNotEmpty()) {
                                    currentName = namePart
                                } else {
                                    val tvgMatch = nameAttrRegex.find(trimmed)
                                    val tvgName = tvgMatch?.let { m ->
                                        m.groupValues[1].ifBlank { m.groupValues[2].ifBlank { m.groupValues[3] } }
                                    }
                                    if (!tvgName.isNullOrBlank()) {
                                        currentName = tvgName.trim()
                                    }
                                }
                            } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                                // It's a stream URL
                                if (currentName.isNotEmpty()) {
                                    val streamType = when {
                                        trimmed.endsWith(".m3u8", ignoreCase = true) -> "m3u8"
                                        trimmed.endsWith(".ts", ignoreCase = true) -> "ts"
                                        else -> "normal"
                                    }
                                    rawItems.add(
                                        ChannelClusteringEngine.RawStream(
                                            rawName = currentName,
                                            streamUrl = trimmed,
                                            logoUrl = currentLogo,
                                            category = currentGroup,
                                            streamType = streamType
                                        )
                                    )
                                    currentName = ""
                                    currentLogo = ""
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing M3U from $fullUrl: ${e.message}", e)
        }

        // Apply intelligent quality clustering & deduplication
        ChannelClusteringEngine.clusterRawStreams(rawItems, categoryName.ifBlank { "عام" })
    }
}
