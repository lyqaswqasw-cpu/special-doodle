package com.example.data.util

import com.example.data.model.Channel
import com.example.data.model.ChannelServer
import java.util.UUID

/**
 * Intelligent Channel Clustering & Quality Server Merging Engine.
 * 
 * Automatically detects multiple stream qualities (4K, FHD, 1080p, HD, 720p, SD, 480p, Low, 240p, HEVC)
 * and server variations for any TV channel (beIN Sports, SSC, AD Sports, Alkass, MBC, OSN, Canal+, Sky, etc.)
 * and merges them into a SINGLE clean Channel with distinct quality servers inside.
 */
object ChannelClusteringEngine {

    data class RawStream(
        val rawName: String,
        val streamUrl: String,
        val logoUrl: String = "",
        val category: String = "عام",
        val streamType: String = "m3u8"
    )

    private data class QualityInfo(
        val qualityTag: String,
        val displayName: String,
        val priority: Int
    )

    // Regex to match prefix brackets/tags like |AR|, [AR], AR:, ARABIC:, |VIP|, |BEIN|, etc.
    private val prefixTagsRegex = Regex("""^(?:\|[^|]+\||\[[^\]]+\]|\([^\)]+\)|[A-Za-z0-9_-]+:)\s*""")

    // Regex to detect quality tokens anywhere in name
    private val quality4kRegex = Regex("""(?i)\b(4k|uhd|2160p?|2160)\b""")
    private val qualityFhdRegex = Regex("""(?i)\b(fhd\+?|1080p?|full\s*hd|1080i)\b""")
    private val qualityHdRegex = Regex("""(?i)\b(hd\+?|720p?|high\s*quality|hq)\b""")
    private val qualitySdRegex = Regex("""(?i)\b(sd|576p?|480p?|576i|480i|standard)\b""")
    private val qualityLowRegex = Regex("""(?i)\b(low|lq|240p?|360p?|144p?|mobile|ضعيفة|منخفضة)\b""")
    private val qualityHevcRegex = Regex("""(?i)\b(hevc|h\.?265)\b""")

    // Regex to strip qualities and cleanup junk from channel name
    private val stripQualityKeywordsRegex = Regex(
        """(?i)\b(4k|uhd|2160p?|fhd\+?|fhd|full\s*hd|1080p?|1080i|hd\+?|hd|720p?|hq|sd|576p?|480p?|360p?|240p?|144p?|low|lq|hevc|h\.?265|vip|backup|auto|multi|server\s*\d+|serveur\s*\d+|src\s*\d+|سيرفر\s*\d+|مصدر\s*\d+)\b"""
    )
    private val bracketJunkRegex = Regex("""[\[\(\{][^\]\)\}]*(?:p|4k|hd|fhd|sd|hevc|fps|mbps|low|hq|uhd)[^\]\)\}]*[\]\)\}]""", RegexOption.IGNORE_CASE)
    private val trailingSymbolsRegex = Regex("""[\s|_\-:,]+$""")
    private val leadingSymbolsRegex = Regex("""^[\s|_\-:,]+""")
    private val extraSpacesRegex = Regex("""\s+""")

    /**
     * Extracts detected quality info from the raw channel title or stream URL.
     */
    private fun detectQuality(rawTitle: String, streamUrl: String): QualityInfo {
        val combined = "$rawTitle $streamUrl"
        val isHevc = qualityHevcRegex.containsMatchIn(combined)
        val hevcSuffix = if (isHevc) " HEVC" else ""

        return when {
            quality4kRegex.containsMatchIn(combined) -> QualityInfo("4K", "سيرفر 4K UHD$hevcSuffix", 100)
            qualityFhdRegex.containsMatchIn(combined) -> QualityInfo("FHD", "سيرفر FHD (1080p)$hevcSuffix", 80)
            qualityHdRegex.containsMatchIn(combined) -> QualityInfo("HD", "سيرفر HD (720p)$hevcSuffix", 60)
            qualitySdRegex.containsMatchIn(combined) -> QualityInfo("SD", "سيرفر SD (480p)$hevcSuffix", 40)
            qualityLowRegex.containsMatchIn(combined) -> QualityInfo("Low", "سيرفر جودة منخفضة (240p)$hevcSuffix", 20)
            isHevc -> QualityInfo("HEVC", "سيرفر HEVC", 50)
            else -> QualityInfo("Standard", "سيرفر رئيسي", 30)
        }
    }

    /**
     * Cleans and normalizes the channel title to its base name.
     * E.g. "|AR| BEIN SPORTS 1 HD [1080P]" -> "beIN SPORTS 1"
     * "SSC 1 SD (576p)" -> "SSC 1"
     * "AD SPORTS PREMIUM 1 FHD" -> "AD SPORTS PREMIUM 1"
     */
    fun cleanChannelName(rawName: String): String {
        var clean = rawName.trim()

        // 1. Remove bracketed quality junk like [1080p], (HD), [HEVC], (240p)
        clean = bracketJunkRegex.replace(clean, " ")

        // 2. Remove leading prefix tags like |AR|, [AR], AR:, ARABIC:
        clean = prefixTagsRegex.replace(clean, "")

        // 3. Remove standalone quality keywords
        clean = stripQualityKeywordsRegex.replace(clean, " ")

        // 4. Clean extra symbols
        clean = leadingSymbolsRegex.replace(clean, "")
        clean = trailingSymbolsRegex.replace(clean, "")
        clean = extraSpacesRegex.replace(clean, " ").trim()

        // If after cleaning name becomes empty, fallback to original trimmed
        if (clean.isBlank()) {
            clean = rawName.trim()
        }

        // Standardize common Arabic/English channel naming conventions
        clean = clean
            .replace(Regex("""(?i)\bbein\s*sport\b"""), "beIN SPORTS")
            .replace(Regex("""(?i)\bbein\s*sports\b"""), "beIN SPORTS")
            .replace(Regex("""(?i)\bssc\s*sport\b"""), "SSC")
            .replace(Regex("""(?i)\balkass\b"""), "Alkass")
            .replace(Regex("""(?i)\bad\s*sport\b"""), "AD Sports")
            .replace(Regex("""(?i)\bad\s*sports\b"""), "AD Sports")

        return clean
    }

