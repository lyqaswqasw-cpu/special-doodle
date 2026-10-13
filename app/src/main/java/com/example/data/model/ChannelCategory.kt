package com.example.data.model

import java.util.UUID

data class ChannelCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val imageUrl: String = "",
    val m3uUrl: String = "",
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "name" to name,
            "order" to order,
            "createdAt" to createdAt
        )
        if (imageUrl.isNotBlank()) {
            map["imageUrl"] = imageUrl
        }
        if (m3uUrl.isNotBlank()) {
            map["m3uUrl"] = m3uUrl
        }
        return map
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): ChannelCategory {
            return ChannelCategory(
                id = id,
                name = map["name"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                m3uUrl = map["m3uUrl"] as? String ?: "",
                order = (map["order"] as? Number)?.toInt() ?: 999,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
