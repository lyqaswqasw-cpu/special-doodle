package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.ui.player.PlayerFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class DownloadItem(
    val downloadId: Long,
    val title: String,
    val fileName: String,
    val originalUrl: String,
    val localUri: String? = null,
    val status: Int = DownloadManager.STATUS_PENDING,
    val pausedReason: Int = 0,
    val failedReason: Int = 0,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isCompleted: Boolean
        get() = status == DownloadManager.STATUS_SUCCESSFUL

    val isRunning: Boolean
        get() = status == DownloadManager.STATUS_RUNNING

    val isPaused: Boolean
        get() = status == DownloadManager.STATUS_PAUSED

    val isPending: Boolean
        get() = status == DownloadManager.STATUS_PENDING

    val isFailed: Boolean
        get() = status == DownloadManager.STATUS_FAILED

    val isWaitingForNetwork: Boolean
        get() = isPaused && (pausedReason == DownloadManager.PAUSED_WAITING_FOR_NETWORK || pausedReason == DownloadManager.PAUSED_QUEUED_FOR_WIFI)

    val progressPercent: Int
        get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100) else 0

    val statusDescription: String
        get() = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> "مكتمل وجاهز للمشاهدة بدون إنترنت"
            DownloadManager.STATUS_RUNNING -> "جاري التحميل $progressPercent%"
            DownloadManager.STATUS_PAUSED -> {
                if (isWaitingForNetwork) {
                    "متوقف بانتظار اتصال الإنترنت (يستأنف تلقائياً)"
                } else if (pausedReason == DownloadManager.PAUSED_WAITING_TO_RETRY) {
                    "متوقف مؤقتاً - جاري إعادة المحاولة..."
                } else {
                    "متوقف مؤقتاً"
                }
            }
            DownloadManager.STATUS_PENDING -> "قيد الانتظار في قائمة التنزيل..."
            DownloadManager.STATUS_FAILED -> "فشل التحميل - اضغط لإعادة المحاولة"
            else -> "قيد المعالجة"
        }
}

object DownloadHelper {
    private const val TAG = "DownloadHelper"
    private const val PREFS_NAME = "downloads_tracker_prefs"
    private const val KEY_DOWNLOADS_LIST = "tracked_downloads_json"

    fun startDownload(context: Context, url: String, title: String, fileName: String): Long {
        try {
            val cleanUrl = url.trim()
            if (cleanUrl.isBlank() || cleanUrl.startsWith("content://") || cleanUrl.startsWith("file://")) {
                Toast.makeText(context, "رابط التنزيل غير صالح", Toast.LENGTH_SHORT).show()
                return -1L
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (downloadManager == null) {
                Toast.makeText(context, "خدمة التنزيلات غير متوفرة في النظام", Toast.LENGTH_SHORT).show()
                return -1L
            }

            val uri = Uri.parse(cleanUrl)

            // Extract correct extension and sanitize filename
            val ext = when {
                cleanUrl.contains(".mp4", ignoreCase = true) -> "mp4"
                cleanUrl.contains(".mkv", ignoreCase = true) -> "mkv"
                cleanUrl.contains(".ts", ignoreCase = true) -> "ts"
                fileName.contains(".") -> fileName.substringAfterLast(".").lowercase()
                else -> "mp4"
            }

            val baseName = fileName.substringBeforeLast(".")
                .replace("[\\\\/:*?\"<>|]".toRegex(), "_")
                .trim()
                .ifBlank { "video_${System.currentTimeMillis()}" }

            val safeFileName = "$baseName.$ext"

            // Target storage directory (App-specific external files dir: guaranteed write access without runtime permission blocks)
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.filesDir, "downloads").apply { mkdirs() }

            val targetFile = File(downloadDir, safeFileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val request = DownloadManager.Request(uri).apply {
                setTitle(title.ifBlank { "تنزيل فيديو" })
                setDescription("جاري تنزيل المحتوى وحفظه محلياً...")
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setAllowedOverRoaming(true)
                setAllowedOverMetered(true)
                setRequiresCharging(false)
                setRequiresDeviceIdle(false)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                addRequestHeader("User-Agent", PlayerFactory.IPTV_USER_AGENT)
                addRequestHeader("Accept", "*/*")
                addRequestHeader("Connection", "keep-alive")
                try {
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, safeFileName)
                } catch (e: Exception) {
                    setDestinationUri(Uri.fromFile(targetFile))
                }
            }

            val downloadId = downloadManager.enqueue(request)

            // Save record in local tracker prefs
            val newRecord = DownloadItem(
                downloadId = downloadId,
                title = title.ifBlank { safeFileName },
                fileName = safeFileName,
                originalUrl = cleanUrl,
                localUri = targetFile.absolutePath,
                status = DownloadManager.STATUS_PENDING,
                createdAt = System.currentTimeMillis()
            )
            saveTrackedDownload(context, newRecord)

            Toast.makeText(
                context,
                "بدأ التحميل: $title\nيتم الحفظ محلياً وسيستأنف تلقائياً إذا انقطع النت",
                Toast.LENGTH_LONG
            ).show()
            return downloadId
        } catch (e: Exception) {
            Log.e(TAG, "Error starting download: ${e.message}", e)
            Toast.makeText(context, "تعذر بدء التحميل: ${e.localizedMessage ?: "خطأ غير متوقع"}", Toast.LENGTH_SHORT).show()
            return -1L
        }
    }

