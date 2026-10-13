package com.example.data.util

import com.example.data.model.XtremeContentType
import com.example.data.model.XtremeItem

/**
 * Ultra-fast, lightweight deduplicator for Library content (Movies and Series).
 * 
 * Guarantees:
 * 1. A movie or series is NEVER shown more than once across multiple Xtreme accounts.
 * 2. No quality inspection or duplicate quality copies - each movie or series is shown once.
 * 3. Operates at lightning speed in O(N) time with minimal overhead.
 */
object LibraryDeduplicator {

    private val cleanupRegex = Regex("""[\[\]\(\)\{\}\|_\-:,.\/\\#@!+~?*"'`]+""")
    private val qualityStripRegex = Regex("""(?i)\b(4k|uhd|2160p?|1080p?|fhd|full\s*hd|720p?|hd|480p?|sd|cam|web-?dl|webrip|bluray|remux)\b""")
    private val whitespaceRegex = Regex("""\s+""")

    /**
     * Fast normalization of titles to ensure matching duplicates across accounts
     * (e.g., "Interstellar [4K]" matches "Interstellar", "Inception (2010)" matches "Inception 2010").
     */
    fun normalizeTitle(title: String): String {
        var clean = qualityStripRegex.replace(title, " ")
        clean = cleanupRegex.replace(clean, " ")
        return whitespaceRegex.replace(clean, " ").trim().lowercase()
    }

    /**
     * Deduplicates items so that every movie or series appears only ONCE.
     * Strictly removes any LIVE stream items as well.
     */
    fun deduplicate(items: List<XtremeItem>): List<XtremeItem> {
        if (items.isEmpty()) return emptyList()

        val seen = HashSet<String>(items.size)
        val result = ArrayList<XtremeItem>(items.size)

        for (item in items) {
            // Strictly exclude LIVE streams
            if (item.type == XtremeContentType.LIVE) continue

            val cleanTitle = normalizeTitle(item.title)
            val key = "${item.type.name}##$cleanTitle"

            if (seen.add(key)) {
                result.add(item)
            }
        }

        return result
    }
}
