package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.ChatUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

class ChatRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "ChatRepository"
        private const val PREFS_NAME = "chat_user_prefs"
        private const val KEY_USERNAME = "logged_username"
        private const val KEY_FULL_NAME = "logged_full_name"
    }

    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance(ChannelRepository.FIREBASE_RTDB_URL)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Firebase instance: ${e.message}")
            FirebaseDatabase.getInstance()
        }
    }

    private val messagesRef by lazy {
        database.getReference("v4_chat_messages")
    }

    private val usersRef by lazy {
        database.getReference("v4_chat_users")
    }

    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Cached local logged in user
    fun getSavedUser(): ChatUser? {
        val sp = prefs ?: return null
        val username = sp.getString(KEY_USERNAME, null) ?: return null
        val fullName = sp.getString(KEY_FULL_NAME, null) ?: return null
        return ChatUser(username = username, fullName = fullName)
    }

    /**
     * Obtains or automatically generates an instant guest profile without requiring login or registration.
     */
    fun getOrCreateGuestUser(): ChatUser {
        val saved = getSavedUser()
        if (saved != null) return saved
        val randomNum = (1000..9999).random()
        val guest = ChatUser(
            username = "fan_$randomNum",
            fullName = "مشاهد #$randomNum"
        )
        saveUserLocally(guest)
        return guest
    }

    fun saveUserLocally(user: ChatUser) {
        prefs?.edit()?.apply {
            putString(KEY_USERNAME, user.username)
            putString(KEY_FULL_NAME, user.fullName)
            apply()
        }
    }

    fun clearSavedUser() {
        prefs?.edit()?.clear()?.apply()
    }

    /**
     * Sanitizes channel ID to be safe for Firebase Realtime Database node paths.
     */
    fun sanitizeChannelId(channelId: String?): String {
        if (channelId.isNullOrBlank()) return "general"
        val clean = channelId.replace(Regex("[.#$\\[\\]/\\x00-\\x1F\\x7F]"), "_").trim()
        return if (clean.isBlank()) "general" else clean
    }

    private fun getChannelMessagesRef(channelId: String) =
        database.getReference("channel_chat_messages").child(sanitizeChannelId(channelId))

    /**
     * Efficient Realtime Flow of the latest messages for a specific channel limited to 200.
     */
    fun getMessagesFlow(channelId: String, limit: Int = 200): Flow<List<ChatMessage>> = callbackFlow {
        val cleanId = sanitizeChannelId(channelId)
        val query = getChannelMessagesRef(cleanId).orderByChild("timestamp").limitToLast(limit)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = child.value
                    if (value is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        val msg = ChatMessage.fromMap(key, value as Map<String, Any?>)
                        if (com.example.data.security.AppSecurityGuard.isMaliciousText(msg.text) ||
                            com.example.data.security.AppSecurityGuard.isMaliciousText(msg.authorFullName) ||
                            com.example.data.security.AppSecurityGuard.isMaliciousText(msg.authorUsername)) {
                            Log.w(TAG, "Shield Action: Dropping and purging malicious chat message ($key)")
                            try { child.ref.removeValue() } catch (_: Exception) {}
                            continue
                        }
                        list.add(msg)
                    }
                }
                list.sortBy { it.timestamp }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Messages listener cancelled for channel $cleanId: ${error.message}")
                trySend(emptyList())
            }
        }

        query.addValueEventListener(listener)

        awaitClose {
            query.removeEventListener(listener)
        }
    }

    /**
     * Backward-compatible default overload.
     */
    fun getMessagesFlow(limit: Int = 80): Flow<List<ChatMessage>> = getMessagesFlow("general", limit)

    suspend fun sendMessage(channelId: String, message: ChatMessage): Result<Unit> = suspendCancellableCoroutine { continuation ->
        if (com.example.data.security.AppSecurityGuard.isMaliciousText(message.text) ||
            com.example.data.security.AppSecurityGuard.isMaliciousText(message.authorFullName) ||
            com.example.data.security.AppSecurityGuard.isMaliciousText(message.authorUsername)) {
            continuation.resume(Result.failure(SecurityException("Message blocked by security policy")))
            return@suspendCancellableCoroutine
        }
        val cleanId = sanitizeChannelId(channelId.ifBlank { message.channelId })
        val ref = getChannelMessagesRef(cleanId)
        val msgId = if (message.id.isBlank()) {
            ref.push().key ?: UUID.randomUUID().toString()
        } else {
            message.id
        }

        val msgToSave = message.copy(id = msgId, channelId = cleanId)
        ref.child(msgId).setValue(msgToSave.toMap())
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to send message to channel $cleanId: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    suspend fun sendMessage(message: ChatMessage): Result<Unit> =
        sendMessage(message.channelId.ifBlank { "general" }, message)

    suspend fun deleteMessage(channelId: String, messageId: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val cleanId = sanitizeChannelId(channelId)
        getChannelMessagesRef(cleanId).child(messageId).removeValue()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete message in channel $cleanId: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> = deleteMessage("general", messageId)

    /**
     * Toggles a user reaction on a specific channel message.
     * If user already reacted with this emoji, removes it; otherwise sets it to true.
     */
    suspend fun toggleReaction(channelId: String, messageId: String, emoji: String, username: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val cleanId = sanitizeChannelId(channelId)
        val reactionRef = getChannelMessagesRef(cleanId).child(messageId).child("reactions").child(emoji).child(username)
        reactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    reactionRef.removeValue()
                        .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                        .addOnFailureListener { continuation.resume(Result.failure(it)) }
                } else {
                    reactionRef.setValue(true)
                        .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                        .addOnFailureListener { continuation.resume(Result.failure(it)) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(Result.failure(error.toException()))
            }
        })
    }

    suspend fun toggleReaction(messageId: String, emoji: String, username: String): Result<Unit> =
        toggleReaction("general", messageId, emoji, username)

    /**
     * Registers a new user. Checks if username is taken.
     */
    suspend fun registerUser(user: ChatUser): Result<ChatUser> = suspendCancellableCoroutine { continuation ->
        val cleanUsername = user.username.trim().lowercase().replace(" ", "_").replace("@", "")
        if (cleanUsername.isBlank() || user.fullName.isBlank() || user.password.isBlank()) {
            continuation.resume(Result.failure(IllegalArgumentException("يرجى ملء جميع الحقول المطلوبة")))
            return@suspendCancellableCoroutine
        }

        val userRef = usersRef.child(cleanUsername)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    continuation.resume(Result.failure(IllegalStateException("اسم المستخدم مسجل مسبقاً، يرجى اختيار اسم آخر")))
                } else {
                    val finalUser = user.copy(username = cleanUsername)
                    userRef.setValue(finalUser.toMap())
                        .addOnSuccessListener {
                            saveUserLocally(finalUser)
                            continuation.resume(Result.success(finalUser))
                        }
                        .addOnFailureListener { exception ->
                            continuation.resume(Result.failure(exception))
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(Result.failure(error.toException()))
            }
        })
    }

    /**
     * Logs in an existing user.
     */
    suspend fun loginUser(username: String, password: String): Result<ChatUser> = suspendCancellableCoroutine { continuation ->
        val cleanUsername = username.trim().lowercase().replace(" ", "_").replace("@", "")
        if (cleanUsername.isBlank() || password.isBlank()) {
            continuation.resume(Result.failure(IllegalArgumentException("يرجى إدخال اسم المستخدم وكلمة المرور")))
            return@suspendCancellableCoroutine
        }

        val userRef = usersRef.child(cleanUsername)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    continuation.resume(Result.failure(IllegalStateException("المستخدم غير موجود، يرجى إنشاء حساب أولاً")))
                    return
                }

                @Suppress("UNCHECKED_CAST")
                val map = snapshot.value as? Map<String, Any?>
                if (map == null) {
                    continuation.resume(Result.failure(IllegalStateException("بيانات الحساب غير صالحة")))
                    return
                }

                val chatUser = ChatUser.fromMap(map)
                if (chatUser.password != password.trim()) {
                    continuation.resume(Result.failure(IllegalStateException("كلمة المرور غير صحيحة")))
                    return
                }

                saveUserLocally(chatUser)
                continuation.resume(Result.success(chatUser))
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(Result.failure(error.toException()))
            }
        })
    }

    /**
     * Deletes all chat messages across all channels permanently from Firebase Realtime Database.
     */
    suspend fun deleteAllMessages(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        database.getReference("channel_chat_messages").removeValue()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete all chat messages: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }
}