    fun retryDownload(context: Context, downloadId: Long): Long {
        val savedList = getSavedDownloads(context)
        val item = savedList.find { it.downloadId == downloadId } ?: return -1L

        // Remove old download from system manager
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            downloadManager?.remove(downloadId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed removing old download before retry: ${e.message}")
        }

        // Delete previous tracked entry
        deleteDownloadRecordOnly(context, downloadId)

        // Start new download
        return startDownload(context, item.originalUrl, item.title, item.fileName)
    }

    fun getDownloads(context: Context): List<DownloadItem> {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        val savedList = getSavedDownloads(context).toMutableList()
        val updatedList = mutableListOf<DownloadItem>()

        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "downloads")

        for (item in savedList) {
            var updated = item
            var querySuccess = false

            if (downloadManager != null) {
                try {
                    val query = DownloadManager.Query().setFilterById(item.downloadId)
                    val cursor: Cursor? = downloadManager.query(query)
                    cursor?.use { c ->
                        if (c.moveToFirst()) {
                            querySuccess = true
                            val statusIdx = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val reasonIdx = c.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val bytesSoFarIdx = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val totalBytesIdx = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            val localUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                            val status = if (statusIdx != -1) c.getInt(statusIdx) else item.status
                            val reason = if (reasonIdx != -1) c.getInt(reasonIdx) else 0
                            val bytesDownloaded = if (bytesSoFarIdx != -1) c.getLong(bytesSoFarIdx) else item.bytesDownloaded
                            val totalBytes = if (totalBytesIdx != -1) c.getLong(totalBytesIdx) else item.totalBytes

                            var localUriStr = if (localUriIdx != -1) c.getString(localUriIdx) else null
                            if (localUriStr.isNullOrBlank() && status == DownloadManager.STATUS_SUCCESSFUL) {
                                val contentUri = downloadManager.getUriForDownloadedFile(item.downloadId)
                                if (contentUri != null) {
                                    localUriStr = contentUri.toString()
                                }
                            }

                            val targetFile = File(downloadDir, item.fileName)
                            if (targetFile.exists() && localUriStr.isNullOrBlank()) {
                                localUriStr = targetFile.absolutePath
                            }

                            val pausedReason = if (status == DownloadManager.STATUS_PAUSED) reason else 0
                            val failedReason = if (status == DownloadManager.STATUS_FAILED) reason else 0

                            updated = item.copy(
                                status = status,
                                pausedReason = pausedReason,
                                failedReason = failedReason,
                                bytesDownloaded = bytesDownloaded,
                                totalBytes = totalBytes,
                                localUri = localUriStr ?: item.localUri ?: targetFile.absolutePath
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to query download id ${item.downloadId}: ${e.message}")
                }
            }

            if (!querySuccess) {
                // If DownloadManager cleared the record, verify if local file still exists
                val targetFile = File(downloadDir, item.fileName)
                val publicFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    item.fileName
                )
                val existingFile = if (targetFile.exists()) targetFile else if (publicFile.exists()) publicFile else null

                if (existingFile != null && existingFile.length() > 0) {
                    updated = item.copy(
                        status = DownloadManager.STATUS_SUCCESSFUL,
                        localUri = existingFile.absolutePath,
                        bytesDownloaded = existingFile.length(),
                        totalBytes = existingFile.length()
                    )
                }
            }

            updatedList.add(updated)
        }

        // Persist updated list
        saveAllTrackedDownloads(context, updatedList)
        return updatedList.sortedByDescending { it.createdAt }
    }

    fun deleteDownload(context: Context, downloadId: Long): Boolean {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            downloadManager?.remove(downloadId)

            val current = getSavedDownloads(context).toMutableList()
            val itemToDelete = current.find { it.downloadId == downloadId }

            // Delete actual file from storage
            if (itemToDelete != null) {
                try {
                    val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val targetFile = if (downloadDir != null) File(downloadDir, itemToDelete.fileName) else null
                    if (targetFile?.exists() == true) {
                        targetFile.delete()
                    }

                    if (itemToDelete.localUri != null) {
                        val localPath = if (itemToDelete.localUri.startsWith("file://")) {
                            itemToDelete.localUri.removePrefix("file://")
                        } else {
                            itemToDelete.localUri
                        }
                        val localFile = File(localPath)
                        if (localFile.exists()) {
                            localFile.delete()
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not delete file from storage: ${e.message}")
                }
            }

            val filtered = current.filter { it.downloadId != downloadId }
            saveAllTrackedDownloads(context, filtered)
            Toast.makeText(context, "تم حذف التنزيل", Toast.LENGTH_SHORT).show()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting download: ${e.message}", e)
            Toast.makeText(context, "تعذر حذف التنزيل", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun deleteDownloadRecordOnly(context: Context, downloadId: Long) {
        val current = getSavedDownloads(context).filter { it.downloadId != downloadId }
        saveAllTrackedDownloads(context, current)
    }

    fun openSystemDownloads(context: Context) {
        try {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val uri = if (downloadDir != null) Uri.fromFile(downloadDir) else Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "*/*")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "تم حفظ الملفات محلياً في مجلد التنزيلات بالتطبيق", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 بايت"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.2f غ.ب", gb)
            mb >= 1.0 -> String.format("%.1f م.ب", mb)
            kb >= 1.0 -> String.format("%.0f ك.ب", kb)
            else -> "$bytes بايت"
        }
    }

    private fun getSavedDownloads(context: Context): List<DownloadItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_DOWNLOADS_LIST, null) ?: return emptyList()
        val list = mutableListOf<DownloadItem>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    DownloadItem(
                        downloadId = obj.optLong("downloadId"),
                        title = obj.optString("title"),
                        fileName = obj.optString("fileName"),
                        originalUrl = obj.optString("originalUrl"),
                        localUri = obj.optString("localUri").takeIf { it.isNotBlank() },
                        status = obj.optInt("status"),
                        pausedReason = obj.optInt("pausedReason", 0),
                        failedReason = obj.optInt("failedReason", 0),
                        bytesDownloaded = obj.optLong("bytesDownloaded"),
                        totalBytes = obj.optLong("totalBytes"),
                        createdAt = obj.optLong("createdAt")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing saved downloads: ${e.message}")
        }
        return list
    }

    private fun saveTrackedDownload(context: Context, item: DownloadItem) {
        val current = getSavedDownloads(context).toMutableList()
        current.removeAll { it.downloadId == item.downloadId }
        current.add(0, item)
        saveAllTrackedDownloads(context, current)
    }

    private fun saveAllTrackedDownloads(context: Context, items: List<DownloadItem>) {
        try {
            val arr = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("downloadId", item.downloadId)
                    put("title", item.title)
                    put("fileName", item.fileName)
                    put("originalUrl", item.originalUrl)
                    put("localUri", item.localUri ?: "")
                    put("status", item.status)
                    put("pausedReason", item.pausedReason)
                    put("failedReason", item.failedReason)
                    put("bytesDownloaded", item.bytesDownloaded)
                    put("totalBytes", item.totalBytes)
                    put("createdAt", item.createdAt)
                }
                arr.put(obj)
            }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_DOWNLOADS_LIST, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving downloads list: ${e.message}")
        }
    }
}
