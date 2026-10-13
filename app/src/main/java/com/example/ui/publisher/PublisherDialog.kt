package com.example.ui.publisher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Channel
import com.example.data.model.ChannelServer
import com.example.data.model.ChannelBranch
import com.example.data.model.ChannelCategory
import com.example.data.model.StreamType
import com.example.data.repository.ChannelRepository
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
import com.example.data.model.XtremeAccount
import com.example.ui.theme.VividViolet
import com.example.ui.viewmodel.PublishUiState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublisherDialog(
    isOpen: Boolean,
    editingChannel: Channel?,
    channels: List<Channel>,
    categories: List<ChannelCategory> = emptyList(),
    branches: List<ChannelBranch> = emptyList(),
    xtremeAccounts: List<XtremeAccount> = emptyList(),
    publishUiState: PublishUiState,
    onDismiss: () -> Unit,
    onSaveChannel: (Channel, Boolean) -> Unit,
    onDeleteChannel: (String) -> Unit,
    onDeleteAllChannels: () -> Unit = {},
    onSaveCategory: (ChannelCategory, Boolean) -> Unit = { _, _ -> },
    onSaveCategoryWithChannels: ((ChannelCategory, List<Channel>) -> Unit)? = null,
    onDeleteCategory: (String) -> Unit = {},
    onMoveCategoryToTop: (String) -> Unit = {},
    onMoveCategoryToSecond: (String) -> Unit = {},
    onMoveCategoryUp: (String) -> Unit = {},
    onMoveCategoryDown: (String) -> Unit = {},
    onSaveBranch: (ChannelBranch, Boolean) -> Unit = { _, _ -> },
    onDeleteBranch: (String) -> Unit = {},
    onAddChannelsToBranch: (String, List<String>, String) -> Unit = { _, _, _ -> },
    onRemoveChannelFromBranch: (String, String) -> Unit = { _, _ -> },
    initialTab: Int = 0,
    onSaveXtremeAccount: (XtremeAccount) -> Unit = {},
    onDeleteXtremeAccount: (String) -> Unit = {},
    onDeleteAllXtremeAccounts: () -> Unit = {},
    onPlayChannel: (Channel) -> Unit,
    onClearPublishState: () -> Unit,
    hideHomeAll: Boolean = false,
    hideHomeMode: String = "all",
    hideHomeCategory: String = "",
    hideChatInPlayer: Boolean = false,
    hideOnlineViewers: Boolean = false,
    hideMatchesSchedule: Boolean = false,
    hideFoulTv: Boolean = false,
    lockOldVersions: Boolean = false,
    appVersion: String = "1.0.0",
    latestVersion: String = "1.0.0",
    updateType: String = "none",
    updateText: String = "",
    updateUrl: String = "",
    onSetHideHomeAll: (Boolean) -> Unit = {},
    onSetHideHomeMode: (String) -> Unit = {},
    onSetHideHomeCategory: (String) -> Unit = {},
    onSetHideChatInPlayer: (Boolean) -> Unit = {},
    onSetHideOnlineViewers: (Boolean) -> Unit = {},
    onSetHideMatchesSchedule: (Boolean) -> Unit = {},
    onSetHideFoulTv: (Boolean) -> Unit = {},
    onSetLockOldVersions: (Boolean) -> Unit = {},
    onSetLatestVersion: (String) -> Unit = {},
    onSetUpdateType: (String) -> Unit = {},
    onSetUpdateText: (String) -> Unit = {},
    onSetUpdateUrl: (String) -> Unit = {},
    onPublishVersion3Smartly: ((String, String) -> Unit)? = null,
    developerPassword: String = "2812246216200810",
    onSetDeveloperPassword: (String) -> Unit = {},
    onDeleteAllMessages: ((Result<Unit>) -> Unit) -> Unit = {},
    eventBanners: List<com.example.data.model.EventBanner> = emptyList(),
    onSaveEventBanner: (com.example.data.model.EventBanner) -> Unit = {},
    onDeleteEventBanner: (String) -> Unit = {},
    globalOnlineUsers: List<com.example.data.repository.OnlineUserSession> = emptyList(),
    globalOnlineCount: Int = 1,
    foulVideos: List<com.example.data.model.FoulVideo> = emptyList(),
    foulGlobalConfig: com.example.data.model.FoulTvGlobalConfig = com.example.data.model.FoulTvGlobalConfig(),
    onSaveFoulVideo: (com.example.data.model.FoulVideo, Boolean) -> Unit = { _, _ -> },
    onDeleteFoulVideo: (String) -> Unit = {},
    onUpdateFoulVideoSettings: (String, Boolean, Boolean, Boolean) -> Unit = { _, _, _, _ -> },
    onUpdateFoulGlobalConfig: (Boolean, Boolean, Boolean) -> Unit = { _, _, _ -> },
    onOpenPublishFoulVideoDialog: ((com.example.data.model.FoulVideo?) -> Unit)? = null
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    var showReorderDialog by remember { mutableStateOf(false) }
    var showDeleteAllChannelsConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAllXtremeAccountsConfirmDialog by remember { mutableStateOf(false) }

    // Filter genuine manually-published channels (excluding M3U & IPTV channels)
    val m3uCategoryNames = remember(categories) {
        categories.filter { it.m3uUrl.isNotBlank() }.map { it.name.trim().lowercase() }.toSet()
    }
    val genuineChannels = remember(channels, m3uCategoryNames) {
        channels.filter { ch ->
            ch.name.isNotBlank() &&
                    !ch.id.startsWith("m3u_") &&
                    !ch.id.startsWith("iptv_") &&
                    !ch.id.startsWith("ch_") &&
                    !m3uCategoryNames.contains(ch.category.trim().lowercase())
        }
    }

    // Event Banner Form State
    var eventTitle by remember { mutableStateOf("") }
    var eventImageUrl by remember { mutableStateOf("") }
    var showWatchNowIcon by remember { mutableStateOf(true) }
    var eventWatchUrl by remember { mutableStateOf("") }
    val selectedEventChannels = remember { mutableStateListOf<Channel>() }
    var showChannelPickerSheet by remember { mutableStateOf(false) }
    var channelSearchFilter by remember { mutableStateOf("") }

    // Xtreme / IPTV Account Form State
    var xtremeAccountName by remember { mutableStateOf("") }
    var xtremeHost by remember { mutableStateOf("") }
    var xtremeUsername by remember { mutableStateOf("") }
    var xtremePassword by remember { mutableStateOf("") }
    var xtremeStreamExtension by remember { mutableStateOf("m3u8") }
    var editingXtremeAccount by remember { mutableStateOf<XtremeAccount?>(null) }
    var xtremeErrorMsg by remember { mutableStateOf<String?>(null) }

    // Category Form State
    var categoryName by remember { mutableStateOf("") }
    var categoryImageUrl by remember { mutableStateOf("") }
    var categoryM3uUrl by remember { mutableStateOf("") }
    var categoryErrorMsg by remember { mutableStateOf<String?>(null) }

    // Channel Form State
    var activeEditingChannel by remember { mutableStateOf<Channel?>(editingChannel) }
    var channelName by remember { mutableStateOf("") }
    var channelImageUrl by remember { mutableStateOf("") }
    var channelCategory by remember { mutableStateOf("") }
    var channelBranchId by remember { mutableStateOf("") }
    var channelBranchName by remember { mutableStateOf("") }
    val serverList = remember { mutableStateListOf<ChannelServer>() }

    // Server Sub-form State
    var serverNameInput by remember { mutableStateOf("SD") }
    var serverUrlInput by remember { mutableStateOf("") }
    var serverTypeSelected by remember { mutableStateOf("m3u8") } // "m3u8", "ts", "normal"
    var serverError by remember { mutableStateOf<String?>(null) }
    var formValidationMessage by remember { mutableStateOf<String?>(null) }

    // Server editing dialog states
    var editingServerItem by remember { mutableStateOf<Pair<Int, ChannelServer>?>(null) }
    var quickEditChannelServers by remember { mutableStateOf<Channel?>(null) }

    // Sync editing channel when opened
    LaunchedEffect(editingChannel) {
        activeEditingChannel = editingChannel
        if (editingChannel != null) {
            channelName = editingChannel.name
            channelImageUrl = editingChannel.imageUrl
            channelCategory = editingChannel.category
            channelBranchId = editingChannel.branchId
            channelBranchName = editingChannel.branchName
            serverList.clear()
            serverList.addAll(editingChannel.servers)
            selectedTab = 0
        } else {
            channelName = ""
            channelImageUrl = ""
            channelCategory = ""
            channelBranchId = ""
            channelBranchName = ""
            serverList.clear()
            serverNameInput = "SD"
            serverUrlInput = ""
            serverTypeSelected = "m3u8"
        }
        formValidationMessage = null
        onClearPublishState()
    }

    val availableBranchesForCategory = remember(branches, channelCategory) {
        if (channelCategory.isBlank()) {
            branches
        } else {
            branches.filter {
                it.categoryName.trim().equals(channelCategory.trim(), ignoreCase = true) ||
                it.categoryId.trim().equals(channelCategory.trim(), ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.75f),
        dragHandle = null,
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .testTag("publisher_modal_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkSurface)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkPurpleContainer, CircleShape)
                            .border(1.dp, NeonPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Developer Settings",
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "إعدادات المطور والنشر",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SuccessGreen, CircleShape)
                                )
                                Text(
                                    text = "قاعدة Firebase متصلة",
                                    color = SuccessGreen,
                                    fontSize = 10.sp
                                )
                            }

                            // Small online users counter badge (عداد المتواجدين الآن)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SuccessGreen.copy(alpha = 0.15f),
                                border = BorderStroke(0.8.dp, SuccessGreen.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable { selectedTab = 6 }
                                    .testTag("dev_header_online_counter_badge")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(SuccessGreen, CircleShape)
                                    )
                                    Text(
                                        text = "$globalOnlineCount متصل الآن 🟢",
                                        color = SuccessGreen,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (categories.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showReorderDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
                            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تعديل ظهور التصنيف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_publisher_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceVariant,
                contentColor = ElectricPurple,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElectricPurple
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = if (activeEditingChannel != null) "تعديل القناة" else "نشر قناة جديدة",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "نشر تصنيف",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "نشر فرع جديد (${branches.size})",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            text = "المنشورات (${genuineChannels.size})",
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 3) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = {
                        Text(
                            text = "حسابات المكتبة و IPTV (${xtremeAccounts.size})",
                            fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 4) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = {
                        Text(
                            text = "قاعدة البيانات",
                            fontWeight = if (selectedTab == 5) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 5) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 },
                    text = {
                        Text(
                            text = "تحكم في التطبيق",
                            fontWeight = if (selectedTab == 6) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 6) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 7,
                    onClick = { selectedTab = 7 },
                    text = {
                        Text(
                            text = "نشر حدث (${eventBanners.size})",
                            fontWeight = if (selectedTab == 7) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 7) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 8,
                    onClick = { selectedTab = 8 },
                    text = {
                        Text(
                            text = "إدارة FOUL TV (${foulVideos.size})",
                            fontWeight = if (selectedTab == 8) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 8) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                )
            }

            // Firebase Auth Admin Authentication Status Indicator
            val currentAdminUser by com.example.data.security.AdminAuthManager.currentAdmin.collectAsState()
            val isAdminAuthenticated = (currentAdminUser != null)
            Surface(
                color = if (isAdminAuthenticated) DarkBackground else LiveRed.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, if (isAdminAuthenticated) NeonPurple.copy(alpha = 0.5f) else LiveRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                                .size(10.dp)
                                .background(if (isAdminAuthenticated) SuccessGreen else LiveRed, CircleShape)
                        )
                        Column {
                            Text(
                                text = if (isAdminAuthenticated) "مصادقة المطور نشطة (Firebase Auth)" else "المصادقة غير نشطة: صلاحية القراءة فقط",
                                color = if (isAdminAuthenticated) TextPrimary else LiveRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAdminAuthenticated) (currentAdminUser?.email?.ifBlank { null } ?: "المعرف: ${currentAdminUser?.uid?.take(8)}") else "يجب تسجيل الدخول كإدمن لتنفيذ أي عملية نشر أو حذف",
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    if (isAdminAuthenticated) {
                        OutlinedButton(
                            onClick = { com.example.data.security.AdminAuthManager.signOut() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("خروج", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Status Banner (Success / Error)
            when (publishUiState) {
                is PublishUiState.Success -> {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SuccessGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Text(
                                text = publishUiState.message,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                is PublishUiState.Error -> {
                    Surface(
                        color = LiveRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, LiveRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = LiveRed)
                            Text(
                                text = publishUiState.message,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {}
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // TAB 0: PUBLISH / EDIT CHANNEL
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = if (activeEditingChannel != null) "تعديل معلومات القناة وسيرفراتها" else "إضافة قناة رياضية جديدة ونشرها في Firebase",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Channel Name Input
                        item {
                            OutlinedTextField(
                                value = channelName,
                                onValueChange = { channelName = it },
                                label = { Text("اسم القناة (مثال: beIN Sports 1 HD)") },
                                placeholder = { Text("أدخل اسم القناة") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("channel_name_input"),
                                colors = outlinedTextFieldColors(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Channel Image URL Input with Live Preview
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = channelImageUrl,
                                    onValueChange = { channelImageUrl = it },
                                    label = { Text("رابط صورة / شعار القناة") },
                                    placeholder = { Text("https://example.com/logo.png") },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.Link, contentDescription = null, tint = NeonPurple)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("channel_image_url_input"),
                                    colors = outlinedTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Image Preview if URL is entered
                                if (channelImageUrl.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(DarkBackground, RoundedCornerShape(10.dp))
                                            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                                            .padding(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(channelImageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Logo Preview",
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Text(
                                            text = "معاينة شعار القناة",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Category Selection (Mandatory)
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "تصنيف القناة (إجباري)",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (categories.isEmpty()) {
                                    Surface(
                                        color = LiveRed.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, LiveRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "لا توجد تصنيفات! يرجى الذهاب إلى تبويب 'نشر تصنيف' لإنشاء تصنيفات أولاً.",
                                            color = LiveRed,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                } else {
                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(categories, key = { it.id }) { cat ->
                                            FilterChip(
                                                selected = channelCategory == cat.name,
                                                onClick = { channelCategory = cat.name },
                                                label = { Text(cat.name) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = ElectricPurple,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Branch Selection (Optional - to nest channel inside a branch)
                        if (availableBranchesForCategory.isNotEmpty()) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "الفرع (اختياري - لنشر القناة داخل فرع)",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        item {
                                            FilterChip(
                                                selected = channelBranchId.isBlank(),
                                                onClick = {
                                                    channelBranchId = ""
                                                    channelBranchName = ""
                                                },
                                                label = { Text("بدون فرع (قناة مستقلة)") },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = ElectricPurple,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }

                                        items(availableBranchesForCategory, key = { it.id }) { branch ->
                                            FilterChip(
                                                selected = channelBranchId == branch.id,
                                                onClick = {
                                                    channelBranchId = branch.id
                                                    channelBranchName = branch.name
                                                },
                                                label = { Text(branch.name) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = NeonPurple,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // SECTION: SERVERS LIST & ADDER
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "إضافة سيرفرات البث للقناة",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = DarkPurpleContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${serverList.size} سيرفرات مضافة",
                                                color = BrightLavender,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    // Sub-fields for adding a server: Name & URL
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = serverNameInput,
                                            onValueChange = { serverNameInput = it },
                                            label = { Text("اسم السيرفر") },
                                            placeholder = { Text("مثلاً SD أو HD") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("server_name_input"),
                                            colors = outlinedTextFieldColors(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        // Quick Server Name Buttons (SD, HD, FHD)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        ) {
                                            listOf("SD", "HD", "FHD").forEach { namePreset ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (serverNameInput == namePreset) VividViolet else DarkBackground,
                                                    border = BorderStroke(1.dp, if (serverNameInput == namePreset) NeonPurple else DarkSurfaceBorder),
                                                    modifier = Modifier.clickable { serverNameInput = namePreset }
                                                ) {
                                                    Text(
                                                        text = namePreset,
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Stream URL Input
                                    OutlinedTextField(
                                        value = serverUrlInput,
                                        onValueChange = {
                                            serverUrlInput = it
                                            // Auto infer type if ends in m3u8 or ts
                                            if (it.contains(".m3u8", ignoreCase = true)) {
                                                serverTypeSelected = "m3u8"
                                            } else if (it.contains(".ts", ignoreCase = true)) {
                                                serverTypeSelected = "ts"
                                            }
                                        },
                                        label = { Text("رابط البث (Stream URL)") },
                                        placeholder = { Text("https://.../live.m3u8") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("server_url_input"),
                                        colors = outlinedTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    // Stream Type Selector (m3u8 / ts / normal)
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "نوع الامتداد لكل سيرفر:",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(
                                                StreamType.M3U8 to "m3u8",
                                                StreamType.TS to "ts",
                                                StreamType.NORMAL to "normal"
                                            ).forEach { (streamType, key) ->
                                                val isSelected = (serverTypeSelected == key)
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { serverTypeSelected = key },
                                                    label = {
                                                        Text(
                                                            text = streamType.title,
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = VividViolet,
                                                        selectedLabelColor = Color.White,
                                                        containerColor = DarkBackground,
                                                        labelColor = TextSecondary
                                                    ),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (isSelected) NeonPurple else DarkSurfaceBorder
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.testTag("stream_type_chip_$key")
                                                )
                                            }
                                        }
                                    }

                                    if (serverError != null) {
                                        Text(
                                            text = serverError ?: "",
                                            color = LiveRed,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Button: Save/Add Server to this Channel
                                    Button(
                                        onClick = {
                                            if (serverNameInput.isBlank()) {
                                                serverError = "يرجى كتابة اسم السيرفر (مثلاً SD أو HD)"
                                                return@Button
                                            }
                                            if (serverUrlInput.isBlank()) {
                                                serverError = "يرجى وضع رابط البث للسيرفر"
                                                return@Button
                                            }
                                            serverError = null
                                            serverList.add(
                                                ChannelServer(
                                                    id = UUID.randomUUID().toString(),
                                                    name = serverNameInput.trim(),
                                                    streamUrl = serverUrlInput.trim(),
                                                    streamType = serverTypeSelected
                                                )
                                            )
                                            // Reset sub-form for adding the next server
                                            serverNameInput = if (serverList.size == 1) "HD" else "سيرفر ${serverList.size + 1}"
                                            serverUrlInput = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("add_server_to_channel_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("حفظ السيرفر في قائمة القناة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Current Added Servers in Form
                                    if (serverList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "السيرفرات المحفوظة لهذه القناة:",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            serverList.forEachIndexed { index, srv ->
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = DarkBackground,
                                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Text(
                                                                    text = srv.name,
                                                                    color = TextPrimary,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                Surface(
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    color = DarkPurpleContainer
                                                                ) {
                                                                    Text(
                                                                        text = srv.streamType.uppercase(),
                                                                        color = NeonPurple,
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                                    )
                                                                }
                                                            }
                                                            Text(
                                                                text = srv.streamUrl,
                                                                color = TextMuted,
                                                                fontSize = 10.sp,
                                                                maxLines = 1
                                                            )
                                                        }

                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                        ) {
                                                            IconButton(
                                                                onClick = { editingServerItem = Pair(index, srv) },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Edit,
                                                                    contentDescription = "Edit server",
                                                                    tint = NeonPurple,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                            IconButton(
                                                                onClick = { serverList.removeAt(index) },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = "Delete server",
                                                                    tint = LiveRed.copy(alpha = 0.8f),
                                                                    modifier = Modifier.size(16.dp)
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

                        // Validation Message
                        if (formValidationMessage != null) {
                            item {
                                Text(
                                    text = formValidationMessage ?: "",
                                    color = LiveRed,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // MAIN ACTION: PUBLISH OR UPDATE TO FIREBASE
                        item {
                            val isUpdate = (activeEditingChannel != null)
                            val isLoading = (publishUiState is PublishUiState.Loading)

                            Button(
                                onClick = {
                                    if (channelName.isBlank()) {
                                        formValidationMessage = "يرجى كتابة اسم القناة"
                                        return@Button
                                    }
                                    if (channelCategory.isBlank()) {
                                        formValidationMessage = "يرجى اختيار تصنيف للقناة"
                                        return@Button
                                    }
                                    if (serverList.isEmpty()) {
                                        formValidationMessage = "يرجى إضافة سيرفر واحد على الأقل للقناة"
                                        return@Button
                                    }
                                    formValidationMessage = null

                                    val previousBranchId = activeEditingChannel?.branchId ?: ""
                                    val channelToSave = Channel(
                                        id = activeEditingChannel?.id ?: UUID.randomUUID().toString(),
                                        name = channelName.trim(),
                                        imageUrl = channelImageUrl.trim(),
                                        category = channelCategory.trim(),
                                        branchId = channelBranchId,
                                        branchName = channelBranchName,
                                        servers = serverList.toList(),
                                        createdAt = activeEditingChannel?.createdAt ?: System.currentTimeMillis(),
                                        views = activeEditingChannel?.views ?: 0L
                                    )

                                    onSaveChannel(channelToSave, isUpdate)
                                    if (channelBranchId.isNotBlank()) {
                                        onAddChannelsToBranch(channelBranchId, listOf(channelToSave.id), channelBranchName)
                                    }
                                    if (previousBranchId.isNotBlank() && previousBranchId != channelBranchId) {
                                        onRemoveChannelFromBranch(previousBranchId, channelToSave.id)
                                    }
                                    // Reset activeEditingChannel after initiating update/save
                                    activeEditingChannel = null
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("publish_or_update_channel_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isUpdate) Icons.Default.Save else Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isUpdate) "تحديث المعلومات" else "نشر القناة أونلاين في Firebase",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            if (isUpdate) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = {
                                        activeEditingChannel = null
                                        channelName = ""
                                        channelImageUrl = ""
                                        serverList.clear()
                                        formValidationMessage = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                                ) {
                                    Text("إلغاء التعديل والبدء بنشر جديد")
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: PUBLISH CATEGORY (M3U Link & IPTV Xtream Channel Selection & Publishing)
                    CategoryPublisherTab(
                        categories = categories,
                        publishUiState = publishUiState,
                        onSaveCategory = onSaveCategory,
                        onSaveCategoryWithChannels = onSaveCategoryWithChannels,
                        onSaveChannel = onSaveChannel,
                        onDeleteCategory = onDeleteCategory,
                        onMoveCategoryToTop = onMoveCategoryToTop,
                        onMoveCategoryToSecond = onMoveCategoryToSecond,
                        onMoveCategoryUp = onMoveCategoryUp,
                        onMoveCategoryDown = onMoveCategoryDown,
                        onShowReorderDialog = { showReorderDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                2 -> {
                    // TAB 2: PUBLISH & MANAGE SUB-BRANCHES (نشر فرع جديد داخل تصنيف)
                    BranchPublisherTab(
                        branches = branches,
                        categories = categories,
                        channels = channels,
                        onSaveBranch = onSaveBranch,
                        onDeleteBranch = onDeleteBranch,
                        onAddChannelsToBranch = onAddChannelsToBranch,
                        onRemoveChannelFromBranch = onRemoveChannelFromBranch,
                        onSaveChannel = onSaveChannel,
                        onPlayChannel = onPlayChannel
                    )
                }

                3 -> {
                    // TAB 3: MANAGE PREVIOUSLY PUBLISHED CHANNELS (Manual publications only)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Action Bar: Delete All Channels Button if any exist in Firebase
                        if (channels.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LiveRed.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تنظيف قاعدة البيانات",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "إجمالي القنوات المسجلة: ${channels.size} قناة",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Button(
                                        onClick = { showDeleteAllChannelsConfirmDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("حذف كافة القنوات", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (genuineChannels.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Text(
                                        text = "لا توجد منشورات يدويّة سابقة في قاعدة البيانات",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "انتقل إلى تبويب 'نشر قناة جديدة' لنشر قناة فردية في Firebase.",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text(
                                        text = "القنوات اليدويّة المنشورة (${genuineChannels.size}):",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                items(genuineChannels, key = { it.id }) { ch ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                AsyncImage(
                                                    model = ch.imageUrl,
                                                    contentDescription = ch.name,
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(DarkBackground),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Column {
                                                    Text(
                                                        text = ch.name,
                                                        color = TextPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${ch.servers.size} سيرفرات: " + ch.servers.joinToString { it.name },
                                                        color = TextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }

                                        // Action buttons: Edit Servers, Edit Channel, Play, Delete
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Play in Player
                                            OutlinedButton(
                                                onClick = {
                                                    onPlayChannel(ch)
                                                    onDismiss()
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("تشغيل", fontSize = 10.sp)
                                            }

                                            // Quick Edit Servers Button
                                            Button(
                                                onClick = { quickEditChannelServers = ch },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = DarkPurpleContainer),
                                                border = BorderStroke(1.dp, NeonPurple),
                                                modifier = Modifier.weight(1.1f),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Link, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("السيرفرات", fontSize = 10.sp, color = Color.White)
                                            }

                                            // Edit Button
                                            Button(
                                                onClick = {
                                                    activeEditingChannel = ch
                                                    channelName = ch.name
                                                    channelImageUrl = ch.imageUrl
                                                    channelCategory = ch.category
                                                    serverList.clear()
                                                    serverList.addAll(ch.servers)
                                                    selectedTab = 0
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("تعديل", fontSize = 10.sp)
                                            }

                                            // Delete Button
                                            IconButton(
                                                onClick = { onDeleteChannel(ch.id) },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = LiveRed,
                                                    modifier = Modifier.size(18.dp)
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

                4 -> {
                    // TAB 4: PUBLISH IPTV ACCOUNTS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = if (editingXtremeAccount != null) "تعديل حساب المكتبة و IPTV" else "نشر حساب جديد لقسم المكتبة و IPTV",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "أدخل بيانات حساب السيرفر (اسم الحساب، الهوست، اسم المستخدم، كلمة المرور). سيتم حفظ الحساب فورياً في Firebase وتحميل أفلام ومسلسلات المكتبة وقنوات البث المباشر بسرعة فائقة جداً.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Account Friendly Name (Optional)
                        item {
                            OutlinedTextField(
                                value = xtremeAccountName,
                                onValueChange = { xtremeAccountName = it },
                                label = { Text("اسم الحساب التوضيحي (اختياري)") },
                                placeholder = { Text("مثال: سيرفر البث الذهبي (اختياري)") },
                                colors = outlinedTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Host
                        item {
                            OutlinedTextField(
                                value = xtremeHost,
                                onValueChange = { xtremeHost = it },
                                label = { Text("الهوست (Host / Server URL)") },
                                placeholder = { Text("مثال: http://example.com:8080") },
                                colors = outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("xtreme_host_input"),
                                singleLine = true
                            )
                        }

                        // Username
                        item {
                            OutlinedTextField(
                                value = xtremeUsername,
                                onValueChange = { xtremeUsername = it },
                                label = { Text("اسم المستخدم (Username)") },
                                colors = outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("xtreme_username_input"),
                                singleLine = true
                            )
                        }

                        // Password
                        item {
                            OutlinedTextField(
                                value = xtremePassword,
                                onValueChange = { xtremePassword = it },
                                label = { Text("كلمة المرور (Password)") },
                                colors = outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("xtreme_password_input"),
                                singleLine = true
                            )
                        }

                        // Stream Extension Selection
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "اختيار امتداد البث المباشر (Extension):",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FilterChip(
                                        selected = xtremeStreamExtension.equals("m3u8", ignoreCase = true),
                                        onClick = { xtremeStreamExtension = "m3u8" },
                                        label = { Text("m3u8 (HLS - موصى به)", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElectricPurple,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    FilterChip(
                                        selected = xtremeStreamExtension.equals("ts", ignoreCase = true),
                                        onClick = { xtremeStreamExtension = "ts" },
                                        label = { Text("ts (MPEG-TS)", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonPurple,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    FilterChip(
                                        selected = xtremeStreamExtension.equals("mp4", ignoreCase = true),
                                        onClick = { xtremeStreamExtension = "mp4" },
                                        label = { Text("mp4", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AccentCyan.copy(alpha = 0.8f),
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        if (xtremeErrorMsg != null) {
                            item {
                                Text(
                                    text = xtremeErrorMsg ?: "",
                                    color = LiveRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Save Button
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (xtremeHost.isBlank() || xtremeUsername.isBlank() || xtremePassword.isBlank()) {
                                            xtremeErrorMsg = "يرجى ملء الهوست واسم المستخدم وكلمة المرور كاملاً"
                                            return@Button
                                        }
                                        xtremeErrorMsg = null
                                        val accountToSave = XtremeAccount(
                                            id = editingXtremeAccount?.id ?: UUID.randomUUID().toString(),
                                            name = xtremeAccountName.ifBlank { "حساب IPTV" },
                                            host = xtremeHost.trim(),
                                            username = xtremeUsername.trim(),
                                            password = xtremePassword.trim(),
                                            streamExtension = xtremeStreamExtension.trim().lowercase().ifBlank { "m3u8" }
                                        )
                                        onSaveXtremeAccount(accountToSave)
                                        // Reset fields
                                        editingXtremeAccount = null
                                        xtremeHost = ""
                                        xtremeUsername = ""
                                        xtremePassword = ""
                                        xtremeAccountName = ""
                                        xtremeStreamExtension = "m3u8"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_xtreme_account_button")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (editingXtremeAccount != null) "حفظ التعديلات" else "نشر الحساب في Firebase",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                if (editingXtremeAccount != null) {
                                    OutlinedButton(
                                        onClick = {
                                            editingXtremeAccount = null
                                            xtremeHost = ""
                                            xtremeUsername = ""
                                            xtremePassword = ""
                                            xtremeAccountName = ""
                                            xtremeStreamExtension = "m3u8"
                                            xtremeErrorMsg = null
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("إلغاء", color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Published Accounts List
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "الحسابات المنشورة حالياً (${xtremeAccounts.size})",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (xtremeAccounts.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { showDeleteAllXtremeAccountsConfirmDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveRed),
                                        border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.8f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = LiveRed,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "حذف جميع الحسابات",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LiveRed
                                        )
                                    }
                                }
                            }
                        }

                        if (xtremeAccounts.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkSurfaceVariant,
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "لا توجد حسابات Xtreme منشورة حالياً. أضف أول حساب أعلاه لبدء تحميل المحتوى للمكتبة.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        } else {
                            items(xtremeAccounts) { acc ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("xtreme_account_item_${acc.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = acc.name,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Host: ${acc.host}",
                                                color = NeonPurple,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "User: ${acc.username}",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    editingXtremeAccount = acc
                                                    xtremeHost = acc.host
                                                    xtremeUsername = acc.username
                                                    xtremePassword = acc.password
                                                    xtremeAccountName = acc.name
                                                    xtremeStreamExtension = acc.streamExtension.ifBlank { "m3u8" }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = ElectricPurple,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteXtremeAccount(acc.id) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = LiveRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // TAB 5: FIREBASE & SERVER DIAGNOSTICS
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "معلومات قاعدة البيانات الرسمية المربوطة",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, DarkSurfaceBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    InfoRow(title = "عنوان قاعدة البيانات:", value = ChannelRepository.FIREBASE_RTDB_URL)
                                    InfoRow(title = "معرف المشروع (Project ID):", value = "loop-848d6")
                                    InfoRow(title = "رقم المشروع (Project No):", value = "49568448612")
                                    InfoRow(title = "حالة الاتصال:", value = "متصل أونلاين - تحديث تلقائي لحظي")
                                    InfoRow(title = "عدد القنوات الفعلي:", value = "${channels.size} قنوات")
                                }
                            }
                        }

                        item {
                            Text(
                                text = "زر المطور لإضافة قنوات تجريبية حقيقية (اختياري)",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "إذا أردت تجربة المشغل فوراً ببث تجريبي حقيقي، يمكنك الضغط على هذا الزر لنشر باقة قنوات رياضية تجريبية تعمل أونلاين في Firebase مع سيرفرات متعددة (m3u8, ts, normal).",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    val testChannels = listOf(
                                        Channel(
                                            id = "ch_bein_1",
                                            name = "beIN Sports HD",
                                            imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=300",
                                            category = "دوري أبطال أوروبا",
                                            servers = listOf(
                                                ChannelServer(
                                                    id = "s1",
                                                    name = "FHD",
                                                    streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                                                    streamType = "m3u8"
                                                ),
                                                ChannelServer(
                                                    id = "s2",
                                                    name = "HD",
                                                    streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                                                    streamType = "m3u8"
                                                ),
                                                ChannelServer(
                                                    id = "s3",
                                                    name = "SD",
                                                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                                                    streamType = "normal"
                                                )
                                            )
                                        ),
                                        Channel(
                                            id = "ch_ssc_1",
                                            name = "SSC Sports HD",
                                            imageUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=300",
                                            category = "الدوري السعودي",
                                            servers = listOf(
                                                ChannelServer(
                                                    id = "s4",
                                                    name = "سيرفر 1",
                                                    streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                                                    streamType = "m3u8"
                                                ),
                                                ChannelServer(
                                                    id = "s5",
                                                    name = "سيرفر 2 (TS)",
                                                    streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                                                    streamType = "ts"
                                                )
                                            )
                                        ),
                                        Channel(
                                            id = "ch_alkass",
                                            name = "Al Kass Sports",
                                            imageUrl = "https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?w=300",
                                            category = "كأس آسيا",
                                            servers = listOf(
                                                ChannelServer(
                                                    id = "s6",
                                                    name = "HD",
                                                    streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                                                    streamType = "m3u8"
                                                )
                                            )
                                        ),
                                        Channel(
                                            id = "ch_premier",
                                            name = "Premier League Live",
                                            imageUrl = "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=300",
                                            category = "الدوري الإنجليزي",
                                            servers = listOf(
                                                ChannelServer(
                                                    id = "s7",
                                                    name = "4K",
                                                    streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                                                    streamType = "m3u8"
                                                ),
                                                ChannelServer(
                                                    id = "s8",
                                                    name = "SD",
                                                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                                                    streamType = "normal"
                                                )
                                            )
                                        )
                                    )
                                    testChannels.forEach { onSaveChannel(it, false) }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("نشر قنوات رياضية تجريبية حقيقية في Firebase", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                6 -> {
                    // TAB 6: DEVELOPER OPTIONS & CONFIGURATIONS
                    var deleteConfirmOpen by remember { mutableStateOf(false) }
                    var deleteLoading by remember { mutableStateOf(false) }
                    var deleteStatusMsg by remember { mutableStateOf("") }
                    var showAllOnlineUsersList by remember { mutableStateOf(false) }
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "لوحة تحكم وإعدادات المطور الخاصة بالتطبيق",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 0. LIVE ONLINE USERS REAL-TIME COUNTER CARD (عداد المتواجدين الآن)
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, SuccessGreen.copy(alpha = 0.8f)),
                                modifier = Modifier.fillMaxWidth().testTag("dev_online_users_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Header: Title + Live Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Group,
                                                    contentDescription = null,
                                                    tint = SuccessGreen,
                                                    modifier = Modifier.size(19.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "عداد المتواجدين الآن أونلاين",
                                                    color = TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "مراقبة حية وفعلية لجميع المتصلين بالتطبيق حالياً",
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = SuccessGreen.copy(alpha = 0.15f),
                                            border = BorderStroke(0.8.dp, SuccessGreen)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(SuccessGreen, CircleShape)
                                                )
                                                Text(
                                                    text = "بث حي ومباشر",
                                                    color = SuccessGreen,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                                    // Compact Live Counter Box (عداد صغير وأنيق)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = DarkBackground,
                                        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "إجمالي المتصلين بالخدمة الآن",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "$globalOnlineCount",
                                                        color = SuccessGreen,
                                                        fontSize = 28.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Text(
                                                        text = "شخص متواجد 🟢",
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = "متصلون عبر Firebase Realtime Presence",
                                                    color = TextMuted,
                                                    fontSize = 9.5.sp
                                                )
                                            }

                                            // Action Button to show / hide all users list
                                            Button(
                                                onClick = { showAllOnlineUsersList = !showAllOnlineUsersList },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (showAllOnlineUsersList) VividViolet else DarkSurfaceVariant
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (showAllOnlineUsersList) "إخفاء القائمة" else "عرض الأشخاص (${globalOnlineUsers.size})",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Detailed list of all online users/sessions
                                    AnimatedVisibility(visible = showAllOnlineUsersList) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "قائمة الأشخاص المتواجدين حالياً (${globalOnlineUsers.size}):",
                                                color = BrightLavender,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            if (globalOnlineUsers.isEmpty()) {
                                                Text(
                                                    text = "لا توجد جلسات أخرى مسجلة حالياً.",
                                                    color = TextMuted,
                                                    fontSize = 11.sp
                                                )
                                            } else {
                                                globalOnlineUsers.forEachIndexed { index, userSession ->
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = DarkSurfaceVariant,
                                                        border = BorderStroke(0.8.dp, DarkSurfaceBorder),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 10.dp, vertical = 8.dp),
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
                                                                        .size(28.dp)
                                                                        .background(ElectricPurple.copy(alpha = 0.25f), CircleShape),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Person,
                                                                        contentDescription = null,
                                                                        tint = BrightLavender,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                                Column {
                                                                    Text(
                                                                        text = "مستخدم #${userSession.sessionId.take(6).uppercase()} ${if (index == 0) "(أنت)" else ""}",
                                                                        color = Color.White,
                                                                        fontSize = 12.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    Text(
                                                                        text = "الموقع: ${userSession.channel} • جهاز: ${userSession.device}",
                                                                        color = TextSecondary,
                                                                        fontSize = 10.sp
                                                                    )
                                                                }
                                                            }

                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = SuccessGreen.copy(alpha = 0.15f),
                                                                border = BorderStroke(0.8.dp, SuccessGreen)
                                                            ) {
                                                                Text(
                                                                    text = "متصل 🟢",
                                                                    color = SuccessGreen,
                                                                    fontSize = 9.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Real presence explanation note
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkPurpleContainer.copy(alpha = 0.4f),
                                        border = BorderStroke(0.6.dp, VividViolet.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = BrightLavender,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "العداد يعمل بشكل فعلي وحقيقي متصل بخوادم Firebase Realtime لحساب جميع المستخدمين النشطين داخل التطبيق في نفس اللحظة.",
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 1. APP CONTROL SECTION (تحكم في التطبيق)
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, ElectricPurple.copy(alpha = 0.8f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(ElectricPurple.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = null,
                                                tint = NeonPurple,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "تحكم في التطبيق",
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "إدارة ظهور القنوات، عداد المتصلين، والمشغل بذكاء",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                                    // 1.1: HIDE HOME CHANNELS TOGGLE
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "إخفاء القنوات من قسم الرئيسية",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "التحكم في إظهار أو إخفاء القنوات من الواجهة الرئيسية",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        androidx.compose.material3.Switch(
                                            checked = hideHomeAll,
                                            onCheckedChange = { onSetHideHomeAll(it) },
                                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = ElectricPurple,
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = DarkSurfaceVariant
                                            )
                                        )
                                    }

                                    // Explanatory Text Box (نص توضيحي)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DarkPurpleContainer.copy(alpha = 0.5f),
                                        border = BorderStroke(0.8.dp, VividViolet.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = BrightLavender,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = "نص توضيحي لإخفاء القنوات:",
                                                    color = BrightLavender,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "عند تفعيل هذا الخيار، سيتم إخفاء قائمة وشبكة القنوات وقسم الأكثر مشاهدة من الصفحة الرئيسية للتطبيق، ويظهر للمستخدمين تنبيه أنيق يفيد بأنه 'لا توجد مباريات جارية حالياً، يرجى العودة لاحقاً'. مع بقاء المشغل ومكتبة الأفلام والمسلسلات وباقي مميزات التطبيق تعمل بكامل طاقتها دون أي تأثر.",
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 10.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }

                                    // 1.2: HIDE ONLINE VIEWERS TOGGLE (شغال وذكي)
                                    HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "إخفاء عدد المتصلين في المشغل",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "إلغاء ظهور شارة المشاهدين المباشرين من شاشة المشغل",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        androidx.compose.material3.Switch(
                                            checked = hideOnlineViewers,
                                            onCheckedChange = { onSetHideOnlineViewers(it) },
                                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = ElectricPurple,
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = DarkSurfaceVariant
                                            )
                                        )
                                    }

                                    // Explanatory note for Online Viewers
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DarkSurfaceVariant.copy(alpha = 0.6f),
                                        border = BorderStroke(0.8.dp, DarkSurfaceBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "نظام ذكي وفوري: عند تفعيل هذا الخيار يتم إخفاء شارة العداد الحي للمتصلين (Live Viewers) في المشغل فوراً في الوضع العادي وفي ملء الشاشة، ويتم حفظ الخيار محلياً وسحابياً.",
                                                color = TextSecondary,
                                                fontSize = 10.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }

                                    // 1.3: HIDE CHAT IN PLAYER TOGGLE
                                    HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "إخفاء قسم الدردشة من المشغل",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "إخفاء أيقونة ونافذة الدردشة المباشرة من مشغل الفيديو",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        androidx.compose.material3.Switch(
                                            checked = hideChatInPlayer,
                                            onCheckedChange = { onSetHideChatInPlayer(it) },
                                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = ElectricPurple,
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = DarkSurfaceVariant
                                            )
                                        )
                                    }

                                    // 1.4: HIDE MATCHES SCHEDULE TOGGLE (إخفاء قسم جدول المباريات من شريط التنقل السفلي)
                                    HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "إخفاء قسم جدول المباريات من الشريط السفلي",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "إلغاء ظهور تبويب جدول المباريات وترتيب باقي الأقسام في شريط التنقل السفلي بشكل أنيق جداً",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        androidx.compose.material3.Switch(
                                            checked = hideMatchesSchedule,
                                            onCheckedChange = { onSetHideMatchesSchedule(it) },
                                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = ElectricPurple,
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = DarkSurfaceVariant
                                            )
                                        )
                                    }

                                    // 1.5: HIDE FOUL TV TAB TOGGLE (إخفاء قسم FOUL TV من الشريط السفلي)
                                    HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "إخفاء قسم FOUL TV من الشريط السفلي",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "إلغاء ظهور تبويب FOUL TV من شريط التنقل السفلي للتطبيق",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        androidx.compose.material3.Switch(
                                            checked = hideFoulTv,
                                            onCheckedChange = { onSetHideFoulTv(it) },
                                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = ElectricPurple,
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = DarkSurfaceVariant
                                            )
                                        )
                                    }

                                    if (hideHomeAll) {
                                        HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)
                                        
                                        Text(
                                            text = "نوع الإخفاء (تعديل الرئيسية):",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { onSetHideHomeMode("all") },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (hideHomeMode == "all") ElectricPurple else DarkSurfaceVariant
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "إخفاء عام (الكل)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (hideHomeMode == "all") Color.White else TextSecondary
                                                )
                                            }

                                            Button(
                                                onClick = { onSetHideHomeMode("specific") },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (hideHomeMode == "specific") ElectricPurple else DarkSurfaceVariant
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "مخصص لقسم محدد",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (hideHomeMode == "specific") Color.White else TextSecondary
                                                )
                                            }
                                        }

                                        if (hideHomeMode == "specific") {
                                            Text(
                                                text = "اختر التصنيف المراد إخفاؤه:",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                            
                                            // Category choices as Chips
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                val categoryList = listOf("دوري أبطال أوروبا", "الدوري السعودي", "الدوري الإنجليزي") + categories.map { it.name }
                                                val uniqueCategories = categoryList.filter { it.isNotBlank() }.distinct()
                                                
                                                androidx.compose.foundation.lazy.LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    contentPadding = PaddingValues(vertical = 2.dp)
                                                ) {
                                                    items(uniqueCategories) { catName ->
                                                        val isSelected = (hideHomeCategory == catName)
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (isSelected) NeonPurple.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                                            border = BorderStroke(1.dp, if (isSelected) NeonPurple else DarkSurfaceBorder),
                                                            modifier = Modifier.clickable { onSetHideHomeCategory(catName) }
                                                        ) {
                                                            Text(
                                                                text = catName,
                                                                color = if (isSelected) Color.White else TextSecondary,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = hideHomeCategory,
                                                onValueChange = { onSetHideHomeCategory(it) },
                                                label = { Text("أو اكتب اسم القسم يدوياً") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ElectricPurple,
                                                    unfocusedBorderColor = DarkSurfaceBorder,
                                                    focusedLabelColor = ElectricPurple,
                                                    unfocusedLabelColor = TextMuted,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. LOCK OLD VERSIONS & APP PROTECTION SECTION
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, if (lockOldVersions) LiveRed else DarkSurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(if (lockOldVersions) LiveRed.copy(alpha = 0.2f) else DarkSurfaceVariant, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = if (lockOldVersions) LiveRed else NeonPurple,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "إغلاق نسخة الإصدار القديم وقفل قاعدة البيانات",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "منع فتح التطبيق وقاعدة البيانات إلا على النسخة الأحدث فقط",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (lockOldVersions) "الحالة: النسخ القديمة مقفولة ومحظورة 🔴" else "الحالة: معطل - جميع النسخ تعمل ⚪",
                                                color = if (lockOldVersions) LiveRed else TextSecondary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "عند التفعيل، لن يفتح التطبيق وقاعدة البيانات على أي نسخة أقل من ($latestVersion)",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }

                                        androidx.compose.material3.Switch(
                                            checked = lockOldVersions,
                                            onCheckedChange = { enabled ->
                                                onSetLockOldVersions(enabled)
                                                if (enabled && updateType == "none") {
                                                    onSetUpdateType("forced")
                                                }
                                            },
                                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = LiveRed,
                                                uncheckedThumbColor = TextSecondary,
                                                uncheckedTrackColor = DarkSurfaceVariant
                                            )
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (lockOldVersions) LiveRed.copy(alpha = 0.1f) else DarkSurfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(0.8.dp, if (lockOldVersions) LiveRed.copy(alpha = 0.5f) else DarkSurfaceBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = if (lockOldVersions) LiveRed else NeonPurple,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = "توضيح القفل التلقائي:",
                                                    color = if (lockOldVersions) LiveRed else NeonPurple,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "عند تفعيل هذا الخيار، سيتم حظر جميع الإصدارات السابقة تماماً ومنع المستخدمين من الدخول أو تصفح القنوات وتظهر لهم شاشة التحديث الإجباري مع حظر زر الرجوع. كما تظهر أيقونة إعدادات المطور أعلى شاشة القفل لتسمح لك بالدخول بكلمة المرور في أي وقت.",
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 10.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2.5 DEVELOPER MASTER PASSWORD SECURITY SECTION
                        item {
                            var newPassInput by remember { mutableStateOf("") }
                            var showSuccessNotice by remember { mutableStateOf(false) }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, NeonPurple),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(NeonPurple.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AdminPanelSettings,
                                                contentDescription = null,
                                                tint = NeonPurple,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "🔐 أمان لوحة التحكم وكلمة المرور",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "كلمة المرور المشفرة المحمية (تم إلغاء وحظر 2008 نهائياً)",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DarkSurfaceVariant.copy(alpha = 0.6f),
                                        border = BorderStroke(0.8.dp, DarkSurfaceBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "🛡️ تم عزل قاعدة البيانات وحظر المخترق نهائياً:",
                                                color = BrightLavender,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "• تم عزل مسارات القنوات بالكامل إلى مسار آمن جديد (v4).\n• تم إلغاء وحظر كلمة المرور القديمة (2008) نهائياً.\n• النسخة القديمة أصبحت عاجزة تماماً ولا تؤثر على التطبيق إطلاقاً.",
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 10.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "كلمة المرور النشطة حالياً:",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = NeonPurple.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, NeonPurple)
                                        ) {
                                            Text(
                                                text = developerPassword.ifBlank { "2812246216200810" },
                                                color = BrightLavender,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = newPassInput,
                                        onValueChange = {
                                            newPassInput = it
                                            showSuccessNotice = false
                                        },
                                        label = { Text("تغيير كلمة المرور إذا أردت تحديثها مستقبلاً") },
                                        placeholder = { Text("مثال: 998877") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonPurple,
                                            unfocusedBorderColor = DarkSurfaceBorder,
                                            focusedLabelColor = NeonPurple,
                                            unfocusedLabelColor = TextMuted,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (newPassInput.isNotBlank()) {
                                        Button(
                                            onClick = {
                                                onSetDeveloperPassword(newPassInput.trim())
                                                newPassInput = ""
                                                showSuccessNotice = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Text("حفظ وتحديث كلمة المرور سحابياً في Firebase", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start = 6.dp))
                                        }
                                    }

                                    if (showSuccessNotice) {
                                        Text(
                                            text = "✅ تم تحديث كلمة المرور سحابياً بنجاح!",
                                            color = SuccessGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // 3. VERSIONS MANAGEMENT SECTION (SMART 3.0.1)
                        item {
                            val isAccepted = com.example.ui.viewmodel.FootballViewModel.isAcceptedVersion(latestVersion)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, if (isAccepted) NeonPurple else LiveRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(NeonPurple.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.NewReleases,
                                                    contentDescription = null,
                                                    tint = NeonPurple,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                             Column {
                                                Text(
                                                    text = "نظام إدارة الإصدارات المطور",
                                                    color = TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "إمكانية تغيير ومسح الإصدار القديم وكتابة الإصدار الجديد بحرية تامة",
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SuccessGreen.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, SuccessGreen)
                                        ) {
                                            Text(
                                                text = "نسخة المطور 🟢",
                                                color = SuccessGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DarkSurfaceVariant.copy(alpha = 0.6f),
                                        border = BorderStroke(0.8.dp, DarkSurfaceBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "💡 ميزة التحكم الذكي بالإصدارات:",
                                                color = BrightLavender,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "يمكنك مسح الإصدار القديم وكتابة أي رقم إصدار جديد تريده (مثلاً 1.0.0 أو غيره). كما يمكنك تصفير القفل وإلغاء شاشة الإجبار لتعمل النسخة الحالية بسلاسة ومن دون أي مشاكل.",
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 10.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }

                                    // Quick Set 4.0.0 & Unlock Button
                                    Button(
                                        onClick = {
                                            onSetLatestVersion("4.0.0")
                                            onSetLockOldVersions(false)
                                            onSetUpdateType("none")
                                            if (onPublishVersion3Smartly != null) {
                                                onPublishVersion3Smartly(
                                                    "تم اعتماد الإصدار الرسمي 4.0.0 بنجاح وإلغاء القفل الإجباري.",
                                                    updateUrl
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "🟢 اعتماد الإصدار 4.0.0 وإلغاء القفل الإجباري فوراً",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }

                                    HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "إصدار التطبيق الحالي:",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = ElectricPurple.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, ElectricPurple)
                                        ) {
                                            Text(
                                                text = "v$appVersion",
                                                color = BrightLavender,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // Editable Version Input with Clear Button
                                    OutlinedTextField(
                                        value = latestVersion,
                                        onValueChange = { input ->
                                            onSetLatestVersion(input)
                                        },
                                        label = { Text("رقم الإصدار الجديد (اكتب أو امسح بحرية)") },
                                        trailingIcon = {
                                            if (latestVersion.isNotBlank()) {
                                                IconButton(onClick = { onSetLatestVersion("") }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "مسح الإصدار",
                                                        tint = TextMuted,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ElectricPurple,
                                            unfocusedBorderColor = DarkSurfaceBorder,
                                            focusedLabelColor = ElectricPurple,
                                            unfocusedLabelColor = TextMuted,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Save / Apply Any Custom Version Button
                                    Button(
                                        onClick = {
                                            val clean = latestVersion.trim().ifBlank { "1.0.0" }
                                            onSetLatestVersion(clean)
                                            if (onPublishVersion3Smartly != null) {
                                                onPublishVersion3Smartly(
                                                    "تم تحديث الإصدار إلى $clean بنجاح.",
                                                    updateUrl
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "🚀 حفظ واعتماد الإصدار المكتوب ومسح القديم",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }

                                    Text(
                                        text = "نوع التحديث الجديد المتاح:",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val updateModes = listOf(
                                            "none" to "لا يوجد تحديث",
                                            "optional" to "تحديث اختياري",
                                            "forced" to "تحديث إجباري"
                                        )
                                        updateModes.forEach { (modeCode, modeLabel) ->
                                            val isSelected = (updateType == modeCode)
                                            Button(
                                                onClick = { onSetUpdateType(modeCode) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSelected) ElectricPurple else DarkSurfaceVariant
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = modeLabel,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else TextSecondary
                                                )
                                            }
                                        }
                                    }

                                    if (updateType != "none") {
                                        OutlinedTextField(
                                            value = updateText,
                                            onValueChange = { onSetUpdateText(it) },
                                            label = { Text("نص وتفاصيل التحديث الجديد") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = ElectricPurple,
                                                unfocusedBorderColor = DarkSurfaceBorder,
                                                focusedLabelColor = ElectricPurple,
                                                unfocusedLabelColor = TextMuted,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        OutlinedTextField(
                                            value = updateUrl,
                                            onValueChange = { onSetUpdateUrl(it) },
                                            label = { Text("رابط تحميل التحديث الجديد (APK Link)") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = ElectricPurple,
                                                unfocusedBorderColor = DarkSurfaceBorder,
                                                focusedLabelColor = ElectricPurple,
                                                unfocusedLabelColor = TextMuted,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        // 3. SENSITIVE DATABASE ACTIONS
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "إجراءات قاعدة البيانات الحساسة",
                                        color = LiveRed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                        if (!deleteConfirmOpen) {
                                            Button(
                                                onClick = { deleteConfirmOpen = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = LiveRed.copy(alpha = 0.15f)),
                                                border = BorderStroke(1.dp, LiveRed),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = LiveRed, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("حذف جميع رسائل الدردشة نهائياً", color = LiveRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(LiveRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                                    .border(1.dp, LiveRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                                    .padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "هل أنت متأكد تماماً من حذف كافة الرسائل من قاعدة البيانات نهائياً؟ لا يمكن التراجع عن هذا الإجراء.",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center
                                                )

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            deleteLoading = true
                                                            onDeleteAllMessages { res ->
                                                                deleteLoading = false
                                                                if (res.isSuccess) {
                                                                    deleteStatusMsg = "تم حذف جميع الرسائل بنجاح!"
                                                                    deleteConfirmOpen = false
                                                                } else {
                                                                    deleteStatusMsg = "فشل الحذف: ${res.exceptionOrNull()?.localizedMessage}"
                                                                }
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        if (deleteLoading) {
                                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                                        } else {
                                                            Text("نعم، احذف الكل", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    Button(
                                                        onClick = { deleteConfirmOpen = false },
                                                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("إلغاء الإجراء", color = TextSecondary, fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }

                                         if (deleteStatusMsg.isNotBlank()) {
                                            Text(
                                                text = deleteStatusMsg,
                                                color = if (deleteStatusMsg.contains("نجاح")) SuccessGreen else LiveRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                7 -> {
                    // TAB 7: PUBLISH EVENT BANNER (نشر حدث مع اختيار القناة أونلاين من الرئيسية)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "نشر حدث أو مباراة خاصة في شريط الرئيسية العلوي",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "يظهر كشريط بنسبة 16:9 بين الشريط العلوي وشريط التصنيفات تلقائياً فقط عند وجود أحداث منشورة مع ربط مباشر بالقناة أو رابط خارجي.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Form Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // 1. ONLINE CHANNEL SELECTION (ربط الحدث بقناة أو أكثر من القنوات المنشورة)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "📺 القنوات المرتبطة بالحدث (${selectedEventChannels.size} محددة):",
                                            color = BrightLavender,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (selectedEventChannels.isNotEmpty()) {
                                            TextButton(
                                                onClick = { selectedEventChannels.clear() },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("مسح الكل", color = LiveRed, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    if (selectedEventChannels.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            selectedEventChannels.forEach { ch ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141026)),
                                                    border = BorderStroke(1.2.dp, NeonPurple),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .border(1.dp, VividViolet, RoundedCornerShape(8.dp))
                                                                .background(DarkSurfaceVariant),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (ch.imageUrl.isNotBlank()) {
                                                                AsyncImage(
                                                                    model = ch.imageUrl,
                                                                    contentDescription = ch.name,
                                                                    modifier = Modifier.fillMaxSize(),
                                                                    contentScale = ContentScale.Crop
                                                                )
                                                            } else {
                                                                Icon(
                                                                    imageVector = Icons.Default.Tv,
                                                                    contentDescription = null,
                                                                    tint = BrightLavender,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                        }

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = ch.name,
                                                                color = Color.White,
                                                                fontSize = 12.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = "قسم: ${ch.category.ifBlank { "عام" }} • سيرفرات: ${ch.servers.size}",
                                                                color = TextSecondary,
                                                                fontSize = 10.sp
                                                            )
                                                        }

                                                        IconButton(
                                                            onClick = { selectedEventChannels.remove(ch) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "إلغاء التحديد",
                                                                tint = LiveRed,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { showChannelPickerSheet = true },
                                                border = BorderStroke(1.dp, NeonPurple),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(38.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("إضافة قناة أخرى للحدث (${channels.size} قناة متاحة)", color = Color.White, fontSize = 11.5.sp)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { showChannelPickerSheet = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141026)),
                                            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(NeonPurple, AccentCyan))),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Tv,
                                                    contentDescription = null,
                                                    tint = AccentCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "اختر القنوات المرتبطة بالحدث أونلاين (${channels.size} قناة)",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Title Input
                                    OutlinedTextField(
                                        value = eventTitle,
                                        onValueChange = { eventTitle = it },
                                        label = { Text("نص الحدث (مثال: ريال مدريد vs برشلونة - الكلاسيكو)") },
                                        placeholder = { Text("أدخل عنوان الحدث أو المباراة") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = outlinedTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    // Image URL Input
                                    OutlinedTextField(
                                        value = eventImageUrl,
                                        onValueChange = { eventImageUrl = it },
                                        label = { Text("رابط صورة المباراة / الحدث (16:9)") },
                                        placeholder = { Text("https://example.com/banner.jpg") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = outlinedTextFieldColors(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    // Image Preview if filled
                                    if (eventImageUrl.isNotBlank()) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9f)
                                        ) {
                                            AsyncImage(
                                                model = eventImageUrl,
                                                contentDescription = "معاينة الصورة",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    // Watch Now Toggle Option
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showWatchNowIcon = !showWatchNowIcon }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = if (showWatchNowIcon) ElectricPurple else TextMuted
                                            )
                                            Text(
                                                text = "إضافة أيقونة وزر 'شاهد الآن'",
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        androidx.compose.material3.Switch(
                                            checked = showWatchNowIcon,
                                            onCheckedChange = { showWatchNowIcon = it }
                                        )
                                    }

                                    if (showWatchNowIcon && selectedEventChannels.isEmpty()) {
                                        OutlinedTextField(
                                            value = eventWatchUrl,
                                            onValueChange = { eventWatchUrl = it },
                                            label = { Text("رابط بث أو موقع مخصص عند ضغط 'شاهد الآن' (اختياري)") },
                                            placeholder = { Text("https://... أو اتركه فارغاً") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = outlinedTextFieldColors(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    // Publish Button
                                    Button(
                                        onClick = {
                                            if (eventTitle.isNotBlank() && eventImageUrl.isNotBlank()) {
                                                val newBanner = com.example.data.model.EventBanner(
                                                    title = eventTitle.trim(),
                                                    imageUrl = eventImageUrl.trim(),
                                                    showWatchNow = showWatchNowIcon,
                                                    watchNowUrl = eventWatchUrl.trim(),
                                                    channelId = selectedEventChannels.firstOrNull()?.id ?: "",
                                                    channelName = selectedEventChannels.firstOrNull()?.name ?: "",
                                                    channelIds = selectedEventChannels.map { it.id },
                                                    channelNames = selectedEventChannels.map { it.name }
                                                )
                                                onSaveEventBanner(newBanner)
                                                eventTitle = ""
                                                eventImageUrl = ""
                                                eventWatchUrl = ""
                                                selectedEventChannels.clear()
                                            }
                                        },
                                        enabled = eventTitle.isNotBlank() && eventImageUrl.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (selectedEventChannels.size > 1) "نشر الحدث مع (${selectedEventChannels.size}) قنوات" else "نشر الحدث الآن",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Nav Schedule Tab Quick Toggle inside Publisher Tab
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "إخفاء قسم جدول المباريات من الشريط السفلي",
                                            color = TextPrimary,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "إخفاء تبويب جدول المباريات وترتيب باقي الأقسام في شريط التنقل السفلي بشكل أنيق جداً",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    androidx.compose.material3.Switch(
                                        checked = hideMatchesSchedule,
                                        onCheckedChange = { onSetHideMatchesSchedule(it) },
                                        colors = androidx.compose.material3.SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = ElectricPurple,
                                            uncheckedThumbColor = TextSecondary,
                                            uncheckedTrackColor = DarkSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        // List of Published Events
                        item {
                            Text(
                                text = "الأحداث المنشورة حالياً (${eventBanners.size})",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (eventBanners.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "لا توجد أحداث منشورة حالياً في التطبيق",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            items(eventBanners, key = { it.id }) { banner ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // 16:9 Thumbnail
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .width(90.dp)
                                                .aspectRatio(16f / 9f)
                                        ) {
                                            AsyncImage(
                                                model = banner.imageUrl,
                                                contentDescription = banner.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = banner.title,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (banner.channelName.isNotBlank()) {
                                                Text(
                                                    text = "📺 قناة: ${banner.channelName}",
                                                    color = AccentCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            if (banner.showWatchNow) {
                                                Text(
                                                    text = "• يتضمن أيقونة 'شاهد الآن'",
                                                    color = ElectricPurple,
                                                    fontSize = 9.5.sp
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { onDeleteEventBanner(banner.id) },
                                            modifier = Modifier.testTag("delete_event_${banner.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف",
                                                tint = LiveRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                8 -> {
                    // TAB 8: FOUL TV MANAGEMENT
                    FoulTvManagerTab(
                        videos = foulVideos,
                        globalConfig = foulGlobalConfig,
                        onPublishNewVideo = { onOpenPublishFoulVideoDialog?.invoke(null) },
                        onEditVideo = { video -> onOpenPublishFoulVideoDialog?.invoke(video) },
                        onDeleteVideo = onDeleteFoulVideo,
                        onUpdateVideoSettings = onUpdateFoulVideoSettings,
                        onUpdateGlobalConfig = onUpdateFoulGlobalConfig,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (showDeleteAllChannelsConfirmDialog) {
                Dialog(
                    onDismissRequest = { showDeleteAllChannelsConfirmDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, LiveRed),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(LiveRed.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, LiveRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = LiveRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = "تأكيد حذف جميع القنوات",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "هل أنت متاكد من حذف جميع القنوات المنشورة (${channels.size}) نهائياً من قاعدة البيانات؟ لا يمكن التراجع عن هذه العملية.",
                                color = TextSecondary,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showDeleteAllChannelsConfirmDialog = false },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("إلغاء", color = TextSecondary, fontSize = 13.sp)
                                }

                                Button(
                                    onClick = {
                                        showDeleteAllChannelsConfirmDialog = false
                                        onDeleteAllChannels()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Text("تأكيد الحذف الكلي", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (showDeleteAllXtremeAccountsConfirmDialog) {
                Dialog(
                    onDismissRequest = { showDeleteAllXtremeAccountsConfirmDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, LiveRed),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                .size(48.dp)
                                .background(LiveRed.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, LiveRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = LiveRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = "تأكيد حذف جميع حسابات IPTV",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "هل أنت متأكد من حذف جميع الحسابات السابقة (${xtremeAccounts.size}) نهائياً من قاعدة البيانات ومسح محتوى المكتبة القديم؟ لا يمكن التراجع عن هذه العملية.",
                                color = TextSecondary,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showDeleteAllXtremeAccountsConfirmDialog = false },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("إلغاء", color = TextSecondary, fontSize = 13.sp)
                                }

                                Button(
                                    onClick = {
                                        showDeleteAllXtremeAccountsConfirmDialog = false
                                        onDeleteAllXtremeAccounts()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Text("تأكيد حذف الحسابات", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (showChannelPickerSheet) {
                EventChannelPickerDialog(
                    isOpen = showChannelPickerSheet,
                    channels = channels,
                    selectedChannelIds = selectedEventChannels.map { it.id },
                    onConfirmChannels = { chList ->
                        selectedEventChannels.clear()
                        selectedEventChannels.addAll(chList)
                        if (eventTitle.isBlank() && chList.isNotEmpty()) {
                            eventTitle = "${chList.first().name} - بث مباشر"
                        }
                        if (eventImageUrl.isBlank() && chList.firstOrNull()?.imageUrl?.isNotBlank() == true) {
                            eventImageUrl = chList.first().imageUrl
                        }
                        showChannelPickerSheet = false
                    },
                    onDismiss = { showChannelPickerSheet = false }
                )
            }

            if (showReorderDialog) {
                CategoryReorderDialog(
                    isOpen = showReorderDialog,
                    categories = categories,
                    onDismiss = { showReorderDialog = false },
                    onMoveToTop = onMoveCategoryToTop,
                    onMoveToSecond = onMoveCategoryToSecond,
                    onMoveUp = onMoveCategoryUp,
                    onMoveDown = onMoveCategoryDown
                )
            }

            // Edit Single Server Sub-Dialog
            if (editingServerItem != null) {
                EditServerDialog(
                    server = editingServerItem!!.second,
                    onSave = { updatedServer ->
                        val index = editingServerItem!!.first
                        if (index in serverList.indices) {
                            serverList[index] = updatedServer
                        }
                        editingServerItem = null
                    },
                    onDismiss = { editingServerItem = null }
                )
            }

            // Quick Edit Channel Servers Dialog
            if (quickEditChannelServers != null) {
                QuickEditChannelServersDialog(
                    channel = quickEditChannelServers!!,
                    onSave = { updatedChannel ->
                        onSaveChannel(updatedChannel, true)
                        quickEditChannelServers = null
                    },
                    onDismiss = { quickEditChannelServers = null }
                )
            }
        }
    }
}

@Composable
fun CategoryReorderDialog(
    isOpen: Boolean,
    categories: List<ChannelCategory>,
    onDismiss: () -> Unit,
    onMoveToTop: (String) -> Unit,
    onMoveToSecond: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 640.dp)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(DarkPurpleContainer, CircleShape)
                                .border(1.dp, NeonPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "تعديل ظهور التصنيف",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "أولوية الترتيب في قسم 'الكل' بالصفحة الرئيسية",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explanatory Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkPurpleContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "• التصنيف الأول (#1) تظهر قنواته بصدارة قسم 'الكل'.\n• التصنيف الثاني (#2) تظهر قنواته تحت الأول مباشرة.",
                            color = BrightLavender,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (categories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد تصنيفات منشورة حتى الآن",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(categories, key = { _, cat -> cat.id }) { index, cat ->
                            val isFirst = index == 0
                            val isSecond = index == 1

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isFirst) DarkPurpleContainer else DarkSurfaceVariant
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isFirst) NeonPurple else if (isSecond) VividViolet.copy(alpha = 0.7f) else DarkSurfaceBorder
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    // Row 1: Rank Badge, Icon/Image, Category Name
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when {
                                                isFirst -> ElectricPurple
                                                isSecond -> DarkPurpleContainer
                                                else -> DarkBackground
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                when {
                                                    isFirst -> NeonPurple
                                                    isSecond -> VividViolet
                                                    else -> DarkSurfaceBorder
                                                }
                                            )
                                        ) {
                                            Text(
                                                text = when {
                                                    isFirst -> "🏆 #1 الأول"
                                                    isSecond -> "🥈 #2 تحت الأول"
                                                    else -> "#${index + 1}"
                                                },
                                                color = if (isFirst) Color.White else TextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }

                                        if (cat.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(cat.imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(DarkBackground, RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Storage,
                                                    contentDescription = null,
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cat.name,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isFirst) {
                                                Text(
                                                    text = "قنواته تظهر أولاً في 'الكل'",
                                                    color = SuccessGreen,
                                                    fontSize = 10.sp
                                                )
                                            } else if (isSecond) {
                                                Text(
                                                    text = "قنواته تظهر تحت الأول",
                                                    color = BrightLavender,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 2: Reorder Action Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Quick move to #1
                                        if (!isFirst) {
                                            Button(
                                                onClick = { onMoveToTop(cat.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("نقل إلى الأول", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Quick move under #1
                                        if (!isSecond && categories.size >= 2) {
                                            OutlinedButton(
                                                onClick = { onMoveToSecond(cat.id) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightLavender),
                                                border = BorderStroke(1.dp, VividViolet.copy(alpha = 0.8f)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = BrightLavender,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isFirst) "نقل لـ #2" else "وضع تحت الأول", fontSize = 11.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        // Step Up
                                        IconButton(
                                            onClick = { onMoveUp(cat.id) },
                                            enabled = index > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Move Up",
                                                tint = if (index > 0) Color.White else TextMuted.copy(alpha = 0.3f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Step Down
                                        IconButton(
                                            onClick = { onMoveDown(cat.id) },
                                            enabled = index < categories.size - 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Move Down",
                                                tint = if (index < categories.size - 1) Color.White else TextMuted.copy(alpha = 0.3f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(14.dp))

                // Done & Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تم وحفظ الترتيب",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, color = TextMuted, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = DarkSurfaceBorder,
    focusedLabelColor = NeonPurple,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = NeonPurple
)

@Composable
fun EditServerDialog(
    server: ChannelServer,
    onSave: (ChannelServer) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(server.name) }
    var url by remember { mutableStateOf(server.streamUrl) }
    var streamType by remember { mutableStateOf(server.streamType) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.2.dp, NeonPurple),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "تعديل بيانات ورابط السيرفر",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم وجودة السيرفر") },
                    placeholder = { Text("مثال: HD 1080p أو FHD أو سيرفر احتياطي") },
                    singleLine = true,
                    colors = outlinedTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick server name suggestions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("FHD", "HD", "SD", "LOW", "4K", "احتياطي").forEach { qName ->
                        FilterChip(
                            selected = (name == qName),
                            onClick = { name = qName },
                            label = { Text(qName, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricPurple,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("رابط السيرفر (Stream URL)") },
                    placeholder = { Text("https://example.com/live.m3u8") },
                    singleLine = false,
                    maxLines = 3,
                    colors = outlinedTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "نوع صيغة البث:",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("m3u8", "ts", "normal").forEach { type ->
                        val isSelected = (streamType.lowercase() == type)
                        FilterChip(
                            selected = isSelected,
                            onClick = { streamType = type },
                            label = { Text(type.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricPurple,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = LiveRed,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMsg = "يرجى كتابة اسم السيرفر"
                                return@Button
                            }
                            if (url.isBlank()) {
                                errorMsg = "يرجى وضع رابط السيرفر"
                                return@Button
                            }
                            errorMsg = null
                            val updatedStreamType = when (streamType.lowercase()) {
                                "ts" -> StreamType.TS
                                "normal" -> StreamType.NORMAL
                                else -> StreamType.M3U8
                            }
                            onSave(
                                server.copy(
                                    name = name.trim(),
                                    streamUrl = url.trim(),
                                    streamType = streamType.lowercase()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ التعديل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickEditChannelServersDialog(
    channel: Channel,
    onSave: (Channel) -> Unit,
    onDismiss: () -> Unit
) {
    val serversList = remember { mutableStateListOf<ChannelServer>().apply { addAll(channel.servers) } }
    var selectedServerToEdit by remember { mutableStateOf<Pair<Int, ChannelServer>?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("HD") }
    var newUrl by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("m3u8") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.2.dp, NeonPurple),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 620.dp)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = channel.imageUrl,
                            contentDescription = channel.name,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = "تعديل سيرفرات: ${channel.name}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تعديل روابط البث وأسماء الجودات مباشرة",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.8.dp)

                // List of servers
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(serversList) { index, srv ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkBackground),
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = srv.name,
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = DarkPurpleContainer
                                        ) {
                                            Text(
                                                text = srv.streamType.uppercase(),
                                                color = NeonPurple,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = srv.streamUrl,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { selectedServerToEdit = Pair(index, srv) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NeonPurple, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { serversList.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LiveRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (isAddingNew) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                                border = BorderStroke(1.dp, NeonPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("إضافة سيرفر جديد للقناة:", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = newName,
                                        onValueChange = { newName = it },
                                        label = { Text("اسم السيرفر") },
                                        singleLine = true,
                                        colors = outlinedTextFieldColors(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = newUrl,
                                        onValueChange = { newUrl = it },
                                        label = { Text("رابط السيرفر") },
                                        singleLine = true,
                                        colors = outlinedTextFieldColors(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                if (newName.isNotBlank() && newUrl.isNotBlank()) {
                                                    val streamT = when (newType.lowercase()) {
                                                        "ts" -> StreamType.TS
                                                        "normal" -> StreamType.NORMAL
                                                        else -> StreamType.M3U8
                                                    }
                                                    serversList.add(
                                                        ChannelServer(
                                                            id = UUID.randomUUID().toString(),
                                                            name = newName.trim(),
                                                            streamUrl = newUrl.trim(),
                                                            streamType = newType.lowercase()
                                                        )
                                                    )
                                                    newUrl = ""
                                                    isAddingNew = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("إضافة السيرفر للقائمة", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add server button
                if (!isAddingNew) {
                    OutlinedButton(
                        onClick = { isAddingNew = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة سيرفر جديد", fontSize = 11.sp)
                    }
                }

                // Save All & Publish to Firebase Button
                Button(
                    onClick = {
                        val updatedChannel = channel.copy(servers = serversList.toList())
                        onSave(updatedChannel)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ التعديلات في قاعدة البيانات", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sub-dialog for editing single server in quick mode
        if (selectedServerToEdit != null) {
            EditServerDialog(
                server = selectedServerToEdit!!.second,
                onSave = { updatedSrv ->
                    serversList[selectedServerToEdit!!.first] = updatedSrv
                    selectedServerToEdit = null
                },
                onDismiss = { selectedServerToEdit = null }
            )
        }
    }
}

/**
 * Event Channel Picker Dialog:
 * Allows selecting one or more existing live online channels from Firebase to link to the published event banner.
 */
@Composable
fun EventChannelPickerDialog(
    isOpen: Boolean,
    channels: List<Channel>,
    selectedChannelIds: List<String> = emptyList(),
    onConfirmChannels: (List<Channel>) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var searchQuery by remember { mutableStateOf("") }
    val selectedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            selectedChannelIds.forEach { this[it] = true }
        }
    }

    val filteredChannels = remember(channels, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            channels
        } else {
            channels.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }
    }

    val selectedCount = selectedMap.count { it.value }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E1A)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonPurple, AccentCyan))),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(DarkPurpleContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = BrightLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "اختيار قنوات الحدث الناقلة",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "المحدد: $selectedCount قنوات من أصل ${filteredChannels.size}",
                                color = AccentCyan,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Quick Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            filteredChannels.forEach { selectedMap[it.id] = true }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("تحديد الكل", fontSize = 11.sp, color = BrightLavender)
                    }

                    OutlinedButton(
                        onClick = {
                            selectedMap.clear()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("إلغاء التحديد", fontSize = 11.sp, color = TextMuted)
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث باسم القناة أو التصنيف...", fontSize = 11.5.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = BrightLavender,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = outlinedTextFieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Channels List
                if (filteredChannels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "لا توجد قنوات مطابقة لبحثك" else "لا توجد قنوات منشورة في قاعدة البيانات حالياً",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredChannels, key = { it.id }) { channel ->
                            val isSelected = selectedMap[channel.id] ?: false
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF1D1738) else DarkSurface
                                ),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isSelected) NeonPurple else Color(0xFF222036)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMap[channel.id] = !isSelected
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedMap[channel.id] = checked
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = ElectricPurple,
                                            checkmarkColor = Color.White
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    // Channel Squircle Logo
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, NeonPurple.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .background(DarkSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (channel.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = channel.imageUrl,
                                                contentDescription = channel.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Tv,
                                                contentDescription = null,
                                                tint = BrightLavender,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // Channel Name & Category Badge
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = channel.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = ElectricPurple.copy(alpha = 0.2f),
                                                border = BorderStroke(0.6.dp, ElectricPurple.copy(alpha = 0.6f))
                                            ) {
                                                Text(
                                                    text = channel.category.ifBlank { "عام" },
                                                    color = BrightLavender,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }

                                            Text(
                                                text = "${channel.servers.size} سيرفر",
                                                color = TextMuted,
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Confirm button
                Button(
                    onClick = {
                        val chosen = channels.filter { selectedMap[it.id] == true }
                        onConfirmChannels(chosen)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(
                        text = if (selectedCount > 0) "تأكيد اختيار ($selectedCount) قنوات للحدث" else "إغلاق والتأكيد",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

