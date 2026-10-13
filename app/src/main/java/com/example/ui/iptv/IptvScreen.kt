package com.example.ui.iptv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.XtremeAccount
import com.example.data.model.XtremeItem
import com.example.ui.components.CategoryChip
import com.example.ui.components.ChannelLogoPlaceholder
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BrightLavender
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IptvScreen(
    liveChannels: List<XtremeItem>,
    accounts: List<XtremeAccount>,
    selectedAccount: XtremeAccount?,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectAccount: (XtremeAccount) -> Unit,
    onRefresh: () -> Unit,
    onPlayChannel: (XtremeItem) -> Unit,
    onOpenDeveloperSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var showAccountSwitcherSheet by remember { mutableStateOf(false) }

    // Pulsing animation for the Live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "iptvPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Extract categories from live channels
    val availableCategories = remember(liveChannels) {
        val list = mutableListOf("الكل")
        val cats = liveChannels.map { it.categoryName.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        list.addAll(cats)
        list
    }

    // Filtered channels by search query and category
    val filteredChannels = remember(liveChannels, selectedCategory, searchQuery) {
        liveChannels.filter { channel ->
            val matchesCategory = if (selectedCategory == "الكل") {
                true
            } else {
                channel.categoryName.trim().equals(selectedCategory.trim(), ignoreCase = true)
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                channel.title.contains(searchQuery.trim(), ignoreCase = true)
            }

            matchesCategory && matchesSearch
        }
    }

    val configuration = LocalConfiguration.current
    val gridColumns = when {
        configuration.screenWidthDp >= 900 -> 6
        configuration.screenWidthDp >= 600 -> 4
        else -> 3
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // 1. TOP HEADER BAR: Title, Live Pill, Account Switcher & Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Title & Pulsing Live Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "IPTV",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                // LIVE badge with pulsing red dot
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = LiveRed.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(LiveRed.copy(alpha = pulseAlpha), CircleShape)
                        )
                        Text(
                            text = "بث مباشر",
                            color = LiveRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (liveChannels.isNotEmpty()) {
                    Text(
                        text = "(${filteredChannels.size})",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right Action Buttons: Account Switcher & Search
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ACCOUNT SWITCHER BUTTON (Compact, refined outer icon and label)
                if (accounts.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F0F16),
                        border = BorderStroke(1.dp, if (accounts.size > 1) NeonPurple.copy(alpha = 0.8f) else Color(0xFF222230)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAccountSwitcherSheet = true }
                            .testTag("iptv_account_switcher_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (accounts.size > 1) Icons.Default.SwapHoriz else Icons.Default.Dns,
                                contentDescription = "تبديل حساب IPTV",
                                tint = BrightLavender,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = selectedAccount?.name?.ifBlank { "حساب IPTV" } ?: "اختر سيرفر",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (accounts.size > 1) {
                                Surface(
                                    shape = CircleShape,
                                    color = NeonPurple,
                                    modifier = Modifier.size(15.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${accounts.size}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Search Toggle Button (Compact)
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF161624),
                    border = BorderStroke(0.8.dp, Color(0xFF2A2A3E)),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { isSearchExpanded = !isSearchExpanded }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Refresh Button (Compact)
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF161624),
                    border = BorderStroke(0.8.dp, Color(0xFF2A2A3E)),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onRefresh() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = AccentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 2. EXPANDABLE SEARCH BAR
        AnimatedVisibility(
            visible = isSearchExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن أي قناة بث مباشر بالاسم...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0C0C14),
                        unfocusedContainerColor = Color(0xFF08080E),
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = Color(0xFF1E1E2C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            }
        }

        // 3. CATEGORIES RIBBON (Fast category filtering)
        if (availableCategories.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("iptv_categories_ribbon"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                items(availableCategories, key = { it }) { cat ->
                    CategoryChip(
                        title = cat,
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }
        }

        // 4. MAIN CONTENT AREA: Channels Grid, Loading or Empty State
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                isLoading && liveChannels.isEmpty() -> {
                    // Loading State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                color = NeonPurple,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(46.dp)
                            )
                            Text(
                                text = "جاري تحميل قنوات البث المباشر بسرعة فائقة...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedAccount?.name?.ifBlank { "سيرفر IPTV" } ?: "الاتصال بالخادم",
                                color = AccentCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                accounts.isEmpty() -> {
                    // No Accounts Configured State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(DarkPurpleContainer, CircleShape)
                                        .border(1.dp, NeonPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = BrightLavender,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Text(
                                    text = "لا يوجد حساب IPTV منشور حالياً",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "سيتم عرض قنوات البث المباشر هنا فور نشر الحساب من قاعدة البيانات.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                filteredChannels.isEmpty() -> {
                    // Empty or Error State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = errorMessage ?: "لا توجد قنوات مطابقة",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onRefresh,
                                colors = ButtonDefaults.buttonColors(containerColor = DarkPurpleContainer),
                                border = BorderStroke(1.dp, NeonPurple),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("إعادة المحاولة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Ultra-fast Channels Grid with smooth scrolling
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("iptv_live_channels_grid"),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 86.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredChannels, key = { it.id }) { channel ->
                            IptvChannelGridCard(
                                channel = channel,
                                onClick = { onPlayChannel(channel) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 5. ACCOUNT SWITCHER MODAL BOTTOM SHEET
    if (showAccountSwitcherSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAccountSwitcherSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF08080C),
            scrimColor = Color.Black.copy(alpha = 0.75f),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "تبديل حساب IPTV",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { showAccountSwitcherSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }

                Text(
                    text = "اختر الحساب المطلوب لعرض قنوات البث المباشر الخاصة به مباشرة:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(accounts, key = { it.id }) { acc ->
                        val isCurrentActive = acc.id == selectedAccount?.id
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentActive) Color(0xFF1E1734) else DarkSurfaceVariant
                            ),
                            border = BorderStroke(
                                width = if (isCurrentActive) 1.5.dp else 1.dp,
                                color = if (isCurrentActive) NeonPurple else DarkSurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onSelectAccount(acc)
                                    showAccountSwitcherSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCurrentActive) NeonPurple.copy(alpha = 0.25f) else Color(0xFF161622),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Dns,
                                                contentDescription = null,
                                                tint = if (isCurrentActive) BrightLavender else TextMuted,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = acc.name.ifBlank { "حساب IPTV" },
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${acc.host} • ${acc.streamExtension.uppercase()}",
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isCurrentActive) {
                                    Surface(
                                        shape = RoundedCornerShape(50.dp),
                                        color = SuccessGreen.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, SuccessGreen)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "نشط",
                                                color = SuccessGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * High-performance IPTV Channel Grid Card:
 * - 1:1 squircle card with subtle border
 * - Centered channel logo with fast image caching
 * - LIVE red badge or extension pill
 * - Channel name directly underneath in clean text
 */
@Composable
fun IptvChannelGridCard(
    channel: XtremeItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val cardBrush = remember {
        Brush.verticalGradient(
            listOf(
                Color(0xFF14141E),
                Color(0xFF090910)
            )
        )
    }

    val borderColor = if (isFocused) ElectricPurple else Color(0xFF282838)
    val borderWidth = if (isFocused) 2.dp else 1.1.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && (event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.Spacebar)) {
                    onClick()
                    true
                } else false
            }
            .clickable(onClick = onClick)
            .testTag("iptv_channel_card_${channel.streamId}")
    ) {
        // 1. Squircle Card (Rounded 22dp, subtle border, centered logo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .scale(if (isFocused) 1.05f else 1f)
                .clip(RoundedCornerShape(22.dp))
                .background(cardBrush)
                .border(borderWidth, borderColor, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (channel.streamIcon.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(channel.streamIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = channel.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = channel.title.take(3).trim(),
                                color = TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    error = {
                        ChannelLogoPlaceholder(channelName = channel.title)
                    }
                )
            } else {
                ChannelLogoPlaceholder(channelName = channel.title)
            }

            // Top-right Extension Pill (e.g. M3U8 or TS)
            Surface(
                shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 20.dp),
                color = ElectricPurple.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    text = channel.containerExtension.ifBlank { "m3u8" }.uppercase(),
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // 2. Channel Title directly beneath the squircle card
        Text(
            text = channel.title,
            color = Color.White,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
    }
}
