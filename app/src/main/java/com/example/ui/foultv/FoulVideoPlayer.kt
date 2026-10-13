package com.example.ui.foultv

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.ChatUser
import com.example.data.model.FoulComment
import com.example.data.model.FoulTvGlobalConfig
import com.example.data.model.FoulVideo
import com.example.ui.player.VideoScaleMode
import com.example.ui.player.findActivity
import com.example.ui.player.setScreenLandscape
import com.example.ui.theme.AccentCyan
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoulVideoPlayer(
    video: FoulVideo,
    currentUser: ChatUser?,
    comments: List<FoulComment>,
    globalConfig: FoulTvGlobalConfig,
    onClosePlayer: () -> Unit,
    onToggleLike: (String) -> Unit,
    onAddComment: (FoulComment) -> Unit,
    onDeleteComment: (String, String) -> Unit,
    onIncrementViews: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val coroutineScope = rememberCoroutineScope()

    var isBuffering by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isScreenLocked by remember { mutableStateOf(false) }
    var showUnlockPrompt by remember { mutableStateOf(false) }

    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var userSeekFraction by remember { mutableFloatStateOf(0f) }

    var scaleMode by remember { mutableStateOf(VideoScaleMode.FIT_16_9) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isSpeedMenuExpanded by remember { mutableStateOf(false) }

    // Gesture State: Brightness & Volume
    var playerWidth by remember { mutableFloatStateOf(1f) }
    var playerHeight by remember { mutableFloatStateOf(1f) }
    var brightnessLevel by remember { mutableFloatStateOf(0.7f) }
    var isAdjustingBrightness by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableFloatStateOf(0.7f) }
    var isAdjustingVolume by remember { mutableStateOf(false) }

    var seekFeedbackText by remember { mutableStateOf<String?>(null) }
    var isCommentsSheetOpen by remember { mutableStateOf(false) }
    var isDetailsPaneOpen by remember { mutableStateOf(false) }

    // Increment views in Firebase when video starts
    LaunchedEffect(video.id) {
        onIncrementViews(video.id)
    }

    // Force Screen Landscape and Fullscreen immediately on mount
    LaunchedEffect(Unit) {
        activity?.setScreenLandscape(true)
    }

    // Back handler smoothly returns to Portrait FOUL TV screen
    BackHandler {
        if (isScreenLocked) {
            showUnlockPrompt = true
        } else if (isCommentsSheetOpen) {
            isCommentsSheetOpen = false
        } else {
            activity?.setScreenLandscape(false)
            onClosePlayer()
        }
    }

    // Clean up screen orientation and wakelock when leaving
    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.setScreenLandscape(false)
        }
    }

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    // Connect lifecycle to ExoPlayer
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                    isPlaying = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.playWhenReady = true
                    isPlaying = true
                }
                else -> Unit
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

    // Load media item into ExoPlayer
    LaunchedEffect(video.videoUrl) {
        if (video.videoUrl.isBlank()) {
            playbackError = "رابط الفيديو غير صالح أو فارغ"
            return@LaunchedEffect
        }
        playbackError = null
        isBuffering = true
        try {
            val uri = Uri.parse(video.videoUrl.trim())
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .apply {
                    if (video.videoUrl.contains(".m3u8", ignoreCase = true)) {
                        setMimeType(MimeTypes.APPLICATION_M3U8)
                    } else if (video.videoUrl.contains(".mpd", ignoreCase = true)) {
                        setMimeType(MimeTypes.APPLICATION_MPD)
                    }
                }
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            isPlaying = true
        } catch (e: Exception) {
            playbackError = "تعذر تشغيل الفيديو: ${e.localizedMessage ?: "تأكد من صحة الرابط"}"
            isBuffering = false
        }
    }

    // Track Player state
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = (state == Player.STATE_BUFFERING)
                if (state == Player.STATE_READY) {
                    playbackError = null
                    totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                playbackError = "خطأ في تشغيل الفيديو (${error.errorCodeName})"
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Polling player position for smooth seekbar updates
    LaunchedEffect(isPlaying, isUserSeeking) {
        while (isPlaying && !isUserSeeking) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    // Auto-hide controls
    LaunchedEffect(areControlsVisible, isPlaying, isScreenLocked) {
        if (areControlsVisible && isPlaying && !isScreenLocked) {
            delay(4500)
            areControlsVisible = false
        }
    }

    // Auto-hide HUDs
    LaunchedEffect(isAdjustingBrightness) {
        if (isAdjustingBrightness) {
            delay(1400)
            isAdjustingBrightness = false
        }
    }
    LaunchedEffect(isAdjustingVolume) {
        if (isAdjustingVolume) {
            delay(1400)
            isAdjustingVolume = false
        }
    }
    LaunchedEffect(seekFeedbackText) {
        if (seekFeedbackText != null) {
            delay(800)
            seekFeedbackText = null
        }
    }

    val isLiked = remember(video.likedUsernames, currentUser) {
        val username = currentUser?.username ?: ""
        username.isNotBlank() && video.likedUsernames.contains(username)
    }

    val isViewsHidden = video.isViewsHidden || globalConfig.hideViewsAll
    val isLikesDisabled = video.isLikesDisabled || globalConfig.hideLikesAll
    val isCommentsDisabled = video.isCommentsDisabled || globalConfig.disableCommentsAll

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged {
                playerWidth = it.width.toFloat().coerceAtLeast(1f)
                playerHeight = it.height.toFloat().coerceAtLeast(1f)
            }
    ) {
        // AndroidView ExoPlayer Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = exoPlayer
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
                playerView.player = exoPlayer
                playerView.resizeMode = when (scaleMode) {
                    VideoScaleMode.FIT_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    VideoScaleMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    VideoScaleMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    VideoScaleMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture Overlay (Taps, Double Taps, Brightness / Volume vertical drags)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isScreenLocked) {
                    if (isScreenLocked) {
                        detectTapGestures(
                            onTap = {
                                showUnlockPrompt = true
                            }
                        )
                    } else {
                        detectTapGestures(
                            onTap = {
                                areControlsVisible = !areControlsVisible
                            },
                            onDoubleTap = { offset ->
                                val isRightSide = offset.x > playerWidth / 2f
                                if (isRightSide) {
                                    val newPos = (exoPlayer.currentPosition + 10_000).coerceAtMost(exoPlayer.duration)
                                    exoPlayer.seekTo(newPos)
                                    currentPositionMs = newPos
                                    seekFeedbackText = "+10 ثوانٍ ⏩"
                                } else {
                                    val newPos = (exoPlayer.currentPosition - 10_000).coerceAtLeast(0L)
                                    exoPlayer.seekTo(newPos)
                                    currentPositionMs = newPos
                                    seekFeedbackText = "⏪ -10 ثوانٍ"
                                }
                            }
                        )
                    }
                }
                .pointerInput(isScreenLocked) {
                    if (!isScreenLocked) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val isLeftSide = offset.x < playerWidth / 2f
                                if (isLeftSide) isAdjustingBrightness = true else isAdjustingVolume = true
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
                                val isLeftSide = change.position.x < playerWidth / 2f
                                val delta = -dragAmount.y / playerHeight

                                if (isLeftSide) {
                                    isAdjustingBrightness = true
                                    brightnessLevel = (brightnessLevel + delta * 1.5f).coerceIn(0.05f, 1.0f)
                                    activity?.let { act ->
                                        val lp = act.window.attributes
                                        lp.screenBrightness = brightnessLevel
                                        act.window.attributes = lp
                                    }
                                } else {
                                    isAdjustingVolume = true
                                    volumeLevel = (volumeLevel + delta * 1.5f).coerceIn(0f, 1.0f)
                                    audioManager?.let { am ->
                                        val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                        val targetVol = (volumeLevel * maxVol).toInt()
                                        am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                                    }
                                }
                            }
                        )
                    }
                }
        )

        // Buffering Spinner
        if (isBuffering && playbackError == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = NeonPurple,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // Playback Error Overlay
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = LiveRed,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = playbackError ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            playbackError = null
                            isBuffering = true
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة المحاولة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Gesture Feedback HUD: Brightness & Volume
        AnimatedVisibility(
            visible = isAdjustingBrightness || isAdjustingVolume,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isAdjustingBrightness) Icons.Default.BrightnessMedium else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = if (isAdjustingBrightness) "السطوع" else "مستوى الصوت",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${((if (isAdjustingBrightness) brightnessLevel else volumeLevel) * 100).toInt()}%",
                            color = BrightLavender,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Double-Tap Seek Feedback HUD
        AnimatedVisibility(
            visible = seekFeedbackText != null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = seekFeedbackText ?: "",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }

        // CONTROLS OVERLAY
        AnimatedVisibility(
            visible = areControlsVisible && !isScreenLocked,
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
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            ) {
                // TOP BAR: Back button + Title + Category + Quality / Scale / Lock / Close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                activity?.setScreenLandscape(false)
                                onClosePlayer()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NeonPurple.copy(alpha = 0.3f),
                                    border = BorderStroke(0.8.dp, NeonPurple)
                                ) {
                                    Text(
                                        text = "FOUL TV",
                                        color = BrightLavender,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = video.category,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Top Right Controls (Speed, Aspect Ratio, Lock)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Playback Speed
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.clickable { isSpeedMenuExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(14.dp))
                                    Text(text = "${playbackSpeed}x", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            DropdownMenu(
                                expanded = isSpeedMenuExpanded,
                                onDismissRequest = { isSpeedMenuExpanded = false }
                            ) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = { Text("${speed}x ${if (speed == 1.0f) "(عادي)" else ""}") },
                                        onClick = {
                                            playbackSpeed = speed
                                            exoPlayer.setPlaybackSpeed(speed)
                                            isSpeedMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Aspect Ratio Switcher
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.clickable {
                                val modes = VideoScaleMode.values()
                                val nextIndex = (scaleMode.ordinal + 1) % modes.size
                                scaleMode = modes[nextIndex]
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AspectRatio, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(14.dp))
                                Text(text = scaleMode.label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Screen Lock Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { isScreenLocked = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = "قفل الشاشة", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // CENTER PLAY / PAUSE / SEEK 10s CONTROLS
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10_000).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Replay10, contentDescription = "-10 ثوانٍ", tint = Color.White, modifier = Modifier.size(24.dp))
                    }

                    Surface(
                        shape = CircleShape,
                        color = ElectricPurple,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isPlaying) {
                                    exoPlayer.pause()
                                    isPlaying = false
                                } else {
                                    exoPlayer.play()
                                    isPlaying = true
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + 10_000).coerceAtMost(exoPlayer.duration)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Forward10, contentDescription = "+10 ثوانٍ", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }

                // BOTTOM CONTROLS: Seek bar + Actions (Like, Comments, Mute, Description)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Time Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(if (isUserSeeking) (userSeekFraction * totalDurationMs).toLong() else currentPositionMs),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Slider(
                            value = if (totalDurationMs > 0) {
                                if (isUserSeeking) userSeekFraction else (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                            } else 0f,
                            onValueChange = { fraction ->
                                isUserSeeking = true
                                userSeekFraction = fraction
                            },
                            onValueChangeFinished = {
                                if (totalDurationMs > 0) {
                                    val targetMs = (userSeekFraction * totalDurationMs).toLong()
                                    exoPlayer.seekTo(targetMs)
                                    currentPositionMs = targetMs
                                }
                                isUserSeeking = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = VividViolet,
                                activeTrackColor = ElectricPurple,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        )

                        Text(
                            text = if (totalDurationMs > 0) formatDuration(totalDurationMs) else video.duration.ifBlank { "--:--" },
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Action Icons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Actions: Mute + Description expander
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    exoPlayer.volume = if (isMuted) 0f else 1f
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "كتم الصوت",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (video.description.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { isDetailsPaneOpen = !isDetailsPaneOpen }
                                ) {
                                    Text(
                                        text = if (isDetailsPaneOpen) "إخفاء الوصف" else "عرض الوصف",
                                        color = BrightLavender,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Right Actions: Views Counter (if not hidden), Like Button, Comments Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Views count badge (if visible)
                            if (!isViewsHidden) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                                        Text(text = "${video.viewsCount} مشاهدة", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            // Like Button (if enabled)
                            if (!isLikesDisabled) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isLiked) LiveRed.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
                                    border = if (isLiked) BorderStroke(1.dp, LiveRed) else null,
                                    modifier = Modifier.clickable { onToggleLike(video.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "إعجاب",
                                            tint = if (isLiked) LiveRed else Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "${video.likesCount}",
                                            color = if (isLiked) LiveRed else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Comments Button (if enabled)
                            if (!isCommentsDisabled) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { isCommentsSheetOpen = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Comment,
                                            contentDescription = "تعليقات",
                                            tint = BrightLavender,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "التعليقات (${comments.size})",
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Screen Lock Overlay (Unlock button when locked)
        if (isScreenLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, NeonPurple),
                    modifier = Modifier.clickable {
                        isScreenLocked = false
                        areControlsVisible = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                        Text("إلغاء قفل الشاشة", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Expandable Description Details Sheet/Pane
        AnimatedVisibility(
            visible = isDetailsPaneOpen && video.description.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(0.5f)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "تفاصيل ووصف الفيديو", color = BrightLavender, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { isDetailsPaneOpen = false }, modifier = Modifier.size(20.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = video.description, color = Color.White, fontSize = 11.5.sp, lineHeight = 16.sp)
                }
            }
        }

        // YouTube-style Comments Bottom Sheet
        if (isCommentsSheetOpen) {
            FoulCommentsSheet(
                videoId = video.id,
                comments = comments,
                currentUser = currentUser,
                onDismiss = { isCommentsSheetOpen = false },
                onAddComment = onAddComment,
                onDeleteComment = onDeleteComment
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoulCommentsSheet(
    videoId: String,
    comments: List<FoulComment>,
    currentUser: ChatUser?,
    onDismiss: () -> Unit,
    onAddComment: (FoulComment) -> Unit,
    onDeleteComment: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newCommentText by remember { mutableStateOf("") }
    val authorName = remember(currentUser) { currentUser?.fullName ?: "متابع FOUL TV" }
    val authorUsername = remember(currentUser) { currentUser?.username ?: "guest_${System.currentTimeMillis() % 1000}" }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Comment, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(20.dp))
                    Text(text = "التعليقات (${comments.size})", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Comment, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                        Text(text = "لا توجد تعليقات حتى الآن", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = "كن أول من يشارك برأيه حول هذا الفيديو!", color = TextMuted, fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        val isMyComment = comment.authorUsername == authorUsername || comment.authorName == authorName
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurface,
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Avatar circle
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(DarkPurpleContainer, CircleShape)
                                        .border(1.dp, NeonPurple.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comment.authorName.take(1).uppercase(),
                                        color = BrightLavender,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = comment.authorName,
                                                color = Color.White,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = formatTimeAgo(comment.createdAt),
                                                color = TextMuted,
                                                fontSize = 9.5.sp
                                            )
                                        }

                                        if (isMyComment) {
                                            IconButton(
                                                onClick = { onDeleteComment(videoId, comment.id) },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف تعليقي",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    val cleanCommentText = remember(comment.text) {
                                        com.example.data.security.AppSecurityGuard.sanitizeComment(comment.text)
                                    }
                                    Text(
                                        text = cleanCommentText,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add Comment Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("أضف تعليقاً باسم $authorName...", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            val comment = FoulComment(
                                videoId = videoId,
                                authorName = authorName,
                                authorUsername = authorUsername,
                                text = newCommentText.trim()
                            )
                            onAddComment(comment)
                            newCommentText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(ElectricPurple, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

private fun formatTimeAgo(timeMillis: Long): String {
    val diff = (System.currentTimeMillis() - timeMillis).coerceAtLeast(0L)
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "منذ $days يوم"
        hours > 0 -> "منذ $hours ساعة"
        minutes > 0 -> "منذ $minutes دقيقة"
        else -> "الآن"
    }
}
