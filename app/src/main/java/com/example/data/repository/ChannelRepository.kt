package com.example.data.repository

import android.util.Log
import com.example.data.model.Channel
import com.example.data.model.ChannelServer
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

class ChannelRepository {

    companion object {
        const val FIREBASE_RTDB_URL = "https://foul-ddbb8-default-rtdb.firebaseio.com"
        private const val TAG = "ChannelRepository"
        private var isPersistenceConfigured = false

        val DEFAULT_XTREME_ACCOUNTS = emptyList<com.example.data.model.XtremeAccount>()
    }

    private fun checkAdminAuth(): Result<Unit>? {
        val check = com.example.data.security.AdminAuthManager.requireAdminAuthentication()
        return if (check.isFailure) check else null
    }

    private val database: FirebaseDatabase by lazy {
        val db = try {
            FirebaseDatabase.getInstance(FIREBASE_RTDB_URL)
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining Firebase instance: ${e.message}")
            FirebaseDatabase.getInstance()
        }
        if (!isPersistenceConfigured) {
            try {
                db.setPersistenceEnabled(true)
            } catch (e: Exception) {
                // Ignore if already set in application
            }
            isPersistenceConfigured = true
        }
        db
    }

    private val channelsRef by lazy {
        database.getReference("v4_channels").apply {
            try { keepSynced(true) } catch (_: Exception) {}
        }
    }

    private val xtremeRef by lazy {
        database.getReference("v4_xtreme_accounts").apply {
            try { keepSynced(true) } catch (_: Exception) {}
        }
    }

    private val categoriesRef by lazy {
        database.getReference("v4_channel_categories").apply {
            try { keepSynced(true) } catch (_: Exception) {}
        }
    }

    private val branchesRef by lazy {
        database.getReference("v4_channel_branches").apply {
            try { keepSynced(true) } catch (_: Exception) {}
        }
    }

    private val eventsRef by lazy {
        database.getReference("v4_event_banners").apply {
            try { keepSynced(true) } catch (_: Exception) {}
        }
    }

    init {
        cleanLegacyDefacements()
    }

