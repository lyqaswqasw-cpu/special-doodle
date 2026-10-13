package com.example.data.model

import java.util.UUID

data class XtremeAccount(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val host: String = "",
    val username: String = "",
    val password: String = "",
    val streamExtension: String = "m3u8",
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncSignature: String = ""
) {
    /**
     * Unique signature composed of host, username, and password.
     * If any of these change in Firebase, we detect that an update occurred and notify user.
     */
    val credentialsSignature: String
        get() = "${host.trim()}|${username.trim()}|${password.trim()}|${streamExtension.trim()}"

    fun getCleanHost(): String {
        var h = host.trim()
        if (!h.startsWith("http://") && !h.startsWith("https://")) {
            h = "http://$h"
        }
        return h.trimEnd('/')
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "host" to host,
            "username" to username,
            "password" to password,
            "streamExtension" to streamExtension,
            "createdAt" to createdAt
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): XtremeAccount {
            return XtremeAccount(
                id = (map["id"] as? String)?.ifBlank { id } ?: id,
                name = map["name"] as? String ?: "حساب IPTV",
                host = map["host"] as? String ?: "",
                username = map["username"] as? String ?: "",
                password = map["password"] as? String ?: "",
                streamExtension = (map["streamExtension"] as? String)?.ifBlank { "m3u8" } ?: "m3u8",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class XtremeItem(
    val id: String,
    val title: String,
    val streamId: String,
    val streamIcon: String,
    val type: XtremeContentType, // LIVE, VOD_MOVIE, SERIES
    val categoryId: String = "",
    val categoryName: String = "",
    val streamUrl: String,
    val accountName: String = "",
    val accountId: String = "",
    val containerExtension: String = "mp4",
    val quality: String = ""
)

enum class XtremeContentType(val label: String) {
    LIVE("بث مباشر"),
    MOVIE("أفلام"),
    SERIES("مسلسلات")
}
