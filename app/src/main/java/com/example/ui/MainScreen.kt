package com.example.ui

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.data.model.Channel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import com.example.R
import com.example.ui.chat.ChatAuthDialog
import com.example.ui.chat.ChatScreen
import com.example.ui.components.ChannelGrid
import com.example.ui.components.PremiumHeader
import com.example.ui.components.PremiumBottomNav
import com.example.ui.downloads.DownloadsScreen
import com.example.ui.iptv.IptvScreen
import com.example.ui.matches.MatchesScheduleScreen
import com.example.ui.language.AppLanguage
import com.example.ui.components.EventChannelChooserDialog
import com.example.ui.language.AppStrings
import com.example.ui.language.LanguageDialog
import com.example.ui.language.LocalAppStrings
import com.example.ui.library.LibraryScreen
import com.example.ui.library.LibraryVideoPlayer
import com.example.ui.player.CustomVideoPlayer
import com.example.ui.player.PlayerFactory
import com.example.ui.publisher.PublisherDialog
import com.example.ui.publisher.DeveloperAuthDialog
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.BrightLavender
import com.example.ui.theme.DarkPurpleContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import com.example.ui.viewmodel.FootballViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: FootballViewModel = viewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isLanguageDialogOpen by viewModel.isLanguageDialogOpen.collectAsStateWithLifecycle()
    val strings = AppStrings.forLanguage(currentLanguage)

    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val allChannels by viewModel.allChannels.collectAsStateWithLifecycle()
    val isChannelsLoading by viewModel.isChannelsLoading.collectAsStateWithLifecycle()
    val mostWatchedChannels by viewModel.mostWatchedChannels.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val xtremeAccounts by viewModel.xtremeAccounts.collectAsStateWithLifecycle()
    val eventBanners by viewModel.eventBanners.collectAsStateWithLifecycle()
    val iptvLiveChannels by viewModel.iptvLiveChannels.collectAsStateWithLifecycle()
    val selectedIptvAccount by viewModel.selectedIptvAccount.collectAsStateWithLifecycle()
    val isIptvLoading by viewModel.isIptvLoading.collectAsStateWithLifecycle()
    val iptvErrorMessage by viewModel.iptvErrorMessage.collectAsStateWithLifecycle()
    val playingIptvChannel by viewModel.playingIptvChannel.collectAsStateWithLifecycle()
    val isIptvFullscreen by viewModel.isIptvFullscreen.collectAsStateWithLifecycle()
    val currentNavIndex by viewModel.currentNavIndex.collectAsStateWithLifecycle()
    val libraryItems by viewModel.libraryItems.collectAsStateWithLifecycle()
    val isLibraryLoading by viewModel.isLibraryLoading.collectAsStateWithLifecycle()
    val isAccountUpdatedError by viewModel.isAccountUpdatedError.collectAsStateWithLifecycle()
    val selectedLibraryAccount by viewModel.selectedLibraryAccount.collectAsStateWithLifecycle()
    val isLibraryAllAccountsMode by viewModel.isLibraryAllAccountsMode.collectAsStateWithLifecycle()

    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val selectedMatchDate by viewModel.selectedMatchDate.collectAsStateWithLifecycle()
    val selectedMatchLeagueFilter by viewModel.selectedMatchLeagueFilter.collectAsStateWithLifecycle()
    val isMatchesLoading by viewModel.isMatchesLoading.collectAsStateWithLifecycle()
    val matchesErrorMessage by viewModel.matchesErrorMessage.collectAsStateWithLifecycle()
    val selectedMatchDetail by viewModel.selectedMatchDetail.collectAsStateWithLifecycle()
    val matchSummary by viewModel.matchSummary.collectAsStateWithLifecycle()
    val isSummaryLoading by viewModel.isSummaryLoading.collectAsStateWithLifecycle()

    val selectedChannel by viewModel.selectedChannel.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val isDeveloperDialogOpen by viewModel.isDeveloperDialogOpen.collectAsStateWithLifecycle()
    val developerInitialTab by viewModel.developerInitialTab.collectAsStateWithLifecycle()
    val editingChannel by viewModel.editingChannel.collectAsStateWithLifecycle()
    val publishUiState by viewModel.publishUiState.collectAsStateWithLifecycle()

    val hideHomeAll by viewModel.hideHomeAll.collectAsStateWithLifecycle()
    val hideHomeMode by viewModel.hideHomeMode.collectAsStateWithLifecycle()
    val hideHomeCategory by viewModel.hideHomeCategory.collectAsStateWithLifecycle()
    val hideChatInPlayer by viewModel.hideChatInPlayer.collectAsStateWithLifecycle()
    val hideOnlineViewers by viewModel.hideOnlineViewers.collectAsStateWithLifecycle()
    val hideMatchesSchedule by viewModel.hideMatchesSchedule.collectAsStateWithLifecycle()
    val hideFoulTv by viewModel.hideFoulTv.collectAsStateWithLifecycle()
    val lockOldVersions by viewModel.lockOldVersions.collectAsStateWithLifecycle()
    val appVersion = viewModel.appVersion
    val latestVersion by viewModel.latestVersion.collectAsStateWithLifecycle()
    val updateType by viewModel.updateType.collectAsStateWithLifecycle()
    val updateText by viewModel.updateText.collectAsStateWithLifecycle()
    val updateUrl by viewModel.updateUrl.collectAsStateWithLifecycle()
    val developerPassword by viewModel.developerPassword.collectAsStateWithLifecycle()
    val isFullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()
    val scaleMode by viewModel.scaleMode.collectAsStateWithLifecycle()
    val isLibraryFullscreen by viewModel.isLibraryFullscreen.collectAsStateWithLifecycle()
    val selectedLibraryItem by viewModel.selectedLibraryItem.collectAsStateWithLifecycle()
    val playingLibraryItem by viewModel.playingLibraryItem.collectAsStateWithLifecycle()
    val seriesInfo by viewModel.seriesInfo.collectAsStateWithLifecycle()
    val isSeriesInfoLoading by viewModel.isSeriesInfoLoading.collectAsStateWithLifecycle()
    val vodInfo by viewModel.vodInfo.collectAsStateWithLifecycle()
    val isVodInfoLoading by viewModel.isVodInfoLoading.collectAsStateWithLifecycle()

    val isChatOpen by viewModel.isChatOpen.collectAsStateWithLifecycle()
    val isLandscapeChatOpen by viewModel.isLandscapeChatOpen.collectAsStateWithLifecycle()
    val unreadChatCount by viewModel.unreadChatCount.collectAsStateWithLifecycle()
    val activeChatChannel by viewModel.activeChatChannel.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val currentChatUser by viewModel.currentChatUser.collectAsStateWithLifecycle()
    val isChatAuthDialogOpen by viewModel.isChatAuthDialogOpen.collectAsStateWithLifecycle()
    val onlineViewersCount by viewModel.onlineViewersCount.collectAsStateWithLifecycle()
    val branches by viewModel.branches.collectAsStateWithLifecycle()
    val globalOnlineUsers by viewModel.globalOnlineUsers.collectAsStateWithLifecycle()
    val globalOnlineCount by viewModel.globalOnlineCount.collectAsStateWithLifecycle()

    val foulVideos by viewModel.foulVideos.collectAsStateWithLifecycle()
    val foulGlobalConfig by viewModel.foulGlobalConfig.collectAsStateWithLifecycle()
    val playingFoulVideo by viewModel.playingFoulVideo.collectAsStateWithLifecycle()
    val isFoulVideoFullscreen by viewModel.isFoulVideoFullscreen.collectAsStateWithLifecycle()
    val foulComments by viewModel.foulComments.collectAsStateWithLifecycle()

    var editingFoulVideo by remember { mutableStateOf<com.example.data.model.FoulVideo?>(null) }
    var isPublishFoulVideoDialogOpen by remember { mutableStateOf(false) }

    var isQuickChannelsOpen by remember { mutableStateOf(false) }
    var isDownloadsOpen by remember { mutableStateOf(false) }
    var isDeveloperPasswordDialogOpen by remember { mutableStateOf(false) }
    var developerDialogInitialTab by remember { mutableStateOf(0) }
    var eventBannerToChooseChannels by remember { mutableStateOf<com.example.data.model.EventBanner?>(null) }
    var eventMatchedChannels by remember { mutableStateOf<List<Channel>>(emptyList()) }

    // BackHandler for Downloads: return to main view
    BackHandler(enabled = isDownloadsOpen) {
        isDownloadsOpen = false
    }

    // BackHandler for Quick Channels Split Pane
    BackHandler(enabled = isFullscreen && isQuickChannelsOpen) {
        isQuickChannelsOpen = false
    }

    // BackHandler for Portrait Chat: When user presses back, close chat smoothly and return to channels
    BackHandler(enabled = isChatOpen) {
        viewModel.closeChat()
    }

    // BackHandler for Landscape Chat in Fullscreen mode: Closes chat pane and expands player
    BackHandler(enabled = isFullscreen && isLandscapeChatOpen) {
        viewModel.closeLandscapeChat()
    }

    val telegramChannelUrl = "https://t.me/jdj_q"

    // Persistent Home ExoPlayer shared across embedded and fullscreen mode to prevent reloading
    val lifecycleOwner = LocalLifecycleOwner.current
    val homeExoPlayer = remember {
        PlayerFactory.createExoPlayer(context)
    }
    DisposableEffect(lifecycleOwner, homeExoPlayer) {
        onDispose {
            homeExoPlayer.stop()
            homeExoPlayer.clearMediaItems()
            homeExoPlayer.release()
        }
    }

    // Stop playback and release stream connections when navigating between sections, playing library video, or exiting fullscreen
    // IPTV/Xtream servers strictly limit concurrent streams to 1 per account (returning HTTP 409 Conflict if exceeded)
    LaunchedEffect(currentNavIndex, playingLibraryItem, isFullscreen, playingFoulVideo) {
        if ((currentNavIndex != 0 && currentNavIndex != 1) || playingLibraryItem != null || playingFoulVideo != null || !isFullscreen) {
            homeExoPlayer.stop()
            homeExoPlayer.clearMediaItems()
            viewModel.pauseHomePlayer()
        } else if ((currentNavIndex == 0 || currentNavIndex == 1) && playingLibraryItem == null && playingFoulVideo == null && isFullscreen) {
            viewModel.stopLibraryPlayback()
            viewModel.stopFoulVideo()
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides (if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr),
        LocalAppStrings provides strings
    ) {
        // If Downloads screen is open
        if (isDownloadsOpen) {
            DownloadsScreen(
                onBack = { isDownloadsOpen = false },
                onPlayDownload = { downloadItem ->
                    isDownloadsOpen = false
                    val localUriOrPath = downloadItem.localUri ?: downloadItem.fileName
                    val movieItem = com.example.data.model.XtremeItem(
                        id = downloadItem.downloadId.toString(),
                        title = downloadItem.title,
                        streamId = downloadItem.downloadId.toString(),
                        streamIcon = "",
                        type = com.example.data.model.XtremeContentType.MOVIE,
                        categoryId = "downloads",
                        categoryName = strings.downloads,
                        streamUrl = localUriOrPath,
                        accountName = strings.downloads,
                        accountId = "local_downloads"
                    )
                    viewModel.playLibraryItem(movieItem)
                },
                modifier = modifier.fillMaxSize()
            )
            return@CompositionLocalProvider
        }

        // If Home player is in FULLSCREEN, display video player (and optional split-screen live chat)
        if (isFullscreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CustomVideoPlayer(
                        channel = selectedChannel,
                        selectedServer = selectedServer,
                        isPlaying = isPlaying,
                        isMuted = isMuted,
                        isFullscreen = true,
                        scaleMode = scaleMode,
                        onServerSelected = { server ->
                            viewModel.selectServer(server)
                        },
                        onTogglePlay = {
                            viewModel.togglePlay()
                        },
                        onToggleMute = {
                            viewModel.toggleMute()
                        },
                        onToggleFullscreen = {
                            viewModel.toggleFullscreen()
                        },
                        onCycleScaleMode = {
                            viewModel.toggleNextScaleMode()
                        },
                        onNextChannel = {
                            viewModel.selectNextChannel()
                        },
                        onPreviousChannel = {
                            viewModel.selectPreviousChannel()
                        },
                        exoPlayer = homeExoPlayer,
                        onlineViewersCount = onlineViewersCount,
                        hideOnlineViewers = hideOnlineViewers,
                        onOpenChat = {
                            if (isQuickChannelsOpen) isQuickChannelsOpen = false
                            viewModel.toggleLandscapeChat()
                        },
                        hideChatInPlayer = hideChatInPlayer,
                        unreadChatCount = unreadChatCount,
                        isChatOpen = isLandscapeChatOpen,
                        onToggleQuickChannels = {
                            if (isLandscapeChatOpen) viewModel.closeLandscapeChat()
                            isQuickChannelsOpen = !isQuickChannelsOpen
                        },
                        isQuickChannelsOpen = isQuickChannelsOpen,
                        modifier = if (isLandscapeChatOpen || isQuickChannelsOpen) Modifier.weight(0.55f).fillMaxHeight() else Modifier.fillMaxSize()
                    )

                    // Landscape Split-Screen Channels Switcher View (انقسام الشاشة وقائمة القنوات)
                    AnimatedVisibility(
                        visible = isQuickChannelsOpen,
                        enter = fadeIn() + slideInHorizontally { it },
                        exit = fadeOut() + slideOutHorizontally { it },
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                    ) {
                        com.example.ui.components.MainChannelSplitPane(
                            currentChannel = selectedChannel,
                            channels = allChannels,
                            categories = categories,
                            branches = branches,
                            onSelectChannel = { ch ->
                                viewModel.selectChannel(ch)
                            },
                            onClose = { isQuickChannelsOpen = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Landscape Split-Screen Chat View (Opens on user request, doesn't force open)
                    AnimatedVisibility(
                        visible = isLandscapeChatOpen,
                        enter = fadeIn() + slideInHorizontally { it },
                        exit = fadeOut() + slideOutHorizontally { it },
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                    ) {
                        Surface(
                            color = DarkBackground,
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ChatScreen(
                                messages = chatMessages,
                                currentUser = currentChatUser,
                                channel = activeChatChannel,
                                allChannels = allChannels,
                                onSelectChannel = { ch -> viewModel.selectChatChannel(ch) },
                                onSendMessage = { text, replyTo -> viewModel.sendChatMessage(text, replyTo) },
                                onDeleteMessage = { msgId -> viewModel.deleteChatMessage(msgId) },
                                onToggleReaction = { msgId, emoji -> viewModel.toggleChatReaction(msgId, emoji) },
                                onRequireAuth = { viewModel.openChatAuthDialog() },
                                onLogout = { viewModel.logoutChatUser() },
                                onBack = { viewModel.closeLandscapeChat() },
                                isCompactLandscape = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            return@CompositionLocalProvider
        }

        // If Library player is in FULLSCREEN, display ONLY the library video player (Immersive landscape, NO top/bottom bars)
        if (isLibraryFullscreen && playingLibraryItem != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                LibraryVideoPlayer(
                    item = playingLibraryItem!!,
                    onClosePlayer = {
                        viewModel.playLibraryItem(null)
                    },
                    isFullscreen = true,
                    onToggleFullscreen = {
                        viewModel.playLibraryItem(null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            return@CompositionLocalProvider
        }

        // If IPTV player is active, display ONLY the dedicated isolated IPTV player (NO top/bottom bars, NO viewers count, NO chat, does NOT touch Home or database)
        if (isIptvFullscreen && playingIptvChannel != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                com.example.ui.iptv.IptvVideoPlayer(
                    item = playingIptvChannel!!,
                    availableChannels = iptvLiveChannels,
                    onSelectChannel = { ch -> viewModel.playIptvChannel(ch) },
                    onClosePlayer = {
                        viewModel.stopIptvPlayback()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            return@CompositionLocalProvider
        }

        // If FOUL TV player is active, display ONLY the dedicated FOUL TV video player in direct horizontal/landscape mode
        if (isFoulVideoFullscreen && playingFoulVideo != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                com.example.ui.foultv.FoulVideoPlayer(
                    video = playingFoulVideo!!,
                    currentUser = currentChatUser,
                    comments = foulComments,
                    globalConfig = foulGlobalConfig,
                    onClosePlayer = {
                        viewModel.stopFoulVideo()
                    },
                    onToggleLike = { videoId ->
                        viewModel.toggleFoulVideoLike(videoId)
                    },
                    onAddComment = { comment ->
                        viewModel.addFoulComment(comment)
                    },
                    onDeleteComment = { videoId, commentId ->
                        viewModel.deleteFoulComment(videoId, commentId)
                    },
                    onIncrementViews = { videoId ->
                        viewModel.incrementFoulVideoViews(videoId)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            return@CompositionLocalProvider
        }

        var isSettingsOpenOnForceUpdate by remember { mutableStateOf(false) }
        var isForceUpdateDismissed by remember { mutableStateOf(false) }

        val isForceUpdateRequired = remember(appVersion, latestVersion, updateType, lockOldVersions, updateText, isForceUpdateDismissed) {
            if (isForceUpdateDismissed) return@remember false

            // Anti-Defacement and Anti-Tampering Check:
            if (com.example.data.security.AppSecurityGuard.isMaliciousText(updateText) ||
                com.example.data.security.AppSecurityGuard.isSuspiciousVersion(latestVersion)) {
                return@remember false
            }

            val isVersionOutdated = com.example.ui.viewmodel.FootballViewModel.isVersionOlder(appVersion, latestVersion)
            val isLegitimate = com.example.data.security.AppSecurityGuard.isLegitimateUpdate(appVersion, latestVersion, updateText)

            if (isVersionOutdated && !isLegitimate) {
                // Reject spoofed jumps (e.g. 7.7.7)
                return@remember false
            }

            val isExplicitForced = updateType.equals("forced", ignoreCase = true) || 
                                  updateType.equals("required", ignoreCase = true) ||
                                  updateType.equals("mandatory", ignoreCase = true)
            val isOptionalWithOlder = isVersionOutdated && updateType.equals("optional", ignoreCase = true)
            val isLockEnabled = lockOldVersions || isExplicitForced
            
            // Show update screen whenever:
            // 1. Current version is older than latestVersion AND lock or update type is active (forced/required/optional)
            // 2. OR lockOldVersions is true and versions differ
            // 3. OR updateType is explicitly forced/required and versions differ
            (isVersionOutdated && (isLockEnabled || isOptionalWithOlder)) || (lockOldVersions && isVersionOutdated) || (isExplicitForced && appVersion != latestVersion)
        }

        if (isForceUpdateRequired) {
            com.example.ui.update.ForceUpdateScreen(
                currentVersion = appVersion,
                latestVersion = latestVersion,
                updateText = updateText,
                updateUrl = updateUrl,
                telegramUrl = "https://t.me/jdj_q",
                onOpenDeveloperAuth = { isDeveloperPasswordDialogOpen = true },
                onOpenSettings = { isSettingsOpenOnForceUpdate = true },
                onDismiss = { isForceUpdateDismissed = true },
                modifier = Modifier.fillMaxSize()
            )

            // SETTINGS SCREEN OVERLAY (When accessed from Force Update Screen)
            if (isSettingsOpenOnForceUpdate) {
                androidx.activity.compose.BackHandler {
                    isSettingsOpenOnForceUpdate = false
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top navigation bar for Settings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { isSettingsOpenOnForceUpdate = false },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(DarkSurfaceVariant, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = "الإعدادات العامة",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.size(40.dp))
                        }

                        SettingsScreen(
                            onOpenDeveloperPanel = { isDeveloperPasswordDialogOpen = true },
                            channelsCount = allChannels.size,
                            xtremeAccountsCount = xtremeAccounts.size,
                            appVersion = appVersion,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // In user release mode, developer dialogs are completely stripped and forbidden
            if (!com.example.ui.viewmodel.FootballViewModel.IS_USER_RELEASE_BUILD) {
                // DEVELOPER MASTER AUTHENTICATION DIALOG (On Force Update Lock Screen)
                DeveloperAuthDialog(
                    isOpen = isDeveloperPasswordDialogOpen,
                    developerPassword = developerPassword,
                    onDismiss = { isDeveloperPasswordDialogOpen = false },
                    onSuccess = {
                        isDeveloperPasswordDialogOpen = false
                        viewModel.openDeveloperDialog()
                    }
                )

                // PUBLISHER & DEVELOPER SETTINGS (On Force Update Lock Screen)
                PublisherDialog(
                    isOpen = isDeveloperDialogOpen,
                    editingChannel = editingChannel,
                    channels = channels,
                    categories = categories,
                    branches = branches,
                    initialTab = developerInitialTab,
                    xtremeAccounts = xtremeAccounts,
                    publishUiState = publishUiState,
                    onDismiss = { viewModel.closeDeveloperDialog() },
                    onSaveChannel = { channel, isUpdate -> viewModel.publishOrUpdateChannel(channel, isUpdate) },
                    onDeleteChannel = { id -> viewModel.deleteChannel(id) },
                    onDeleteAllChannels = { viewModel.deleteAllChannels() },
                    onSaveCategory = { category, isUpdate -> viewModel.publishOrUpdateCategory(category, isUpdate) },
                    onSaveCategoryWithChannels = { category, channelsList -> viewModel.publishCategoryWithChannels(category, channelsList) },
                    onDeleteCategory = { id -> viewModel.deleteCategory(id) },
                    onMoveCategoryToTop = { id -> viewModel.moveCategoryToTop(id) },
                    onMoveCategoryToSecond = { id -> viewModel.moveCategoryToSecond(id) },
                    onMoveCategoryUp = { id -> viewModel.moveCategoryUp(id) },
                    onMoveCategoryDown = { id -> viewModel.moveCategoryDown(id) },
                    onSaveBranch = { branch, isUpdate -> viewModel.publishOrUpdateBranch(branch, isUpdate) },
                    onDeleteBranch = { id -> viewModel.deleteBranch(id) },
                    onAddChannelsToBranch = { branchId, channelIds, branchName -> viewModel.addChannelsToBranch(branchId, channelIds, branchName) },
                    onRemoveChannelFromBranch = { branchId, channelId -> viewModel.removeChannelFromBranch(branchId, channelId) },
                    onSaveXtremeAccount = { account -> viewModel.publishOrUpdateXtremeAccount(account) },
                    onDeleteXtremeAccount = { id -> viewModel.deleteXtremeAccount(id) },
                    onDeleteAllXtremeAccounts = { viewModel.deleteAllXtremeAccounts() },
                    onPlayChannel = { channel -> viewModel.selectChannel(channel) },
                    onClearPublishState = { viewModel.clearPublishState() },
                    hideHomeAll = hideHomeAll,
                    hideHomeMode = hideHomeMode,
                    hideHomeCategory = hideHomeCategory,
                    hideChatInPlayer = hideChatInPlayer,
                    hideOnlineViewers = hideOnlineViewers,
                    hideMatchesSchedule = hideMatchesSchedule,
                    hideFoulTv = hideFoulTv,
                    lockOldVersions = lockOldVersions,
                    appVersion = appVersion,
                    latestVersion = latestVersion,
                    updateType = updateType,
                    updateText = updateText,
                    updateUrl = updateUrl,
                    onSetHideHomeAll = { viewModel.setHideHomeAll(it) },
                    onSetHideHomeMode = { viewModel.setHideHomeMode(it) },
                    onSetHideHomeCategory = { viewModel.setHideHomeCategory(it) },
                    onSetHideChatInPlayer = { viewModel.setHideChatInPlayer(it) },
                    onSetHideOnlineViewers = { viewModel.setHideOnlineViewers(it) },
                    onSetHideMatchesSchedule = { viewModel.setHideMatchesSchedule(it) },
                    onSetHideFoulTv = { viewModel.setHideFoulTv(it) },
                    onSetLockOldVersions = { viewModel.setLockOldVersions(it) },
                    onSetLatestVersion = { viewModel.setLatestVersion(it) },
                    onSetUpdateType = { viewModel.setUpdateType(it) },
                    onSetUpdateText = { viewModel.setUpdateText(it) },
                    onSetUpdateUrl = { viewModel.setUpdateUrl(it) },
                    onPublishVersion3Smartly = { text, url -> viewModel.publishVersionSmartly(text, url) },
                    developerPassword = developerPassword,
                    onSetDeveloperPassword = { viewModel.setDeveloperPassword(it) },
                    onDeleteAllMessages = { callback -> viewModel.deleteAllChatMessages(callback) },
                    eventBanners = eventBanners,
                    onSaveEventBanner = { banner -> viewModel.publishOrUpdateEventBanner(banner) },
                    onDeleteEventBanner = { id -> viewModel.deleteEventBanner(id) },
                    globalOnlineUsers = globalOnlineUsers,
                    globalOnlineCount = globalOnlineCount,
                    foulVideos = foulVideos,
                    foulGlobalConfig = foulGlobalConfig,
                    onSaveFoulVideo = { video, isUpdate -> viewModel.publishOrUpdateFoulVideo(video, isUpdate) },
                    onDeleteFoulVideo = { id -> viewModel.deleteFoulVideo(id) },
                    onUpdateFoulVideoSettings = { videoId, disableComments, disableLikes, hideViews -> viewModel.updateFoulVideoSettings(videoId, disableComments, disableLikes, hideViews) },
                    onUpdateFoulGlobalConfig = { hideViews, hideLikes, disableComments -> viewModel.updateFoulGlobalConfig(hideViews, hideLikes, disableComments) },
                    onOpenPublishFoulVideoDialog = { video ->
                        editingFoulVideo = video
                        isPublishFoulVideoDialogOpen = true
                    }
                )
            }

            return@CompositionLocalProvider
        }

        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            containerColor = DarkBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            // BOTTOM NAVIGATION BAR: 1. الرئيسية  2. IPTV  3. المكتبة  4. جدول المباريات (Hides when Chat is open or viewing full match detail)
            bottomBar = {
                if (!isChatOpen && selectedMatchDetail == null) {
                    PremiumBottomNav(
                        currentIndex = currentNavIndex,
                        onTabSelected = { index -> viewModel.setNavIndex(index) },
                        hideMatchesSchedule = hideMatchesSchedule,
                        hideFoulTv = hideFoulTv
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkBackground)
            ) {
                // PREMIUM TOP BAR HEADER (Hides when viewing full match detail page)
                if (selectedMatchDetail == null) {
                    PremiumHeader(
                        currentLanguage = currentLanguage,
                        onOpenLanguageSelector = { viewModel.openLanguageDialog() },
                        onOpenDownloads = { isDownloadsOpen = true },
                        onOpenDeveloper = { isDeveloperPasswordDialogOpen = true },
                        telegramUrl = telegramChannelUrl
                    )
                }

                // SECTION SWITCHER BASED ON BOTTOM BAR
                when (currentNavIndex) {
                    0 -> {
                        // 1. الرئيسية: Full screen Channels Grid matching reference design (OR Live Chat if open)
                        if (isChatOpen) {
                            // CHAT SECTION: Chat is shown with Back Arrow to return
                            ChatScreen(
                                messages = chatMessages,
                                currentUser = currentChatUser,
                                channel = activeChatChannel,
                                allChannels = allChannels,
                                onSelectChannel = { ch ->
                                    viewModel.selectChatChannel(ch)
                                },
                                onSendMessage = { text, replyTo ->
                                    viewModel.sendChatMessage(text, replyTo)
                                },
                                onDeleteMessage = { msgId ->
                                    viewModel.deleteChatMessage(msgId)
                                },
                                onToggleReaction = { msgId, emoji ->
                                    viewModel.toggleChatReaction(msgId, emoji)
                                },
                                onRequireAuth = {
                                    viewModel.openChatAuthDialog()
                                },
                                onLogout = {
                                    viewModel.logoutChatUser()
                                },
                                onBack = {
                                    viewModel.closeChat()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Reference Design Channels Grid:
                            // Top category pills + 3-Column Grid of squircle cards + titles underneath.
                            // Player does NOT show at the top; tapping any channel directly opens fullscreen landscape mode!
                             ChannelGrid(
                                channels = allChannels,
                                categories = categories,
                                branches = branches,
                                mostWatchedChannels = mostWatchedChannels,
                                selectedChannel = selectedChannel,
                                eventBanners = eventBanners,
                                isLoading = isChannelsLoading,
                                onChannelClick = { channel ->
                                    viewModel.playChannelFullscreen(channel)
                                },
                                onWatchNowClick = { banner ->
                                    val allTargetChannelIds = banner.getAllChannelIds()
                                    val matched = allChannels.filter { ch -> allTargetChannelIds.contains(ch.id) }
                                    if (matched.size > 1) {
                                        eventBannerToChooseChannels = banner
                                        eventMatchedChannels = matched
                                    } else if (matched.size == 1) {
                                        viewModel.playChannelFullscreen(matched.first())
                                    } else {
                                        val targetChannel = if (banner.channelId.isNotBlank()) {
                                            allChannels.find { it.id == banner.channelId }
                                        } else if (banner.watchNowUrl.isNotBlank() && !banner.watchNowUrl.startsWith("http")) {
                                            allChannels.find { it.id == banner.watchNowUrl || it.name.contains(banner.watchNowUrl, ignoreCase = true) }
                                        } else {
                                            allChannels.find { it.name.contains(banner.title, ignoreCase = true) }
                                        }
                                        if (targetChannel != null) {
                                            viewModel.playChannelFullscreen(targetChannel)
                                        }
                                    }
                                },
                                onOpenPublisher = {},
                                modifier = Modifier.fillMaxSize(),
                                hideHomeAll = hideHomeAll,
                                hideHomeMode = hideHomeMode,
                                hideHomeCategory = hideHomeCategory
                            )
                        }
                    }

                    1 -> {
                        // 2. IPTV: Pure Live Channels Grid with Ultra-fast scrolling & Account Switcher
                        IptvScreen(
                            liveChannels = iptvLiveChannels,
                            accounts = xtremeAccounts,
                            selectedAccount = selectedIptvAccount,
                            isLoading = isIptvLoading,
                            errorMessage = iptvErrorMessage,
                            onSelectAccount = { acc -> viewModel.selectIptvAccount(acc) },
                            onRefresh = { viewModel.loadIptvChannels() },
                            onPlayChannel = { channelItem -> viewModel.playIptvChannel(channelItem) },
                            onOpenDeveloperSettings = { isDeveloperPasswordDialogOpen = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    2 -> {
                        // 3. FOUL TV: YouTube-style Video Feed & Community
                        com.example.ui.foultv.FoulTvScreen(
                            videos = foulVideos,
                            playingVideo = playingFoulVideo,
                            currentUser = currentChatUser,
                            comments = foulComments,
                            globalConfig = foulGlobalConfig,
                            onPlayVideo = { video -> viewModel.playFoulVideo(video, fullscreen = false) },
                            onToggleFullscreen = { viewModel.setFoulVideoFullscreen(true) },
                            onStopVideo = { viewModel.stopFoulVideo() },
                            onToggleLike = { videoId -> viewModel.toggleFoulVideoLike(videoId) },
                            onIncrementViews = { videoId -> viewModel.incrementFoulVideoViews(videoId) },
                            onAddComment = { comment -> viewModel.addFoulComment(comment) },
                            onDeleteComment = { videoId, commentId -> viewModel.deleteFoulComment(videoId, commentId) },
                            onOpenPublishVideoDialog = { isPublishFoulVideoDialogOpen = true },
                            onOpenDeveloperAuth = { isDeveloperPasswordDialogOpen = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    3 -> {
                        // 4. المكتبة: Dedicated Series Screen & Direct Movie/Episode Playback
                        LibraryScreen(
                            libraryItems = libraryItems,
                            selectedItem = selectedLibraryItem,
                            seriesInfo = seriesInfo,
                            isSeriesInfoLoading = isSeriesInfoLoading,
                            vodInfo = vodInfo,
                            isVodInfoLoading = isVodInfoLoading,
                            onSelectItem = { item ->
                                viewModel.selectLibraryItem(item)
                            },
                            onPlayMovie = { movie ->
                                viewModel.playLibraryItem(movie)
                            },
                            onPlayEpisode = { episode, series ->
                                viewModel.playEpisode(episode, series)
                            },
                            isLoading = isLibraryLoading,
                            isAccountUpdatedError = isAccountUpdatedError,
                            onRefresh = { viewModel.refreshLibraryContent() },
                            accounts = xtremeAccounts,
                            selectedAccount = selectedLibraryAccount,
                            isAllAccountsMode = isLibraryAllAccountsMode,
                            onSelectAccount = { account -> viewModel.selectLibraryAccount(account) },
                            onSelectAllAccounts = { viewModel.setLibraryAllAccountsMode() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    4 -> {
                        // 5. جدول المباريات: Match Schedule & Live Scores (ESPN Soccer API + Firebase Events Engine)
                        MatchesScheduleScreen(
                            matches = matches,
                            selectedDate = selectedMatchDate,
                            selectedFilter = selectedMatchLeagueFilter,
                            isLoading = isMatchesLoading,
                            errorMessage = matchesErrorMessage,
                            selectedMatchDetail = selectedMatchDetail,
                            matchSummary = matchSummary,
                            isSummaryLoading = isSummaryLoading,
                            onSelectDate = { date -> viewModel.selectMatchDate(date) },
                            onSelectFilter = { filter -> viewModel.selectMatchLeagueFilter(filter) },
                            onRefresh = { viewModel.refreshMatches() },
                            onOpenMatchDetail = { match -> viewModel.openMatchDetail(match) },
                            onCloseMatchDetail = { viewModel.closeMatchDetail() },
                            onRefreshSummary = { viewModel.loadMatchSummary() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> Unit
                }
            }

            // LANGUAGE SELECTION MODAL BOTTOM SHEET
            LanguageDialog(
                isOpen = isLanguageDialogOpen,
                currentLanguage = currentLanguage,
                onSelectLanguage = { lang ->
                    viewModel.selectLanguage(lang)
                },
                onDismiss = {
                    viewModel.closeLanguageDialog()
                }
            )

            // CHAT AUTHENTICATION DIALOG (Sign Up & Login with Cancel option)
            ChatAuthDialog(
                isOpen = isChatAuthDialogOpen,
                onDismiss = { viewModel.closeChatAuthDialog() },
                onRegister = { user, callback ->
                    viewModel.registerChatUser(user, callback)
                },
                onLogin = { username, password, callback ->
                    viewModel.loginChatUser(username, password, callback)
                }
            )

            // EVENT MULTI-CHANNEL CHOOSER MODAL (When tapping 'Watch Now' on an event linked to multiple channels)
            com.example.ui.components.EventChannelChooserDialog(
                isOpen = (eventBannerToChooseChannels != null && eventMatchedChannels.isNotEmpty()),
                eventBanner = eventBannerToChooseChannels,
                channels = eventMatchedChannels,
                onSelectChannel = { channel ->
                    eventBannerToChooseChannels = null
                    eventMatchedChannels = emptyList()
                    viewModel.playChannelFullscreen(channel)
                },
                onDismiss = {
                    eventBannerToChooseChannels = null
                    eventMatchedChannels = emptyList()
                }
            )

            // In user release mode, developer dialogs are strictly stripped and never rendered
            if (!com.example.ui.viewmodel.FootballViewModel.IS_USER_RELEASE_BUILD) {
                // DEVELOPER MASTER AUTHENTICATION DIALOG (Default: 2008 or dynamic from Firebase)
                DeveloperAuthDialog(
                    isOpen = isDeveloperPasswordDialogOpen,
                    developerPassword = developerPassword,
                    onDismiss = { isDeveloperPasswordDialogOpen = false },
                    onSuccess = {
                        isDeveloperPasswordDialogOpen = false
                        viewModel.openDeveloperDialog()
                    }
                )

                // PUBLISHER & DEVELOPER SETTINGS MODAL BOTTOM SHEET
                PublisherDialog(
                    isOpen = isDeveloperDialogOpen,
                    editingChannel = editingChannel,
                    channels = channels,
                    categories = categories,
                    branches = branches,
                    initialTab = developerInitialTab,
                    xtremeAccounts = xtremeAccounts,
                    publishUiState = publishUiState,
                    onDismiss = { viewModel.closeDeveloperDialog() },
                    onSaveChannel = { channel, isUpdate -> viewModel.publishOrUpdateChannel(channel, isUpdate) },
                    onDeleteChannel = { id -> viewModel.deleteChannel(id) },
                    onDeleteAllChannels = { viewModel.deleteAllChannels() },
                    onSaveCategory = { category, isUpdate -> viewModel.publishOrUpdateCategory(category, isUpdate) },
                    onSaveCategoryWithChannels = { category, channelsList -> viewModel.publishCategoryWithChannels(category, channelsList) },
                    onDeleteCategory = { id -> viewModel.deleteCategory(id) },
                    onMoveCategoryToTop = { id -> viewModel.moveCategoryToTop(id) },
                    onMoveCategoryToSecond = { id -> viewModel.moveCategoryToSecond(id) },
                    onMoveCategoryUp = { id -> viewModel.moveCategoryUp(id) },
                    onMoveCategoryDown = { id -> viewModel.moveCategoryDown(id) },
                    onSaveBranch = { branch, isUpdate -> viewModel.publishOrUpdateBranch(branch, isUpdate) },
                    onDeleteBranch = { id -> viewModel.deleteBranch(id) },
                    onAddChannelsToBranch = { branchId, channelIds, branchName -> viewModel.addChannelsToBranch(branchId, channelIds, branchName) },
                    onRemoveChannelFromBranch = { branchId, channelId -> viewModel.removeChannelFromBranch(branchId, channelId) },
                    onSaveXtremeAccount = { account -> viewModel.publishOrUpdateXtremeAccount(account) },
                    onDeleteXtremeAccount = { id -> viewModel.deleteXtremeAccount(id) },
                    onDeleteAllXtremeAccounts = { viewModel.deleteAllXtremeAccounts() },
                    onPlayChannel = { channel -> viewModel.selectChannel(channel) },
                    onClearPublishState = { viewModel.clearPublishState() },
                    hideHomeAll = hideHomeAll,
                    hideHomeMode = hideHomeMode,
                    hideHomeCategory = hideHomeCategory,
                    hideChatInPlayer = hideChatInPlayer,
                    hideOnlineViewers = hideOnlineViewers,
                    hideMatchesSchedule = hideMatchesSchedule,
                    hideFoulTv = hideFoulTv,
                    lockOldVersions = lockOldVersions,
                    appVersion = appVersion,
                    latestVersion = latestVersion,
                    updateType = updateType,
                    updateText = updateText,
                    updateUrl = updateUrl,
                    onSetHideHomeAll = { viewModel.setHideHomeAll(it) },
                    onSetHideHomeMode = { viewModel.setHideHomeMode(it) },
                    onSetHideHomeCategory = { viewModel.setHideHomeCategory(it) },
                    onSetHideChatInPlayer = { viewModel.setHideChatInPlayer(it) },
                    onSetHideOnlineViewers = { viewModel.setHideOnlineViewers(it) },
                    onSetHideMatchesSchedule = { viewModel.setHideMatchesSchedule(it) },
                    onSetHideFoulTv = { viewModel.setHideFoulTv(it) },
                    onSetLockOldVersions = { viewModel.setLockOldVersions(it) },
                    onSetLatestVersion = { viewModel.setLatestVersion(it) },
                    onSetUpdateType = { viewModel.setUpdateType(it) },
                    onSetUpdateText = { viewModel.setUpdateText(it) },
                    onSetUpdateUrl = { viewModel.setUpdateUrl(it) },
                    onPublishVersion3Smartly = { text, url -> viewModel.publishVersionSmartly(text, url) },
                    developerPassword = developerPassword,
                    onSetDeveloperPassword = { viewModel.setDeveloperPassword(it) },
                    onDeleteAllMessages = { callback -> viewModel.deleteAllChatMessages(callback) },
                    eventBanners = eventBanners,
                    onSaveEventBanner = { banner -> viewModel.publishOrUpdateEventBanner(banner) },
                    onDeleteEventBanner = { id -> viewModel.deleteEventBanner(id) },
                    globalOnlineUsers = globalOnlineUsers,
                    globalOnlineCount = globalOnlineCount,
                    foulVideos = foulVideos,
                    foulGlobalConfig = foulGlobalConfig,
                    onSaveFoulVideo = { video, isUpdate -> viewModel.publishOrUpdateFoulVideo(video, isUpdate) },
                    onDeleteFoulVideo = { id -> viewModel.deleteFoulVideo(id) },
                    onUpdateFoulVideoSettings = { videoId, disableComments, disableLikes, hideViews -> viewModel.updateFoulVideoSettings(videoId, disableComments, disableLikes, hideViews) },
                    onUpdateFoulGlobalConfig = { hideViews, hideLikes, disableComments -> viewModel.updateFoulGlobalConfig(hideViews, hideLikes, disableComments) },
                    onOpenPublishFoulVideoDialog = { video ->
                        editingFoulVideo = video
                        isPublishFoulVideoDialogOpen = true
                    }
                )
            }
        }
    }
}
