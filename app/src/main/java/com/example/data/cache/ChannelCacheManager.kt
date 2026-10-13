package com.example.data.cache

import android.content.Context
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import com.example.data.model.Channel
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * High-performance, zero-latency local disk cache for Channels and Categories.
 * Enables instant (0ms) preloading and rendering of the Home section upon app launch
 * before Firebase Realtime Database completes its network sync.
 */
class ChannelCacheManager(private val context: Context) {

    companion object {
        private const val TAG = "ChannelCacheManager"
        private const val CHANNELS_CACHE_FILE = "channels_preloaded_cache.json"
        private const val CATEGORIES_CACHE_FILE = "categories_preloaded_cache.json"
    }

    private val channelsFile: File
        get() = File(context.filesDir, CHANNELS_CACHE_FILE)

    private val categoriesFile: File
        get() = File(context.filesDir, CATEGORIES_CACHE_FILE)

    // Synchronous in-memory snapshot
    @Volatile
    private var memoryCachedChannels: List<Channel>? = null

    @Volatile
    private var memoryCachedCategories: List<ChannelCategory>? = null

    /**
     * Loads preloaded channels synchronously from memory or disk cache on the spot.
     * Guaranteed never to crash, returning immediately for zero-delay UI rendering.
     */
    fun loadCachedChannelsSync(): List<Channel> {
        memoryCachedChannels?.let { return it }

        if (!channelsFile.exists() || channelsFile.length() == 0L) {
            return emptyList()
        }

        val list = ArrayList<Channel>()
        var foundMalicious = false
        try {
            FileReader(channelsFile).use { fileReader ->
                JsonReader(fileReader).use { reader ->
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val channel = readChannel(reader)
                        if (channel != null) {
                            if (com.example.data.security.AppSecurityGuard.isMaliciousChannel(channel.name, channel.category)) {
                                foundMalicious = true
                            } else {
                                list.add(channel)
                            }
                        }
                    }
                    reader.endArray()
                }
            }
            if (foundMalicious) {
                try { channelsFile.delete() } catch (_: Exception) {}
            }
            memoryCachedChannels = list
        } catch (e: Exception) {
            Log.w(TAG, "Error loading cached channels synchronously: ${e.message}")
        }
        return list
    }

    /**
     * Loads preloaded categories synchronously from memory or disk cache.
     */
    fun loadCachedCategoriesSync(): List<ChannelCategory> {
        memoryCachedCategories?.let { return it }

        if (!categoriesFile.exists() || categoriesFile.length() == 0L) {
            return emptyList()
        }

        val list = ArrayList<ChannelCategory>()
        var foundMalicious = false
        try {
            FileReader(categoriesFile).use { fileReader ->
                JsonReader(fileReader).use { reader ->
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val cat = readCategory(reader)
                        if (cat != null) {
                            if (com.example.data.security.AppSecurityGuard.isMaliciousText(cat.name)) {
                                foundMalicious = true
                            } else {
                                list.add(cat)
                            }
                        }
                    }
                    reader.endArray()
                }
            }
            if (foundMalicious) {
                try { categoriesFile.delete() } catch (_: Exception) {}
            }
            memoryCachedCategories = list
        } catch (e: Exception) {
            Log.w(TAG, "Error loading cached categories synchronously: ${e.message}")
        }
        return list
    }

    /**
     * Asynchronously saves channels to disk cache using fast streaming JsonWriter.
     */
    suspend fun saveChannels(channels: List<Channel>) = withContext(Dispatchers.IO) {
        val cleanChannels = channels.filterNot {
            com.example.data.security.AppSecurityGuard.isMaliciousChannel(it.name, it.category)
        }
        if (cleanChannels.isEmpty()) return@withContext
        memoryCachedChannels = cleanChannels

        val tempFile = File(context.filesDir, "$CHANNELS_CACHE_FILE.tmp")
        try {
            FileWriter(tempFile).use { fileWriter ->
                JsonWriter(fileWriter).use { writer ->
                    writer.beginArray()
                    for (ch in cleanChannels) {
                        writeChannel(writer, ch)
                    }
                    writer.endArray()
                }
            }
            if (channelsFile.exists()) {
                channelsFile.delete()
            }
            tempFile.renameTo(channelsFile)
        } catch (e: Exception) {
            Log.w(TAG, "Error saving channels to disk cache: ${e.message}")
            try { tempFile.delete() } catch (_: Exception) {}
        }
    }

    /**
     * Asynchronously saves categories to disk cache using fast streaming JsonWriter.
     */
    suspend fun saveCategories(categories: List<ChannelCategory>) = withContext(Dispatchers.IO) {
        val cleanCategories = categories.filterNot {
            com.example.data.security.AppSecurityGuard.isMaliciousText(it.name)
        }
        if (cleanCategories.isEmpty()) return@withContext
        memoryCachedCategories = cleanCategories

        val tempFile = File(context.filesDir, "$CATEGORIES_CACHE_FILE.tmp")
        try {
            FileWriter(tempFile).use { fileWriter ->
                JsonWriter(fileWriter).use { writer ->
                    writer.beginArray()
                    for (cat in categories) {
                        writeCategory(writer, cat)
                    }
                    writer.endArray()
                }
            }
            if (categoriesFile.exists()) {
                categoriesFile.delete()
            }
            tempFile.renameTo(categoriesFile)
        } catch (e: Exception) {
            Log.w(TAG, "Error saving categories to disk cache: ${e.message}")
            try { tempFile.delete() } catch (_: Exception) {}
        }
    }

    private fun writeChannel(writer: JsonWriter, ch: Channel) {
        writer.beginObject()
        writer.name("id").value(ch.id)
        writer.name("name").value(ch.name)
        writer.name("imageUrl").value(ch.imageUrl)
        writer.name("category").value(ch.category)
        writer.name("views").value(ch.views)
        writer.name("createdAt").value(ch.createdAt)

        writer.name("servers")
        writer.beginArray()
        for (server in ch.servers) {
            writer.beginObject()
            writer.name("id").value(server.id)
            writer.name("name").value(server.name)
            writer.name("streamUrl").value(server.streamUrl)
            writer.name("streamType").value(server.streamType)
            writer.endObject()
        }
        writer.endArray()

        writer.endObject()
    }

    private fun readChannel(reader: JsonReader): Channel? {
        var id = ""
        var name = ""
        var imageUrl = ""
        var category = "عام"
        var views = 0L
        var createdAt = 0L
        val servers = mutableListOf<ChannelServer>()

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "name" -> name = reader.nextString()
                "imageUrl" -> imageUrl = reader.nextString()
                "category" -> category = reader.nextString()
                "views" -> views = reader.nextLong()
                "createdAt" -> createdAt = reader.nextLong()
                "servers" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val s = readServer(reader)
                        if (s != null) servers.add(s)
                    }
                    reader.endArray()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (id.isBlank() || name.isBlank()) return null

        return Channel(
            id = id,
            name = name,
            imageUrl = imageUrl,
            category = category,
            views = views,
            createdAt = createdAt,
            servers = servers
        )
    }

    private fun readServer(reader: JsonReader): ChannelServer? {
        var id = ""
        var name = ""
        var streamUrl = ""
        var streamType = "m3u8"

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "name" -> name = reader.nextString()
                "streamUrl" -> streamUrl = reader.nextString()
                "streamType" -> streamType = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (streamUrl.isBlank()) return null

        return ChannelServer(
            id = id,
            name = name,
            streamUrl = streamUrl,
            streamType = streamType
        )
    }

    private fun writeCategory(writer: JsonWriter, cat: ChannelCategory) {
        writer.beginObject()
        writer.name("id").value(cat.id)
        writer.name("name").value(cat.name)
        writer.name("imageUrl").value(cat.imageUrl)
        writer.name("m3uUrl").value(cat.m3uUrl)
        writer.name("order").value(cat.order.toLong())
        writer.name("createdAt").value(cat.createdAt)
        writer.endObject()
    }

    private fun readCategory(reader: JsonReader): ChannelCategory? {
        var id = ""
        var name = ""
        var imageUrl = ""
        var m3uUrl = ""
        var order = 999
        var createdAt = 0L

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "name" -> name = reader.nextString()
                "imageUrl" -> imageUrl = reader.nextString()
                "m3uUrl" -> m3uUrl = reader.nextString()
                "order" -> order = reader.nextInt()
                "createdAt" -> createdAt = reader.nextLong()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (id.isBlank() || name.isBlank()) return null

        return ChannelCategory(
            id = id,
            name = name,
            imageUrl = imageUrl,
            m3uUrl = m3uUrl,
            order = order,
            createdAt = createdAt
        )
    }
}
