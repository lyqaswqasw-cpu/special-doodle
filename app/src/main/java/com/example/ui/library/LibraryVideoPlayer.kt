package com.example.ui.library

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ui.player.PlayerFactory
import com.example.data.model.XtremeContentType
import com.example.data.model.XtremeItem
import com.example.ui.player.VideoScaleMode
import com.example.ui.player.findActivity
import com.example.ui.player.setScreenLandscape
import com.example.ui.theme.BrightLavender
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkPurpleContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private val probeClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(1800, TimeUnit.MILLISECONDS)
        .readTimeout(1800, TimeUnit.MILLISECONDS)
        .followRedirects(true)
        .build()
}

fun buildCandidateUrl(streamUrl: String, extension: String): String {
    val cleanUrl = streamUrl.trim()
    val lastSlash = cleanUrl.lastIndexOf('/')
    val lastDot = cleanUrl.lastIndexOf('.')
    return if (lastDot > lastSlash && lastDot != -1) {
        cleanUrl.substring(0, lastDot) + "." + extension
    } else {
        "$cleanUrl.$extension"
    }
}

suspend fun probeCandidateStream(url: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "IPTVSmarters/2.0")
            .header("Range", "bytes=0-1024")
            .head()
            .build()
        val response = probeClient.newCall(request).execute()
        val code = response.code
        response.close()
        code in 200..308
    } catch (_: Exception) {
        try {
            val getReq = Request.Builder()
                .url(url)
                .header("User-Agent", "IPTVSmarters/2.0")
                .header("Range", "bytes=0-1024")
                .get()
                .build()
            val getResp = probeClient.newCall(getReq).execute()
            val code = getResp.code
            getResp.close()
            code in 200..308
        } catch (_: Exception) {
            false
        }
    }
}

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@OptIn(UnstableApi::class)
@Composable
fun LibraryVideoPlayer(
    item: XtremeItem?,
    onClosePlayer: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    var isProbing by remember { mutableStateOf(false) }
    var scaleMode by remember { mutableStateOf(VideoScaleMode.FIT_16_9) }

    // Screen Lock Mode (TOD Style)
    var isScreenLocked by remember { mutableStateOf(false) }
    var showUnlockPrompt by remember { mutableStateOf(false) }

    // Gesture State: Brightness & Volume
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    var playerWidth by remember { mutableFloatStateOf(1f) }
    var playerHeight by remember { mutableFloatStateOf(1f) }
    var brightnessLevel by remember { mutableFloatStateOf(0.7f) }
    var isAdjustingBrightness by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableFloatStateOf(0.7f) }
    var isAdjustingVolume by remember { mutableStateOf(false) }
    var seekFeedbackSide by remember { mutableStateOf<String?>(null) }
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }

    // Playback Speed
    val speedOptions = remember { listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f) }
    var currentSpeed by remember { mutableFloatStateOf(1.0f) }
    var isSpeedMenuOpen by remember { mutableStateOf(false) }

    // Video Timeline / Seeking state
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekSliderPosition by remember { mutableFloatStateOf(0f) }

    // Sequential extension probing order: MKV -> MP4 -> M3U8
    val extensionsOrder = remember { listOf("mkv", "mp4", "m3u8") }
    var activeCandidateIndex by remember { mutableStateOf(0) }
    var currentActiveExt by remember { mutableStateOf("mkv") }
    var activeStreamUrl by remember { mutableStateOf<String?>(null) }
    var expiredError by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }

    // BackHandler for Fullscreen exit
    if (isFullscreen) {
        BackHandler {
            onToggleFullscreen()
        }
    }

    // Screen On lock while playing
    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Immersive landscape orientation when entering fullscreen mode
    LaunchedEffect(isFullscreen) {
        activity?.setScreenLandscape(isFullscreen)
    }

    DisposableEffect(activity) {
        onDispose {
            activity?.setScreenLandscape(false)
        }
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(areControlsVisible, isPlaying, isUserSeeking, isScreenLocked) {
        if (areControlsVisible && isPlaying && !isUserSeeking && !isScreenLocked) {
            delay(4000)
            areControlsVisible = false
        }
    }

    // Auto-hide unlock prompt after 3.5 seconds
    LaunchedEffect(showUnlockPrompt) {
        if (showUnlockPrompt) {
            delay(3500)
            showUnlockPrompt = false
        }
    }

    // Auto-hide brightness HUD
    LaunchedEffect(isAdjustingBrightness) {
        if (isAdjustingBrightness) {
            delay(1400)
            isAdjustingBrightness = false
        }
    }

    // Auto-hide volume HUD
    LaunchedEffect(isAdjustingVolume) {
        if (isAdjustingVolume) {
            delay(1400)
            isAdjustingVolume = false
        }
    }

    // Auto-hide seek feedback
    LaunchedEffect(seekFeedbackText) {
        if (seekFeedbackText != null) {
            delay(900)
            seekFeedbackText = null
            seekFeedbackSide = null
        }
    }

    val exoPlayer = remember {
        PlayerFactory.createExoPlayer(context)
    }

    // Progress timeline updater loop
    LaunchedEffect(exoPlayer, isPlaying) {
        while (true) {
            if (!isUserSeeking) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
                if (totalDurationMs > 0) {
                    seekSliderPosition = (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    seekSliderPosition = 0f
                }
            }
            delay(350)
        }
    }

    // Stop playback on lifecycle STOP
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                Lifecycle.Event.ON_START -> if (isPlaying) exoPlayer.play()
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }

    fun playCandidateUrl(url: String, ext: String) {
        val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(url))
        when (ext.lowercase()) {
            "m3u8" -> mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
            "mp4" -> mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MP4)
            "mkv" -> mediaItemBuilder.setMimeType(MimeTypes.VIDEO_MATROSKA)
            else -> {}
        }
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(mediaItemBuilder.build())
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // ExoPlayer listener with automated fallback to next format on error
    DisposableEffect(exoPlayer, item?.id, retryTrigger) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = (state == Player.STATE_BUFFERING)
                if (state == Player.STATE_READY) {
                    expiredError = null
                    isProbing = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                val rawUrl = item?.streamUrl?.trim() ?: ""
                val isLocal = rawUrl.startsWith("content://") || rawUrl.startsWith("file://") || rawUrl.startsWith("/")
                if (isLocal) {
                    expiredError = "تعذر تشغيل الفيديو المحمّل، تأكد من اكتمال التنزيل"
                    isProbing = false
                    return
                }
                val is409 = PlayerFactory.isHttp409Conflict(error)
                coroutineScope.launch {
                    if (is409) {
                        // The IPTV server returned 409 Conflict (max concurrency / session limit reached).
                        // Stop immediately to close our end of the socket, wait 1.5s for the server to clear it, then retry.
                        exoPlayer.stop()
                        exoPlayer.clearMediaItems()
                        delay(1500)
                    }
                    val nextIndex = activeCandidateIndex + 1
                    if (nextIndex < extensionsOrder.size && item != null) {
                        activeCandidateIndex = nextIndex
                        val nextExt = extensionsOrder[nextIndex]
                        currentActiveExt = nextExt
                        val nextUrl = buildCandidateUrl(item.streamUrl, nextExt)
                        activeStreamUrl = nextUrl
                        playCandidateUrl(nextUrl, nextExt)
                    } else {
                        // All candidates failed: show exact error text
                        expiredError = if (is409) {
                            "تم الوصول للحد الأقصى للاتصالات المتزامنة على هذا السيرفر (HTTP 409). يرجى الانتظار والمحاولة مجدداً."
                        } else {
                            "هذا الفيديو لا يمكن تشغيله تمت انتهاء المدة"
                        }
                        isProbing = false
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Sequential probe in background without displaying any text to the user
    LaunchedEffect(item?.id, item?.streamUrl, retryTrigger) {
        if (item == null || item.streamUrl.isBlank()) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            activeStreamUrl = null
            expiredError = null
            isProbing = false
            return@LaunchedEffect
        }

        expiredError = null
        val rawUrl = item.streamUrl.trim()
        val isLocal = rawUrl.startsWith("content://") || rawUrl.startsWith("file://") || rawUrl.startsWith("/")

        if (isLocal) {
            isProbing = false
            activeStreamUrl = rawUrl
            val uri = if (rawUrl.startsWith("/")) Uri.fromFile(java.io.File(rawUrl)) else Uri.parse(rawUrl)
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            return@LaunchedEffect
        }

        isProbing = true
        activeCandidateIndex = 0

        var workingUrl: String? = null
        var workingExt: String? = null

        for (index in extensionsOrder.indices) {
            val ext = extensionsOrder[index]
            activeCandidateIndex = index
            currentActiveExt = ext
            val candidate = buildCandidateUrl(item.streamUrl, ext)

            val isAlive = probeCandidateStream(candidate)
            if (isAlive) {
                workingUrl = candidate
                workingExt = ext
                break
            }
        }

        if (workingUrl != null && workingExt != null) {
            activeStreamUrl = workingUrl
            currentActiveExt = workingExt
            playCandidateUrl(workingUrl, workingExt)
            isProbing = false
        } else {
            val firstCandidate = buildCandidateUrl(item.streamUrl, "mkv")
            currentActiveExt = "mkv"
            activeCandidateIndex = 0
            activeStreamUrl = firstCandidate
            playCandidateUrl(firstCandidate, "mkv")
            delay(3500)
            if (exoPlayer.playbackState != Player.STATE_READY && exoPlayer.playbackState != Player.STATE_BUFFERING) {
                expiredError = "هذا الفيديو لا يمكن تشغيله تمت انتهاء المدة"
                isProbing = false
            }
        }
    }

    // Empty placeholder if no item is selected yet
    if (item == null) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(DarkPurpleContainer.copy(alpha = 0.5f), DarkSurface)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                            .border(1.5.dp, NeonPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Library Player",
                            tint = NeonPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "مشغل المحتوى السريع",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اختر أي فيلم، مسلسل، أو بث من القائمة.\nللمسلسلات: اختر الحلقة من الأسفل للتشغيل.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    // Active Video Player View (Clean 100% full screen in fullscreen mode)
    Surface(
        shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp),
        color = Color.Black,
        border = if (isFullscreen) null else BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = modifier
            .then(
                if (isFullscreen) Modifier.fillMaxSize()
                else Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            .clip(if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp))
            .testTag("library_video_player_container")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onSizeChanged {
                    playerWidth = it.width.toFloat().coerceAtLeast(1f)
                    playerHeight = it.height.toFloat().coerceAtLeast(1f)
                }
                .pointerInput(isScreenLocked, isPlaying) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (isScreenLocked) return@detectDragGestures
                            if (offset.x < playerWidth * 0.45f) {
                                isAdjustingBrightness = true
                            } else if (offset.x > playerWidth * 0.55f) {
                                isAdjustingVolume = true
                            }
                        },
                        onDragEnd = {
                            isAdjustingBrightness = false
                            isAdjustingVolume = false
                        },
                        onDragCancel = {
                            isAdjustingBrightness = false
                            isAdjustingVolume = false
                        },
                        onDrag = { change, dragAmount ->
                            if (isScreenLocked) return@detectDragGestures
                            change.consume()
                            val delta = -dragAmount.y / playerHeight

                            if (change.position.x < playerWidth * 0.45f) {
                                isAdjustingBrightness = true
                                val newB = (brightnessLevel + delta * 1.5f).coerceIn(0.05f, 1.0f)
                                brightnessLevel = newB
                                activity?.let { act ->
                                    val lp = act.window.attributes
                                    lp.screenBrightness = newB
                                    act.window.attributes = lp
                                }
                            } else if (change.position.x > playerWidth * 0.55f) {
                                isAdjustingVolume = true
                                audioManager?.let { am ->
                                    val maxV = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
                                    val currentV = volumeLevel * maxV
                                    val newV = (currentV + delta * maxV * 1.5f).coerceIn(0f, maxV)
                                    volumeLevel = (newV / maxV).coerceIn(0f, 1f)
                                    am.setStreamVolume(AudioManager.STREAM_MUSIC, newV.toInt(), 0)
                                }
                            }
                        }
                    )
                }
                .pointerInput(isScreenLocked, areControlsVisible, isPlaying) {
                    detectTapGestures(
                        onTap = {
                            if (isScreenLocked) {
                                showUnlockPrompt = true
                            } else {
                                areControlsVisible = !areControlsVisible
                            }
                        },
                        onDoubleTap = { offset ->
                            if (isScreenLocked) {
                                showUnlockPrompt = true
                                return@detectTapGestures
                            }
                            when {
                                offset.x < playerWidth * 0.35f -> {
                                    val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                    seekFeedbackSide = "left"
                                    seekFeedbackText = "-10 ثانية"
                                }
                                offset.x > playerWidth * 0.65f -> {
                                    val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(
                                        if (exoPlayer.duration > 0) exoPlayer.duration else Long.MAX_VALUE
                                    )
                                    exoPlayer.seekTo(newPos)
                                    seekFeedbackSide = "right"
                                    seekFeedbackText = "+10 ثانية"
                                }
                                else -> {
                                    isPlaying = !isPlaying
                                }
                            }
                        }
                    )
                }
        ) {
            // AndroidView wrapping PlayerView
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                        setBackgroundColor(android.graphics.Color.BLACK)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        resizeMode = when (scaleMode) {
                            VideoScaleMode.FIT_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            VideoScaleMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                            VideoScaleMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            VideoScaleMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                        }
                    }
                },
                update = { view ->
                    if (view.player != exoPlayer) {
                        view.player = exoPlayer
                    }
                    view.resizeMode = when (scaleMode) {
                        VideoScaleMode.FIT_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        VideoScaleMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        VideoScaleMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        VideoScaleMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    }
                },
                onRelease = { view ->
                    view.player = null
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering / Fast Checking Indicator
            if (isBuffering || isProbing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = NeonPurple,
                        strokeWidth = 3.5.dp,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Expiration Error Message
            if (expiredError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.88f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Expired",
                            tint = LiveRed,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = expiredError ?: "هذا الفيديو لا يمكن تشغيله تمت انتهاء المدة",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { retryTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إعادة المحاولة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onClosePlayer,
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("إغلاق", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // HUD 1: Brightness Adjustment HUD
            AnimatedVisibility(
                visible = isAdjustingBrightness,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = "Brightness",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(80.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(brightnessLevel)
                                    .background(BrightLavender)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(brightnessLevel * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // HUD 2: Volume Adjustment HUD
            AnimatedVisibility(
                visible = isAdjustingVolume,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = if (volumeLevel == 0f) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Volume",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(80.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(volumeLevel)
                                    .background(NeonPurple)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(volumeLevel * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // HUD 3: Double-Tap Seek Feedback
            if (seekFeedbackText != null) {
                val align = if (seekFeedbackSide == "left") Alignment.CenterStart else Alignment.CenterEnd
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp),
                    contentAlignment = align
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Black.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (seekFeedbackSide == "left") Icons.Default.Replay10 else Icons.Default.Forward10,
                                contentDescription = null,
                                tint = BrightLavender,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = seekFeedbackText ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // HUD 4: Floating Unlock Button (Visible only when locked and tapped)
            AnimatedVisibility(
                visible = isScreenLocked && showUnlockPrompt,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.88f),
                    border = BorderStroke(1.5.dp, NeonPurple),
                    modifier = Modifier.clickable {
                        isScreenLocked = false
                        showUnlockPrompt = false
                        areControlsVisible = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Unlock",
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "الشاشة مقفلة • اضغط لفتح القفل",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive Controls Overlay
            AnimatedVisibility(
                visible = !isScreenLocked && areControlsVisible && expiredError == null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.90f)
                                )
                            )
                        )
                ) {
                    // TOP BAR: Title & Format & Lock & Speed & Close Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = VividViolet
                            ) {
                                Text(
                                    text = currentActiveExt.uppercase(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Screen Lock Button (TOD Style)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceVariant.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier.clickable {
                                    isScreenLocked = true
                                    areControlsVisible = false
                                    showUnlockPrompt = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = "قفل الشاشة",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(15.dp)
                                )
                            }

                            // Playback Speed Selector (TOD / Netflix Style)
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant.copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier.clickable { isSpeedMenuOpen = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = "السرعة",
                                            tint = BrightLavender,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "${currentSpeed}x",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isSpeedMenuOpen,
                                    onDismissRequest = { isSpeedMenuOpen = false },
                                    modifier = Modifier
                                        .background(DarkSurfaceVariant)
                                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = "سرعة التشغيل",
                                        color = NeonPurple,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                    speedOptions.forEach { speed ->
                                        val isSelected = (currentSpeed == speed)
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = SuccessGreen,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = "${speed}x",
                                                        color = if (isSelected) SuccessGreen else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            },
                                            onClick = {
                                                currentSpeed = speed
                                                isSpeedMenuOpen = false
                                                exoPlayer.setPlaybackSpeed(speed)
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = onClosePlayer,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Player",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // CENTER PLAY/PAUSE & FAST SEEK BUTTONS (-10s / +10s)
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        // Rewind 10s
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                    seekFeedbackSide = "left"
                                    seekFeedbackText = "-10 ثانية"
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "Rewind 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Play/Pause Center Action
                        Surface(
                            shape = CircleShape,
                            color = VividViolet,
                            border = BorderStroke(2.dp, NeonPurple),
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { isPlaying = !isPlaying }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Forward 10s
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(
                                        if (exoPlayer.duration > 0) exoPlayer.duration else Long.MAX_VALUE
                                    )
                                    exoPlayer.seekTo(newPos)
                                    seekFeedbackSide = "right"
                                    seekFeedbackText = "+10 ثانية"
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "Forward 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // BOTTOM CONTROLS & TIMELINE TRACK
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // SEEKBAR / TIMELINE TRACK
                        if (item.type != XtremeContentType.LIVE) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val displayCurr = if (totalDurationMs > 0) {
                                    formatDuration(if (isUserSeeking) (seekSliderPosition * totalDurationMs).toLong() else currentPositionMs)
                                } else {
                                    formatDuration(currentPositionMs)
                                }
                                val displayTotal = if (totalDurationMs > 0) formatDuration(totalDurationMs) else "--:--"

                                Text(
                                    text = displayCurr,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Slider(
                                    value = if (totalDurationMs > 0) seekSliderPosition else 0f,
                                    enabled = (totalDurationMs > 0),
                                    onValueChange = { fraction ->
                                        isUserSeeking = true
                                        seekSliderPosition = fraction
                                    },
                                    onValueChangeFinished = {
                                        if (totalDurationMs > 0) {
                                            val targetMs = (seekSliderPosition * totalDurationMs).toLong()
                                            exoPlayer.seekTo(targetMs)
                                            currentPositionMs = targetMs
                                            isUserSeeking = false
                                        }
                                    },
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonPurple,
                                        activeTrackColor = NeonPurple,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                                        disabledThumbColor = NeonPurple.copy(alpha = 0.7f),
                                        disabledActiveTrackColor = NeonPurple.copy(alpha = 0.5f),
                                        disabledInactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp)
                                        .testTag("video_timeline_slider")
                                )

                                Text(
                                    text = displayTotal,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            // Live Stream indicator
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = LiveRed.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "بث مباشر • LIVE",
                                        color = LiveRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Bottom Actions: Mute, Aspect Ratio, Fullscreen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { isMuted = !isMuted },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Mute",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val modes = VideoScaleMode.values()
                                        val next = (scaleMode.ordinal + 1) % modes.size
                                        scaleMode = modes[next]
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "Aspect Ratio",
                                        tint = NeonPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onToggleFullscreen,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
