package com.example.data.model

import java.util.UUID

data class EventBanner(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val imageUrl: String = "",
    val showWatchNow: Boolean = true,
    val watchNowUrl: String = "",
    val channelId: String = "",
    val channelName: String = "",
    val channelIds: List<String> = emptyList(),
    val channelNames: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Returns all associated channel IDs (combining legacy single channelId and new multiple channelIds).
     */
    fun getAllChannelIds(): List<String> {
        val list = mutableListOf<String>()
        if (channelIds.isNotEmpty()) {
            list.addAll(channelIds)
        }
        if (channelId.isNotBlank() && !list.contains(channelId)) {
            list.add(0, channelId)
        }
        return list
    }

    /**
     * Returns all associated channel names.
     */
    fun getAllChannelNames(): List<String> {
        val list = mutableListOf<String>()
        if (channelNames.isNotEmpty()) {
            list.addAll(channelNames)
        }
        if (channelName.isNotBlank() && !list.contains(channelName)) {
            list.add(0, channelName)
        }
        return list
    }

    fun toMap(): Map<String, Any?> {
        val allIds = getAllChannelIds()
        val allNames = getAllChannelNames()
        return mapOf(
            "id" to id,
            "title" to title,
            "imageUrl" to imageUrl,
            "showWatchNow" to showWatchNow,
            "watchNowUrl" to watchNowUrl,
            "channelId" to (allIds.firstOrNull() ?: channelId),
            "channelName" to (allNames.firstOrNull() ?: channelName),
            "channelIds" to allIds,
            "channelNames" to allNames,
            "createdAt" to createdAt,
            "appVersion" to "1.0.0"
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): EventBanner {
            val rawChannelIds = map["channelIds"] as? List<*>
            val parsedChannelIds = rawChannelIds?.mapNotNull { it?.toString() } ?: emptyList()

            val rawChannelNames = map["channelNames"] as? List<*>
            val parsedChannelNames = rawChannelNames?.mapNotNull { it?.toString() } ?: emptyList()

            val singleId = map["channelId"] as? String ?: ""
            val singleName = map["channelName"] as? String ?: ""

            return EventBanner(
                id = id,
                title = map["title"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                showWatchNow = map["showWatchNow"] as? Boolean ?: true,
                watchNowUrl = map["watchNowUrl"] as? String ?: "",
                channelId = singleId,
                channelName = singleName,
                channelIds = if (parsedChannelIds.isNotEmpty()) parsedChannelIds else if (singleId.isNotBlank()) listOf(singleId) else emptyList(),
                channelNames = if (parsedChannelNames.isNotEmpty()) parsedChannelNames else if (singleName.isNotBlank()) listOf(singleName) else emptyList(),
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
