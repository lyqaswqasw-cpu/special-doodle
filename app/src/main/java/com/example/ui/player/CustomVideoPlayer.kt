package com.example.ui.player

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.Channel
import com.example.data.model.ChannelServer
import com.example.ui.language.LocalAppStrings
import com.example.ui.theme.BrightLavender
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkPurpleContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class VideoTrackOption(
    val id: String,
    val label: String,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val isAuto: Boolean = false,
    val trackGroup: Tracks.Group? = null,
    val trackIndex: Int = -1
)

data class AudioTrackOption(
    val id: String,
    val label: String,
    val language: String?,
    val trackGroup: Tracks.Group? = null,
    val trackIndex: Int = -1
)

/**
 * Ultra-High Performance Streaming Player inspired by TOD, beIN Connect, and Netflix.
 *
 * Highlights:
 * - Smart Gesture Controls: Vertical swipe for Brightness (left) & Volume (right) with sleek HUD.
 * - Double-Tap Seek: Instant -10s / +10s with visual pulse animations.
 * - Screen Lock (قفل الشاشة): Prevents accidental touches during live matches.
 * - Live Edge Sync: Always stay synchronized to real-time action with one tap.
 * - Audio Commentary Switcher: Easily switch commentators if multi-audio is available.
 * - Quick Channel Navigation: In fullscreen, switch channels instantaneously.
 * - Deep Pure Obsidian Black Aesthetics.
 */
