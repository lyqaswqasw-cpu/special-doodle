package com.example.ui.iptv

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.XtremeItem
import com.example.ui.player.PlayerFactory
import com.example.ui.player.VideoScaleMode
import com.example.ui.player.findActivity
import com.example.ui.player.setScreenLandscape
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BrightLavender
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.VividViolet
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay

/**
 * Dedicated, completely isolated IPTV Video Player:
 * - Does NOT show online viewers count (لا يظهر عدد المتصلين).
 * - Does NOT show chat (لا يظهر الدردشة).
 * - Does NOT save viewed channels to database or Home screen.
 * - Supports seamless aspect ratio scaling, volume & brightness swipe gestures,
 *   screen lock mode, stream format indicator, and high-performance playback.
 */
@OptIn(UnstableApi::class)
@Composable
fun IptvVideoPlayer(
    item: XtremeItem,
    availableChannels: List<XtremeItem> = emptyList(),
    onSelectChannel: (XtremeItem) -> Unit = {},
    onClosePlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isQuickChannelsOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Force Screen Landscape directly when IPTV player opens
    LaunchedEffect(Unit) {
        activity?.setScreenLandscape(true)
    }

    // Intercept back button to close split channels first, or exit player cleanly
    BackHandler {
        if (isQuickChannelsOpen) {
            isQuickChannelsOpen = false
        } else {
            activity?.setScreenLandscape(false)
            onClosePlayer()
        }
    }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }

    var isBuffering by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var reloadTrigger by remember { mutableStateOf(0) }
    var scaleMode by remember { mutableStateOf(VideoScaleMode.FIT_16_9) }
    var scaleFeedback by remember { mutableStateOf<String?>(null) }
    var isScreenLocked by remember { mutableStateOf(false) }

    // Gesture State: Brightness & Volume
    var playerWidth by remember { mutableFloatStateOf(1f) }
    var playerHeight by remember { mutableFloatStateOf(1f) }
    var brightnessLevel by remember { mutableFloatStateOf(0.7f) }
    var isAdjustingBrightness by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableFloatStateOf(0.5f) }
    var isAdjustingVolume by remember { mutableStateOf(false) }

    // Pulsing animation for live badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_iptv_live")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlphaIptvLive"
    )

    // Dedicated independent ExoPlayer for IPTV playback
    val iptvPlayer = remember {
        PlayerFactory.createExoPlayer(context)
    }

    // Auto-hide controls timer
    LaunchedEffect(areControlsVisible, isPlaying, isScreenLocked) {
        if (areControlsVisible && isPlaying && !isScreenLocked) {
            delay(4500)
            areControlsVisible = false
        }
    }

    // Set screen flags: keep screen on & full brightness in fullscreen
    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.setScreenLandscape(false)
        }
    }

    // Prepare and play the live stream
    LaunchedEffect(item.streamUrl, reloadTrigger) {
        val url = item.streamUrl.trim()
        if (url.isNotBlank()) {
            isBuffering = true
            playbackError = null

            val ext = item.containerExtension.lowercase()
            val mediaItem = when {
                ext == "m3u8" || url.contains(".m3u8", ignoreCase = true) -> {
                    MediaItem.Builder()
                        .setUri(url)
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .setLiveConfiguration(
                            MediaItem.LiveConfiguration.Builder()
                                .setMaxPlaybackSpeed(1.02f)
                                .setMinPlaybackSpeed(0.98f)
                                .build()
                        )
                        .build()
                }
                ext == "mpd" || url.contains(".mpd", ignoreCase = true) -> {
                    MediaItem.Builder()
                        .setUri(url)
                        .setMimeType(MimeTypes.APPLICATION_MPD)
                        .build()
                }
                else -> {
                    MediaItem.fromUri(url)
                }
            }

            iptvPlayer.setMediaItem(mediaItem)
            iptvPlayer.prepare()
            iptvPlayer.playWhenReady = true
        } else {
            playbackError = "رابط البث غير متوفر"
        }
    }

    // Player events listener
    DisposableEffect(iptvPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    playbackError = null
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                iptvPlayer.stop()
                iptvPlayer.clearMediaItems()
                playbackError = "تعذر تشغيل قناة البث المباشر (خطأ في الخادم أو انقطاع البث)"
            }
        }
        iptvPlayer.addListener(listener)

        onDispose {
            iptvPlayer.removeListener(listener)
            iptvPlayer.stop()
            iptvPlayer.clearMediaItems()
            iptvPlayer.release()
        }
    }

    // Mute control
    LaunchedEffect(isMuted) {
        iptvPlayer.volume = if (isMuted) 0f else 1f
    }

    // Manage activity lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> iptvPlayer.pause()
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying) iptvPlayer.play()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = (if (isQuickChannelsOpen) Modifier.weight(0.55f).fillMaxHeight() else Modifier.fillMaxSize())
                .background(Color.Black)
                .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        areControlsVisible = !areControlsVisible
                    }
                )
            }
            .pointerInput(isScreenLocked) {
                if (!isScreenLocked) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val isLeftSide = offset.x < playerWidth / 2f
                            if (isLeftSide) {
                                isAdjustingBrightness = true
                            } else {
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
                            change.consume()
                            val deltaY = -dragAmount.y / playerHeight
                            if (isAdjustingBrightness) {
                                brightnessLevel = (brightnessLevel + deltaY * 1.5f).coerceIn(0.1f, 1f)
                                activity?.let { act ->
                                    val lp = act.window.attributes
                                    lp.screenBrightness = brightnessLevel
                                    act.window.attributes = lp
                                }
                            } else if (isAdjustingVolume) {
                                volumeLevel = (volumeLevel + deltaY * 1.5f).coerceIn(0f, 1f)
                                audioManager?.let { am ->
                                    val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val target = (volumeLevel * max).toInt()
                                    am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                                }
                            }
                        }
                    )
                }
            }
    ) {
        // 1. VIDEO SURFACE
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = iptvPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = when (scaleMode) {
                        VideoScaleMode.FIT_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        VideoScaleMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        VideoScaleMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        VideoScaleMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                }
            },
            update = { playerView ->
                playerView.player = iptvPlayer
                playerView.resizeMode = when (scaleMode) {
                    VideoScaleMode.FIT_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    VideoScaleMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    VideoScaleMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    VideoScaleMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. BUFFERING SPINNER
        if (isBuffering && playbackError == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = "جاري الاتصال بالبث المباشر...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. ERROR OVERLAY WITH RETRY
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = LiveRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = playbackError ?: "خطأ في تشغيل القناة",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(
                            onClick = { reloadTrigger++ },
                            modifier = Modifier
                                .background(NeonPurple, CircleShape)
                                .size(42.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "إعادة المحاولة", tint = Color.White)
                        }
                        IconButton(
                            onClick = onClosePlayer,
                            modifier = Modifier
                                .background(Color(0xFF222234), CircleShape)
                                .size(42.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                        }
                    }
                }
            }
        }

        // 4. GESTURE HUD (Brightness / Volume)
        if (isAdjustingBrightness) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Text(text = "${(brightnessLevel * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isAdjustingVolume) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(20.dp))
                    Text(text = "${(volumeLevel * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. SCALE FEEDBACK HUD
        scaleFeedback?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 6. OVERLAY CONTROLS (Top Bar & Bottom Controls)
        androidx.compose.animation.AnimatedVisibility(
            visible = areControlsVisible && !isScreenLocked,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Gradient & Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Arrow + Channel Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { onClosePlayer() }
                                    .testTag("iptv_player_back_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "رجوع",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Channel Icon (if present)
                            if (item.streamIcon.isNotBlank()) {
                                SubcomposeAsyncImage(
                                    model = item.streamIcon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Column {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Live Badge
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = LiveRed.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, LiveRed)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(LiveRed.copy(alpha = pulseAlpha), CircleShape)
                                            )
                                            Text(
                                                text = "بث مباشر",
                                                color = LiveRed,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (item.categoryName.isNotBlank()) {
                                        Text(
                                            text = item.categoryName,
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (item.containerExtension.isNotBlank()) {
                                        Text(
                                            text = "• ${item.containerExtension.uppercase()}",
                                            color = AccentCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Right actions: Lock screen, Scale mode, Mute
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Quick Channels Split Screen Switcher Button (انقسام الشاشة وقائمة القنوات)
                            if (availableChannels.isNotEmpty()) {
                                IconButton(
                                    onClick = { isQuickChannelsOpen = !isQuickChannelsOpen },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isQuickChannelsOpen) VividViolet else Color.White.copy(alpha = 0.12f), CircleShape)
                                        .testTag("iptv_player_quick_channels_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = "قائمة القنوات (انقسام الشاشة)",
                                        tint = if (isQuickChannelsOpen) Color.White else BrightLavender,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Lock Screen Button
                            IconButton(
                                onClick = {
                                    isScreenLocked = true
                                    areControlsVisible = false
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = "قفل الشاشة", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            // Scale Mode Button
                            IconButton(
                                onClick = {
                                    val modes = VideoScaleMode.values()
                                    val nextIndex = (scaleMode.ordinal + 1) % modes.size
                                    scaleMode = modes[nextIndex]
                                    scaleFeedback = when (scaleMode) {
                                        VideoScaleMode.FIT_16_9 -> "تناسب أصلي (16:9)"
                                        VideoScaleMode.FILL -> "ملء الشاشة (Fill)"
                                        VideoScaleMode.ZOOM -> "تكبير بدون حواف (Zoom)"
                                        VideoScaleMode.FIT_SCREEN -> "احتواء كامل الشاشة (Fit)"
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(Icons.Default.AspectRatio, contentDescription = "تغيير الأبعاد", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            // Mute / Unmute Button
                            IconButton(
                                onClick = { isMuted = !isMuted },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "كتم الصوت",
                                    tint = if (isMuted) LiveRed else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Center Play / Pause
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                iptvPlayer.pause()
                            } else {
                                iptvPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // 7. SCREEN LOCK BUTTON (Only button visible when locked)
        if (isScreenLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = {
                        isScreenLocked = false
                        areControlsVisible = true
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "إلغاء قفل الشاشة", tint = AccentCyan, modifier = Modifier.size(22.dp))
                }
            }
        }
        }

        // Split-Screen Channels Switcher Pane (انقسام نصف الشاشة لقنوات IPTV)
        AnimatedVisibility(
            visible = isQuickChannelsOpen,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
        ) {
            com.example.ui.components.IptvChannelSplitPane(
                currentItem = item,
                channels = availableChannels,
                onSelectChannel = { newCh ->
                    onSelectChannel(newCh)
                },
                onClose = { isQuickChannelsOpen = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
