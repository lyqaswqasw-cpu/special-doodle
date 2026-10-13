package com.example.ui.foultv

import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.ChatUser
import com.example.data.model.FoulComment
import com.example.data.model.FoulTvGlobalConfig
import com.example.data.model.FoulVideo
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import kotlinx.coroutines.delay
import java.util.Locale

enum class FoulSortOption(val title: String) {
    NEWEST("الأحدث أولاً"),
    MOST_VIEWED("الأكثر مشاهدة 🔥"),
    MOST_LIKED("الأكثر إعجاباً ❤️"),
    MOST_COMMENTED("الأكثر تفاعلاً بالتعليقات 💬")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoulTvScreen(
    videos: List<FoulVideo>,
    playingVideo: FoulVideo? = null,
    currentUser: ChatUser? = null,
    comments: List<FoulComment> = emptyList(),
    globalConfig: FoulTvGlobalConfig = FoulTvGlobalConfig(),
    onPlayVideo: (FoulVideo) -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    onStopVideo: () -> Unit = {},
    onToggleLike: (String) -> Unit = {},
    onIncrementViews: (String) -> Unit = {},
    onAddComment: (FoulComment) -> Unit = {},
    onDeleteComment: (String, String) -> Unit = { _, _ -> },
    onOpenPublishVideoDialog: (FoulVideo?) -> Unit = {},
    onOpenDeveloperAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf(FoulSortOption.NEWEST) }
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    // State for comments sheet before entering full horizontal screen
    var isCommentsSheetOpen by remember { mutableStateOf(false) }
    var activeCommentVideoId by remember { mutableStateOf<String?>(null) }

    // Intercept back button when comments sheet is open
    BackHandler(enabled = isCommentsSheetOpen) {
        isCommentsSheetOpen = false
    }

    // Smart Filtering & Sorting Engine (All videos directly in feed)
    val filteredVideos = remember(videos, searchQuery, selectedSortOption) {
        var list = videos

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { v ->
                v.title.lowercase().contains(q) ||
                v.description.lowercase().contains(q) ||
                v.category.lowercase().contains(q) ||
                v.authorName.lowercase().contains(q) ||
                v.tags.any { it.lowercase().contains(q) }
            }
        }

