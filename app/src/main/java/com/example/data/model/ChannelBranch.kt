package com.example.data.model

import java.util.UUID

/**
 * Represents a sub-branch (فرع) inside a Category.
 * For example: Category "كرة قدم" can contain branches like "قنوات الكأس", "قنوات SSC", etc.
 * Each branch has a name, image URL, parent category, and a list of channel IDs.
 */
data class ChannelBranch(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val imageUrl: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val channelIds: List<String> = emptyList(),
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "name" to name,
            "categoryId" to categoryId,
            "categoryName" to categoryName,
            "channelIds" to channelIds,
            "order" to order,
            "createdAt" to createdAt
        )
        if (imageUrl.isNotBlank()) {
            map["imageUrl"] = imageUrl
        }
        return map
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): ChannelBranch {
            val chIdsRaw = map["channelIds"]
            val chList = mutableListOf<String>()
            when (chIdsRaw) {
                is List<*> -> {
                    for (item in chIdsRaw) {
                        if (item is String && item.isNotBlank()) {
                            chList.add(item)
                        }
                    }
                }
                is Map<*, *> -> {
                    for ((_, item) in chIdsRaw) {
                        if (item is String && item.isNotBlank()) {
                            chList.add(item)
                        }
                    }
                }
            }

            return ChannelBranch(
                id = (map["id"] as? String)?.takeIf { it.isNotBlank() } ?: id,
                name = map["name"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                categoryId = map["categoryId"] as? String ?: "",
                categoryName = map["categoryName"] as? String ?: "",
                channelIds = chList,
                order = (map["order"] as? Number)?.toInt() ?: 0,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
