package com.example.data.model

import java.util.UUID

data class FoulVideo(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val duration: String = "",
    val category: String = "أهداف وملخصات",
    val authorName: String = "FOUL TV",
    val authorAvatar: String = "",
    val viewsCount: Long = 0,
    val likesCount: Long = 0,
    val commentsCount: Long = 0,
    val likedUsernames: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isCommentsDisabled: Boolean = false,
    val isLikesDisabled: Boolean = false,
    val isViewsHidden: Boolean = false,
    val tags: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "videoUrl" to videoUrl,
        "thumbnailUrl" to thumbnailUrl,
        "duration" to duration,
        "category" to category,
        "authorName" to authorName,
        "authorAvatar" to authorAvatar,
        "viewsCount" to viewsCount,
        "likesCount" to likesCount,
        "commentsCount" to commentsCount,
        "likedUsernames" to likedUsernames,
        "createdAt" to createdAt,
        "isCommentsDisabled" to isCommentsDisabled,
        "isLikesDisabled" to isLikesDisabled,
        "isViewsHidden" to isViewsHidden,
        "tags" to tags,
        "appVersion" to "1.0.0"
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): FoulVideo {
            return FoulVideo(
                id = (map["id"] as? String) ?: id,
                title = (map["title"] as? String) ?: "",
                description = (map["description"] as? String) ?: "",
                videoUrl = (map["videoUrl"] as? String) ?: "",
                thumbnailUrl = (map["thumbnailUrl"] as? String) ?: "",
                duration = (map["duration"] as? String) ?: "",
                category = (map["category"] as? String) ?: "أهداف وملخصات",
                authorName = (map["authorName"] as? String) ?: "FOUL TV",
                authorAvatar = (map["authorAvatar"] as? String) ?: "",
                viewsCount = (map["viewsCount"] as? Number)?.toLong() ?: 0L,
                likesCount = (map["likesCount"] as? Number)?.toLong() ?: 0L,
                commentsCount = (map["commentsCount"] as? Number)?.toLong() ?: 0L,
                likedUsernames = (map["likedUsernames"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isCommentsDisabled = (map["isCommentsDisabled"] as? Boolean) ?: false,
                isLikesDisabled = (map["isLikesDisabled"] as? Boolean) ?: false,
                isViewsHidden = (map["isViewsHidden"] as? Boolean) ?: false,
                tags = (map["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}

data class FoulComment(
    val id: String = UUID.randomUUID().toString(),
    val videoId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorAvatar: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likesCount: Long = 0L
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "videoId" to videoId,
        "authorName" to authorName,
        "authorUsername" to authorUsername,
        "authorAvatar" to authorAvatar,
        "text" to text,
        "createdAt" to createdAt,
        "likesCount" to likesCount
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): FoulComment {
            return FoulComment(
                id = (map["id"] as? String) ?: id,
                videoId = (map["videoId"] as? String) ?: "",
                authorName = (map["authorName"] as? String) ?: "",
                authorUsername = (map["authorUsername"] as? String) ?: "",
                authorAvatar = (map["authorAvatar"] as? String) ?: "",
                text = (map["text"] as? String) ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                likesCount = (map["likesCount"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

data class FoulTvGlobalConfig(
    val hideViewsAll: Boolean = false,
    val hideLikesAll: Boolean = false,
    val disableCommentsAll: Boolean = false
)
