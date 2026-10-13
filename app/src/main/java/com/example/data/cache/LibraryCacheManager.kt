package com.example.data.cache

import android.content.Context
import android.util.Log
import com.example.data.model.XtremeAccount
import com.example.data.model.XtremeContentType
import com.example.data.model.XtremeItem
import com.example.data.util.LibraryDeduplicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Manages local offline caching of complete Library content (Movies and Series).
 * Prevents re-downloading library content on every app launch.
 * Tracks accounts credentials signature to intelligently detect any modification,
 * addition, or deletion of accounts, triggering a mandatory update and wiping old cache.
 */
class LibraryCacheManager(private val context: Context) {

    companion object {
        private const val TAG = "LibraryCacheManager"
        private const val PREFS_NAME = "xtreme_library_cache_prefs"
        private const val KEY_SIGNATURE = "saved_accounts_signature"
        private const val KEY_LAST_SAVED = "last_saved_timestamp"
        private const val CACHE_FILE_NAME = "xtreme_library_cache.json"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val cacheFile: File
        get() = File(context.filesDir, CACHE_FILE_NAME)

    /**
     * Computes a precise signature for the current list of accounts based on credentials.
     * If an account is added, deleted, or if its host, username, or password
     * is modified, the returned signature strictly changes.
     */
    fun computeAccountsSignature(accounts: List<XtremeAccount>): String {
        if (accounts.isEmpty()) return ""
        // Sort accounts by ID for stable, deterministic signature
        return accounts.sortedBy { it.id }.joinToString(";") { account ->
            "${account.id}#${account.host.trim().lowercase().trimEnd('/')}#${account.username.trim()}#${account.password.trim()}"
        }
    }

    fun getSavedSignature(): String {
        return prefs.getString(KEY_SIGNATURE, "") ?: ""
    }

    fun getLastSavedTimestamp(): Long {
        return prefs.getLong(KEY_LAST_SAVED, 0L)
    }

    fun hasCache(): Boolean {
        return cacheFile.exists() && cacheFile.length() > 0
    }

    // Fast in-memory cache for ultra-fast instant UI access
    @Volatile
    private var memoryCachedLibrary: List<XtremeItem>? = null

    /**
     * Synchronously loads cached library items for immediate 0ms rendering upon entering the Library section.
     */
    fun loadCachedItemsSync(): List<XtremeItem> {
        memoryCachedLibrary?.let { if (it.isNotEmpty()) return it }

        if (!cacheFile.exists() || cacheFile.length() == 0L) {
            return emptyList()
        }

        val list = ArrayList<XtremeItem>()
        try {
            java.io.FileReader(cacheFile).use { fileReader ->
                android.util.JsonReader(fileReader).use { reader ->
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val item = readItem(reader)
                        if (item != null && item.type != XtremeContentType.LIVE) {
                            list.add(item)
                        }
                    }
                    reader.endArray()
                }
            }
            val deduplicated = LibraryDeduplicator.deduplicate(list)
            memoryCachedLibrary = deduplicated
            return deduplicated
        } catch (e: Exception) {
            Log.w(TAG, "Error loading cached items synchronously: ${e.message}")
        }
        return emptyList()
    }

