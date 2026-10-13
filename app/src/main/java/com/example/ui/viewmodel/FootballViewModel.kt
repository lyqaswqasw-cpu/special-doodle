package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.cache.LibraryCacheManager
import com.example.data.model.Channel
import com.example.data.model.ChannelBranch
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelServer
import com.example.data.model.XtremeAccount
import com.example.data.model.XtremeContentType
import com.example.data.model.XtremeItem
import com.example.data.repository.ChannelRepository
import com.example.data.util.LibraryDeduplicator
import com.example.data.xtreme.XtremeApiClient
import com.example.ui.language.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.util.M3uParser
import com.example.data.matches.MatchItem
import com.example.data.matches.MatchRepository
import com.example.data.matches.MatchStatus
import com.example.data.matches.MatchSummaryDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface PublishUiState {
    data object Idle : PublishUiState
    data object Loading : PublishUiState
    data class Success(val message: String) : PublishUiState
    data class Error(val message: String) : PublishUiState
}

class FootballViewModel(
    application: Application,
    private val repository: ChannelRepository,
    private val xtremeApiClient: XtremeApiClient
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        repository = ChannelRepository(),
        xtremeApiClient = XtremeApiClient()
    )

    private val cacheManager = LibraryCacheManager(application.applicationContext)
    private val channelCacheManager = com.example.data.cache.ChannelCacheManager(application.applicationContext)
    private val chatRepository = com.example.data.repository.ChatRepository(application.applicationContext)
    private val presenceRepository = com.example.data.repository.ChannelPresenceRepository(application.applicationContext)
    private val foulTvRepository = com.example.data.repository.FoulTvRepository()

    // FOUL TV Videos & Configuration Flows from Firebase
    val foulVideos: StateFlow<List<com.example.data.model.FoulVideo>> = foulTvRepository.getVideosFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val foulGlobalConfig: StateFlow<com.example.data.model.FoulTvGlobalConfig> = foulTvRepository.getGlobalConfigFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = com.example.data.model.FoulTvGlobalConfig()
        )

    private val _playingFoulVideo = MutableStateFlow<com.example.data.model.FoulVideo?>(null)
    val playingFoulVideo: StateFlow<com.example.data.model.FoulVideo?> = _playingFoulVideo.asStateFlow()

    private val _isFoulVideoFullscreen = MutableStateFlow(false)
    val isFoulVideoFullscreen: StateFlow<Boolean> = _isFoulVideoFullscreen.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val foulComments: StateFlow<List<com.example.data.model.FoulComment>> = _playingFoulVideo
        .flatMapLatest { video ->
            if (video != null) {
                foulTvRepository.getCommentsFlow(video.id)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Synchronously load cached channels and categories for instant 0ms preloaded display upon opening
    private val initialCachedChannels: List<Channel> = channelCacheManager.loadCachedChannelsSync()
    private val initialCachedCategories: List<ChannelCategory> = channelCacheManager.loadCachedCategoriesSync()

    // Current Chat User (Instant direct guest user - No login required)
    private val _currentChatUser = MutableStateFlow<com.example.data.model.ChatUser?>(chatRepository.getOrCreateGuestUser())
    val currentChatUser: StateFlow<com.example.data.model.ChatUser?> = _currentChatUser.asStateFlow()

    // Real-time Global Online Users across the entire app for Developer Settings
    private val _globalOnlineUsers = MutableStateFlow<List<com.example.data.repository.OnlineUserSession>>(emptyList())
    val globalOnlineUsers: StateFlow<List<com.example.data.repository.OnlineUserSession>> = _globalOnlineUsers.asStateFlow()
    val globalOnlineCount: StateFlow<Int> = _globalOnlineUsers.map { it.size }.stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    // Auth Dialog State
    private val _isChatAuthDialogOpen = MutableStateFlow(false)
    val isChatAuthDialogOpen: StateFlow<Boolean> = _isChatAuthDialogOpen.asStateFlow()

    // Real-time Online Viewers Count for current channel (Only active for first 15 channels)
    private val _onlineViewersCount = MutableStateFlow<Int?>(null)
    val onlineViewersCount: StateFlow<Int?> = _onlineViewersCount.asStateFlow()

    private val _isChannelsLoading = MutableStateFlow(initialCachedChannels.isEmpty())
    val isChannelsLoading: StateFlow<Boolean> = _isChannelsLoading.asStateFlow()

    private var presenceJob: kotlinx.coroutines.Job? = null

    // Main Live Channels from Firebase with zero-delay instant preloading
    val channels: StateFlow<List<Channel>> = repository.getChannelsFlow()
        .onEach { _isChannelsLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialCachedChannels
        )

    // Top 15 Most Watched Channels dynamically sorted by real views from Firebase
    val mostWatchedChannels: StateFlow<List<Channel>> = channels
        .map { list ->
            list.sortedWith(
                compareByDescending<Channel> { it.views }
                    .thenByDescending { it.createdAt }
            ).take(15)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialCachedChannels.sortedWith(
                compareByDescending<Channel> { it.views }
                    .thenByDescending { it.createdAt }
            ).take(15)
        )

    // Channel Categories from Firebase
    val categories: StateFlow<List<ChannelCategory>> = repository.getCategoriesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialCachedCategories
        )

    // Channel Branches (الفروع داخل التصنيفات) from Firebase
    val branches: StateFlow<List<ChannelBranch>> = repository.getBranchesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _m3uChannels = MutableStateFlow<List<Channel>>(emptyList())

    // Combined channels (Firebase + parsed M3U)
    val allChannels: StateFlow<List<Channel>> = combine(channels, _m3uChannels) { firebase, m3u ->
        firebase + m3u
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = initialCachedChannels
    )

    // Xtreme Accounts from Firebase
    val xtremeAccounts: StateFlow<List<XtremeAccount>> = repository.getXtremeAccountsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Event Banners from Firebase
    val eventBanners: StateFlow<List<com.example.data.model.EventBanner>> = repository.getEventBannersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Navigation Tab state (0: Home/الرئيسية, 1: IPTV, 2: Library/المكتبة)
    private val _currentNavIndex = MutableStateFlow(0)
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    // IPTV Live Channels State
    private val _selectedIptvAccount = MutableStateFlow<XtremeAccount?>(null)
    val selectedIptvAccount: StateFlow<XtremeAccount?> = _selectedIptvAccount.asStateFlow()

    // Library Dedicated Account State (Supports per-account content view & All accounts broadcast)
    private val _selectedLibraryAccount = MutableStateFlow<XtremeAccount?>(null)
    val selectedLibraryAccount: StateFlow<XtremeAccount?> = _selectedLibraryAccount.asStateFlow()

    private val _isLibraryAllAccountsMode = MutableStateFlow(true)
    val isLibraryAllAccountsMode: StateFlow<Boolean> = _isLibraryAllAccountsMode.asStateFlow()

    // In-memory cache for ultra-fast instant switching between accounts
    private val accountLibraryCache = java.util.concurrent.ConcurrentHashMap<String, List<XtremeItem>>()
    @Volatile
    private var allAccountsLibraryCache: List<XtremeItem> = emptyList()

    private val _iptvLiveChannels = MutableStateFlow<List<XtremeItem>>(emptyList())
    val iptvLiveChannels: StateFlow<List<XtremeItem>> = _iptvLiveChannels.asStateFlow()

    private val _isIptvLoading = MutableStateFlow<Boolean>(false)
    val isIptvLoading: StateFlow<Boolean> = _isIptvLoading.asStateFlow()

    private val _iptvErrorMessage = MutableStateFlow<String?>(null)
    val iptvErrorMessage: StateFlow<String?> = _iptvErrorMessage.asStateFlow()

    // Isolated IPTV Playback State (Independent, does NOT appear in Home or save to database)
    private val _playingIptvChannel = MutableStateFlow<XtremeItem?>(null)
    val playingIptvChannel: StateFlow<XtremeItem?> = _playingIptvChannel.asStateFlow()

    private val _isIptvFullscreen = MutableStateFlow(false)
    val isIptvFullscreen: StateFlow<Boolean> = _isIptvFullscreen.asStateFlow()

    // Match Schedule (ESPN Soccer API + Firebase Events Engine)
    private val matchRepository = MatchRepository()

    private val _selectedMatchDate = MutableStateFlow(
        try {
            SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        } catch (_: Exception) {
            "20260925"
        }
    )
    val selectedMatchDate: StateFlow<String> = _selectedMatchDate.asStateFlow()

    private val _selectedMatchLeagueFilter = MutableStateFlow("all")
    val selectedMatchLeagueFilter: StateFlow<String> = _selectedMatchLeagueFilter.asStateFlow()

    private val _isMatchesLoading = MutableStateFlow(false)
    val isMatchesLoading: StateFlow<Boolean> = _isMatchesLoading.asStateFlow()

    private val _matchesErrorMessage = MutableStateFlow<String?>(null)
    val matchesErrorMessage: StateFlow<String?> = _matchesErrorMessage.asStateFlow()

    private val _espnMatches = MutableStateFlow<List<MatchItem>>(emptyList())
    private val _firebaseEventsMatches = MutableStateFlow<List<MatchItem>>(emptyList())

    // Selected Match & Detailed Real-time Summary
    private val _selectedMatchDetail = MutableStateFlow<MatchItem?>(null)
    val selectedMatchDetail: StateFlow<MatchItem?> = _selectedMatchDetail.asStateFlow()

    private val _matchSummary = MutableStateFlow<MatchSummaryDetail?>(null)
    val matchSummary: StateFlow<MatchSummaryDetail?> = _matchSummary.asStateFlow()

    private val _isSummaryLoading = MutableStateFlow(false)
    val isSummaryLoading: StateFlow<Boolean> = _isSummaryLoading.asStateFlow()

    // Combined Matches (Firebase Events Engine + ESPN live scoreboard)
    val matches: StateFlow<List<MatchItem>> = combine(_espnMatches, _firebaseEventsMatches) { espn, fb ->
        val list = mutableListOf<MatchItem>()
        // Firebase events matches first (priority for live broadcasting channels in app)
        list.addAll(fb)
        // Add ESPN matches, deduplicating if event already in Firebase
        for (m in espn) {
            val alreadyExists = fb.any { f ->
                (f.homeTeamName.isNotBlank() && m.homeTeamName.isNotBlank() &&
                        (f.homeTeamName.contains(m.homeTeamName, ignoreCase = true) || m.homeTeamName.contains(f.homeTeamName, ignoreCase = true))) &&
                (f.awayTeamName.isNotBlank() && m.awayTeamName.isNotBlank() &&
                        (f.awayTeamName.contains(m.awayTeamName, ignoreCase = true) || m.awayTeamName.contains(f.awayTeamName, ignoreCase = true)))
            }
            if (!alreadyExists) {
                list.add(m)
            }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Language State (Defaults to Arabic for first-time users, saved in SharedPreferences)
    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("app_language", AppLanguage.AR.code) ?: AppLanguage.AR.code)
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // App Config / Settings (both from Firebase real-time and fallback to Local SharedPreferences)
    private val _hideHomeAll = MutableStateFlow(prefs.getBoolean("hide_home_all", false))
    val hideHomeAll: StateFlow<Boolean> = _hideHomeAll.asStateFlow()

    private val _hideHomeMode = MutableStateFlow(prefs.getString("hide_home_mode", "all") ?: "all")
    val hideHomeMode: StateFlow<String> = _hideHomeMode.asStateFlow()

    private val _hideHomeCategory = MutableStateFlow(prefs.getString("hide_home_category", "") ?: "")
    val hideHomeCategory: StateFlow<String> = _hideHomeCategory.asStateFlow()

    private val _hideChatInPlayer = MutableStateFlow(prefs.getBoolean("hide_chat_in_player", false))
    val hideChatInPlayer: StateFlow<Boolean> = _hideChatInPlayer.asStateFlow()

    private val _hideOnlineViewers = MutableStateFlow(prefs.getBoolean("hide_online_viewers", false))
    val hideOnlineViewers: StateFlow<Boolean> = _hideOnlineViewers.asStateFlow()

    // Hide Matches Schedule Tab from Bottom Navigation Bar
    private val _hideMatchesSchedule = MutableStateFlow(prefs.getBoolean("hide_matches_schedule", false))
    val hideMatchesSchedule: StateFlow<Boolean> = _hideMatchesSchedule.asStateFlow()

    // Hide FOUL TV Tab from Bottom Navigation Bar
    private val _hideFoulTv = MutableStateFlow(prefs.getBoolean("hide_foul_tv", false))
    val hideFoulTv: StateFlow<Boolean> = _hideFoulTv.asStateFlow()

    // Lock Old Versions and disable database access for previous releases
    private val _lockOldVersions = MutableStateFlow(prefs.getBoolean("lock_old_versions", false))
    val lockOldVersions: StateFlow<Boolean> = _lockOldVersions.asStateFlow()

    // Developer Master Password (stored in Firebase real-time and fallback to Local SharedPreferences, default 2812246216200810)
    private val _developerPassword = MutableStateFlow(
        run {
            val saved = prefs.getString("developer_password", "2812246216200810") ?: "2812246216200810"
            if (saved == "2008" || saved == "998877") "2812246216200810" else saved
        }
    )
    val developerPassword: StateFlow<String> = _developerPassword.asStateFlow()

    // Current fixed version of the app (Updated to 3.0.1 to invalidate old versions)
    val appVersion: String = CURRENT_APP_VERSION

    private val _latestVersion = MutableStateFlow(prefs.getString("latest_version", CURRENT_APP_VERSION) ?: CURRENT_APP_VERSION)
    val latestVersion: StateFlow<String> = _latestVersion.asStateFlow()

    private val _updateType = MutableStateFlow(prefs.getString("update_type", "none") ?: "none")
    val updateType: StateFlow<String> = _updateType.asStateFlow()

    private val _updateText = MutableStateFlow(prefs.getString("update_text", "") ?: "")
    val updateText: StateFlow<String> = _updateText.asStateFlow()

    private val _updateUrl = MutableStateFlow(prefs.getString("update_url", "") ?: "")
    val updateUrl: StateFlow<String> = _updateUrl.asStateFlow()

    private val _isLanguageDialogOpen = MutableStateFlow(false)
    val isLanguageDialogOpen: StateFlow<Boolean> = _isLanguageDialogOpen.asStateFlow()

    fun openLanguageDialog() {
        _isLanguageDialogOpen.value = true
    }

    fun closeLanguageDialog() {
        _isLanguageDialogOpen.value = false
    }

    fun selectLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("app_language", language.code).apply()
        _isLanguageDialogOpen.value = false
    }

    // Player State
    private val _selectedChannel = MutableStateFlow<Channel?>(null)
    val selectedChannel: StateFlow<Channel?> = _selectedChannel.asStateFlow()

    private val _selectedServer = MutableStateFlow<ChannelServer?>(null)
    val selectedServer: StateFlow<ChannelServer?> = _selectedServer.asStateFlow()

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isDeveloperDialogOpen = MutableStateFlow(false)
    val isDeveloperDialogOpen: StateFlow<Boolean> = _isDeveloperDialogOpen.asStateFlow()

    private val _editingChannel = MutableStateFlow<Channel?>(null)
    val editingChannel: StateFlow<Channel?> = _editingChannel.asStateFlow()

    private val _publishUiState = MutableStateFlow<PublishUiState>(PublishUiState.Idle)
    val publishUiState: StateFlow<PublishUiState> = _publishUiState.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _scaleMode = MutableStateFlow(com.example.ui.player.VideoScaleMode.FIT_16_9)
    val scaleMode: StateFlow<com.example.ui.player.VideoScaleMode> = _scaleMode.asStateFlow()

    // Chat Visibility State
    private val _isChatOpen = MutableStateFlow(false)
    val isChatOpen: StateFlow<Boolean> = _isChatOpen.asStateFlow()

    // Landscape Split-Screen Chat Visibility State
    private val _isLandscapeChatOpen = MutableStateFlow(false)
    val isLandscapeChatOpen: StateFlow<Boolean> = _isLandscapeChatOpen.asStateFlow()

    // Unread Chat Messages Counter (Badges for closed chat, resets to 0 when opened)
    private val _unreadChatCount = MutableStateFlow(0)
    val unreadChatCount: StateFlow<Int> = _unreadChatCount.asStateFlow()

    private var lastSeenMessageId: String? = null
    private var isInitialChatMessagesLoad = true

    // Explicitly selected channel for chat (if null, falls back to selectedChannel)
    private val _chatChannel = MutableStateFlow<Channel?>(null)
    val chatChannel: StateFlow<Channel?> = _chatChannel.asStateFlow()

    // Active Channel for the chat session: combines manual chat channel, player selected channel, or first channel
    val activeChatChannel: StateFlow<Channel?> = combine(_chatChannel, _selectedChannel, allChannels) { chatCh, selCh, all ->
        chatCh ?: selCh ?: all.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Real-time Chat Messages for the active channel
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<List<com.example.data.model.ChatMessage>> = activeChatChannel
        .flatMapLatest { channel ->
            val targetId = channel?.id ?: "general"
            chatRepository.getMessagesFlow(targetId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combined Library State with instant pre-warming from disk cache
    private val _libraryItems = MutableStateFlow<List<XtremeItem>>(cacheManager.loadCachedItemsSync())
    val libraryItems: StateFlow<List<XtremeItem>> = _libraryItems.asStateFlow()

    private val _selectedLibraryItem = MutableStateFlow<XtremeItem?>(null)
    val selectedLibraryItem: StateFlow<XtremeItem?> = _selectedLibraryItem.asStateFlow()

    // Dedicated item (movie or episode) currently playing in fullscreen horizontal mode
    private val _playingLibraryItem = MutableStateFlow<XtremeItem?>(null)
    val playingLibraryItem: StateFlow<XtremeItem?> = _playingLibraryItem.asStateFlow()

    private val _isLibraryFullscreen = MutableStateFlow(false)
    val isLibraryFullscreen: StateFlow<Boolean> = _isLibraryFullscreen.asStateFlow()

    private val _isLibraryLoading = MutableStateFlow(false)
    val isLibraryLoading: StateFlow<Boolean> = _isLibraryLoading.asStateFlow()

    private val _seriesInfo = MutableStateFlow<com.example.data.model.SeriesInfo?>(null)
    val seriesInfo: StateFlow<com.example.data.model.SeriesInfo?> = _seriesInfo.asStateFlow()

    private val _isSeriesInfoLoading = MutableStateFlow(false)
    val isSeriesInfoLoading: StateFlow<Boolean> = _isSeriesInfoLoading.asStateFlow()

    private val _vodInfo = MutableStateFlow<com.example.data.model.VodInfo?>(null)
    val vodInfo: StateFlow<com.example.data.model.VodInfo?> = _vodInfo.asStateFlow()

    private val _isVodInfoLoading = MutableStateFlow(false)
    val isVodInfoLoading: StateFlow<Boolean> = _isVodInfoLoading.asStateFlow()

    // Mandatory update flag: triggers when host, user, password, accounts count/IDs changed or account deleted
    private val _isAccountUpdatedError = MutableStateFlow(false)
    val isAccountUpdatedError: StateFlow<Boolean> = _isAccountUpdatedError.asStateFlow()

    init {
        // Observe real-time App Configuration from Firebase
        viewModelScope.launch {
            repository.getAppConfigFlow().collect { configMap ->
                val editor = prefs.edit()
                
                configMap["hide_home_all"]?.let {
                    val value = when(it) {
                        is Boolean -> it
                        is String -> it.toBoolean()
                        is Number -> it.toInt() == 1
                        else -> false
                    }
                    _hideHomeAll.value = value
                    editor.putBoolean("hide_home_all", value)
                }
                
                configMap["hide_home_mode"]?.let {
                    val value = it.toString()
                    _hideHomeMode.value = value
                    editor.putString("hide_home_mode", value)
                }

                configMap["hide_home_category"]?.let {
                    val value = it.toString()
                    _hideHomeCategory.value = value
                    editor.putString("hide_home_category", value)
                }

                configMap["hide_chat_in_player"]?.let {
                    val value = when(it) {
                        is Boolean -> it
                        is String -> it.toBoolean()
                        is Number -> it.toInt() == 1
                        else -> false
                    }
                    _hideChatInPlayer.value = value
                    editor.putBoolean("hide_chat_in_player", value)
                }

                configMap["hide_online_viewers"]?.let {
                    val value = when(it) {
                        is Boolean -> it
                        is String -> it.toBoolean()
                        is Number -> it.toInt() == 1
                        else -> false
                    }
                    _hideOnlineViewers.value = value
                    editor.putBoolean("hide_online_viewers", value)
                }

                configMap["hide_matches_schedule"]?.let {
                    val value = when(it) {
                        is Boolean -> it
                        is String -> it.toBoolean()
                        is Number -> it.toInt() == 1
                        else -> false
                    }
                    _hideMatchesSchedule.value = value
                    editor.putBoolean("hide_matches_schedule", value)
                    if (value && _currentNavIndex.value == 4) {
                        _currentNavIndex.value = 0
                    }
                }

                configMap["hide_foul_tv"]?.let {
                    val value = when(it) {
                        is Boolean -> it
                        is String -> it.toBoolean()
                        is Number -> it.toInt() == 1
                        else -> false
                    }
                    _hideFoulTv.value = value
                    editor.putBoolean("hide_foul_tv", value)
                    if (value && _currentNavIndex.value == 2) {
                        _currentNavIndex.value = 0
                    }
                }

                // Anti-Defacement and Anti-Tamper Security Guard
                val rawVersion = configMap["latest_version"]?.toString()?.trim() ?: ""
                val rawText = configMap["update_text"]?.toString()?.trim() ?: ""
                val isAttackDetected = com.example.data.security.AppSecurityGuard.isMaliciousText(rawText) ||
                        com.example.data.security.AppSecurityGuard.isSuspiciousVersion(rawVersion)

                if (isAttackDetected) {
                    android.util.Log.e("SecurityGuard", "Blocked malicious attack payload from server! Discarding immediately.")
                    _latestVersion.value = CURRENT_APP_VERSION
                    editor.putString("latest_version", CURRENT_APP_VERSION)
                    _lockOldVersions.value = false
                    editor.putBoolean("lock_old_versions", false)
                    _updateType.value = "none"
                    editor.putString("update_type", "none")
                    _updateText.value = ""
                    editor.putString("update_text", "")
                    editor.apply()

                    // Automatically heal Firebase Realtime Database
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            repository.saveAppConfigValue("latest_version", CURRENT_APP_VERSION)
                            repository.saveAppConfigValue("accepted_version", CURRENT_APP_VERSION)
                            repository.saveAppConfigValue("min_version", CURRENT_APP_VERSION)
                            repository.saveAppConfigValue("lock_old_versions", false)
                            repository.saveAppConfigValue("update_type", "none")
                            repository.saveAppConfigValue("update_text", "")
                        } catch (_: Exception) {}
                    }
                    return@collect
                }

                configMap["lock_old_versions"]?.let {
                    val value = when(it) {
                        is Boolean -> it
                        is String -> it.toBoolean()
                        is Number -> it.toInt() == 1
                        else -> false
                    }
                    _lockOldVersions.value = value
                    editor.putBoolean("lock_old_versions", value)
                }

                configMap["latest_version"]?.let {
                    val raw = it.toString().trim()
                    val normalized = normalizeVersion(raw)
                    if (normalized.isNotBlank() && !com.example.data.security.AppSecurityGuard.isSuspiciousVersion(normalized)) {
                        _latestVersion.value = normalized
                        editor.putString("latest_version", normalized)
                    }
                }

                configMap["update_type"]?.let {
                    val value = it.toString()
                    _updateType.value = value
                    editor.putString("update_type", value)
                }

                configMap["update_text"]?.let {
                    val value = com.example.data.security.AppSecurityGuard.sanitizeUpdateText(it.toString())
                    _updateText.value = value
                    editor.putString("update_text", value)
                }

                configMap["update_url"]?.let {
                    val value = it.toString()
                    _updateUrl.value = value
                    editor.putString("update_url", value)
                }

                configMap["developer_password"]?.let {
                    val pass = it.toString().trim()
                    if (pass.isNotBlank() && pass != "2008" && pass != "998877") {
                        _developerPassword.value = pass
                        editor.putString("developer_password", pass)
                    }
                }

                editor.apply()
            }
        }

        // Start Global App Presence tracking across the entire application for Developer Settings
        presenceRepository.startGlobalPresence(_selectedChannel.value?.name ?: "الرئيسية")
        viewModelScope.launch {
            presenceRepository.getGlobalOnlineUsersFlow().collect { list ->
                _globalOnlineUsers.value = list
            }
        }
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(45000L)
                presenceRepository.heartbeat()
            }
        }

        // Preload cached library items immediately on start for instant display
        viewModelScope.launch(Dispatchers.IO) {
            val cached = cacheManager.loadCachedItems()
            if (cached.isNotEmpty()) {
                _libraryItems.value = cached
            }
        }

        // 1. Set default selected channel without auto-playing audio until user taps to play
        if (_selectedChannel.value == null && initialCachedChannels.isNotEmpty()) {
            val first = initialCachedChannels.first()
            _selectedChannel.value = first
            _chatChannel.value = first
            _selectedServer.value = first.servers.firstOrNull()
            _isPlaying.value = false
        }

        // 2. Persist real-time channel updates from Firebase to local preloaded cache
        viewModelScope.launch {
            channels.collect { list ->
                if (list.isNotEmpty()) {
                    channelCacheManager.saveChannels(list)
                }
            }
        }

        // 3. Persist real-time category updates from Firebase to local preloaded cache
        viewModelScope.launch {
            categories.collect { cats ->
                if (cats.isNotEmpty()) {
                    channelCacheManager.saveCategories(cats)
                }
            }
        }

        // 4. Observe real-time Xtreme accounts from Firebase to detect changes/deletions/additions
        viewModelScope.launch {
            xtremeAccounts.collect { accounts ->
                if (accounts.isNotEmpty()) {
                    checkCredentialsChanged(accounts)
                }
            }
        }

        // 5. Observe categories to parse M3U urls
        viewModelScope.launch {
            categories.collect { cats ->
                val m3uCategories = cats.filter { it.m3uUrl.isNotBlank() }
                if (m3uCategories.isEmpty()) {
                    _m3uChannels.value = emptyList()
                    return@collect
                }
                
                // Fetch and parse in parallel
                val allFetchedM3us = kotlinx.coroutines.coroutineScope {
                    m3uCategories.map { cat ->
                        async { M3uParser.parse(cat.m3uUrl, cat.name) }
                    }.awaitAll().flatten()
                }
                
                _m3uChannels.value = allFetchedM3us
            }
        }

        // 6. Observe selected channel and playback state to manage real-time presence (Only for first 15 channels)
        viewModelScope.launch {
            combine(_selectedChannel, _isPlaying, allChannels) { sel, playing, all ->
                Triple(sel, playing, all)
            }.collect { (sel, playing, all) ->
                if (sel != null && playing) {
                    val currentList = all.ifEmpty { channels.value }
                    val index = currentList.indexOfFirst { it.id == sel.id }
                    if (index in 0..14) {
                        // Eligible for online presence (first 15 channels)
                        presenceJob?.cancel()
                        presenceRepository.startWatchingChannel(sel.id)
                        presenceJob = viewModelScope.launch {
                            presenceRepository.getOnlineViewersFlow(sel.id).collect { count ->
                                _onlineViewersCount.value = count
                            }
                        }
                    } else {
                        presenceJob?.cancel()
                        presenceRepository.stopWatchingCurrentChannel()
                        _onlineViewersCount.value = null
                    }
                } else {
                    presenceJob?.cancel()
                    presenceRepository.stopWatchingCurrentChannel()
                    _onlineViewersCount.value = null
                }
            }
        }

        // 7. Auto-select first channel when channels load if none is selected yet
        viewModelScope.launch {
            allChannels.collect { list ->
                if (_selectedChannel.value == null && list.isNotEmpty()) {
                    selectChannel(list.first())
                }
            }
        }

        // 8. Observe real-time chat messages to calculate unread message count
        viewModelScope.launch {
            combine(chatMessages, _isChatOpen, _isLandscapeChatOpen) { msgs, portraitOpen, landscapeOpen ->
                Triple(msgs, portraitOpen, landscapeOpen)
            }.collect { (msgs, portraitOpen, landscapeOpen) ->
                if (portraitOpen || landscapeOpen) {
                    // Chat is open, all messages are immediately seen and counter resets to 0
                    _unreadChatCount.value = 0
                    lastSeenMessageId = msgs.lastOrNull()?.id
                    isInitialChatMessagesLoad = false
                } else {
                    if (isInitialChatMessagesLoad) {
                        // Reflect existing messages count on first load (capped at 100)
                        _unreadChatCount.value = msgs.size.coerceAtMost(100)
                        lastSeenMessageId = msgs.lastOrNull()?.id
                        isInitialChatMessagesLoad = false
                    } else if (lastSeenMessageId != null) {
                        val index = msgs.indexOfLast { it.id == lastSeenMessageId }
                        if (index >= 0) {
                            val newMsgs = (msgs.size - 1 - index).coerceAtLeast(0)
                            _unreadChatCount.value = newMsgs
                        } else if (msgs.isNotEmpty()) {
                            _unreadChatCount.value = msgs.size.coerceAtMost(100)
                        }
                    } else {
                        _unreadChatCount.value = msgs.size.coerceAtMost(100)
                    }
                }
            }
        }

        // 9. Auto-select first IPTV account and load live channels
        viewModelScope.launch {
            xtremeAccounts.collect { accounts ->
                if (accounts.isNotEmpty()) {
                    if (_selectedIptvAccount.value == null || accounts.none { it.id == _selectedIptvAccount.value?.id }) {
                        _selectedIptvAccount.value = accounts.first()
                        loadIptvChannels(accounts.first())
                    } else {
                        val current = _selectedIptvAccount.value
                        val updated = accounts.find { it.id == current?.id }
                        if (updated != null && updated.credentialsSignature != current?.credentialsSignature) {
                            _selectedIptvAccount.value = updated
                            loadIptvChannels(updated)
                        }
                    }
                } else {
                    _selectedIptvAccount.value = null
                    _iptvLiveChannels.value = emptyList()
                    _isIptvLoading.value = false
                    _iptvErrorMessage.value = null
                }
            }
        }

        // 10. Listen to Firebase Realtime Events for Live Matches & Gulf Cup
        viewModelScope.launch {
            matchRepository.getFirebaseEventsFlow().collect { fbMatches ->
                _firebaseEventsMatches.value = fbMatches
            }
        }

        // Initial fetch of today's matches
        loadMatches(_selectedMatchDate.value)
    }

    fun selectIptvAccount(account: XtremeAccount) {
        _selectedIptvAccount.value = account
        loadIptvChannels(account)
    }

    fun selectLibraryAccount(account: XtremeAccount?) {
        _selectedLibraryAccount.value = account
        _isLibraryAllAccountsMode.value = (account == null)
        loadLibraryForAccount(account)
    }

    fun setLibraryAllAccountsMode() {
        _selectedLibraryAccount.value = null
        _isLibraryAllAccountsMode.value = true
        if (allAccountsLibraryCache.isNotEmpty()) {
            _libraryItems.value = allAccountsLibraryCache
        }
        refreshLibraryContent(wipeOld = false)
    }

    fun loadLibraryForAccount(account: XtremeAccount?) {
        if (account == null) {
            setLibraryAllAccountsMode()
            return
        }
        // Ultra-Fast Instant Switch: Show from memory cache immediately if already loaded
        val cached = accountLibraryCache[account.id]
        if (!cached.isNullOrEmpty()) {
            _libraryItems.value = cached
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (cached.isNullOrEmpty()) {
                _isLibraryLoading.value = true
            }
            _isAccountUpdatedError.value = false
            try {
                // Fetch content specifically for this selected account with high speed
                val list = xtremeApiClient.fetchAccountContent(account)
                val nonLiveItems = list.filter { it.type != XtremeContentType.LIVE }
                val deduplicated = com.example.data.util.LibraryDeduplicator.deduplicate(nonLiveItems)
                if (deduplicated.isNotEmpty()) {
                    accountLibraryCache[account.id] = deduplicated
                    if (_selectedLibraryAccount.value?.id == account.id && !_isLibraryAllAccountsMode.value) {
                        _libraryItems.value = deduplicated
                    }
                }
            } catch (e: Exception) {
                Log.w("FootballViewModel", "Error loading library for ${account.name}: ${e.message}")
            } finally {
                _isLibraryLoading.value = false
            }
        }
    }

    fun loadIptvChannels(account: XtremeAccount? = _selectedIptvAccount.value) {
        val targetAccount = account ?: xtremeAccounts.value.firstOrNull() ?: return
        _selectedIptvAccount.value = targetAccount
        viewModelScope.launch(Dispatchers.IO) {
            _isIptvLoading.value = true
            _iptvErrorMessage.value = null
            try {
                val liveList = xtremeApiClient.fetchLiveStreams(targetAccount)
                _iptvLiveChannels.value = liveList
                if (liveList.isEmpty()) {
                    _iptvErrorMessage.value = "لا توجد قنوات بث مباشر متاحة في هذا الحساب حالياً"
                }
            } catch (e: Exception) {
                android.util.Log.e("FootballViewModel", "Failed to load IPTV channels: ${e.message}", e)
                _iptvErrorMessage.value = "تعذر تحميل القنوات: ${e.localizedMessage ?: "تأكد من صحة بيانات الخادم"}"
            } finally {
                _isIptvLoading.value = false
            }
        }
    }

    fun playIptvChannel(item: XtremeItem) {
        // Pause any home player
        _isPlaying.value = false
        // Play strictly inside the isolated IPTV player without saving to database or Home channels
        _playingIptvChannel.value = item
        _isIptvFullscreen.value = true
        presenceRepository.updateGlobalPresenceChannel("IPTV: ${item.title}")
    }

    fun stopIptvPlayback() {
        _playingIptvChannel.value = null
        _isIptvFullscreen.value = false
    }

    /**
     * Checks if credentials (host, username, password) changed in Xtreme accounts.
     * If modified, prompts the user in the Library section with an update banner.
     * Does NOT reload or wipe existing local content automatically.
     */
    private fun checkCredentialsChanged(accounts: List<XtremeAccount>) {
        val effectiveAccounts = accounts.ifEmpty { ChannelRepository.DEFAULT_XTREME_ACCOUNTS }
        if (effectiveAccounts.isEmpty()) return

        val currentSignature = cacheManager.computeAccountsSignature(effectiveAccounts)
        val savedSignature = cacheManager.getSavedSignature()

        if (!cacheManager.hasCache()) {
            // First time ever: download and save fresh local cache
            _isAccountUpdatedError.value = false
            refreshLibraryContent(wipeOld = false)
            return
        }

        // Only prompt update if developer edited host, username, or password
        if (savedSignature.isNotBlank() && currentSignature != savedSignature) {
            _isAccountUpdatedError.value = true
        } else {
            _isAccountUpdatedError.value = false
            if (_libraryItems.value.isEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    val cached = cacheManager.loadCachedItems()
                    if (cached.isNotEmpty()) {
                        _libraryItems.value = cached
                    }
                }
            }
        }
    }

    fun setNavIndex(index: Int) {
        _currentNavIndex.value = index

        // If leaving Home, IPTV, and FOUL TV sections, pause playback
        if (index != 0 && index != 1 && index != 2) {
            _isPlaying.value = false
        }

        // If leaving IPTV section (index 1), stop IPTV playback
        if (index != 1) {
            _playingIptvChannel.value = null
            _isIptvFullscreen.value = false
        }

        // If leaving FOUL TV section (index 2), stop FOUL TV playback
        if (index != 2) {
            _playingFoulVideo.value = null
            _isFoulVideoFullscreen.value = false
        }

        // If leaving Library section (index 3), stop/close Library playback
        if (index != 3) {
            _playingLibraryItem.value = null
            _isLibraryFullscreen.value = false
        }

        // When entering IPTV section (index 1): load live channels if empty
        if (index == 1) {
            if (_iptvLiveChannels.value.isEmpty()) {
                loadIptvChannels()
            }
        }

        // When entering Library section (index 3): load from local cache if empty, and check credentials
        if (index == 3) {
            if (_libraryItems.value.isEmpty()) {
                val cached = cacheManager.loadCachedItemsSync()
                if (cached.isNotEmpty()) {
                    _libraryItems.value = cached
                }
            }
            checkCredentialsChanged(xtremeAccounts.value)
        }

        // When entering Match Schedule section (index 4): load matches if empty
        if (index == 4) {
            if (_espnMatches.value.isEmpty()) {
                loadMatches(_selectedMatchDate.value)
            }
        }
    }

    // --- FOUL TV Methods ---

    fun playFoulVideo(video: com.example.data.model.FoulVideo, fullscreen: Boolean = false) {
        _isPlaying.value = false
        _playingIptvChannel.value = null
        _playingLibraryItem.value = null
        _playingFoulVideo.value = video
        _isFoulVideoFullscreen.value = fullscreen
        presenceRepository.updateGlobalPresenceChannel("FOUL TV: ${video.title}")
    }

    fun setFoulVideoFullscreen(fullscreen: Boolean) {
        _isFoulVideoFullscreen.value = fullscreen
    }

    fun stopFoulVideo() {
        _playingFoulVideo.value = null
        _isFoulVideoFullscreen.value = false
    }

    fun incrementFoulVideoViews(videoId: String) {
        foulTvRepository.incrementVideoViews(videoId)
    }

    fun toggleFoulVideoLike(videoId: String) {
        val user = _currentChatUser.value ?: chatRepository.getOrCreateGuestUser().also { _currentChatUser.value = it }
        foulTvRepository.toggleVideoLike(videoId, user.username)
    }

    fun addFoulComment(comment: com.example.data.model.FoulComment) {
        if (com.example.data.security.AppSecurityGuard.isMaliciousText(comment.text)) {
            android.util.Log.w("AppSecurityGuard", "Blocked abusive Foul TV comment attempt")
            return
        }
        viewModelScope.launch {
            foulTvRepository.addComment(comment)
        }
    }

    fun deleteFoulComment(videoId: String, commentId: String) {
        viewModelScope.launch {
            foulTvRepository.deleteComment(videoId, commentId)
        }
    }

    fun publishOrUpdateFoulVideo(video: com.example.data.model.FoulVideo, isUpdate: Boolean) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = foulTvRepository.saveVideo(video)
            result.fold(
                onSuccess = {
                    val msg = if (isUpdate) "تم تحديث فيديو FOUL TV بنجاح في Firebase" else "تم نشر الفيديو بنجاح في FOUL TV"
                    _publishUiState.value = PublishUiState.Success(msg)
                    if (_playingFoulVideo.value?.id == video.id) {
                        _playingFoulVideo.value = video
                    }
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء حفظ الفيديو"
                    )
                }
            )
        }
    }

    fun deleteFoulVideo(videoId: String) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = foulTvRepository.deleteVideo(videoId)
            result.fold(
                onSuccess = {
                    _publishUiState.value = PublishUiState.Success("تم حذف الفيديو من FOUL TV بنجاح")
                    if (_playingFoulVideo.value?.id == videoId) {
                        _playingFoulVideo.value = null
                        _isFoulVideoFullscreen.value = false
                    }
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء حذف الفيديو"
                    )
                }
            )
        }
    }

    fun updateFoulVideoSettings(
        videoId: String,
        disableComments: Boolean,
        disableLikes: Boolean,
        hideViews: Boolean
    ) {
        foulTvRepository.updateVideoSettings(videoId, disableComments, disableLikes, hideViews)
    }

    fun updateFoulGlobalConfig(
        hideViewsAll: Boolean,
        hideLikesAll: Boolean,
        disableCommentsAll: Boolean
    ) {
        foulTvRepository.updateGlobalConfig(hideViewsAll, hideLikesAll, disableCommentsAll)
    }

    // --- Matches Schedule Functions ---

    fun selectMatchDate(dateYyyyMmDd: String) {
        _selectedMatchDate.value = dateYyyyMmDd
        loadMatches(dateYyyyMmDd)
    }

    fun selectMatchLeagueFilter(filter: String) {
        _selectedMatchLeagueFilter.value = filter
    }

    fun refreshMatches() {
        loadMatches(_selectedMatchDate.value)
    }

    fun loadMatches(dateYyyyMmDd: String = _selectedMatchDate.value) {
        viewModelScope.launch(Dispatchers.IO) {
            _isMatchesLoading.value = true
            _matchesErrorMessage.value = null
            try {
                val espnList = matchRepository.fetchAllMatchesForDate(dateYyyyMmDd)
                _espnMatches.value = espnList
                if (espnList.isEmpty() && _firebaseEventsMatches.value.isEmpty()) {
                    _matchesErrorMessage.value = "لا توجد مباريات مجدولة لهذا التاريخ"
                }
            } catch (e: Exception) {
                Log.e("FootballViewModel", "Error fetching matches: ${e.message}", e)
                _matchesErrorMessage.value = "تعذر جلب جدول المباريات: ${e.localizedMessage ?: "تأكد من الاتصال بالإنترنت"}"
            } finally {
                _isMatchesLoading.value = false
            }
        }
    }

    fun openMatchDetail(match: MatchItem) {
        _selectedMatchDetail.value = match
        _matchSummary.value = null
        loadMatchSummary(match)
    }

    fun closeMatchDetail() {
        _selectedMatchDetail.value = null
        _matchSummary.value = null
        _isSummaryLoading.value = false
    }

    fun loadMatchSummary(match: MatchItem? = _selectedMatchDetail.value) {
        val current = match ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isSummaryLoading.value = true
            try {
                val eventId = current.summaryEventId.ifBlank { current.id.removePrefix("espn_").removePrefix("firebase_") }
                val summary = matchRepository.fetchMatchSummary(current.leagueId, eventId)
                _matchSummary.value = summary
            } catch (e: Exception) {
                Log.e("FootballViewModel", "Error fetching match summary: ${e.message}", e)
            } finally {
                _isSummaryLoading.value = false
            }
        }
    }

    /**
     * Plays the live broadcast of a match in the app's player.
     * Searches for linked targetChannelId, custom match servers, or matching channel by broadcaster/name.
     */
    fun playMatchBroadcast(match: MatchItem) {
        // 1. Direct target channel id
        if (!match.targetChannelId.isNullOrBlank()) {
            val target = allChannels.value.find { it.id == match.targetChannelId }
                ?: channels.value.find { it.id == match.targetChannelId }
            if (target != null) {
                playChannelFullscreen(target)
                return
            }
        }

        // 2. Direct server streams inside the match
        if (match.servers.isNotEmpty()) {
            val channel = Channel(
                id = match.id,
                name = "${match.displayHomeName} ضد ${match.displayAwayName}",
                imageUrl = match.leagueLogo.ifBlank { match.homeTeamLogo },
                category = match.leagueName,
                servers = match.servers
            )
            playChannelFullscreen(channel)
            return
        }

        // 3. Match by Broadcaster name in channels (e.g. "beIN SPORTS", "SSC", "Alkass", "Abu Dhabi")
        if (match.broadcaster.isNotBlank()) {
            val broadcasterKeywords = match.broadcaster.split(" ", "-", "/").filter { it.length > 2 }
            val matchedByBroadcaster = allChannels.value.find { ch ->
                broadcasterKeywords.any { kw -> ch.name.contains(kw, ignoreCase = true) }
            }
            if (matchedByBroadcaster != null) {
                playChannelFullscreen(matchedByBroadcaster)
                return
            }
        }

        // 4. Match by team names in channel names (for dedicated club channels e.g. Real Madrid TV)
        val matchedByTeam = allChannels.value.find { ch ->
            (match.homeTeamName.isNotBlank() && ch.name.contains(match.homeTeamName, ignoreCase = true)) ||
            (match.displayHomeName.isNotBlank() && ch.name.contains(match.displayHomeName, ignoreCase = true)) ||
            (match.awayTeamName.isNotBlank() && ch.name.contains(match.awayTeamName, ignoreCase = true)) ||
            (match.displayAwayName.isNotBlank() && ch.name.contains(match.displayAwayName, ignoreCase = true))
        }
        if (matchedByTeam != null) {
            playChannelFullscreen(matchedByTeam)
            return
        }

        // 5. Fallback: If sports channels exist, play the first sports channel (e.g. beIN or SSC)
        val firstSportsChannel = allChannels.value.find {
            it.category.contains("رياض", ignoreCase = true) ||
            it.category.contains("Sport", ignoreCase = true) ||
            it.name.contains("beIN", ignoreCase = true) ||
            it.name.contains("SSC", ignoreCase = true)
        } ?: allChannels.value.firstOrNull()

        if (firstSportsChannel != null) {
            playChannelFullscreen(firstSportsChannel)
        }
    }

    fun pauseHomePlayer() {
        _isPlaying.value = false
    }

    fun stopLibraryPlayback() {
        _playingLibraryItem.value = null
        _isLibraryFullscreen.value = false
    }

    /**
     * Loads/refreshes actual content directly from all Xtreme server accounts at ultra-fast speeds,
     * excludes any live streams, deduplicates content so each movie/series appears once,
     * and saves the new content locally.
     */
    fun refreshLibraryContent(wipeOld: Boolean = false) {
        val accounts = xtremeAccounts.value.ifEmpty { ChannelRepository.DEFAULT_XTREME_ACCOUNTS }
        if (accounts.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLibraryLoading.value = true
            _isAccountUpdatedError.value = false

            if (wipeOld) {
                _libraryItems.value = emptyList()
            }

            // Parallel concurrent fetch across all accounts for ultra-fast downloading
            val results = coroutineScope {
                accounts.map { account ->
                    async {
                        try {
                            xtremeApiClient.fetchAccountContent(account)
                        } catch (e: Exception) {
                            Log.w("FootballViewModel", "Error fetching content for ${account.name}: ${e.message}")
                            emptyList<XtremeItem>()
                        }
                    }
                }.awaitAll()
            }

            val combinedList = ArrayList<XtremeItem>()
            for (list in results) {
                combinedList.addAll(list)
            }

            // Ensure no LIVE streams exist in library items
            val nonLiveItems = combinedList.filter { it.type != XtremeContentType.LIVE }
            val deduplicatedItems = com.example.data.util.LibraryDeduplicator.deduplicate(nonLiveItems)

            if (deduplicatedItems.isNotEmpty()) {
                allAccountsLibraryCache = deduplicatedItems
                // Save fresh content locally and store the new accounts signature
                val newSignature = cacheManager.computeAccountsSignature(accounts)
                cacheManager.saveCache(newSignature, deduplicatedItems)
                if (_isLibraryAllAccountsMode.value) {
                    _libraryItems.value = deduplicatedItems
                }
            } else {
                // Fallback to disk cache if network returned empty
                val cached = cacheManager.loadCachedItems()
                if (cached.isNotEmpty()) {
                    _libraryItems.value = cached
                }
            }
            _isAccountUpdatedError.value = false
            _isLibraryLoading.value = false
        }
    }

    fun playXtremeItem(item: XtremeItem) {
        // Create a temporary or virtual channel to play directly in top player
        val virtualServer = ChannelServer(
            id = item.id,
            name = when (item.type) {
                XtremeContentType.LIVE -> "Live Stream"
                XtremeContentType.MOVIE -> "Movie Stream"
                XtremeContentType.SERIES -> "Series Stream"
            },
            streamUrl = item.streamUrl,
            streamType = item.containerExtension
        )

        val virtualChannel = Channel(
            id = item.id,
            name = item.title,
            imageUrl = item.streamIcon,
            category = item.type.label,
            servers = listOf(virtualServer)
        )

        _selectedChannel.value = virtualChannel
        _selectedServer.value = virtualServer
        _isPlaying.value = true
    }

    fun playChannelFullscreen(channel: Channel) {
        selectChannel(channel)
        _isPlaying.value = true
        _isFullscreen.value = true
    }

    fun toggleFullscreen() {
        val next = !_isFullscreen.value
        _isFullscreen.value = next
        if (!next) {
            _isPlaying.value = false
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
        if (!fullscreen) {
            _isPlaying.value = false
        }
    }

    fun selectLibraryItem(item: XtremeItem?) {
        _selectedLibraryItem.value = item
        _seriesInfo.value = null
        _vodInfo.value = null
        
        if (item != null && item.type == com.example.data.model.XtremeContentType.SERIES) {
            _isSeriesInfoLoading.value = true
            viewModelScope.launch {
                val accounts = xtremeAccounts.value.ifEmpty { ChannelRepository.DEFAULT_XTREME_ACCOUNTS }
                val account = accounts.find { 
                    (item.accountId.isNotBlank() && it.id == item.accountId) || it.name == item.accountName 
                } ?: accounts.firstOrNull()
                if (account != null) {
                    val info = xtremeApiClient.getSeriesInfo(account, item.streamId)
                    _seriesInfo.value = info
                }
                _isSeriesInfoLoading.value = false
            }
        } else if (item != null && item.type == com.example.data.model.XtremeContentType.MOVIE) {
            _isVodInfoLoading.value = true
            viewModelScope.launch {
                val accounts = xtremeAccounts.value.ifEmpty { ChannelRepository.DEFAULT_XTREME_ACCOUNTS }
                val account = accounts.find { 
                    (item.accountId.isNotBlank() && it.id == item.accountId) || it.name == item.accountName 
                } ?: accounts.firstOrNull()
                if (account != null) {
                    val info = xtremeApiClient.getVodInfo(account, item.streamId)
                    _vodInfo.value = info
                }
                _isVodInfoLoading.value = false
            }
        }
    }

    fun playLibraryItem(item: XtremeItem?) {
        if (item != null) {
            // Pause Home player when starting library video
            _isPlaying.value = false
        }
        _playingLibraryItem.value = item
        _isLibraryFullscreen.value = (item != null)
    }

    fun playEpisode(episode: com.example.data.model.XtremeEpisode, series: XtremeItem) {
        val epItem = XtremeItem(
            id = episode.id,
            title = "${series.title} - ${episode.title}",
            streamId = episode.streamId,
            streamIcon = episode.streamIcon.ifBlank { series.streamIcon },
            type = com.example.data.model.XtremeContentType.SERIES,
            categoryId = series.categoryId,
            categoryName = series.categoryName,
            streamUrl = episode.streamUrl,
            accountName = series.accountName,
            accountId = series.accountId,
            containerExtension = episode.containerExtension
        )
        playLibraryItem(epItem)
    }

    fun setLibraryFullscreen(fullscreen: Boolean) {
        _isLibraryFullscreen.value = fullscreen
    }

    fun toggleLibraryFullscreen() {
        _isLibraryFullscreen.value = !_isLibraryFullscreen.value
    }

    fun setScaleMode(mode: com.example.ui.player.VideoScaleMode) {
        _scaleMode.value = mode
    }

    fun toggleNextScaleMode() {
        val modes = com.example.ui.player.VideoScaleMode.values()
        val nextIndex = (_scaleMode.value.ordinal + 1) % modes.size
        _scaleMode.value = modes[nextIndex]
    }

    private var lastViewedChannelId: String? = null
    private var lastViewIncrementTime: Long = 0L

    fun selectChannel(channel: Channel) {
        // Stop any library playback when switching to live channel
        _playingLibraryItem.value = null
        _isLibraryFullscreen.value = false

        _selectedChannel.value = channel
        _chatChannel.value = channel
        _selectedServer.value = channel.servers.firstOrNull()
        _isPlaying.value = true
        presenceRepository.updateGlobalPresenceChannel(channel.name)

        // 100% genuine real-time view tracking in Firebase for published channels only
        // M3U channels, Xtreme items, and matches do NOT write to database
        val isFirebaseChannel = channels.value.any { it.id == channel.id } &&
                !channel.id.startsWith("m3u_") &&
                !channel.id.startsWith("xtreme_") &&
                !channel.id.startsWith("espn_")

        if (isFirebaseChannel) {
            val now = System.currentTimeMillis()
            if (channel.id != lastViewedChannelId || (now - lastViewIncrementTime) > 15_000L) {
                lastViewedChannelId = channel.id
                lastViewIncrementTime = now
                repository.incrementChannelViews(channel.id)
            }
        }
    }

    fun selectChatChannel(channel: Channel, playAlso: Boolean = true) {
        _chatChannel.value = channel
        if (playAlso) {
            selectChannel(channel)
        }
    }

    fun selectServer(server: ChannelServer) {
        _selectedServer.value = server
        _isPlaying.value = true
    }

    fun selectNextChannel() {
        val list = channels.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _selectedChannel.value?.id }
        val nextIndex = if (currentIndex in 0 until list.size - 1) currentIndex + 1 else 0
        selectChannel(list[nextIndex])
    }

    fun selectPreviousChannel() {
        val list = channels.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == _selectedChannel.value?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        selectChannel(list[prevIndex])
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    private val _developerInitialTab = MutableStateFlow(0)
    val developerInitialTab: StateFlow<Int> = _developerInitialTab.asStateFlow()

    fun openDeveloperDialog(channelToEdit: Channel? = null, initialTab: Int = 0) {
        if (IS_USER_RELEASE_BUILD) {
            // Strict Security Lockdown: Developer panel is completely disabled in user release
            _isDeveloperDialogOpen.value = false
            return
        }
        _editingChannel.value = channelToEdit
        _developerInitialTab.value = initialTab
        _publishUiState.value = PublishUiState.Idle
        _isDeveloperDialogOpen.value = true
    }

    fun closeDeveloperDialog() {
        _isDeveloperDialogOpen.value = false
        _editingChannel.value = null
        _developerInitialTab.value = 0
        _publishUiState.value = PublishUiState.Idle
    }

    fun setEditingChannel(channel: Channel?) {
        _editingChannel.value = channel
    }

    fun clearPublishState() {
        _publishUiState.value = PublishUiState.Idle
    }

    // Channel Publishing
    fun publishOrUpdateCategory(category: ChannelCategory, isUpdate: Boolean = false) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            // If new category, place it at the end of the current order list
            val categoryWithOrder = if (!isUpdate && category.order == 0 && categories.value.isNotEmpty()) {
                category.copy(order = categories.value.size)
            } else {
                category
            }
            val result = repository.saveCategory(categoryWithOrder)
            result.fold(
                onSuccess = {
                    val msg = if (isUpdate) "تم تحديث التصنيف بنجاح في Firebase" else "تم نشر التصنيف بنجاح في Firebase"
                    _publishUiState.value = PublishUiState.Success(msg)
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء الاتصال بقاعدة البيانات"
                    )
                }
            )
        }
    }

    fun publishCategoryWithChannels(category: ChannelCategory, channelsToPublish: List<Channel>) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val categoryWithOrder = if (category.order == 0 && categories.value.isNotEmpty()) {
                category.copy(order = categories.value.size)
            } else {
                category
            }
            val catResult = repository.saveCategory(categoryWithOrder)
            if (channelsToPublish.isNotEmpty()) {
                val chResult = repository.saveChannels(channelsToPublish)
                if (chResult.isSuccess && catResult.isSuccess) {
                    _publishUiState.value = PublishUiState.Success("تم نشر التصنيف مع ${channelsToPublish.size} قناة بنجاح في Firebase")
                } else if (catResult.isSuccess) {
                    _publishUiState.value = PublishUiState.Success("تم نشر التصنيف بنجاح")
                } else {
                    _publishUiState.value = PublishUiState.Error("حدث خطأ أثناء حفظ التصنيف أو القنوات")
                }
            } else {
                catResult.fold(
                    onSuccess = { _publishUiState.value = PublishUiState.Success("تم نشر التصنيف بنجاح") },
                    onFailure = { _publishUiState.value = PublishUiState.Error(it.message ?: "فشل حفظ التصنيف") }
                )
            }
        }
    }

    fun reorderCategories(newOrderedList: List<ChannelCategory>) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.updateCategoriesOrder(newOrderedList)
            result.fold(
                onSuccess = {
                    _publishUiState.value = PublishUiState.Success("تم تحديث ترتيب التصنيفات وقنواتها بنجاح")
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء تحديث ترتيب التصنيفات"
                    )
                }
            )
        }
    }

    /**
     * Moves a category to the absolute first position (#1).
     * Channels belonging to this category will now appear FIRST in the "الكل" section.
     */
    fun moveCategoryToTop(categoryId: String) {
        val current = categories.value.toMutableList()
        val index = current.indexOfFirst { it.id == categoryId }
        if (index > 0) {
            val cat = current.removeAt(index)
            current.add(0, cat)
            reorderCategories(current)
        }
    }

    /**
     * Moves a category to the second position (#2, directly under the first category).
     * Channels belonging to this category will appear at the top, directly under the first category's channels.
     */
    fun moveCategoryToSecond(categoryId: String) {
        val current = categories.value.toMutableList()
        val index = current.indexOfFirst { it.id == categoryId }
        if (index != -1 && current.size >= 2) {
            val cat = current.removeAt(index)
            current.add(1, cat)
            reorderCategories(current)
        }
    }

    /**
     * Moves a category up one position.
     */
    fun moveCategoryUp(categoryId: String) {
        val current = categories.value.toMutableList()
        val index = current.indexOfFirst { it.id == categoryId }
        if (index > 0) {
            val cat = current.removeAt(index)
            current.add(index - 1, cat)
            reorderCategories(current)
        }
    }

    /**
     * Moves a category down one position.
     */
    fun moveCategoryDown(categoryId: String) {
        val current = categories.value.toMutableList()
        val index = current.indexOfFirst { it.id == categoryId }
        if (index in 0 until current.size - 1) {
            val cat = current.removeAt(index)
            current.add(index + 1, cat)
            reorderCategories(current)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    fun publishOrUpdateBranch(branch: ChannelBranch, isUpdate: Boolean = false) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val branchWithOrder = if (!isUpdate && branch.order == 0 && branches.value.isNotEmpty()) {
                branch.copy(order = branches.value.size)
            } else {
                branch
            }
            val result = repository.saveBranch(branchWithOrder)
            result.fold(
                onSuccess = {
                    val msg = if (isUpdate) "تم تحديث بيانات الفرع بنجاح" else "تم نشر الفرع بنجاح داخل التصنيف"
                    _publishUiState.value = PublishUiState.Success(msg)
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء حفظ الفرع"
                    )
                }
            )
        }
    }

    fun deleteBranch(branchId: String) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.deleteBranch(branchId)
            result.fold(
                onSuccess = {
                    _publishUiState.value = PublishUiState.Success("تم حذف الفرع بنجاح")
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء حذف الفرع"
                    )
                }
            )
        }
    }

    fun addChannelsToBranch(branchId: String, channelIds: List<String>, branchName: String = "") {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.addChannelsToBranch(branchId, channelIds, branchName)
            result.fold(
                onSuccess = {
                    _publishUiState.value = PublishUiState.Success("تم إضافة القنوات إلى الفرع بنجاح")
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء إضافة القنوات إلى الفرع"
                    )
                }
            )
        }
    }

    fun removeChannelFromBranch(branchId: String, channelId: String) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.removeChannelFromBranch(branchId, channelId)
            result.fold(
                onSuccess = {
                    _publishUiState.value = PublishUiState.Success("تم إزالة القناة من الفرع بنجاح")
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء إزالة القناة من الفرع"
                    )
                }
            )
        }
    }

    fun publishOrUpdateChannel(channel: Channel, isUpdate: Boolean) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.saveChannel(channel)
            result.fold(
                onSuccess = {
                    val msg = if (isUpdate) "تم تحديث معلومات القناة بنجاح في Firebase" else "تم نشر القناة بنجاح في Firebase"
                    _publishUiState.value = PublishUiState.Success(msg)
                    if (_selectedChannel.value?.id == channel.id) {
                        _selectedChannel.value = channel
                        val currentServerStillExists = channel.servers.any { it.id == _selectedServer.value?.id }
                        if (!currentServerStillExists) {
                            _selectedServer.value = channel.servers.firstOrNull()
                        }
                    }
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء الاتصال بقاعدة البيانات"
                    )
                }
            )
        }
    }

    fun deleteChannel(channelId: String) {
        viewModelScope.launch {
            repository.deleteChannel(channelId)
            if (_selectedChannel.value?.id == channelId) {
                _selectedChannel.value = null
                _selectedServer.value = null
            }
        }
    }

    fun deleteAllChannels() {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.deleteAllChannels()
            result.fold(
                onSuccess = {
                    _selectedChannel.value = null
                    _selectedServer.value = null
                    _publishUiState.value = PublishUiState.Success("تم حذف جميع القنوات المنشورة بنجاح من قاعدة البيانات")
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(error.localizedMessage ?: "حدث خطأ أثناء حذف القنوات")
                }
            )
        }
    }

    // Xtreme Accounts Publishing
    fun publishOrUpdateXtremeAccount(account: XtremeAccount) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.saveXtremeAccount(account)
            result.fold(
                onSuccess = {
                    _publishUiState.value = PublishUiState.Success("تم حفظ ونشر حساب IPTV بنجاح في Firebase")
                    _selectedIptvAccount.value = account
                    loadIptvChannels(account)
                    refreshLibraryContent()
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء حفظ حساب IPTV"
                    )
                }
            )
        }
    }

    fun deleteXtremeAccount(accountId: String) {
        viewModelScope.launch {
            repository.deleteXtremeAccount(accountId)
            accountLibraryCache.remove(accountId)
            if (_selectedIptvAccount.value?.id == accountId) {
                _selectedIptvAccount.value = null
                _iptvLiveChannels.value = emptyList()
            }
            if (_selectedLibraryAccount.value?.id == accountId) {
                _selectedLibraryAccount.value = null
                _isLibraryAllAccountsMode.value = true
            }
            refreshLibraryContent()
        }
    }

    fun deleteAllXtremeAccounts() {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.deleteAllXtremeAccounts()
            result.fold(
                onSuccess = {
                    _publishUiState.value = PublishUiState.Success("تم حذف جميع الحسابات السابقة بنجاح من قاعدة البيانات")
                    _selectedIptvAccount.value = null
                    _selectedLibraryAccount.value = null
                    _iptvLiveChannels.value = emptyList()
                    _libraryItems.value = emptyList()
                    accountLibraryCache.clear()
                    allAccountsLibraryCache = emptyList()
                    cacheManager.clearCache()
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء حذف جميع الحسابات"
                    )
                }
            )
        }
    }

    // Event Banner Actions
    fun publishOrUpdateEventBanner(banner: com.example.data.model.EventBanner) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.saveEventBanner(banner)
            result.fold(
                onSuccess = {
                    _publishUiState.value = PublishUiState.Success("تم نشر الحدث بنجاح في التطبيق")
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء نشر الحدث"
                    )
                }
            )
        }
    }

    fun deleteEventBanner(eventId: String) {
        viewModelScope.launch {
            _publishUiState.value = PublishUiState.Loading
            val result = repository.deleteEventBanner(eventId)
            result.fold(
                onSuccess = {
                    _publishUiState.value = PublishUiState.Success("تم حذف الحدث بنجاح")
                },
                onFailure = { error ->
                    _publishUiState.value = PublishUiState.Error(
                        error.localizedMessage ?: "حدث خطأ أثناء حذف الحدث"
                    )
                }
            )
        }
    }

    // Chat Actions
    fun openChat(channel: Channel? = null) {
        if (channel != null) {
            _chatChannel.value = channel
            selectChannel(channel)
        } else if (_chatChannel.value == null) {
            _chatChannel.value = _selectedChannel.value ?: allChannels.value.firstOrNull()
        }
        _isChatOpen.value = true
        _unreadChatCount.value = 0
        lastSeenMessageId = chatMessages.value.lastOrNull()?.id
    }

    fun closeChat() {
        _isChatOpen.value = false
        lastSeenMessageId = chatMessages.value.lastOrNull()?.id
    }

    fun toggleChat() {
        if (!_isChatOpen.value) {
            if (_chatChannel.value == null) {
                _chatChannel.value = _selectedChannel.value ?: allChannels.value.firstOrNull()
            }
            _isChatOpen.value = true
            _unreadChatCount.value = 0
            lastSeenMessageId = chatMessages.value.lastOrNull()?.id
        } else {
            _isChatOpen.value = false
            lastSeenMessageId = chatMessages.value.lastOrNull()?.id
        }
    }

    fun toggleLandscapeChat() {
        _isLandscapeChatOpen.value = !_isLandscapeChatOpen.value
        if (_isLandscapeChatOpen.value) {
            _unreadChatCount.value = 0
            lastSeenMessageId = chatMessages.value.lastOrNull()?.id
        } else {
            lastSeenMessageId = chatMessages.value.lastOrNull()?.id
        }
    }

    fun openLandscapeChat() {
        _isLandscapeChatOpen.value = true
        _unreadChatCount.value = 0
        lastSeenMessageId = chatMessages.value.lastOrNull()?.id
    }

    fun closeLandscapeChat() {
        _isLandscapeChatOpen.value = false
        lastSeenMessageId = chatMessages.value.lastOrNull()?.id
    }

    fun openChatAuthDialog() {
        _isChatAuthDialogOpen.value = true
    }

    fun closeChatAuthDialog() {
        _isChatAuthDialogOpen.value = false
    }

    fun sendChatMessage(text: String, replyTo: com.example.data.model.ChatMessage?) {
        val user = _currentChatUser.value ?: chatRepository.getOrCreateGuestUser().also { _currentChatUser.value = it }
        if (text.isBlank()) return
        if (com.example.data.security.AppSecurityGuard.isMaliciousText(text)) {
            android.util.Log.w("AppSecurityGuard", "Blocked abusive chat message sending attempt")
            return
        }
        val currentChannel = activeChatChannel.value
        val channelId = currentChannel?.id ?: "general"
        val msg = com.example.data.model.ChatMessage(
            channelId = channelId,
            text = text.trim(),
            authorFullName = user.fullName,
            authorUsername = user.username,
            replyToId = replyTo?.id,
            replyToAuthor = replyTo?.authorFullName,
            replyToText = replyTo?.text
        )
        viewModelScope.launch {
            chatRepository.sendMessage(channelId, msg)
        }
    }

    fun deleteChatMessage(messageId: String) {
        val currentChannel = activeChatChannel.value
        val channelId = currentChannel?.id ?: "general"
        viewModelScope.launch {
            chatRepository.deleteMessage(channelId, messageId)
        }
    }

    fun toggleChatReaction(messageId: String, emoji: String) {
        val user = _currentChatUser.value ?: chatRepository.getOrCreateGuestUser().also { _currentChatUser.value = it }
        val currentChannel = activeChatChannel.value
        val channelId = currentChannel?.id ?: "general"
        viewModelScope.launch {
            chatRepository.toggleReaction(channelId, messageId, emoji, user.username)
        }
    }

    fun registerChatUser(user: com.example.data.model.ChatUser, onResult: (Result<com.example.data.model.ChatUser>) -> Unit) {
        viewModelScope.launch {
            val result = chatRepository.registerUser(user)
            if (result.isSuccess) {
                _currentChatUser.value = result.getOrNull()
            }
            onResult(result)
        }
    }

    fun loginChatUser(username: String, password: String, onResult: (Result<com.example.data.model.ChatUser>) -> Unit) {
        viewModelScope.launch {
            val result = chatRepository.loginUser(username, password)
            if (result.isSuccess) {
                _currentChatUser.value = result.getOrNull()
            }
            onResult(result)
        }
    }

    fun logoutChatUser() {
        // Chat Actions
        chatRepository.clearSavedUser()
        _currentChatUser.value = null
    }

    fun setHideHomeAll(value: Boolean) {
        _hideHomeAll.value = value
        prefs.edit().putBoolean("hide_home_all", value).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("hide_home_all", value)
        }
    }

    fun setHideHomeMode(value: String) {
        _hideHomeMode.value = value
        prefs.edit().putString("hide_home_mode", value).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("hide_home_mode", value)
        }
    }

    fun setHideHomeCategory(value: String) {
        _hideHomeCategory.value = value
        prefs.edit().putString("hide_home_category", value).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("hide_home_category", value)
        }
    }

    fun setHideChatInPlayer(value: Boolean) {
        _hideChatInPlayer.value = value
        prefs.edit().putBoolean("hide_chat_in_player", value).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("hide_chat_in_player", value)
        }
    }

    fun setHideOnlineViewers(value: Boolean) {
        _hideOnlineViewers.value = value
        prefs.edit().putBoolean("hide_online_viewers", value).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("hide_online_viewers", value)
        }
    }

    fun setHideMatchesSchedule(value: Boolean) {
        _hideMatchesSchedule.value = value
        prefs.edit().putBoolean("hide_matches_schedule", value).apply()
        if (value && _currentNavIndex.value == 4) {
            _currentNavIndex.value = 0
        }
        viewModelScope.launch {
            repository.saveAppConfigValue("hide_matches_schedule", value)
        }
    }

    fun setHideFoulTv(value: Boolean) {
        _hideFoulTv.value = value
        prefs.edit().putBoolean("hide_foul_tv", value).apply()
        if (value && _currentNavIndex.value == 2) {
            _currentNavIndex.value = 0
        }
        viewModelScope.launch {
            repository.saveAppConfigValue("hide_foul_tv", value)
        }
    }

    fun setLockOldVersions(value: Boolean) {
        _lockOldVersions.value = value
        prefs.edit().putBoolean("lock_old_versions", value).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("lock_old_versions", value)
        }
    }

    fun setLatestVersion(value: String) {
        val normalized = normalizeVersion(value)
        _latestVersion.value = normalized
        prefs.edit().putString("latest_version", normalized).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("latest_version", normalized)
        }
    }

    fun setDeveloperPassword(value: String) {
        val trimmed = value.trim()
        if (trimmed.isNotBlank() && trimmed != "2008") {
            _developerPassword.value = trimmed
            prefs.edit().putString("developer_password", trimmed).apply()
            viewModelScope.launch {
                repository.saveAppConfigValue("developer_password", trimmed)
            }
        }
    }

    /**
     * Changes app version, wipes old locks, and sets new version cleanly without issues.
     */
    fun changeVersionAndClearOld(newVersion: String, updateMode: String = "none", updateMsg: String = "", url: String = "") {
        val cleanVer = normalizeVersion(newVersion)
        _latestVersion.value = cleanVer
        prefs.edit().putString("latest_version", cleanVer).apply()
        _updateType.value = updateMode
        prefs.edit().putString("update_type", updateMode).apply()
        _updateText.value = updateMsg
        prefs.edit().putString("update_text", updateMsg).apply()
        _lockOldVersions.value = false
        prefs.edit().putBoolean("lock_old_versions", false).apply()
        if (url.isNotBlank()) {
            _updateUrl.value = url
            prefs.edit().putString("update_url", url).apply()
        }

        viewModelScope.launch {
            repository.saveAppConfigValue("latest_version", cleanVer)
            repository.saveAppConfigValue("accepted_version", cleanVer)
            repository.saveAppConfigValue("min_version", cleanVer)
            repository.saveAppConfigValue("lock_old_versions", false)
            repository.saveAppConfigValue("update_type", updateMode)
            repository.saveAppConfigValue("update_text", updateMsg)
            if (url.isNotBlank()) {
                repository.saveAppConfigValue("update_url", url)
            }
        }
    }

    /**
     * Smart publishing for any selected version:
     * Updates latestVersion, updates developer password to 2008 in Firebase,
     * and sets update status cleanly.
     */
    fun publishVersionSmartly(customText: String? = null, customUrl: String? = null, targetVersion: String? = null) {
        val ver = targetVersion?.let { normalizeVersion(it) } ?: _latestVersion.value.ifBlank { CURRENT_APP_VERSION }
        val text = if (!customText.isNullOrBlank()) customText else "تم اعتماد وإطلاق الإصدار $ver بنجاح."
        val url = if (!customUrl.isNullOrBlank()) customUrl else _updateUrl.value

        _latestVersion.value = ver
        prefs.edit().putString("latest_version", ver).apply()

        _lockOldVersions.value = false
        prefs.edit().putBoolean("lock_old_versions", false).apply()

        _updateType.value = "none"
        prefs.edit().putString("update_type", "none").apply()

        _updateText.value = text
        prefs.edit().putString("update_text", text).apply()

        if (url.isNotBlank()) {
            _updateUrl.value = url
            prefs.edit().putString("update_url", url).apply()
        }

        viewModelScope.launch {
            repository.saveAppConfigValue("latest_version", ver)
            repository.saveAppConfigValue("accepted_version", ver)
            repository.saveAppConfigValue("min_version", ver)
            repository.saveAppConfigValue("lock_old_versions", false)
            repository.saveAppConfigValue("update_type", "none")
            repository.saveAppConfigValue("update_text", text)
            repository.saveAppConfigValue("developer_password", _developerPassword.value.ifBlank { "2812246216200810" })
            if (url.isNotBlank()) {
                repository.saveAppConfigValue("update_url", url)
            }
        }
    }

    fun publishVersion3Smartly(customText: String? = null, customUrl: String? = null) {
        publishVersionSmartly(customText, customUrl)
    }

    fun setUpdateType(value: String) {
        _updateType.value = value
        prefs.edit().putString("update_type", value).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("update_type", value)
        }
    }

    fun setUpdateText(value: String) {
        _updateText.value = value
        prefs.edit().putString("update_text", value).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("update_text", value)
        }
    }

    fun setUpdateUrl(value: String) {
        _updateUrl.value = value
        prefs.edit().putString("update_url", value).apply()
        viewModelScope.launch {
            repository.saveAppConfigValue("update_url", value)
        }
    }

    fun deleteAllChatMessages(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val res = chatRepository.deleteAllMessages()
            onResult(res)
        }
    }

    override fun onCleared() {
        super.onCleared()
        presenceJob?.cancel()
        presenceRepository.stopWatchingCurrentChannel()
    }

    companion object {
        const val CURRENT_APP_VERSION = "4.0.0"

        /**
         * Developer Mode Enabled:
         * Developer panel is accessible with secure password 2812246216200810.
         * Set to true when switching to user release mode.
         */
        const val IS_USER_RELEASE_BUILD = false

        /**
         * Normalizes any version string (e.g. "v4.0.0", "4.0.0", "4.0")
         */
        fun normalizeVersion(raw: String): String {
            val cleaned = raw.trim().removePrefix("v").removePrefix("V").trim()
            if (cleaned.isBlank()) return CURRENT_APP_VERSION
            return cleaned
        }

        /**
         * Checks whether a given version string is accepted.
         * Accepts 4.0.0, 3.5.0, 1.0.0, 3.0.1 or the current app version.
         */
        fun isAcceptedVersion(version: String): Boolean {
            val normalized = normalizeVersion(version)
            return normalized == CURRENT_APP_VERSION || normalized == "4.0.0" || normalized == "3.5.0" || normalized == "1.0.0" || normalized == "3.0.1"
        }

        /**
         * Semantic version comparison.
         * Returns true if current is older than target.
         */
        fun isVersionOlder(current: String, target: String): Boolean {
            try {
                val currParts = current.trim().removePrefix("v").removePrefix("V").split(".").map { it.toIntOrNull() ?: 0 }
                val targParts = target.trim().removePrefix("v").removePrefix("V").split(".").map { it.toIntOrNull() ?: 0 }
                val maxLen = maxOf(currParts.size, targParts.size)
                for (i in 0 until maxLen) {
                    val c = currParts.getOrElse(i) { 0 }
                    val t = targParts.getOrElse(i) { 0 }
                    if (c < t) return true
                    if (c > t) return false
                }
            } catch (_: Exception) {}
            return false
        }
    }
}