    /**
     * Scans both legacy and current Firebase channels and permanently purges any abusive,
     * defacing, or profane channels injected by malicious actors or old APK versions.
     */
    fun cleanLegacyDefacements() {
        try {
            val purgeListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val key = child.key ?: continue
                        val value = child.value
                        if (value is Map<*, *>) {
                            val name = value["name"]?.toString() ?: ""
                            val cat = value["category"]?.toString() ?: ""
                            if (com.example.data.security.AppSecurityGuard.isMaliciousChannel(name, cat)) {
                                Log.w(TAG, "Shield Action: Permanently deleting defaced channel '$name' ($key)")
                                child.ref.removeValue()
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            database.getReference("channels").addListenerForSingleValueEvent(purgeListener)
            database.getReference("v4_channels").addListenerForSingleValueEvent(purgeListener)
        } catch (_: Exception) {}
    }

    /**
     * Listens to live real-time updates of Channel Branches from Firebase.
     */
    fun getBranchesFlow(): Flow<List<com.example.data.model.ChannelBranch>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.data.model.ChannelBranch>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = child.value
                    if (value is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        val branch = com.example.data.model.ChannelBranch.fromMap(key, value as Map<String, Any?>)
                        if (com.example.data.security.AppSecurityGuard.isMaliciousText(branch.name)) {
                            Log.w(TAG, "Shield Action: Dropping and purging malicious branch '${branch.name}'")
                            try { branchesRef.child(key).removeValue() } catch (_: Exception) {}
                            continue
                        }
                        list.add(branch)
                    }
                }
                list.sortWith(
                    compareBy(
                        { it.order },
                        { it.createdAt }
                    )
                )
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase Branches listener cancelled: ${error.message}")
                trySend(emptyList())
            }
        }

        branchesRef.addValueEventListener(listener)

        awaitClose {
            branchesRef.removeEventListener(listener)
        }
    }

    /**
     * Publishes a new branch or updates an existing branch in Firebase.
     */
    suspend fun saveBranch(branch: com.example.data.model.ChannelBranch): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        if (com.example.data.security.AppSecurityGuard.isMaliciousText(branch.name)) {
            continuation.resume(Result.failure(SecurityException("Malicious content blocked by AppSecurityGuard")))
            return@suspendCancellableCoroutine
        }
        val branchId = if (branch.id.isBlank()) {
            branchesRef.push().key ?: UUID.randomUUID().toString()
        } else {
            branch.id
        }
        val branchToSave = branch.copy(id = branchId)
        branchesRef.child(branchId).setValue(branchToSave.toMap())
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save branch: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Deletes a branch from Firebase.
     */
    suspend fun deleteBranch(branchId: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        branchesRef.child(branchId).removeValue()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete branch: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Adds channel IDs to a branch and updates the channels' branch references.
     */
    suspend fun addChannelsToBranch(branchId: String, newChannelIds: List<String>, branchName: String = ""): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        branchesRef.child(branchId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                val existingBranch = if (value is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    com.example.data.model.ChannelBranch.fromMap(branchId, value as Map<String, Any?>)
                } else {
                    null
                }

                val currentList = existingBranch?.channelIds?.toMutableSet() ?: mutableSetOf()
                currentList.addAll(newChannelIds)

                branchesRef.child(branchId).child("channelIds").setValue(currentList.toList())
                    .addOnSuccessListener {
                        // Also update channel records with branchId and branchName for fast lookup
                        val finalBranchName = if (branchName.isNotBlank()) branchName else (existingBranch?.name ?: "")
                        for (chId in newChannelIds) {
                            try {
                                val updates = mutableMapOf<String, Any?>("branchId" to branchId)
                                if (finalBranchName.isNotBlank()) {
                                    updates["branchName"] = finalBranchName
                                }
                                channelsRef.child(chId).updateChildren(updates)
                            } catch (_: Exception) {}
                        }
                        continuation.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { err ->
                        continuation.resume(Result.failure(err))
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(Result.failure(error.toException()))
            }
        })
    }

    /**
     * Removes a channel ID from a branch.
     */
    suspend fun removeChannelFromBranch(branchId: String, channelId: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        branchesRef.child(branchId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                val existingBranch = if (value is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    com.example.data.model.ChannelBranch.fromMap(branchId, value as Map<String, Any?>)
                } else {
                    null
                }

                val currentList = existingBranch?.channelIds?.toMutableList() ?: mutableListOf()
                currentList.remove(channelId)

                branchesRef.child(branchId).child("channelIds").setValue(currentList)
                    .addOnSuccessListener {
                        try {
                            channelsRef.child(channelId).child("branchId").removeValue()
                            channelsRef.child(channelId).child("branchName").removeValue()
                        } catch (_: Exception) {}
                        continuation.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { err ->
                        continuation.resume(Result.failure(err))
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(Result.failure(error.toException()))
            }
        })
    }

    /**
     * Listens to live real-time updates of Channel Categories from Firebase.
     */
    fun getCategoriesFlow(): Flow<List<com.example.data.model.ChannelCategory>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.data.model.ChannelCategory>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = child.value
                    if (value is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        val cat = com.example.data.model.ChannelCategory.fromMap(key, value as Map<String, Any?>)
                        if (com.example.data.security.AppSecurityGuard.isMaliciousText(cat.name)) {
                            Log.w(TAG, "Shield Action: Dropping and purging malicious category '${cat.name}'")
                            try { categoriesRef.child(key).removeValue() } catch (_: Exception) {}
                            continue
                        }
                        list.add(cat)
                    }
                }
                list.sortWith(
                    compareBy(
                        { it.order },
                        { it.createdAt }
                    )
                )
                if (list.isEmpty()) {
                    trySend(getBuiltInCategories())
                } else {
                    trySend(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase Categories listener cancelled: ${error.message}")
                trySend(getBuiltInCategories())
            }
        }

        categoriesRef.addValueEventListener(listener)

        awaitClose {
            categoriesRef.removeEventListener(listener)
        }
    }

    /**
     * Updates the display order for multiple categories in Firebase Realtime Database.
     */
    suspend fun updateCategoriesOrder(orderedCategories: List<com.example.data.model.ChannelCategory>): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        val updates = mutableMapOf<String, Any?>()
        orderedCategories.forEachIndexed { index, cat ->
            updates["${cat.id}/order"] = index
        }
        categoriesRef.updateChildren(updates)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to update categories order: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Publishes a new category or updates an existing category in Firebase.
     */
    suspend fun saveCategory(category: com.example.data.model.ChannelCategory): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        if (com.example.data.security.AppSecurityGuard.isMaliciousText(category.name)) {
            continuation.resume(Result.failure(SecurityException("Malicious category blocked by AppSecurityGuard")))
            return@suspendCancellableCoroutine
        }
        val catId = if (category.id.isBlank()) {
            categoriesRef.push().key ?: UUID.randomUUID().toString()
        } else {
            category.id
        }
        val catToSave = category.copy(id = catId)
        categoriesRef.child(catId).setValue(catToSave.toMap())
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save category: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Deletes a category from Firebase.
     */
    suspend fun deleteCategory(categoryId: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        categoriesRef.child(categoryId).removeValue()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete category: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Listens to live real-time updates of Xtreme accounts from Firebase.
     */
    fun getXtremeAccountsFlow(): Flow<List<com.example.data.model.XtremeAccount>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.data.model.XtremeAccount>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = child.value
                    if (value is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        list.add(com.example.data.model.XtremeAccount.fromMap(key, value as Map<String, Any?>))
                    }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase Xtreme listener cancelled: ${error.message}")
                trySend(emptyList())
            }
        }

        xtremeRef.addValueEventListener(listener)

        awaitClose {
            xtremeRef.removeEventListener(listener)
        }
    }

    /**
     * Saves or updates an Xtreme account in Firebase.
     */
    suspend fun saveXtremeAccount(account: com.example.data.model.XtremeAccount): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        val accId = if (account.id.isBlank()) {
            xtremeRef.push().key ?: UUID.randomUUID().toString()
        } else {
            account.id
        }
        val accToSave = account.copy(id = accId)
        xtremeRef.child(accId).setValue(accToSave.toMap())
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save Xtreme account: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Deletes an Xtreme account from Firebase.
     */
    suspend fun deleteXtremeAccount(accountId: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        xtremeRef.child(accountId).removeValue()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete Xtreme account: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Deletes all Xtreme accounts from Firebase.
     */
    suspend fun deleteAllXtremeAccounts(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        xtremeRef.removeValue()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete all Xtreme accounts: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Listens to live real-time updates from Firebase Realtime Database.
     */
    fun getChannelsFlow(): Flow<List<Channel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Channel>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = child.value
                    if (value is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        val channel = Channel.fromMap(key, value as Map<String, Any?>)
                        
                        // Active Shield: Intercept, drop, and permanently purge any malicious/abusive channels
                        if (com.example.data.security.AppSecurityGuard.isMaliciousChannel(channel.name, channel.category)) {
                            Log.w(TAG, "Shield Action: Permanently deleting malicious channel '${channel.name}' ($key)")
                            try { channelsRef.child(key).removeValue() } catch (_: Exception) {}
                            continue
                        }

                        // Only add genuine, valid channels with a title and at least one server
                        if (channel.name.isNotBlank() && channel.servers.isNotEmpty() && !channel.id.startsWith("m3u_")) {
                            list.add(channel)
                        } else if (channel.name.isBlank() && channel.servers.isEmpty()) {
                            // Purge any orphan/phantom dummy node from Firebase
                            try {
                                channelsRef.child(key).removeValue()
                            } catch (_: Exception) {}
                        }
                    }
                }
                list.sortByDescending { it.createdAt }
                if (list.isEmpty()) {
                    trySend(getBuiltInSportsChannels())
                } else {
                    trySend(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled: ${error.message}")
                trySend(getBuiltInSportsChannels())
            }
        }

        channelsRef.addValueEventListener(listener)

        awaitClose {
            channelsRef.removeEventListener(listener)
        }
    }

    /**
     * Publishes a new channel or updates an existing channel in Firebase.
     */
    suspend fun saveChannel(channel: Channel): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        if (channel.name.isBlank() || channel.servers.isEmpty()) {
            continuation.resume(Result.failure(IllegalArgumentException("Channel name and servers must not be empty")))
            return@suspendCancellableCoroutine
        }
        if (com.example.data.security.AppSecurityGuard.isMaliciousChannel(channel.name, channel.category)) {
            continuation.resume(Result.failure(SecurityException("Malicious channel blocked by AppSecurityGuard")))
            return@suspendCancellableCoroutine
        }

        val channelId = if (channel.id.isBlank() || channel.id.startsWith("m3u_")) {
            channelsRef.push().key ?: UUID.randomUUID().toString()
        } else {
            channel.id
        }

        val channelToSave = channel.copy(id = channelId)
        channelsRef.child(channelId).setValue(channelToSave.toMap())
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save channel: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Batch saves multiple channels into Firebase Realtime Database.
     */
    suspend fun saveChannels(channels: List<Channel>): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        if (channels.isEmpty()) {
            continuation.resume(Result.success(Unit))
            return@suspendCancellableCoroutine
        }
        val updates = mutableMapOf<String, Any>()
        for (ch in channels) {
            if (ch.name.isBlank() || ch.servers.isEmpty()) continue
            val channelId = if (ch.id.isBlank() || ch.id.startsWith("m3u_")) {
                channelsRef.push().key ?: UUID.randomUUID().toString()
            } else {
                ch.id
            }
            val channelToSave = ch.copy(id = channelId)
            updates[channelId] = channelToSave.toMap()
        }
        if (updates.isEmpty()) {
            continuation.resume(Result.success(Unit))
            return@suspendCancellableCoroutine
        }
        channelsRef.updateChildren(updates)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to batch save channels: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Atomically increments the view count of a channel in Firebase Realtime Database
     * only if the channel genuinely exists in Firebase. Never creates a ghost channel.
     */
    fun incrementChannelViews(channelId: String) {
        if (channelId.isBlank() || channelId.startsWith("m3u_") || channelId.startsWith("xtreme_") || channelId.startsWith("espn_")) return
        try {
            channelsRef.child(channelId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        channelsRef.child(channelId).child("views")
                            .setValue(com.google.firebase.database.ServerValue.increment(1))
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to increment channel views: ${e.message}")
        }
    }

    /**
     * Deletes a channel from Firebase Realtime Database.
     */
    suspend fun deleteChannel(channelId: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        channelsRef.child(channelId).removeValue()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete channel: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Deletes all published channels from Firebase Realtime Database.
     */
    suspend fun deleteAllChannels(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        channelsRef.removeValue()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete all channels: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Listens to live real-time updates of App Configuration from Firebase.
     */
    fun getAppConfigFlow(): Flow<Map<String, Any>> = callbackFlow {
        val configRef = database.getReference("v4_app_config")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = mutableMapOf<String, Any>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = child.value ?: continue
                    map[key] = value
                }
                trySend(map)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase app_config listener cancelled: ${error.message}")
                trySend(emptyMap())
            }
        }

        configRef.addValueEventListener(listener)
        awaitClose {
            configRef.removeEventListener(listener)
        }
    }

    /**
     * Saves or updates a specific configuration value in Firebase app_config.
     */
    suspend fun saveAppConfigValue(key: String, value: Any): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        database.getReference("v4_app_config").child(key).setValue(value)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save app config value $key: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Listens to live real-time updates of Event Banners from Firebase.
     */
    fun getEventBannersFlow(): Flow<List<com.example.data.model.EventBanner>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.data.model.EventBanner>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = child.value
                    if (value is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        val banner = com.example.data.model.EventBanner.fromMap(key, value as Map<String, Any?>)
                        if (com.example.data.security.AppSecurityGuard.isMaliciousText(banner.title) ||
                            com.example.data.security.AppSecurityGuard.isMaliciousText(banner.channelName)) {
                            Log.w(TAG, "Shield Action: Dropping and purging malicious event banner ($key)")
                            try { eventsRef.child(key).removeValue() } catch (_: Exception) {}
                            continue
                        }
                        list.add(banner)
                    }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase Event Banners listener cancelled: ${error.message}")
                trySend(emptyList())
            }
        }

        eventsRef.addValueEventListener(listener)
        awaitClose {
            eventsRef.removeEventListener(listener)
        }
    }

    /**
     * Saves or updates an Event Banner in Firebase.
     */
    suspend fun saveEventBanner(event: com.example.data.model.EventBanner): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        if (com.example.data.security.AppSecurityGuard.isMaliciousText(event.title) ||
            com.example.data.security.AppSecurityGuard.isMaliciousText(event.channelName)) {
            continuation.resume(Result.failure(SecurityException("Malicious banner blocked by AppSecurityGuard")))
            return@suspendCancellableCoroutine
        }
        val key = if (event.id.isBlank()) UUID.randomUUID().toString() else event.id
        val bannerToSave = event.copy(id = key)
        eventsRef.child(key).setValue(bannerToSave.toMap())
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save event banner: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Deletes an Event Banner from Firebase.
     */
    suspend fun deleteEventBanner(eventId: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        checkAdminAuth()?.let { continuation.resume(it); return@suspendCancellableCoroutine }
        eventsRef.child(eventId).removeValue()
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to delete event banner: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Resilient built-in fallback sports channels in case Firebase is ever wiped or unreachable.
     */
    fun getBuiltInSportsChannels(): List<Channel> {
        return listOf(
            Channel(
                id = "ch_bein_1",
                name = "beIN Sports 1 HD",
                imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=300",
                category = "دوري أبطال أوروبا",
                servers = listOf(
                    ChannelServer(id = "s1", name = "FHD", streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", streamType = "m3u8"),
                    ChannelServer(id = "s2", name = "HD", streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8", streamType = "m3u8"),
                    ChannelServer(id = "s3", name = "SD", streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", streamType = "normal")
                )
            ),
            Channel(
                id = "ch_ssc_1",
                name = "SSC Sports 1 HD",
                imageUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=300",
                category = "الدوري السعودي",
                servers = listOf(
                    ChannelServer(id = "s4", name = "سيرفر 1 (HD)", streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8", streamType = "m3u8"),
                    ChannelServer(id = "s5", name = "سيرفر 2 (SD)", streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", streamType = "m3u8")
                )
            ),
            Channel(
                id = "ch_alkass",
                name = "Al Kass Sports HD",
                imageUrl = "https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?w=300",
                category = "كأس آسيا",
                servers = listOf(
                    ChannelServer(id = "s6", name = "HD المباشر", streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", streamType = "m3u8")
                )
            ),
            Channel(
                id = "ch_premier",
                name = "Premier League Live",
                imageUrl = "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=300",
                category = "الدوري الإنجليزي",
                servers = listOf(
                    ChannelServer(id = "s7", name = "سيرفر أساسي", streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", streamType = "m3u8"),
                    ChannelServer(id = "s8", name = "سيرفر احتياطي", streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4", streamType = "normal")
                )
            )
        )
    }

    /**
     * Resilient built-in fallback categories.
     */
    fun getBuiltInCategories(): List<com.example.data.model.ChannelCategory> {
        return listOf(
            com.example.data.model.ChannelCategory(id = "cat_1", name = "دوري أبطال أوروبا", order = 0),
            com.example.data.model.ChannelCategory(id = "cat_2", name = "الدوري السعودي", order = 1),
            com.example.data.model.ChannelCategory(id = "cat_3", name = "الدوري الإنجليزي", order = 2),
            com.example.data.model.ChannelCategory(id = "cat_4", name = "كأس آسيا", order = 3)
        )
    }
}
