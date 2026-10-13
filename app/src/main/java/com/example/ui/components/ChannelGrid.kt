package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.Channel
import com.example.data.model.ChannelBranch
import com.example.data.model.ChannelCategory
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BrightLavender
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkPurpleContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet

/**
 * Filter Chip pill matching the requested design:
 * - Selected: Beautiful, professional, colorful multi-color gradient border mix
 *   (Electric Purple -> Neon Magenta -> Vibrant Cyan) with sleek dark interior and glowing dot.
 * - Unselected: Sleek dark rounded pill, elegant subtle border, clear legible text.
 */
@Composable
fun CategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val selectionBorderGradient = remember {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF8A2BE2), // Electric Purple
                Color(0xFFFF007A), // Neon Magenta
                Color(0xFF00E5FF)  // Vibrant Cyan
            )
        )
    }

    val selectedBackgroundGradient = remember {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF281146),
                Color(0xFF160A2E)
            )
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .testTag("category_chip_$title")
            .then(
                if (isSelected) {
                    Modifier
                        .border(
                            width = 1.8.dp,
                            brush = selectionBorderGradient,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .background(selectedBackgroundGradient)
                } else {
                    Modifier
                        .border(
                            border = BorderStroke(1.dp, Color(0xFF1E1E2A)),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .background(Color(0xFF0D0D14))
                }
            )
            .padding(horizontal = 18.dp, vertical = 8.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(selectionBorderGradient, CircleShape)
                )
            }
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFF9E9EBA),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Main Home Channels Screen matching the provided reference screenshot:
 * 1. Top horizontal row of rounded category filter pills.
 * 2. 3-Column Grid of squircle cards with centered channel logos and subtle stylish borders.
 * 3. Channel title placed cleanly outside and directly beneath each squircle card.
 * 4. Clicking a channel opens the landscape full-screen player directly.
 */
