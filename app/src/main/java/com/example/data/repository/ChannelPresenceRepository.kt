package com.example.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

data class OnlineUserSession(
    val sessionId: String = "",
    val channel: String = "الرئيسية",
    val timestamp: Long = System.currentTimeMillis(),
    val device: String = "Android"
)

class ChannelPresenceRepository(
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "ChannelPresenceRepo"
        private const val PREFS_NAME = "presence_prefs"
        private const val KEY_SESSION_ID = "device_session_id"
    }

    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance(ChannelRepository.FIREBASE_RTDB_URL)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Firebase instance: ${e.message}")
            FirebaseDatabase.getInstance()
        }
    }

    val sessionId: String by lazy {
        val sp = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var id = sp?.getString(KEY_SESSION_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString().take(12)
            sp?.edit()?.putString(KEY_SESSION_ID, id)?.apply()
        }
        id
    }

    private var activeChannelId: String? = null
    private var connectedListener: ValueEventListener? = null

    /**
     * Registers the current user as an active viewer on the given channel.
     * Uses Firebase's onDisconnect() to automatically clean up if app closes, crashes, or loses connection.
     */
    fun startWatchingChannel(channelId: String) {
        if (channelId.isBlank()) return
        stopWatchingCurrentChannel()

        activeChannelId = channelId
        val presenceRef = database.getReference("channel_presence").child(channelId).child(sessionId)
        val connectedRef = database.getReference(".info/connected")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    presenceRef.onDisconnect().removeValue()
                    presenceRef.setValue(ServerValue.TIMESTAMP)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Connected listener cancelled: ${error.message}")
            }
        }

        connectedListener = listener
        connectedRef.addValueEventListener(listener)
    }

    /**
     * Unregisters the user from the channel viewers when stopping or switching channels.
     */
    fun stopWatchingCurrentChannel() {
        val prevChannelId = activeChannelId
        val listener = connectedListener
        if (listener != null) {
            database.getReference(".info/connected").removeEventListener(listener)
            connectedListener = null
        }

        if (prevChannelId != null) {
            try {
                database.getReference("channel_presence").child(prevChannelId).child(sessionId).removeValue()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove presence: ${e.message}")
            }
            activeChannelId = null
        }
    }

    /**
     * Observes real-time online viewers count for a specific channel.
     */
    fun getOnlineViewersFlow(channelId: String): Flow<Int> = callbackFlow {
        if (channelId.isBlank()) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val channelPresenceRef = database.getReference("channel_presence").child(channelId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val realCount = snapshot.childrenCount.toInt()
                // If user is currently watching this channel, ensure at least 1 viewer is reflected
                val displayCount = if (activeChannelId == channelId) maxOf(1, realCount) else realCount
                trySend(displayCount)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Presence listener cancelled: ${error.message}")
                trySend(1)
            }
        }

        channelPresenceRef.addValueEventListener(listener)

        awaitClose {
            channelPresenceRef.removeEventListener(listener)
        }
    }

    /**
     * Registers current app user into global app presence so developer settings displays real-time active users.
     */
    fun startGlobalPresence(initialChannelName: String = "الرئيسية") {
        val presenceRef = database.getReference("global_app_presence").child(sessionId)
        try {
            presenceRef.onDisconnect().removeValue()
            val initialData = mapOf(
                "sessionId" to sessionId,
                "channel" to initialChannelName,
                "timestamp" to ServerValue.TIMESTAMP,
                "device" to "Android"
            )
            presenceRef.setValue(initialData)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting initial presence: ${e.message}")
        }

        val connectedRef = database.getReference(".info/connected")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    presenceRef.onDisconnect().removeValue()
                    val data = mapOf(
                        "sessionId" to sessionId,
                        "channel" to initialChannelName,
                        "timestamp" to ServerValue.TIMESTAMP,
                        "device" to "Android"
                    )
                    presenceRef.setValue(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Global presence listener cancelled: ${error.message}")
            }
        }
        connectedRef.addValueEventListener(listener)
    }

    fun updateGlobalPresenceChannel(channelName: String) {
        try {
            database.getReference("global_app_presence").child(sessionId).child("channel").setValue(channelName)
            database.getReference("global_app_presence").child(sessionId).child("timestamp").setValue(ServerValue.TIMESTAMP)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating global presence channel: ${e.message}")
        }
    }

    fun heartbeat() {
        try {
            database.getReference("global_app_presence").child(sessionId).child("timestamp").setValue(ServerValue.TIMESTAMP)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending presence heartbeat: ${e.message}")
        }
    }

    /**
     * Observes real-time online users across the entire application for Developer Settings.
     */
    fun getGlobalOnlineUsersFlow(): Flow<List<OnlineUserSession>> = callbackFlow {
        val ref = database.getReference("global_app_presence")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val now = System.currentTimeMillis()
                val users = mutableListOf<OnlineUserSession>()
                var selfFound = false

                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val channelName = child.child("channel").getValue(String::class.java) ?: "الرئيسية"
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: now
                    val device = child.child("device").getValue(String::class.java) ?: "Android"
                    // Filter out stale sessions older than 4 minutes (unless it is current session)
                    if (key == sessionId || (now - timestamp) < 240000L) {
                        if (key == sessionId) selfFound = true
                        users.add(OnlineUserSession(sessionId = key, channel = channelName, timestamp = timestamp, device = device))
                    }
                }
                if (!selfFound) {
                    users.add(OnlineUserSession(sessionId = sessionId, channel = "الرئيسية", timestamp = now, device = "Android"))
                }
                trySend(users)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Global presence listener cancelled: ${error.message}")
                trySend(listOf(OnlineUserSession(sessionId = sessionId, channel = "الرئيسية", timestamp = System.currentTimeMillis(), device = "Android")))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose {
            ref.removeEventListener(listener)
        }
    }
}
