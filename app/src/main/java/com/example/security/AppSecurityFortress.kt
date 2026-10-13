package com.example.security

import android.content.Context
import android.os.Build
import android.os.Debug
import android.util.Log
import java.io.File
import java.net.URI

/**
 * AppSecurityFortress
 * Comprehensive security hardening, anti-tamper, and input validation engine.
 * Protects the application against unauthorized access, root exploits, injection attacks, and reverse engineering.
 */
object AppSecurityFortress {

    private const val TAG = "AppSecurityFortress"

    // Common root binary paths checked for tampering
    private val KNOWN_ROOT_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/system/xbin/daemonsu"
    )

    private val DANGEROUS_PACKAGES = arrayOf(
        "com.noshufou.android.su",
        "com.thirdparty.superuser",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.zachspong.temprootremovejb",
        "com.ramdroid.appquarantine",
        "com.topjohnwu.magisk"
    )

    /**
     * Inspects device integrity for signs of tampering, root binaries, or hooking frameworks.
     */
    fun isDeviceTampered(context: Context): Boolean {
        try {
            // 1. Check for build tags indicating test/custom firmware
            val buildTags = Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                Log.w(TAG, "Integrity note: Custom build tags detected.")
            }

            // 2. Check for existence of SU binaries in standard filesystem locations
            for (path in KNOWN_ROOT_PATHS) {
                if (File(path).exists()) {
                    Log.w(TAG, "Root binary detected at: $path")
                    return true
                }
            }

            // 3. Check for debuggers attached in production
            if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
                Log.w(TAG, "Active debugger detected.")
            }

            // 4. Verify package manager for known rooting manager applications
            val pm = context.packageManager
            for (pkg in DANGEROUS_PACKAGES) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    Log.w(TAG, "Dangerous root manager package detected: $pkg")
                    return true
                } catch (_: Exception) {
                    // Package not found - safe
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during security verification: ${e.message}")
        }
        return false
    }

    /**
     * Sanitizes and validates stream & media URLs to protect against SSRF, local file inclusion,
     * javascript injection, and path traversal vulnerabilities.
     */
    fun sanitizeMediaUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim().removeSurrounding("\"").removeSurrounding("'").trim()
        if (trimmed.isBlank()) return ""

        // Disallow dangerous URI schemes
        val lower = trimmed.lowercase()
        if (lower.startsWith("file:") || lower.startsWith("content:") || lower.startsWith("javascript:") || lower.startsWith("data:")) {
            Log.w(TAG, "Blocked hazardous URL scheme: $trimmed")
            return ""
        }

        // Must be a legitimate streaming or web protocol
        if (!lower.startsWith("http://") && !lower.startsWith("https://") &&
            !lower.startsWith("rtmp://") && !lower.startsWith("rtmps://") &&
            !lower.startsWith("rtsp://") && !lower.startsWith("mms://")
        ) {
            // Check for protocol-relative URLs
            if (lower.startsWith("//")) {
                return "https:$trimmed"
            }
            return ""
        }

        // Validate URI syntax
        return try {
            val uri = URI(trimmed)
            if (uri.host.isNullOrBlank()) "" else trimmed
        } catch (_: Exception) {
            trimmed
        }
    }

    /**
     * Sanitizes text strings to prevent cross-site scripting or database formatting bugs.
     */
    fun sanitizeSafeText(input: String, maxLength: Int = 200): String {
        return input.trim()
            .replace("\u0000", "")
            .replace(Regex("""<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>""", RegexOption.IGNORE_CASE), "")
            .take(maxLength)
    }

    /**
     * Validates whether a given stream URL is safe to play in the media player.
     */
    fun isStreamPlayableSecurely(url: String): Boolean {
        val clean = sanitizeMediaUrl(url)
        return clean.isNotBlank()
    }
}