@Composable
fun ChannelGrid(
    channels: List<Channel>,
    categories: List<ChannelCategory> = emptyList(),
    branches: List<ChannelBranch> = emptyList(),
    mostWatchedChannels: List<Channel> = emptyList(),
    selectedChannel: Channel?,
    onChannelClick: (Channel) -> Unit,
    onOpenPublisher: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    hideHomeAll: Boolean = false,
    hideHomeMode: String = "all",
    hideHomeCategory: String = "",
    eventBanners: List<com.example.data.model.EventBanner> = emptyList(),
    onWatchNowClick: (com.example.data.model.EventBanner) -> Unit = {}
) {
    if (hideHomeAll && hideHomeMode == "all") {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = ElectricPurple,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "القسم يفتح اذا كان هناك مباريات متواجدة",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "لا توجد مباريات جارية حالياً، يرجى العودة لاحقاً لمتابعة المباريات المباشرة.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    } else {
        val visibleCategories = remember(categories, hideHomeAll, hideHomeMode, hideHomeCategory) {
            if (hideHomeAll && hideHomeMode == "specific" && hideHomeCategory.isNotBlank()) {
                categories.filter { !it.name.trim().equals(hideHomeCategory.trim(), ignoreCase = true) }
            } else {
                categories
            }
        }

        val visibleChannels = remember(channels, hideHomeAll, hideHomeMode, hideHomeCategory) {
            if (hideHomeAll && hideHomeMode == "specific" && hideHomeCategory.isNotBlank()) {
                channels.filter { !it.category.trim().equals(hideHomeCategory.trim(), ignoreCase = true) }
            } else {
                channels
            }
        }

        // Active category selection
        var selectedCategory by remember { mutableStateOf("الكل") }
        var searchQuery by remember { mutableStateOf("") }
        var isSearchExpanded by remember { mutableStateOf(false) }
        var isCategoryLoading by remember { mutableStateOf(false) }

        LaunchedEffect(selectedCategory) {
            isCategoryLoading = true
            kotlinx.coroutines.delay(120)
            isCategoryLoading = false
        }

        // Active branch selection (When user enters a branch)
        var activeBranch by remember { mutableStateOf<ChannelBranch?>(null) }

        // All channel IDs assigned to any branch
        val branchChannelIds = remember(branches) {
            val ids = mutableSetOf<String>()
            branches.forEach { b ->
                ids.addAll(b.channelIds)
            }
            ids
        }

        // Branches belonging to currently selected category
        val categoryBranches = remember(branches, selectedCategory) {
            if (selectedCategory == "الكل") {
                emptyList()
            } else {
                branches.filter {
                    it.categoryName.trim().equals(selectedCategory.trim(), ignoreCase = true) ||
                    it.categoryId.trim().equals(selectedCategory.trim(), ignoreCase = true)
                }.sortedBy { it.order }
            }
        }

        // Standalone channels in this category (channels inside any branch are nested inside the branch)
        val standaloneChannels = remember(visibleChannels, selectedCategory, visibleCategories, branchChannelIds) {
            val baseList = if (selectedCategory == "الكل") {
                visibleChannels
            } else {
                visibleChannels.filter { ch ->
                    ch.branchId.isBlank() && !branchChannelIds.contains(ch.id)
                }
            }
            when (selectedCategory) {
                "الكل" -> {
                    val categoryOrderMap = mutableMapOf<String, Int>()
                    visibleCategories.forEachIndexed { index, cat ->
                        val trimmed = cat.name.trim()
                        if (trimmed.isNotEmpty()) {
                            categoryOrderMap[trimmed] = index
                            categoryOrderMap[trimmed.lowercase()] = index
                        }
                    }
                    baseList.sortedWith(
                        compareBy(
                            { ch ->
                                val cat = ch.category.trim()
                                categoryOrderMap[cat] ?: categoryOrderMap[cat.lowercase()] ?: 9999
                            },
                            { -it.createdAt }
                        )
                    )
                }
                else -> {
                    visibleChannels.filter { it.category.trim().equals(selectedCategory.trim(), ignoreCase = true) }
                }
            }
        }

        // Channels specifically inside active branch
        val activeBranchChannels = remember(visibleChannels, activeBranch, searchQuery) {
            val branch = activeBranch ?: return@remember emptyList()
            val list = visibleChannels.filter { ch ->
                branch.channelIds.contains(ch.id) || ch.branchId == branch.id
            }
            val q = searchQuery.trim()
            if (q.isBlank()) list else list.filter { it.name.contains(q, ignoreCase = true) }
        }

        val searchedChannels = remember(visibleChannels, standaloneChannels, searchQuery) {
            val q = searchQuery.trim()
            if (q.isBlank()) {
                standaloneChannels
            } else {
                visibleChannels.filter { ch ->
                    ch.name.contains(q, ignoreCase = true) ||
                    ch.category.contains(q, ignoreCase = true) ||
                    ch.servers.any { srv -> srv.name.contains(q, ignoreCase = true) }
                }
            }
        }

        val searchedBranches = remember(branches, categoryBranches, searchQuery) {
            val q = searchQuery.trim()
            if (q.isBlank()) {
                categoryBranches
            } else {
                branches.filter { b ->
                    b.name.contains(q, ignoreCase = true) ||
                    b.categoryName.contains(q, ignoreCase = true)
                }
            }
        }

        if (visibleChannels.isEmpty() && categoryBranches.isEmpty()) {
            if (isLoading) {
                ChannelsLoadingView(modifier = modifier)
            } else {
                EmptyChannelsView(
                    onOpenPublisher = onOpenPublisher,
                    modifier = modifier
                )
            }
        } else {
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
                // SUB-VIEW A: INSIDE A SPECIFIC BRANCH (Shows only channels of this branch + back arrow)
                if (activeBranch != null) {
                    val branch = activeBranch!!

                    // Handle device hardware/gesture back press
                    BackHandler(enabled = true) {
                        activeBranch = null
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                    ) {
                        // Sleek Header with Back Arrow (سهم الرجوع) and Branch Name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF161624),
                                border = BorderStroke(1.dp, Color(0xFF2C2C40)),
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .clickable { activeBranch = null }
                                    .testTag("branch_back_button")
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

                            Text(
                                text = branch.name,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (activeBranchChannels.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد قنوات في هذا الفرع حالياً",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridColumns),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("branch_channels_grid"),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 86.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(activeBranchChannels, key = { it.id }) { channel ->
                                    val isSelected = (channel.id == selectedChannel?.id)
                                    ChannelGridCard(
                                        channel = channel,
                                        isSelected = isSelected,
                                        onClick = { onChannelClick(channel) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // SUB-VIEW B: NORMAL VIEW WITH CATEGORIES + CHANNELS & BRANCHES IN THE GRID
                    // 0. SMART HOME EXPANDABLE SEARCH BAR
                    AnimatedVisibility(
                        visible = isSearchExpanded || searchQuery.isNotBlank(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "بحث عن قناة أو تصنيف...",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "بحث",
                                        tint = BrightLavender,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "مسح",
                                                tint = TextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricPurple,
                                    unfocusedBorderColor = DarkSurfaceBorder,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = ElectricPurple
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("home_search_bar")
                            )

                            // Close Search Bar Button
                            Surface(
                                shape = CircleShape,
                                color = DarkSurfaceVariant,
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        isSearchExpanded = false
                                        searchQuery = ""
                                    }
                                    .testTag("close_search_bar_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "إغلاق البحث",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 1. EVENT BANNERS CAROUSEL (Renders only when published, between Top Header and Category Bar)
                    if (eventBanners.isNotEmpty()) {
                        EventBannerCarousel(
                            events = eventBanners,
                            onWatchNowClick = onWatchNowClick
                        )
                    }

                    // 2. TOP CATEGORY BAR (Pinned Search Icon Button + Category Chips Carousel)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sleek Pinned Search Icon Button
                        Surface(
                            shape = CircleShape,
                            color = if (isSearchExpanded || searchQuery.isNotBlank()) ElectricPurple else DarkSurfaceVariant,
                            border = BorderStroke(1.2.dp, if (isSearchExpanded || searchQuery.isNotBlank()) NeonPurple else DarkSurfaceBorder),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    isSearchExpanded = !isSearchExpanded
                                    if (!isSearchExpanded) searchQuery = ""
                                }
                                .testTag("toggle_home_search_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "بحث القنوات",
                                    tint = if (isSearchExpanded || searchQuery.isNotBlank()) Color.White else BrightLavender,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        LazyRow(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("categories_ribbon"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                CategoryChip(
                                    title = "الكل",
                                    isSelected = selectedCategory == "الكل",
                                    onClick = {
                                        selectedCategory = "الكل"
                                        activeBranch = null
                                    }
                                )
                            }

                            items(visibleCategories, key = { it.id }) { cat ->
                                CategoryChip(
                                    title = cat.name,
                                    isSelected = selectedCategory == cat.name,
                                    onClick = {
                                        selectedCategory = cat.name
                                        activeBranch = null
                                    }
                                )
                            }
                        }
                    }

                    // 3. CATEGORY / SEARCH LOADING SPINNER
                    if (isCategoryLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = ElectricPurple,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "جاري تصفية قنوات $selectedCategory...",
                                    color = BrightLavender,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else if (searchedChannels.isEmpty() && searchedBranches.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "لا توجد قنوات أو تصنيفات مطابقة لـ '$searchQuery'" else "لا توجد قنوات في هذا التصنيف حالياً",
                                color = TextMuted,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("channels_3x3_grid"),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 86.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Branches in this category
                            items(searchedBranches, key = { "branch_${it.id}" }) { branch ->
                                BranchGridCard(
                                    branch = branch,
                                    onClick = { activeBranch = branch }
                                )
                            }

                            // 2. Standalone channels in this category
                            items(searchedChannels, key = { it.id }) { channel ->
                                val isSelected = (channel.id == selectedChannel?.id)
                                ChannelGridCard(
                                    channel = channel,
                                    isSelected = isSelected,
                                    onClick = { onChannelClick(channel) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Branch Card in the channel grid:
 * Renders in the EXACT same style as ChannelGridCard:
 * - 1:1 Squircle Card (Rounded 22dp, subtle border, centered logo)
 * - Branch title cleanly below the squircle card
 * - NO explanatory text, perfectly matching regular channels
 */
@Composable
fun BranchGridCard(
    branch: ChannelBranch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val cardBrush = remember(branch.name, branch.categoryName) {
        getChannelCardBrush(branch.name, branch.categoryName)
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
            .testTag("branch_card_${branch.id}")
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
            if (branch.imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(branch.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = branch.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        ChannelLogoPlaceholder(channelName = branch.name)
                    }
                )
            } else {
                ChannelLogoPlaceholder(channelName = branch.name)
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // 2. Branch Title directly beneath the squircle card (NO explanatory text)
        Text(
            text = branch.name,
            color = Color.White,
            fontSize = 12.sp,
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

/**
 * 3-Column Channel Item:
 * - Rounded squircle card with centered channel logo/graphic
 * - Channel title placed directly under the squircle card in clean white text
 */
@Composable
fun ChannelGridCard(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val cardBrush = remember(channel.name, channel.category) {
        getChannelCardBrush(channel.name, channel.category)
    }

    val borderColor = when {
        isSelected -> NeonPurple
        isFocused -> ElectricPurple
        else -> Color(0xFF282838)
    }
    val borderWidth = if (isSelected || isFocused) 2.dp else 1.1.dp

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
            .testTag("channel_card_${channel.id}")
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
            val cleanImgUrl = remember(channel.imageUrl) {
                val raw = channel.imageUrl.trim().removeSurrounding("\"").removeSurrounding("'").trim()
                if (raw.startsWith("//")) "https:$raw" else raw
            }

            if (cleanImgUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(cleanImgUrl)
                        .crossfade(true)
                        .setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10)")
                        .build(),
                    contentDescription = channel.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    error = {
                        ChannelLogoPlaceholder(channelName = channel.name)
                    }
                )
            } else {
                ChannelLogoPlaceholder(channelName = channel.name)
            }

            // Subtle selected tint overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NeonPurple.copy(alpha = 0.18f))
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // 2. Channel Title directly under the card
        Text(
            text = channel.name,
            color = Color.White,
            fontSize = 12.sp,
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

/**
 * Fallback stylized logo placeholder if image is absent or failing to load
 */
@Composable
fun ChannelLogoPlaceholder(channelName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SportsSoccer,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = channelName.take(3).trim(),
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Adaptive brush styling matching the screenshot's distinctive rich network colors
 */
private fun getChannelCardBrush(name: String, category: String): Brush {
    val lower = name.lowercase()
    return when {
        lower.contains("دبي") || lower.contains("dubai") -> {
            Brush.verticalGradient(listOf(Color(0xFFC62828), Color(0xFF8E0000)))
        }
        lower.contains("بحرين") || lower.contains("bahrain") -> {
            Brush.verticalGradient(listOf(Color(0xFFB71C1C), Color(0xFF6B0E17)))
        }
        lower.contains("كأس") || lower.contains("alkass") || lower.contains("kass") -> {
            Brush.verticalGradient(listOf(Color(0xFF881024), Color(0xFF4A0814)))
        }
        lower.contains("ad sport") || lower.contains("أبوظبي") || lower.contains("ابوظبي") -> {
            Brush.verticalGradient(listOf(Color(0xFF14141E), Color(0xFF0A0A10)))
        }
        lower.contains("bein") || lower.contains("بي ان") || lower.contains("بي إن") -> {
            Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFF1E0638)))
        }
        lower.contains("ssc") -> {
            Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF051D44)))
        }
        lower.contains("شاهد") || lower.contains("mbc") -> {
            Brush.verticalGradient(listOf(Color(0xFF181826), Color(0xFF0E0E16)))
        }
        else -> {
            Brush.verticalGradient(listOf(Color(0xFF181826), Color(0xFF0F0F16)))
        }
    }
}

/**
 * Backward compatibility alias if referenced anywhere
 */
@Composable
fun ChannelCard(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ChannelGridCard(
        channel = channel,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun ChannelsLoadingView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(DarkPurpleContainer, CircleShape)
                        .border(1.2.dp, VividViolet.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = NeonPurple,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "جاري تحميل القنوات...",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "يرجى الانتظار لحظات لمزامنة القنوات المباشرة",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun EmptyChannelsView(
    onOpenPublisher: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(DarkPurpleContainer, CircleShape)
                        .border(1.2.dp, VividViolet.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = "Empty Channels",
                        tint = BrightLavender,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Text(
                    text = "لا توجد قنوات حالياً",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "يرجى الانتظار حتى يتم تحميل القنوات أو التحقق من الاتصال بالإنترنت.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
