package com.example.data.model

import java.util.UUID

data class ChatUser(
    val username: String = "",
    val fullName: String = "",
    val password: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "username" to username,
            "fullName" to fullName,
            "password" to password,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): ChatUser {
            return ChatUser(
                username = map["username"] as? String ?: "",
                fullName = map["fullName"] as? String ?: "",
                password = map["password"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val channelId: String = "",
    val text: String = "",
    val authorFullName: String = "",
    val authorUsername: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val replyToId: String? = null,
    val replyToAuthor: String? = null,
    val replyToText: String? = null,
    // Map of emoji -> list of usernames who reacted
    val reactions: Map<String, List<String>> = emptyMap()
) {
    fun toMap(): Map<String, Any?> {
        val reactionsMap = mutableMapOf<String, Map<String, Boolean>>()
        reactions.forEach { (emoji, users) ->
            val userMap = mutableMapOf<String, Boolean>()
            users.forEach { u -> userMap[u] = true }
            reactionsMap[emoji] = userMap
        }

        return mapOf(
            "id" to id,
            "channelId" to channelId,
            "text" to text,
            "authorFullName" to authorFullName,
            "authorUsername" to authorUsername,
            "timestamp" to timestamp,
            "replyToId" to replyToId,
            "replyToAuthor" to replyToAuthor,
            "replyToText" to replyToText,
            "reactions" to reactionsMap
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): ChatMessage {
            val reactionsRaw = map["reactions"] as? Map<String, Any?> ?: emptyMap()
            val parsedReactions = mutableMapOf<String, List<String>>()

            reactionsRaw.forEach { (emoji, usersVal) ->
                when (usersVal) {
                    is Map<*, *> -> {
                        val userList = usersVal.keys.filterIsInstance<String>()
                        if (userList.isNotEmpty()) {
                            parsedReactions[emoji] = userList
                        }
                    }
                    is List<*> -> {
                        val userList = usersVal.filterIsInstance<String>()
                        if (userList.isNotEmpty()) {
                            parsedReactions[emoji] = userList
                        }
                    }
                }
            }

            return ChatMessage(
                id = (map["id"] as? String)?.takeIf { it.isNotBlank() } ?: id,
                channelId = map["channelId"] as? String ?: "",
                text = map["text"] as? String ?: "",
                authorFullName = map["authorFullName"] as? String ?: "",
                authorUsername = map["authorUsername"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                replyToId = map["replyToId"] as? String,
                replyToAuthor = map["replyToAuthor"] as? String,
                replyToText = map["replyToText"] as? String,
                reactions = parsedReactions
            )
        }
    }
}