        when (selectedSortOption) {
            FoulSortOption.NEWEST -> list.sortedByDescending { it.createdAt }
            FoulSortOption.MOST_VIEWED -> list.sortedByDescending { it.viewsCount }
            FoulSortOption.MOST_LIKED -> list.sortedByDescending { it.likesCount }
            FoulSortOption.MOST_COMMENTED -> list.sortedByDescending { it.commentsCount }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. STICKY TOP INLINE PLAYER (Plays video at the top with full controls, seek bar & Fullscreen button)
        if (playingVideo != null) {
            FoulInlinePlayer(
                video = playingVideo,
                currentUser = currentUser,
                comments = comments,
                globalConfig = globalConfig,
                onToggleFullscreen = onToggleFullscreen,
                onClosePlayer = onStopVideo,
                onToggleLike = onToggleLike,
                onOpenComments = {
                    activeCommentVideoId = playingVideo.id
                    isCommentsSheetOpen = true
                },
                onIncrementViews = onIncrementViews,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            // 1.1 YOUTUBE-STYLE COMMENTS STRIP (Directly below the inline player)
            if (!globalConfig.disableCommentsAll && !playingVideo.isCommentsDisabled) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurface,
                    border = BorderStroke(0.8.dp, DarkSurfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            activeCommentVideoId = playingVideo.id
                            isCommentsSheetOpen = true
                        }
                        .testTag("foul_comments_preview_strip")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(DarkPurpleContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Comment,
                                    contentDescription = null,
                                    tint = BrightLavender,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "التعليقات",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "(${comments.size.coerceAtLeast(playingVideo.commentsCount.toInt())})",
                                        color = BrightLavender,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (comments.isNotEmpty()) comments.last().text else "انقر للمشاهدة وإضافة تعليقك قبل ملء الشاشة...",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElectricPurple.copy(alpha = 0.2f),
                            border = BorderStroke(0.8.dp, ElectricPurple)
                        ) {
                            Text(
                                text = "فتح التعليقات 💬",
                                color = BrightLavender,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. CLEAN TOP COMPACT ACTION BAR
        Surface(
            color = DarkSurface,
            border = BorderStroke(0.5.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!isSearchExpanded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElectricPurple,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "الفيديوهات واللقطات (${filteredVideos.size})",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ابحث في الفيديوهات...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("foul_tv_search_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Action Icons (Search Toggle, Filter)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSearchExpanded) ElectricPurple else DarkSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSearchExpanded) BrightLavender else DarkSurfaceBorder),
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { isSearchExpanded = !isSearchExpanded }
                                .testTag("foul_tv_search_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = if (isSearchExpanded) Color.White else BrightLavender,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Smart Filter Button
                        Surface(
                            shape = CircleShape,
                            color = if (selectedSortOption != FoulSortOption.NEWEST) VividViolet else DarkSurfaceVariant,
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { isFilterSheetOpen = true }
                                .testTag("foul_tv_filter_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "ترتيب وتصفية",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. VIDEOS VERTICAL FEED
        if (filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(DarkPurpleContainer, CircleShape)
                            .border(1.2.dp, VividViolet.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = BrightLavender,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = if (searchQuery.isNotBlank()) "لم يتم العثور على فيديوهات مطابقة للبحث" else "لا توجد فيديوهات في FOUL TV حالياً",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("foul_tv_videos_feed"),
                contentPadding = PaddingValues(top = 2.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredVideos, key = { it.id }) { video ->
                    FoulVideoCard(
                        video = video,
                        globalConfig = globalConfig,
                        currentUser = currentUser,
                        isPlaying = playingVideo?.id == video.id,
                        onClick = { onPlayVideo(video) },
                        onToggleLike = { onToggleLike(video.id) },
                        onOpenComments = {
                            activeCommentVideoId = video.id
                            if (playingVideo?.id != video.id) {
                                onPlayVideo(video)
                            }
                            isCommentsSheetOpen = true
                        }
                    )
                }
            }
        }
    }

    // 4. COMMENTS BOTTOM SHEET (Opens in portrait mode before going fullscreen)
    if (isCommentsSheetOpen) {
        val targetVideo = remember(activeCommentVideoId, playingVideo, videos) {
            videos.find { it.id == activeCommentVideoId } ?: playingVideo
        }

        if (targetVideo != null) {
            FoulCommentsSheet(
                videoId = targetVideo.id,
                comments = comments,
                currentUser = currentUser,
                onDismiss = { isCommentsSheetOpen = false },
                onAddComment = onAddComment,
                onDeleteComment = onDeleteComment
            )
        }
    }

    // 5. SMART FILTER MODAL BOTTOM SHEET
    if (isFilterSheetOpen) {
        FoulFilterBottomSheet(
            currentSort = selectedSortOption,
            onSelectSort = { selectedSortOption = it },
            onDismiss = { isFilterSheetOpen = false }
        )
    }
}

/**
 * Sticky Top Inline Player with Video Controls, Custom Seek Bar, Comments Button, and Fullscreen button
 */
@OptIn(UnstableApi::class)
@Composable
fun FoulInlinePlayer(
    video: FoulVideo,
    currentUser: ChatUser?,
    comments: List<FoulComment> = emptyList(),
    globalConfig: FoulTvGlobalConfig,
    onToggleFullscreen: () -> Unit,
    onClosePlayer: () -> Unit,
    onToggleLike: (String) -> Unit,
    onOpenComments: () -> Unit,
    onIncrementViews: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isBuffering by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }

    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var userSeekFraction by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(video.id) {
        onIncrementViews(video.id)
    }

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

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

    LaunchedEffect(video.videoUrl) {
        if (video.videoUrl.isBlank()) {
            playbackError = "رابط الفيديو غير صالح"
            return@LaunchedEffect
        }
        playbackError = null
        isBuffering = true
        try {
            val uri = Uri.parse(video.videoUrl.trim())
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .apply {
                    if (video.videoUrl.contains(".m3u8", ignoreCase = true) || video.videoUrl.contains(".m3u", ignoreCase = true)) {
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
            playbackError = "خطأ في تشغيل الفيديو"
            isBuffering = false
        }
    }

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
                playbackError = "تعذر تشغيل البث"
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

    LaunchedEffect(isPlaying, isUserSeeking) {
        while (isPlaying && !isUserSeeking) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    LaunchedEffect(areControlsVisible, isPlaying) {
        if (areControlsVisible && isPlaying) {
            delay(4000)
            areControlsVisible = false
        }
    }

    val isLiked = remember(video.likedUsernames, currentUser) {
        val username = currentUser?.username ?: ""
        username.isNotBlank() && video.likedUsernames.contains(username)
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { areControlsVisible = !areControlsVisible }
                )
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonPurple, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
            }
        }

        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(playbackError ?: "", color = LiveRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            exoPlayer.prepare()
                            exoPlayer.play()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إعادة المحاولة", fontSize = 11.sp)
                    }
                }
            }
        }

        // INLINE CONTROLS OVERLAY
        AnimatedVisibility(
            visible = areControlsVisible,
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
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Control Row: Close (X) + Title + FULLSCREEN BUTTON (زر ملء الشاشة)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onClosePlayer) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "إغلاق", tint = Color.White)
                    }

                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    // FULLSCREEN BUTTON (زر ملء الشاشة)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricPurple,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleFullscreen() }
                            .testTag("foul_tv_inline_fullscreen_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "ملء الشاشة",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ملء الشاشة",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Center Control Play/Pause & Seek 10s
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10_000).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(ElectricPurple, CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                if (isPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.play()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + 10_000).coerceAtMost(exoPlayer.duration)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }

                // Bottom Progress Seek Bar + Time indicator + Like + Comments
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    val currentVal = if (isUserSeeking) userSeekFraction else {
                        if (totalDurationMs > 0) currentPositionMs.toFloat() / totalDurationMs.toFloat() else 0f
                    }

                    Slider(
                        value = currentVal.coerceIn(0f, 1f),
                        onValueChange = { fraction ->
                            isUserSeeking = true
                            userSeekFraction = fraction
                        },
                        onValueChangeFinished = {
                            val seekToMs = (userSeekFraction * totalDurationMs).toLong()
                            exoPlayer.seekTo(seekToMs)
                            currentPositionMs = seekToMs
                            isUserSeeking = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricPurple,
                            activeTrackColor = ElectricPurple,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("foul_inline_seek_bar")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayPosMs = if (isUserSeeking) (userSeekFraction * totalDurationMs).toLong() else currentPositionMs
                        Text(
                            text = "${formatMs(displayPosMs)} / ${formatMs(totalDurationMs)}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Comments icon on player
                            if (!globalConfig.disableCommentsAll && !video.isCommentsDisabled) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onOpenComments() }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("foul_inline_comments_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "التعليقات",
                                        tint = BrightLavender,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = formatCount(comments.size.toLong().coerceAtLeast(video.commentsCount)),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Like button on player
                            if (!globalConfig.hideLikesAll && !video.isLikesDisabled) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onToggleLike(video.id) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "إعجاب",
                                        tint = if (isLiked) LiveRed else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = formatCount(video.likesCount),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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

/**
 * YouTube-inspired Compact Video Card with 16:9 Thumbnail, duration badge, author avatar, title, channel info, Like button & Comments button
 * Note: 'تعديل (للمطور)' has been intentionally removed from 3-dots to protect against unauthorized modifications.
 */
@Composable
fun FoulVideoCard(
    video: FoulVideo,
    globalConfig: FoulTvGlobalConfig,
    currentUser: ChatUser?,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenComments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isViewsHidden = video.isViewsHidden || globalConfig.hideViewsAll
    val isLikesDisabled = video.isLikesDisabled || globalConfig.hideLikesAll
    val isCommentsDisabled = video.isCommentsDisabled || globalConfig.disableCommentsAll

    val isLiked = remember(video.likedUsernames, currentUser) {
        val username = currentUser?.username ?: ""
        username.isNotBlank() && video.likedUsernames.contains(username)
    }

    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPlaying) DarkPurpleContainer else DarkSurface),
        border = BorderStroke(1.dp, if (isPlaying) ElectricPurple else DarkSurfaceBorder),
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 680.dp)
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick)
            .testTag("foul_video_card_${video.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. 16:9 THUMBNAIL PREVIEW
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF0D0D12))
            ) {
                if (video.thumbnailUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = NeonPurple, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            FoulVideoThumbnailFallback(video = video)
                        }
                    )
                } else {
                    FoulVideoThumbnailFallback(video = video)
                }

                // Playing indicator tag
                if (isPlaying) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = LiveRed,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "يعمل الآن ▶",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // HD Badge (top-left)
                if (!isPlaying) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "HD",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                // Category pill (top-right)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricPurple.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = video.category,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }

                // Play Icon Overlay in center
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, VividViolet.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Duration Badge (bottom-right)
                if (video.duration.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = video.duration,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // 2. VIDEO INFO ROW (Avatar + Title + Author & Views/Likes/Comments Metrics)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Author Avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            Brush.linearGradient(listOf(NeonPurple, ElectricPurple)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.authorName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Video Title
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Channel name • Views • Time ago
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = video.authorName,
                            color = BrightLavender,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isViewsHidden) {
                            Text(text = "•", color = TextMuted, fontSize = 9.sp)
                            Text(
                                text = "${formatCount(video.viewsCount)} مشاهدة",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Text(text = "•", color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = formatTimeAgoCard(video.createdAt),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Interactive Comments Button (💬) on Card
                if (!isCommentsDisabled) {
                    Surface(
                        shape = CircleShape,
                        color = DarkSurfaceVariant,
                        border = BorderStroke(0.8.dp, DarkSurfaceBorder),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onOpenComments() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Comment,
                                contentDescription = "تعليقات",
                                tint = BrightLavender,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = formatCount(video.commentsCount),
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Interactive Like Button (❤️) on Card
                if (!isLikesDisabled) {
                    Surface(
                        shape = CircleShape,
                        color = if (isLiked) LiveRed.copy(alpha = 0.2f) else DarkSurfaceVariant,
                        border = BorderStroke(0.8.dp, if (isLiked) LiveRed else DarkSurfaceBorder),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onToggleLike() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "إعجاب",
                                tint = if (isLiked) LiveRed else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = formatCount(video.likesCount),
                                color = if (isLiked) LiveRed else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3-Dots Menu (Developer Edit button REMOVED to protect from unauthorized edits)
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "خيارات", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("تشغيل الفيديو ▶️", color = Color.White, fontSize = 12.sp) },
                            onClick = {
                                showMenu = false
                                onClick()
                            }
                        )
                        if (!isLikesDisabled) {
                            DropdownMenuItem(
                                text = { Text(if (isLiked) "إلغاء الإعجاب ❤️" else "إعجاب بالفيديو ❤️", color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    showMenu = false
                                    onToggleLike()
                                }
                            )
                        }
                        if (!isCommentsDisabled) {
                            DropdownMenuItem(
                                text = { Text("فتح التعليقات (${video.commentsCount}) 💬", color = BrightLavender, fontSize = 12.sp) },
                                onClick = {
                                    showMenu = false
                                    onOpenComments()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoulVideoThumbnailFallback(video: FoulVideo) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1E0938),
                        Color(0xFF0F051D),
                        Color(0xFF0A0A10)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = BrightLavender.copy(alpha = 0.6f),
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = video.title,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoulFilterBottomSheet(
    currentSort: FoulSortOption,
    onSelectSort: (FoulSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = BrightLavender)
                    Text(text = "ترتيب فيديوهات FOUL TV", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "إغلاق", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FoulSortOption.values().forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onSelectSort(option)
                            onDismiss()
                        }
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSort == option,
                        onClick = {
                            onSelectSort(option)
                            onDismiss()
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = ElectricPurple, unselectedColor = TextSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = option.title, color = if (currentSort == option) Color.White else TextSecondary, fontSize = 13.sp, fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
        else -> "$count"
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

private fun formatTimeAgoCard(timeMillis: Long): String {
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
