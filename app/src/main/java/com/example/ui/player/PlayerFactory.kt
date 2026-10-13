package com.example.ui.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

/**
 * Centralized High-Performance ExoPlayer Factory designed for live sports and streaming OTT (TOD & beIN level).
 *
 * Key features:
 * 1. Ultra-snappy startup time (<1000ms playback initiation buffer).
 * 2. Extended high-stability buffer ceiling (60s) to prevent buffering during matches.
 * 3. Back-buffer caching (15s) enabling instant rewind without network spikes.
 * 4. Automatic audio-focus management (smooth ducking/pause on calls).
 * 5. IPTV-standard User-Agent and connection headers preventing HTTP 409 conflict.
 */
object PlayerFactory {

    const val IPTV_USER_AGENT = "IPTVSmartersPro/3.1.5 (Linux; Android 13; Mobile)"

    @OptIn(UnstableApi::class)
    fun createHttpDataSourceFactory(): DefaultHttpDataSource.Factory {
        return DefaultHttpDataSource.Factory()
            .setUserAgent(IPTV_USER_AGENT)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)
            .setDefaultRequestProperties(
                mapOf(
                    "User-Agent" to IPTV_USER_AGENT,
                    "Accept" to "*/*"
                )
            )
    }

    @OptIn(UnstableApi::class)
    fun createExoPlayer(context: Context): ExoPlayer {
        val httpDataSourceFactory = createHttpDataSourceFactory()
        val upstreamDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(upstreamDataSourceFactory)

        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context)
            .setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 10_000,
                /* maxBufferMs = */ 60_000,
                /* bufferForPlaybackMs = */ 1_000,
                /* bufferForPlaybackAfterRebufferMs = */ 2_500
            )
            .setBackBuffer(15_000, true)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        return ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setSeekParameters(SeekParameters.CLOSEST_SYNC)
            .setAudioAttributes(audioAttributes, true)
            .build()
            .apply {
                playWhenReady = false
            }
    }

    /**
     * Checks if error or its causal chain was caused by HTTP 409 Conflict.
     */
    fun isHttp409Conflict(error: Throwable?): Boolean {
        var curr: Throwable? = error
        while (curr != null) {
            if (curr is HttpDataSource.InvalidResponseCodeException && curr.responseCode == 409) {
                return true
            }
            if (curr.message?.contains("Response code: 409", ignoreCase = true) == true) {
                return true
            }
            curr = curr.cause
        }
        return false
    }

    /**
     * Returns a human-friendly Arabic error message for HTTP or playback errors.
     */
    fun getReadableErrorMessage(error: Throwable?): String {
        if (isHttp409Conflict(error)) {
            return "تم الوصول للحد الأقصى للاتصالات المتزامنة على هذا السيرفر (HTTP 409). يرجى الانتظار لحظات أو الضغط على إعادة المحاولة."
        }
        var curr: Throwable? = error
        while (curr != null) {
            if (curr is HttpDataSource.InvalidResponseCodeException) {
                return when (curr.responseCode) {
                    401, 403 -> "تعذر الوصول للسيرفر (غير مصرح أو انتهت صلاحية الحساب - كود ${curr.responseCode})"
                    404 -> "القناة أو الفيديو غير موجود على السيرفر (كود 404)"
                    409 -> "الحد الأقصى للاتصالات المتزامنة تم الوصول إليه (كود 409)"
                    500, 502, 503, 504 -> "سيرفر البث يواجه ضغطاً مؤقتاً (كود ${curr.responseCode})"
                    else -> "خطأ في الاتصال بسيرفر البث (كود ${curr.responseCode})"
                }
            }
            curr = curr.cause
        }
        return "تعذر تشغيل هذا السيرفر. يرجى التحقق من الرابط أو التبديل لسيرفر آخر."
    }
}