    /**
     * Saves library items to local JSON cache using fast streaming JsonWriter.
     * Ensures any LIVE stream items are excluded from the library cache.
     */
    suspend fun saveCache(signature: String, items: List<XtremeItem>): Boolean = withContext(Dispatchers.IO) {
        try {
            // Apply intelligent deduplication: identical titles shown once
            val deduplicatedItems = LibraryDeduplicator.deduplicate(items)
            memoryCachedLibrary = deduplicatedItems

            val tempFile = File(context.filesDir, "$CACHE_FILE_NAME.tmp")
            java.io.FileWriter(tempFile).use { fileWriter ->
                android.util.JsonWriter(fileWriter).use { writer ->
                    writer.beginArray()
                    for (item in deduplicatedItems) {
                        writeItem(writer, item)
                    }
                    writer.endArray()
                }
            }

            val renameSuccessful = if (cacheFile.exists()) {
                cacheFile.delete() && tempFile.renameTo(cacheFile)
            } else {
                tempFile.renameTo(cacheFile)
            }

            if (!renameSuccessful) {
                tempFile.copyTo(cacheFile, overwrite = true)
                tempFile.delete()
            }

            prefs.edit()
                .putString(KEY_SIGNATURE, signature)
                .putLong(KEY_LAST_SAVED, System.currentTimeMillis())
                .apply()

            Log.d(TAG, "Cached ${deduplicatedItems.size} deduplicated items locally with signature hash: ${signature.hashCode()}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save library cache to disk: ${e.message}", e)
            false
        }
    }

    /**
     * Reads and parses cached items directly from local storage with fast streaming.
     */
    suspend fun loadCachedItems(): List<XtremeItem> = withContext(Dispatchers.IO) {
        loadCachedItemsSync()
    }

    private fun writeItem(writer: android.util.JsonWriter, item: XtremeItem) {
        writer.beginObject()
        writer.name("id").value(item.id)
        writer.name("title").value(item.title)
        writer.name("streamId").value(item.streamId)
        writer.name("streamIcon").value(item.streamIcon)
        writer.name("type").value(item.type.name)
        writer.name("categoryId").value(item.categoryId)
        writer.name("categoryName").value(item.categoryName)
        writer.name("streamUrl").value(item.streamUrl)
        writer.name("accountName").value(item.accountName)
        writer.name("accountId").value(item.accountId)
        writer.name("containerExtension").value(item.containerExtension)
        writer.name("quality").value(item.quality)
        writer.endObject()
    }

    private fun readItem(reader: android.util.JsonReader): XtremeItem? {
        var id = ""
        var title = ""
        var streamId = ""
        var streamIcon = ""
        var type = XtremeContentType.MOVIE
        var categoryId = ""
        var categoryName = ""
        var streamUrl = ""
        var accountName = ""
        var accountId = ""
        var containerExtension = "mp4"
        var quality = ""

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "title" -> title = reader.nextString()
                "streamId" -> streamId = reader.nextString()
                "streamIcon" -> streamIcon = reader.nextString()
                "type" -> {
                    type = try {
                        XtremeContentType.valueOf(reader.nextString())
                    } catch (_: Exception) {
                        XtremeContentType.MOVIE
                    }
                }
                "categoryId" -> categoryId = reader.nextString()
                "categoryName" -> categoryName = reader.nextString()
                "streamUrl" -> streamUrl = reader.nextString()
                "accountName" -> accountName = reader.nextString()
                "accountId" -> accountId = reader.nextString()
                "containerExtension" -> containerExtension = reader.nextString()
                "quality" -> quality = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (id.isBlank() || title.isBlank()) return null

        return XtremeItem(
            id = id,
            title = title,
            streamId = streamId,
            streamIcon = streamIcon,
            type = type,
            categoryId = categoryId,
            categoryName = categoryName,
            streamUrl = streamUrl,
            accountName = accountName,
            accountId = accountId,
            containerExtension = containerExtension,
            quality = quality
        )
    }

    /**
     * Wipes local cache file and signature, ensuring complete deletion of outdated items.
     */
    suspend fun clearCache(): Unit = withContext(Dispatchers.IO) {
        memoryCachedLibrary = null
        try {
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
            val tempFile = File(context.filesDir, "$CACHE_FILE_NAME.tmp")
            if (tempFile.exists()) {
                tempFile.delete()
            }
            prefs.edit().clear().apply()
            Log.d(TAG, "Successfully cleared local library cache and stored signature")
        } catch (e: Exception) {
            Log.e(TAG, "Error while clearing local library cache: ${e.message}", e)
        }
    }
}