@OptIn(UnstableApi::class)
@Composable
fun CustomVideoPlayer(
    channel: Channel?,
    selectedServer: ChannelServer?,
    isPlaying: Boolean,
    isMuted: Boolean,
    onServerSelected: (ChannelServer) -> Unit,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    scaleMode: VideoScaleMode = VideoScaleMode.FIT_16_9,
    onToggleFullscreen: () -> Unit = {},
    onCycleScaleMode: () -> Unit = {},
    exoPlayer: ExoPlayer? = null,
    onlineViewersCount: Int? = null,
    hideOnlineViewers: Boolean = false,
    onOpenChat: () -> Unit = {},
    hideChatInPlayer: Boolean = false,
    unreadChatCount: Int = 0,
    isChatOpen: Boolean = false,
    onNextChannel: (() -> Unit)? = null,
    onPreviousChannel: (() -> Unit)? = null,
    onToggleQuickChannels: (() -> Unit)? = null,
    isQuickChannelsOpen: Boolean = false
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }

    var isBuffering by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var reloadTrigger by remember { mutableStateOf(0) }

    // Screen Lock Mode (Prevents accidental touches during live matches, TOD style)
    var isScreenLocked by remember { mutableStateOf(false) }
    var showUnlockPrompt by remember { mutableStateOf(false) }

    // Real Track & Quality state
    var currentResolutionText by remember { mutableStateOf("تلقائي") }
    var availableVideoTracks by remember { mutableStateOf<List<VideoTrackOption>>(emptyList()) }
    var selectedTrackOption by remember { mutableStateOf<VideoTrackOption?>(null) }
    var isQualityMenuExpanded by remember { mutableStateOf(false) }

    // Audio Commentary Tracks
    var availableAudioTracks by remember { mutableStateOf<List<AudioTrackOption>>(emptyList()) }
    var selectedAudioTrack by remember { mutableStateOf<AudioTrackOption?>(null) }
    var isAudioMenuExpanded by remember { mutableStateOf(false) }

    // Aspect Ratio HUD Feedback
    var scaleFeedback by remember { mutableStateOf<String?>(null) }

    // Gesture State: Brightness & Volume
    var playerWidth by remember { mutableFloatStateOf(1f) }
    var playerHeight by remember { mutableFloatStateOf(1f) }
    var brightnessLevel by remember { mutableFloatStateOf(0.7f) }
    var isAdjustingBrightness by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableFloatStateOf(0.7f) }
    var isAdjustingVolume by remember { mutableStateOf(false) }

    // Double-tap Seek Feedback
    var seekFeedbackSide by remember { mutableStateOf<String?>(null) } // "left" or "right"
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Intercept back button when in fullscreen
    if (isFullscreen) {
        BackHandler {
            if (isScreenLocked) {
                showUnlockPrompt = true
            } else {
                onToggleFullscreen()
            }
        }
    }

    // Keep screen awake while player is active
    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Handle landscape orientation and immersive system bars when fullscreen is active
    LaunchedEffect(isFullscreen) {
        activity?.setScreenLandscape(isFullscreen)
    }

    // Clean up orientation when leaving composition
    DisposableEffect(activity) {
        onDispose {
            activity?.setScreenLandscape(false)
        }
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(areControlsVisible, isPlaying, isScreenLocked) {
        if (areControlsVisible && isPlaying && !isScreenLocked) {
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

    // Auto-hide scale feedback
    LaunchedEffect(scaleFeedback) {
        if (scaleFeedback != null) {
            delay(1500)
            scaleFeedback = null
        }
    }

    // Use passed ExoPlayer or create new using PlayerFactory
    val player = exoPlayer ?: remember {
        PlayerFactory.createExoPlayer(context)
    }

    // Listener for playback state, video sizes, and audio/video tracks
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = (playbackState == Player.STATE_BUFFERING)
                if (playbackState == Player.STATE_READY) {
                    playbackError = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                val errorMsg = PlayerFactory.getReadableErrorMessage(error)
                // Anti-burn socket protection: Stop player and clear media items immediately to close socket connection on server
                player.stop()
                player.clearMediaItems()

                // Anti-burn automatic failover: switch to backup server if available
                val servers = channel?.servers ?: emptyList()
                if (servers.size > 1 && selectedServer != null) {
                    val currentIndex = servers.indexOfFirst { it.id == selectedServer.id || it.streamUrl == selectedServer.streamUrl }
                    if (currentIndex != -1 && currentIndex + 1 < servers.size) {
                        val nextServer = servers[currentIndex + 1]
                        onServerSelected(nextServer)
                        playbackError = "تم رصد ضغط على السيرفر، جاري التحويل التلقائي للسيرفر البديل (${nextServer.name})..."
                        return
                    }
                }

                playbackError = errorMsg
            }

            override fun onTracksChanged(tracks: Tracks) {
                // 1. Process Video Tracks
                val videoList = mutableListOf<VideoTrackOption>()
                videoList.add(
                    VideoTrackOption(
                        id = "auto",
                        label = "تلقائي (Auto)",
                        width = 0,
                        height = 0,
                        bitrate = 0,
                        isAuto = true
                    )
                )

                for (trackGroup in tracks.groups) {
                    if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until trackGroup.length) {
                            if (trackGroup.isTrackSupported(i)) {
                                val format = trackGroup.getTrackFormat(i)
                                val height = format.height
                                val width = format.width
                                val bitrate = format.bitrate

                                val label = if (height > 0) {
                                    val hdTag = if (height >= 1080) " (FHD)" else if (height >= 720) " (HD)" else ""
                                    "${height}p$hdTag"
                                } else if (bitrate > 0) {
                                    "${bitrate / 1000} kbps"
                                } else {
                                    "جودة ${i + 1}"
                                }

                                videoList.add(
                                    VideoTrackOption(
                                        id = "${trackGroup.mediaTrackGroup.id}_$i",
                                        label = label,
                                        width = width,
                                        height = height,
                                        bitrate = bitrate,
                                        isAuto = false,
                                        trackGroup = trackGroup,
                                        trackIndex = i
                                    )
                                )
                            }
                        }
                    }
                }
                availableVideoTracks = listOf(videoList.first()) + videoList.drop(1).sortedByDescending { it.height }

                // 2. Process Audio Tracks (Commentary / Dual Audio)
                val audioList = mutableListOf<AudioTrackOption>()
                var audioCounter = 1
                for (trackGroup in tracks.groups) {
                    if (trackGroup.type == C.TRACK_TYPE_AUDIO) {
                        for (i in 0 until trackGroup.length) {
                            if (trackGroup.isTrackSupported(i)) {
                                val format = trackGroup.getTrackFormat(i)
                                val lang = format.language ?: ""
                                val trackLabel = format.label?.takeIf { it.isNotBlank() }
                                    ?: if (lang.equals("ar", ignoreCase = true) || lang.contains("ara", ignoreCase = true)) {
                                        "صوت عربي $audioCounter"
                                    } else if (lang.equals("en", ignoreCase = true) || lang.contains("eng", ignoreCase = true)) {
                                        "صوت إنجليزي $audioCounter"
                                    } else {
                                        "قناة صوتية $audioCounter"
                                    }

                                audioList.add(
                                    AudioTrackOption(
                                        id = "${trackGroup.mediaTrackGroup.id}_$i",
                                        label = trackLabel,
                                        language = lang,
                                        trackGroup = trackGroup,
                                        trackIndex = i
                                    )
                                )
                                audioCounter++
                            }
                        }
                    }
                }
                availableAudioTracks = audioList
            }

            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                if (videoSize.height > 0) {
                    val qualityName = if (videoSize.height >= 1080) {
                        "${videoSize.height}p FHD"
                    } else if (videoSize.height >= 720) {
                        "${videoSize.height}p HD"
                    } else {
                        "${videoSize.height}p"
                    }

                    currentResolutionText = if (selectedTrackOption == null || selectedTrackOption?.isAuto == true) {
                        "تلقائي ($qualityName)"
                    } else {
                        selectedTrackOption?.label ?: qualityName
                    }
                }
            }
        }

        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    // Stop playback gracefully when app goes to background / paused
    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    player.pause()
                }
                Lifecycle.Event.ON_START -> {
                    if (isPlaying) {
                        player.play()
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    if (exoPlayer == null) {
                        player.stop()
                        player.clearMediaItems()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.pause()
            player.stop()
            player.clearMediaItems()
            if (exoPlayer == null) {
                player.release()
            }
        }
    }

    // Volume state update
    LaunchedEffect(isMuted) {
        player.volume = if (isMuted) 0f else 1f
    }

    // Play/Pause state update
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player.play()
        } else {
            player.pause()
        }
    }

    // Stream Setup & Seamless Connection
    LaunchedEffect(channel?.id, selectedServer?.id, reloadTrigger, isPlaying) {
        if (!isPlaying) {
            player.pause()
            return@LaunchedEffect
        }

        playbackError = null
        currentResolutionText = "تلقائي"
        availableVideoTracks = emptyList()
        availableAudioTracks = emptyList()
        selectedTrackOption = null
        selectedAudioTrack = null

        val streamUrl = selectedServer?.streamUrl?.trim()

        if (streamUrl.isNullOrBlank()) {
            player.stop()
            player.clearMediaItems()
            return@LaunchedEffect
        }

        // Avoid re-buffering if already playing exact stream URL
        val currentUri = player.currentMediaItem?.localConfiguration?.uri?.toString()
        if (currentUri == streamUrl && reloadTrigger == 0 &&
            (player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING)) {
            return@LaunchedEffect
        }

        player.stop()
        player.clearMediaItems()

        val streamType = selectedServer.streamType.lowercase()
        val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(streamUrl))

        when {
            streamType == "m3u8" || streamUrl.contains(".m3u8", ignoreCase = true) -> {
                mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
            }
            streamType == "ts" || streamUrl.contains(".ts", ignoreCase = true) -> {
                mediaItemBuilder.setMimeType(MimeTypes.VIDEO_MP2T)
            }
            streamType == "mp4" || streamUrl.contains(".mp4", ignoreCase = true) -> {
                mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MP4)
            }
        }

        player.setMediaItem(mediaItemBuilder.build())
        player.prepare()
        player.play()
    }

    // Apply video quality track
    fun applyQualityTrack(option: VideoTrackOption) {
        selectedTrackOption = option
        isQualityMenuExpanded = false

        if (option.isAuto || option.trackGroup == null) {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .build()
            currentResolutionText = "تلقائي"
        } else {
            val override = TrackSelectionOverride(
                option.trackGroup.mediaTrackGroup,
                listOf(option.trackIndex)
            )
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setOverrideForType(override)
                .build()
            currentResolutionText = option.label
        }
    }

    // Apply audio commentary track
    fun applyAudioTrack(option: AudioTrackOption) {
        selectedAudioTrack = option
        isAudioMenuExpanded = false

        if (option.trackGroup != null) {
            val override = TrackSelectionOverride(
                option.trackGroup.mediaTrackGroup,
                listOf(option.trackIndex)
            )
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setOverrideForType(override)
                .build()
        }
    }

    // Map VideoScaleMode to ExoPlayer resize mode
    val exoResizeMode = when (scaleMode) {
        VideoScaleMode.FIT_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        VideoScaleMode.FILL -> if (isFullscreen) AspectRatioFrameLayout.RESIZE_MODE_FILL else AspectRatioFrameLayout.RESIZE_MODE_FIT
        VideoScaleMode.ZOOM -> if (isFullscreen) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
        VideoScaleMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    }

    // Container Modifier with pure OLED black
    val containerModifier = if (isFullscreen) {
        modifier
            .background(Color.Black)
            .testTag("custom_video_player_box_fullscreen")
    } else {
        modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .border(width = 1.dp, color = DarkSurfaceBorder, shape = RoundedCornerShape(16.dp))
            .testTag("custom_video_player_box")
    }

    Box(
        modifier = containerModifier
            .onSizeChanged {
                playerWidth = it.width.toFloat().coerceAtLeast(1f)
                playerHeight = it.height.toFloat().coerceAtLeast(1f)
            }
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    areControlsVisible = true
                    when (event.key) {
                        Key.DirectionCenter, Key.Enter, Key.Spacebar -> {
                            onTogglePlay()
                            true
                        }
                        Key.F -> {
                            onToggleFullscreen()
                            true
                        }
                        Key.M -> {
                            onToggleMute()
                            true
                        }
                        Key.DirectionLeft -> {
                            val newPos = (player.currentPosition - 10_000L).coerceAtLeast(0L)
                            player.seekTo(newPos)
                            seekFeedbackSide = "left"
                            seekFeedbackText = "-10 ثانية"
                            true
                        }
                        Key.DirectionRight -> {
                            val newPos = player.currentPosition + 10_000L
                            player.seekTo(newPos)
                            seekFeedbackSide = "right"
                            seekFeedbackText = "+10 ثانية"
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .pointerInput(isScreenLocked, isPlaying) {
                // TOD-Style Gesture Handler: Dragging for Brightness & Volume
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
                            // Brightness Adjustment (Left side)
                            isAdjustingBrightness = true
                            val newB = (brightnessLevel + delta * 1.5f).coerceIn(0.05f, 1.0f)
                            brightnessLevel = newB
                            activity?.let { act ->
                                val lp = act.window.attributes
                                lp.screenBrightness = newB
                                act.window.attributes = lp
                            }
                        } else if (change.position.x > playerWidth * 0.55f) {
                            // Volume Adjustment (Right side)
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
                // Taps & Double Taps
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
                                // Double tap left: Rewind 10s
                                val newPos = (player.currentPosition - 10_000L).coerceAtLeast(0L)
                                player.seekTo(newPos)
                                seekFeedbackSide = "left"
                                seekFeedbackText = "-10 ثانية"
                            }
                            offset.x > playerWidth * 0.65f -> {
                                // Double tap right: Forward 10s
                                val newPos = player.currentPosition + 10_000L
                                player.seekTo(newPos)
                                seekFeedbackSide = "right"
                                seekFeedbackText = "+10 ثانية"
                            }
                            else -> {
                                // Double tap center: Toggle Play/Pause
                                onTogglePlay()
                            }
                        }
                    }
                )
            }
    ) {
        if (channel == null || selectedServer == null) {
            PlayerIdleView()
        } else {
            // Video View
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = exoResizeMode
                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                        setBackgroundColor(android.graphics.Color.BLACK)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    }
                },
                update = { view ->
                    if (view.player != player) {
                        view.player = player
                    }
                    view.resizeMode = exoResizeMode
                },
                onRelease = { view ->
                    view.player = null
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering Indicator
            if (isBuffering && playbackError == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NeonPurple,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "جاري الاتصال بالبث المباشر...",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Error Overlay with Fast Retry & Alternative Server Suggestion
            if (playbackError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground.copy(alpha = 0.94f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = LiveRed,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = playbackError ?: "خطأ في تشغيل البث",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ElectricPurple,
                                modifier = Modifier
                                    .clickable { reloadTrigger++ }
                                    .testTag("retry_stream_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "إعادة المحاولة",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Switch Server option if multiple servers exist
                            if (channel.servers.size > 1) {
                                val otherServer = channel.servers.firstOrNull { it.id != selectedServer.id }
                                if (otherServer != null) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = DarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                                        modifier = Modifier.clickable { onServerSelected(otherServer) }
                                    ) {
                                        Text(
                                            text = "تجربة: ${otherServer.name}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // HUD 1: Brightness Adjustment HUD (TOD Style)
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
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

            // HUD 2: Volume Adjustment HUD (TOD Style)
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
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

            // HUD 3: Double-Tap Seek Feedback Ripple
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.7f))
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

            // HUD 4: Floating Unlock Button (TOD Style: Visible only when locked and tapped)
            AnimatedVisibility(
                visible = isScreenLocked && showUnlockPrompt,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.88f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonPurple),
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

            // CONTROLS OVERLAY (TOD & beIN Stream Design)
            AnimatedVisibility(
                visible = !isScreenLocked && (areControlsVisible || !isPlaying),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.88f),
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.90f)
                                )
                            )
                        )
                ) {
                    // TOP BAR: Channel Name + Live Pulse + Screen Lock + Commentary/Audio + Quality + Scale + Close
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(
                                horizontal = if (isFullscreen) 18.dp else 12.dp,
                                vertical = if (isFullscreen) 12.dp else 8.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Side: Back Button (in Fullscreen) + Live Badge + Channel Title + Online Viewers
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                if (isFullscreen) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.65f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable { onToggleFullscreen() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "رجوع",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .size(18.dp)
                                        )
                                    }
                                }

                                LiveIndicator()

                                Text(
                                    text = channel.name,
                                    color = Color.White,
                                    fontSize = if (isFullscreen) 15.sp else 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (!hideOnlineViewers && onlineViewersCount != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Black.copy(alpha = 0.65f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.6f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(SuccessGreen, CircleShape)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = "$onlineViewersCount",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Right Side: Quick Action Pills (Lock + Audio + Quality + Scale)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                // 1. Screen Lock Button (TOD Signature)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant.copy(alpha = 0.85f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier
                                        .clickable {
                                            isScreenLocked = true
                                            areControlsVisible = false
                                            showUnlockPrompt = true
                                        }
                                        .testTag("player_lock_button")
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

                                // 2. Audio Commentary Selector (beIN Style)
                                if (availableAudioTracks.size > 1) {
                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = DarkSurfaceVariant.copy(alpha = 0.85f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BrightLavender.copy(alpha = 0.7f)),
                                            modifier = Modifier.clickable { isAudioMenuExpanded = true }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Audiotrack,
                                                    contentDescription = "التعليق الصوتي",
                                                    tint = BrightLavender,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = selectedAudioTrack?.label ?: "الصوت",
                                                    color = Color.White,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = isAudioMenuExpanded,
                                            onDismissRequest = { isAudioMenuExpanded = false },
                                            modifier = Modifier
                                                .background(DarkSurfaceVariant)
                                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                        ) {
                                            Text(
                                                text = "قنوات التعليق الصوتي",
                                                color = BrightLavender,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                            availableAudioTracks.forEach { track ->
                                                val isSelected = (selectedAudioTrack?.id == track.id)
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
                                                                text = track.label,
                                                                color = if (isSelected) SuccessGreen else Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        }
                                                    },
                                                    onClick = { applyAudioTrack(track) }
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. Real Quality Selector (Auto, 1080p, 720p)
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkPurpleContainer.copy(alpha = 0.85f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.7f)),
                                        modifier = Modifier
                                            .clickable { isQualityMenuExpanded = true }
                                            .testTag("player_quality_badge")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HighQuality,
                                                contentDescription = "الجودة",
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = currentResolutionText,
                                                color = Color.White,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = isQualityMenuExpanded,
                                        onDismissRequest = { isQualityMenuExpanded = false },
                                        modifier = Modifier
                                            .background(DarkSurfaceVariant)
                                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        Text(
                                            text = "جودات البث المتوفرة",
                                            color = NeonPurple,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )

                                        if (availableVideoTracks.isEmpty()) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "جاري الكشف...",
                                                        color = TextMuted,
                                                        fontSize = 11.sp
                                                    )
                                                },
                                                onClick = { isQualityMenuExpanded = false }
                                            )
                                        } else {
                                            availableVideoTracks.forEach { track ->
                                                val isSelected = if (track.isAuto) {
                                                    selectedTrackOption == null || selectedTrackOption?.isAuto == true
                                                } else {
                                                    selectedTrackOption?.id == track.id
                                                }

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
                                                                text = track.label,
                                                                color = if (isSelected) SuccessGreen else TextPrimary,
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        }
                                                    },
                                                    onClick = { applyQualityTrack(track) }
                                                )
                                            }
                                        }
                                    }
                                }

                                // 4. Aspect Ratio Toggle
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant.copy(alpha = 0.85f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .clickable {
                                            onCycleScaleMode()
                                            scaleFeedback = "وضع العرض: ${scaleMode.label}"
                                        }
                                        .testTag("player_aspect_ratio_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AspectRatio,
                                            contentDescription = "Scale",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = scaleMode.label,
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // 5. Fullscreen Toggle
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElectricPurple.copy(alpha = 0.9f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple),
                                    modifier = Modifier
                                        .clickable { onToggleFullscreen() }
                                        .testTag("player_fullscreen_button")
                                ) {
                                    Icon(
                                        imageVector = if (isFullscreen) Icons.Default.CloseFullscreen else Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(15.dp)
                                    )
                                }
                            }
                        }

                        // Stream Extension Badge & Servers horizontal selector
                        if (channel.servers.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "السيرفرات:",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                channel.servers.forEach { srv ->
                                    val isCurrent = (srv.id == selectedServer.id)
                                    val serverBg = if (isCurrent) VividViolet else DarkSurfaceVariant.copy(alpha = 0.85f)
                                    val serverBorder = if (isCurrent) Color.White.copy(alpha = 0.8f) else DarkSurfaceBorder

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = serverBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, serverBorder),
                                        modifier = Modifier
                                            .clickable { onServerSelected(srv) }
                                            .testTag("server_chip_${srv.name}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            if (isCurrent) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(Color.White, CircleShape)
                                                )
                                            }
                                            Text(
                                                text = srv.name,
                                                color = Color.White,
                                                fontSize = 9.5.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CENTER CONTROLS: -10s Rewind + Big Glowing Play/Pause + +10s Forward
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        // Quick 10s Rewind
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    val newPos = (player.currentPosition - 10_000L).coerceAtLeast(0L)
                                    player.seekTo(newPos)
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

                        // Big Glowing Center Play/Pause
                        Surface(
                            shape = CircleShape,
                            color = ElectricPurple,
                            border = androidx.compose.foundation.BorderStroke(2.dp, NeonPurple),
                            modifier = Modifier
                                .size(58.dp)
                                .shadow(12.dp, CircleShape, spotColor = NeonPurple)
                                .clickable { onTogglePlay() }
                                .testTag("center_play_pause_button")
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

                        // Quick 10s Forward
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    val newPos = player.currentPosition + 10_000L
                                    player.seekTo(newPos)
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

                    // BOTTOM BAR: Live Edge Jump + Channel Switchers + Mute/Reload + Chat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(
                                horizontal = if (isFullscreen) 18.dp else 12.dp,
                                vertical = if (isFullscreen) 12.dp else 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Play/Pause mini + Reload + Mute
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onTogglePlay,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { reloadTrigger++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onToggleMute,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Jump to Live Edge Button (TOD Style)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LiveRed.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LiveRed.copy(alpha = 0.6f)),
                                modifier = Modifier.clickable {
                                    player.seekToDefaultPosition()
                                    if (!isPlaying) onTogglePlay()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(LiveRed, CircleShape)
                                    )
                                    Text(
                                        text = "البث المباشر",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Right: Fullscreen Channel Switchers (TOD Style) + Chat
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Quick channel switch in fullscreen!
                            if (isFullscreen && (onPreviousChannel != null || onNextChannel != null)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    if (onPreviousChannel != null) {
                                        IconButton(
                                            onClick = onPreviousChannel,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SkipPrevious,
                                                contentDescription = "القناة السابقة",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    if (onNextChannel != null) {
                                        IconButton(
                                            onClick = onNextChannel,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SkipNext,
                                                contentDescription = "القناة التالية",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Channels Split Screen Button
                            if (onToggleQuickChannels != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isQuickChannelsOpen) VividViolet.copy(alpha = 0.9f) else DarkSurfaceVariant.copy(alpha = 0.85f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isQuickChannelsOpen) BrightLavender else NeonPurple.copy(alpha = 0.8f)),
                                    modifier = Modifier
                                        .clickable { onToggleQuickChannels() }
                                        .testTag("player_quick_channels_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LiveTv,
                                            contentDescription = "قائمة القنوات (انقسام الشاشة)",
                                            tint = if (isQuickChannelsOpen) Color.White else BrightLavender,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "القنوات",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Chat Overlay Button
                            if (!hideChatInPlayer) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChatOpen) VividViolet.copy(alpha = 0.9f) else DarkSurfaceVariant.copy(alpha = 0.85f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isChatOpen) BrightLavender else NeonPurple.copy(alpha = 0.8f)),
                                    modifier = Modifier
                                        .clickable { onOpenChat() }
                                        .testTag("player_chat_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubble,
                                            contentDescription = strings.chat,
                                            tint = if (isChatOpen) Color.White else BrightLavender,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = strings.chat,
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        // Intelligent Unread Messages Badge (+99 capped)
                                        if (unreadChatCount > 0 && !isChatOpen) {
                                            val badgeText = if (unreadChatCount > 99) "+99" else "$unreadChatCount"
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = LiveRed,
                                                modifier = Modifier.padding(start = 2.dp)
                                            ) {
                                                Text(
                                                    text = badgeText,
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = LiveRed.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, LiveRed.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .scale(alpha)
                    .background(LiveRed, CircleShape)
            )
            Text(
                text = "مباشر",
                color = LiveRed,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlayerIdleView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(DarkSurfaceVariant, CircleShape)
                .border(1.5.dp, NeonPurple.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "اختر أي قناة لبدء البث المباشر",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "سيرفرات فائقة السرعة وجودة متكيفة مثل مشغل TOD",
                color = BrightLavender,
                fontSize = 10.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