    /**
     * Cleans and sanitizes raw logo URLs extracted from M3U or Xtream API.
     */
    fun cleanLogoUrl(rawLogo: String): String {
        var logo = rawLogo.trim().removeSurrounding("\"").removeSurrounding("'").trim()
        if (logo.isBlank() || logo.equals("null", ignoreCase = true) || logo.equals("undefined", ignoreCase = true)) return ""
        if (logo.startsWith("//")) {
            logo = "https:$logo"
        }
        return logo
    }

    /**
     * Normalizes a clean name to a grouping key for clustering.
     */
    private fun getGroupKey(cleanName: String, category: String): String {
        val normalized = cleanName.lowercase()
            .replace(Regex("""[^a-z0-9\u0600-\u06FF]"""), "")
        return "${category.trim().lowercase()}##$normalized"
    }

    /**
     * Clusters a list of raw stream items into consolidated Channels with quality servers.
     */
    fun clusterRawStreams(rawStreams: List<RawStream>, defaultCategory: String): List<Channel> {
        if (rawStreams.isEmpty()) return emptyList()

        val groupMap = LinkedHashMap<String, MutableList<Pair<RawStream, QualityInfo>>>()
        val bestNameMap = LinkedHashMap<String, String>()
        val bestLogoMap = LinkedHashMap<String, String>()

        for (item in rawStreams) {
            if (item.streamUrl.isBlank() || item.rawName.isBlank()) continue

            val cat = item.category.ifBlank { defaultCategory }.ifBlank { "عام" }
            val cleanName = cleanChannelName(item.rawName)
            val groupKey = getGroupKey(cleanName, cat)
            val quality = detectQuality(item.rawName, item.streamUrl)

            if (!groupMap.containsKey(groupKey)) {
                groupMap[groupKey] = mutableListOf()
                bestNameMap[groupKey] = cleanName
            }
            groupMap[groupKey]?.add(Pair(item, quality))

            // Keep the best non-empty logo
            val cleanLogo = cleanLogoUrl(item.logoUrl)
            if (cleanLogo.isNotBlank() && bestLogoMap[groupKey].isNullOrBlank()) {
                bestLogoMap[groupKey] = cleanLogo
            }
        }

        val clusteredChannels = mutableListOf<Channel>()

        for ((groupKey, streamPairs) in groupMap) {
            val displayName = bestNameMap[groupKey] ?: "قناة"
            val logo = bestLogoMap[groupKey] ?: cleanLogoUrl(streamPairs.firstOrNull { it.first.logoUrl.isNotBlank() }?.first?.logoUrl ?: "")
            val cat = streamPairs.firstOrNull()?.first?.category?.ifBlank { defaultCategory } ?: defaultCategory

            // Sort servers by quality priority descending (4K -> FHD -> HD -> SD -> Low)
            val sortedPairs = streamPairs.sortedByDescending { it.second.priority }

            // Deduplicate same stream URLs within the channel
            val seenUrls = HashSet<String>()
            val servers = mutableListOf<ChannelServer>()
            val qualityCounts = mutableMapOf<String, Int>()

            for ((streamItem, qInfo) in sortedPairs) {
                val cleanUrl = streamItem.streamUrl.trim()
                if (seenUrls.add(cleanUrl)) {
                    val count = qualityCounts.getOrDefault(qInfo.qualityTag, 0) + 1
                    qualityCounts[qInfo.qualityTag] = count

                    val serverLabel = if (streamPairs.size > 1) {
                        if (count > 1) {
                            "${qInfo.displayName} (سيرفر $count)"
                        } else {
                            qInfo.displayName
                        }
                    } else {
                        "سيرفر رئيسي"
                    }

                    val streamType = when {
                        cleanUrl.endsWith(".m3u8", ignoreCase = true) || streamItem.streamType == "m3u8" -> "m3u8"
                        cleanUrl.endsWith(".ts", ignoreCase = true) || streamItem.streamType == "ts" -> "ts"
                        else -> "normal"
                    }

                    val serverId = "srv_" + UUID.randomUUID().toString().take(8)
                    servers.add(
                        ChannelServer(
                            id = serverId,
                            name = serverLabel,
                            streamUrl = cleanUrl,
                            streamType = streamType
                        )
                    )
                }
            }

            if (servers.isNotEmpty()) {
                val channelId = "ch_" + UUID.nameUUIDFromBytes("${cat}_${displayName}".toByteArray()).toString().take(12)
                clusteredChannels.add(
                    Channel(
                        id = channelId,
                        name = displayName,
                        imageUrl = logo,
                        category = cat,
                        servers = servers
                    )
                )
            }
        }

        return clusteredChannels
    }
}
