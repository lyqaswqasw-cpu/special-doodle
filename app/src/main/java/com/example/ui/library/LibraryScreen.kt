package com.example.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SeriesInfo
import com.example.data.model.XtremeAccount
import com.example.data.model.XtremeContentType
import com.example.data.model.XtremeEpisode
import com.example.data.model.XtremeItem
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    libraryItems: List<XtremeItem>,
    selectedItem: XtremeItem?,
    seriesInfo: SeriesInfo?,
    isSeriesInfoLoading: Boolean,
    vodInfo: com.example.data.model.VodInfo?,
    isVodInfoLoading: Boolean,
    onSelectItem: (XtremeItem?) -> Unit,
    onPlayMovie: (XtremeItem) -> Unit,
    onPlayEpisode: (XtremeEpisode, XtremeItem) -> Unit,
    isLoading: Boolean,
    isAccountUpdatedError: Boolean,
    onRefresh: () -> Unit,
    accounts: List<XtremeAccount> = emptyList(),
    selectedAccount: XtremeAccount? = null,
    isAllAccountsMode: Boolean = true,
    onSelectAccount: (XtremeAccount?) -> Unit = {},
    onSelectAllAccounts: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // If an item is selected, display the dedicated full-screen Details Page
    if (selectedItem != null) {
        if (selectedItem.type == XtremeContentType.SERIES) {
            SeriesDetailsScreen(
                series = selectedItem,
                seriesInfo = seriesInfo,
                isLoading = isSeriesInfoLoading,
                onBack = { onSelectItem(null) },
                onPlayEpisode = { episode -> onPlayEpisode(episode, selectedItem) },
                onDownloadEpisode = { episode ->
                    com.example.util.DownloadHelper.startDownload(
                        context = context,
                        url = episode.streamUrl,
                        title = "${selectedItem.title} - ${episode.title}",
                        fileName = "${selectedItem.title}_${episode.title}.${episode.containerExtension}"
                    )
                },
                onRetry = { onSelectItem(selectedItem) },
                modifier = modifier
            )
        } else if (selectedItem.type == XtremeContentType.MOVIE) {
            VodDetailsScreen(
                movie = selectedItem,
                vodInfo = vodInfo,
                isLoading = isVodInfoLoading,
                onBack = { onSelectItem(null) },
                onPlayMovie = { onPlayMovie(it) },
                onDownloadMovie = { info ->
                    com.example.util.DownloadHelper.startDownload(
                        context = context,
                        url = info.streamUrl,
                        title = info.name.ifBlank { selectedItem.title },
                        fileName = "${info.name.ifBlank { selectedItem.title }}.${info.containerExtension}"
                    )
                },
                onRetry = { onSelectItem(selectedItem) },
                modifier = modifier
            )
        }
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedTabCategory by remember { mutableStateOf("all") } // "all", "featured", "movies", "series", "top_rated"
    var selectedLibraryCardId by remember { mutableStateOf<String?>(null) }
    var showAccountSwitcherSheet by remember { mutableStateOf(false) }

    // Filter non-live items
    val nonLiveItems = remember(libraryItems) {
        libraryItems.filter { it.type != XtremeContentType.LIVE }
    }

    val totalMoviesCount = remember(nonLiveItems) {
        nonLiveItems.count { it.type == XtremeContentType.MOVIE }
    }

    val totalSeriesCount = remember(nonLiveItems) {
        nonLiveItems.count { it.type == XtremeContentType.SERIES }
    }

    // Filter items based on active tab & search query
    val filteredItems = remember(nonLiveItems, searchQuery, selectedTabCategory) {
        val base = when (selectedTabCategory) {
            "movies" -> nonLiveItems.filter { it.type == XtremeContentType.MOVIE }
            "series" -> nonLiveItems.filter { it.type == XtremeContentType.SERIES }
            "featured", "top_rated" -> nonLiveItems.take(20)
            else -> nonLiveItems
        }

        val query = searchQuery.trim()
        if (query.isEmpty()) {
            base
        } else {
            base.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                        item.accountName.contains(query, ignoreCase = true) ||
                        item.categoryName.contains(query, ignoreCase = true)
            }
        }
    }

    // Featured items for Hero Horizontal Carousel
    val heroFeaturedItems = remember(nonLiveItems) {
        if (nonLiveItems.size <= 5) nonLiveItems else nonLiveItems.take(8)
    }

    val moviesItems = remember(nonLiveItems) {
        nonLiveItems.filter { it.type == XtremeContentType.MOVIE }
    }

    val seriesItems = remember(nonLiveItems) {
        nonLiveItems.filter { it.type == XtremeContentType.SERIES }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            // 1. TOP PROFESSIONAL CONTENT TRANSITIONS BAR (انتقالات المحتوى العلوية)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Scrollable Tabs Ribbon
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val tabs = listOf(
                        Triple("all", "الكل", "✨"),
                        Triple("featured", "🔥 المميز", "🔥"),
                        Triple("movies", "🎬 الأفلام", "🎬"),
                        Triple("series", "📺 المسلسلات", "📺"),
                        Triple("top_rated", "⭐ الأعلى تقييماً", "⭐")
                    )

                    items(tabs, key = { it.first }) { (id, label, _) ->
                        val isSelected = selectedTabCategory == id
                        val chipBgGradient = if (isSelected) {
                            Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF9333EA)))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFF0F0F1A), Color(0xFF141220)))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(chipBgGradient)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else Color(0xFF222234),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedTabCategory = id }
                                .padding(horizontal = 13.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFFA0A0B8),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right Action Buttons: Search Toggle & Refresh Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Search Lens Icon Button
                    Surface(
                        shape = CircleShape,
                        color = if (isSearchExpanded) ElectricPurple else Color(0xFF12121E),
                        border = BorderStroke(
                            1.dp,
                            if (isSearchExpanded) NeonPurple else Color(0xFF242438)
                        ),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) searchQuery = ""
                            }
                            .testTag("library_search_lens_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "البحث في المكتبة",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Refresh Button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF12121E),
                        border = BorderStroke(1.dp, Color(0xFF242438)),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !isLoading) { onRefresh() }
                            .testTag("library_refresh_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = NeonPurple,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(15.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث المكتبة",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. ACCOUNT SWITCHER & MOVIES/SERIES COUNTERS BAR (شريط الحسابات وعداد الأفلام والمسلسلات)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Account Switcher Pill & Broadcast Toggle
                if (accounts.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F0E1C),
                            border = BorderStroke(
                                1.dp,
                                if (isAllAccountsMode) NeonPurple else Color(0xFF28243E)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showAccountSwitcherSheet = true }
                                .testTag("library_account_switcher_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAllAccountsMode) Icons.Default.Podcasts else (if (accounts.size > 1) Icons.Default.SwapHoriz else Icons.Default.Dns),
                                    contentDescription = "تبديل حساب المكتبة",
                                    tint = if (isAllAccountsMode) AccentCyan else BrightLavender,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isAllAccountsMode) "بث جميع الحسابات" else (selectedAccount?.name?.ifBlank { "سيرفر المكتبة" } ?: "سيرفر المكتبة"),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (accounts.size > 1) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isAllAccountsMode) AccentCyan else NeonPurple,
                                        modifier = Modifier.size(15.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${accounts.size}",
                                                color = if (isAllAccountsMode) Color.Black else Color.White,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Quick Broadcast Toggle
                        if (!isAllAccountsMode) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF141224),
                                border = BorderStroke(0.8.dp, Color(0xFF2E264E)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectAllAccounts() }
                                    .testTag("library_broadcast_all_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Podcasts,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "بث الكل",
                                        color = AccentCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Right side: Movies & Series Counter Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Movies Counter Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F0E1C),
                        border = BorderStroke(0.8.dp, Color(0xFF242036))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "الأفلام:",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "$totalMoviesCount",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Series Counter Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F0E1C),
                        border = BorderStroke(0.8.dp, Color(0xFF242036))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = VividViolet,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "المسلسلات:",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "$totalSeriesCount",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Sleek Compact Search Bar (Expands smoothly)
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F0F1A),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(42.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = BrightLavender,
                            modifier = Modifier.size(18.dp)
                        )

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(NeonPurple),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("library_compact_search_input"),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "ابحث عن أي فيلم أو مسلسل بالتصنيف أو الاسم...",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "مسح",
                                    tint = TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Account Credentials Updated Banner
            if (isAccountUpdatedError) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141026)),
                    border = BorderStroke(1.2.dp, ElectricPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("library_account_updated_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(DarkPurpleContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تحديث متوفر للسيرفرات",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تم تعديل بيانات السيرفر (الهوست/اسم المستخدم). اضغط لتنزيل الجديد.",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }

                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "تحديث الآن",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Subtle loading bar when syncing in background
            if (isLoading && filteredItems.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 2.dp)
                        .background(Color(0xFF121020), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    CircularProgressIndicator(
                        color = NeonPurple,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "جاري مزامنة باقي المحتوى...",
                        color = BrightLavender,
                        fontSize = 9.5.sp
                    )
                }
            }

            // 2. MAIN BODY SCROLL VIEW (Full Cinema Experience)
            if (isLoading && filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NeonPurple,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "جاري تحميل المكتبة والبوسترات بخاصية الفائق السريع...",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "سيتم حفظ الأفلام والمسلسلات لتفتح مباشرة وبدون انتظار",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            } else if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D14)),
                        border = BorderStroke(1.dp, Color(0xFF1A1A28)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = BrightLavender,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة لبحثك" else "المكتبة فارغة حالياً",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "جرّب البحث باسم فيلم أو مسلسل آخر"
                                } else {
                                    "اضغط على زر التحديث لتحميل وحفظ أفلام ومسلسلات سيرفراتك محلياً."
                                },
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            if (searchQuery.isNotEmpty()) {
                                Button(
                                    onClick = { searchQuery = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("مسح البحث", color = Color.White, fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = onRefresh,
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تحميل المحتوى الآن", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // MAIN CINEMATIC SCROLLABLE LAYOUT
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // FEATURED HERO BANNER CAROUSEL (عرض جميل جداً بشكل تمرير أفقي)
                    if (searchQuery.isEmpty() && (selectedTabCategory == "all" || selectedTabCategory == "featured") && heroFeaturedItems.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "🔥 العروض المميزة الحصرية",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50.dp),
                                            color = LiveRed.copy(alpha = 0.2f),
                                            border = BorderStroke(0.8.dp, LiveRed)
                                        ) {
                                            Text(
                                                text = "TOP 10",
                                                color = LiveRed,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "اسحب للأفقي ◄",
                                        color = BrightLavender,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Horizontally scrollable Cinematic Banners
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp),
                                    modifier = Modifier.testTag("library_hero_carousel")
                                ) {
                                    itemsIndexed(
                                        heroFeaturedItems,
                                        key = { idx, heroItem -> "hero_${heroItem.id.ifBlank { "item" }}_$idx" }
                                    ) { _, heroItem ->
                                        HeroCarouselBannerCard(
                                            item = heroItem,
                                            onPlay = {
                                                if (heroItem.type == XtremeContentType.SERIES) {
                                                    onSelectItem(heroItem)
                                                } else {
                                                    onPlayMovie(heroItem)
                                                }
                                            },
                                            onDetails = { onSelectItem(heroItem) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // HORIZONTAL SECTION 1: MOVIES (أحدث الأفلام)
                    if (searchQuery.isEmpty() && (selectedTabCategory == "all" || selectedTabCategory == "movies") && moviesItems.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeaderTitle(
                                    title = "🎬 أحدث الأفلام السينمائية",
                                    badgeCount = "${moviesItems.size} فيلم",
                                    badgeColor = VividViolet
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    itemsIndexed(
                                        moviesItems.take(15),
                                        key = { idx, mov -> "row_mov_${mov.id.ifBlank { "item" }}_$idx" }
                                    ) { _, mov ->
                                        val isSelected = (mov.id == selectedLibraryCardId)
                                        HorizontalPosterCard(
                                            item = mov,
                                            isSelected = isSelected,
                                            onClick = {
                                                if (selectedLibraryCardId == mov.id) {
                                                    onSelectItem(mov)
                                                } else {
                                                    selectedLibraryCardId = mov.id
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // HORIZONTAL SECTION 2: SERIES (أحدث المسلسلات)
                    if (searchQuery.isEmpty() && (selectedTabCategory == "all" || selectedTabCategory == "series") && seriesItems.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeaderTitle(
                                    title = "📺 المسلسلات والدراما اليومية",
                                    badgeCount = "${seriesItems.size} مسلسل",
                                    badgeColor = ElectricPurple
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    itemsIndexed(
                                        seriesItems.take(15),
                                        key = { idx, ser -> "row_ser_${ser.id.ifBlank { "item" }}_$idx" }
                                    ) { _, ser ->
                                        val isSelected = (ser.id == selectedLibraryCardId)
                                        HorizontalPosterCard(
                                            item = ser,
                                            isSelected = isSelected,
                                            onClick = {
                                                if (selectedLibraryCardId == ser.id) {
                                                    onSelectItem(ser)
                                                } else {
                                                    selectedLibraryCardId = ser.id
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // MAIN GRID SECTION HEADER
                    item {
                        SectionHeaderTitle(
                            title = when {
                                searchQuery.isNotEmpty() -> "🔍 نتائج البحث (${filteredItems.size})"
                                selectedTabCategory == "movies" -> "🎬 جميع الأفلام"
                                selectedTabCategory == "series" -> "📺 جميع المسلسلات"
                                selectedTabCategory == "top_rated" -> "⭐ الأعلى تقييمًا"
                                else -> "🍿 جميع المحتويات السينمائية (${filteredItems.size})"
                            },
                            badgeCount = "${filteredItems.size}",
                            badgeColor = AccentCyan
                        )
                    }

                    // ALL CONTENT GRID (Properly virtualized rows for ultra-smooth scrolling without lag or memory issues)
                    val gridRows = filteredItems.chunked(3)
                    itemsIndexed(
                        items = gridRows,
                        key = { rowIndex, rowList ->
                            "grid_row_${rowIndex}_" + rowList.joinToString("_") { it.id.ifBlank { "itm" } }
                        }
                    ) { _, rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            for (item in rowItems) {
                                val isCardSelected = (item.id == selectedLibraryCardId)
                                Box(modifier = Modifier.weight(1f)) {
                                    LibraryItemCard(
                                        item = item,
                                        isSelected = isCardSelected,
                                        onClick = {
                                            if (selectedLibraryCardId == item.id) {
                                                when (item.type) {
                                                    XtremeContentType.SERIES, XtremeContentType.MOVIE -> {
                                                        onSelectItem(item)
                                                    }
                                                    else -> {
                                                        onPlayMovie(item)
                                                    }
                                                }
                                            } else {
                                                selectedLibraryCardId = item.id
                                            }
                                        }
                                    )
                                }
                            }
                            // Fill empty slots if last row has fewer items
                            if (rowItems.size < 3) {
                                for (i in 0 until (3 - rowItems.size)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Elegant Floating Selection Action Card (Shown ONLY when a movie or series card is selected)
        val selectedItemCard = remember(selectedLibraryCardId, filteredItems) {
            filteredItems.find { it.id == selectedLibraryCardId }
        }

        AnimatedVisibility(
            visible = selectedItemCard != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 10.dp, end = 10.dp, bottom = 66.dp)
        ) {
            if (selectedItemCard != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF100E1C)),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(NeonPurple, AccentCyan))),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_selected_action_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Selected item thumbnail with purple square border
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, NeonPurple, RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            if (selectedItemCard.streamIcon.isNotBlank()) {
                                AsyncImage(
                                    model = selectedItemCard.streamIcon,
                                    contentDescription = selectedItemCard.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedItemCard.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${selectedItemCard.type.label} • ${selectedItemCard.categoryName.ifBlank { "مكتبة سينمائية" }}",
                                color = BrightLavender,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Action: Play / Open
                        Button(
                            onClick = {
                                when (selectedItemCard.type) {
                                    XtremeContentType.SERIES, XtremeContentType.MOVIE -> {
                                        onSelectItem(selectedItemCard)
                                    }
                                    else -> {
                                        onPlayMovie(selectedItemCard)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedItemCard.type == XtremeContentType.SERIES) Icons.Default.Tv else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (selectedItemCard.type == XtremeContentType.SERIES) "عرض الحلقات" else "تشغيل",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Close / Deselect
                        IconButton(
                            onClick = { selectedLibraryCardId = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إلغاء التحديد",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showAccountSwitcherSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAccountSwitcherSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = DarkSurface,
                scrimColor = Color.Black.copy(alpha = 0.65f),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .size(width = 38.dp, height = 4.dp)
                            .background(Color(0xFF3B3B4F), CircleShape)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp),
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
                            Surface(
                                shape = CircleShape,
                                color = NeonPurple.copy(alpha = 0.2f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = NeonPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "تبديل حساب وسيرفر المكتبة",
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
                        text = "اختر حساباً لعرض وتحميل محتواه فقط بسرعة فائقة، أو اختر بث كافة الحسابات معاً:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // OPTION 1: بث على جميع الحسابات
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAllAccountsMode) Color(0xFF1E1734) else DarkSurfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isAllAccountsMode) 1.5.dp else 1.dp,
                            color = if (isAllAccountsMode) AccentCyan else DarkSurfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectAllAccounts()
                                showAccountSwitcherSheet = false
                            }
                            .testTag("library_sheet_all_accounts_option")
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
                                    color = if (isAllAccountsMode) AccentCyan.copy(alpha = 0.2f) else Color(0xFF161622),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Podcasts,
                                            contentDescription = null,
                                            tint = if (isAllAccountsMode) AccentCyan else TextMuted,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "📡 بث على جميع الحسابات (شامل)",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "عرض كافة أفلام ومسلسلات جميع السيرفرات (${accounts.size} سيرفر)",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isAllAccountsMode) {
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

                    // Divider / Label
                    Text(
                        text = "أو اختر حساباً مستقلاً (تحميل محتوى الحساب فقط):",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Individual Accounts List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(accounts, key = { it.id }) { acc ->
                            val isCurrentActive = !isAllAccountsMode && acc.id == selectedAccount?.id
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
                                    .testTag("library_sheet_account_${acc.id}")
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
                                                text = acc.name.ifBlank { "حساب المكتبة" },
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${acc.host} • تحميل مخصص",
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
                }
            }
        }
    }
}

/**
 * Section Header Title
 */
@Composable
private fun SectionHeaderTitle(
    title: String,
    badgeCount: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = badgeColor.copy(alpha = 0.18f),
                border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.8f))
            ) {
                Text(
                    text = badgeCount,
                    color = badgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Horizontally Scrollable Hero Banner Card (عرض جميل جداً بشكل تمرير أفقي)
 */
@Composable
fun HeroCarouselBannerCard(
    item: XtremeItem,
    onPlay: () -> Unit,
    onDetails: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0E1A)),
        border = BorderStroke(1.2.dp, Color(0xFF26243A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .width(290.dp)
            .height(165.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onDetails() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Poster / Backdrop Image
            if (item.streamIcon.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.streamIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dual Dark Gradient Overlay for Cinematic Legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.95f),
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LiveRed,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "🔥 حصرًا",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        border = BorderStroke(0.8.dp, AccentCyan)
                    ) {
                        Text(
                            text = item.type.label.uppercase(),
                            color = AccentCyan,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Title & Quick Actions
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "4K Ultra HD • ${item.categoryName.ifBlank { "أعلى تقييم" }}",
                            color = Color(0xFFD0D0E0),
                            fontSize = 9.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onPlay,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("مشاهدة الان", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onDetails,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Text("التفاصيل", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Horizontal Cinema Poster Card (2:3 aspect ratio)
 */
@Composable
fun HorizontalPosterCard(
    item: XtremeItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) NeonPurple else Color(0xFF1E1E28)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(115.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(12.dp))
                .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant)
        ) {
            if (item.streamIcon.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.streamIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.type == XtremeContentType.SERIES) Icons.Default.Tv else Icons.Default.Movie,
                        contentDescription = null,
                        tint = BrightLavender,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(VividViolet)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = item.type.label,
                    color = Color.White,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.title,
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Ultra-professional Netflix & TOD cinematic poster card for Grid
 */
@Composable
fun LibraryItemCard(
    item: XtremeItem,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) NeonPurple else Color(0xFF1E1E24)
    val borderWidth = if (isSelected) 2.5.dp else 0.8.dp

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF161224) else DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("library_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Netflix / TOD 2:3 Cinema Poster
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.68f)
                    .background(DarkSurfaceVariant)
            ) {
                if (item.streamIcon.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.streamIcon)
                            .crossfade(true)
                            .memoryCacheKey(item.streamIcon)
                            .diskCacheKey(item.streamIcon)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(DarkSurfaceVariant, DarkSurface)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (item.type) {
                                    XtremeContentType.SERIES -> Icons.Default.Tv
                                    else -> Icons.Default.Movie
                                },
                                contentDescription = null,
                                tint = NeonPurple.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = item.type.label,
                                color = TextMuted,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Netflix / TOD Double Scrim Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                // Top-End Type Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (item.type) {
                                XtremeContentType.SERIES -> ElectricPurple.copy(alpha = 0.92f)
                                else -> VividViolet.copy(alpha = 0.92f)
                            }
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.type.label,
                        color = Color.White,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Top-Start Selection Badge (ONLY when selected)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(5.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonPurple)
                            .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(9.dp)
                            )
                            Text(
                                text = "محدد",
                                color = Color.White,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NeonPurple.copy(alpha = 0.15f))
                    )
                }

                // Bottom Category / Rating accent
                if (item.categoryName.isNotBlank()) {
                    Text(
                        text = item.categoryName,
                        color = BrightLavender.copy(alpha = 0.9f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 5.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom metadata info (Title & Server/Category)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.accountName.isNotBlank()) {
                    Text(
                        text = item.accountName,
                        color = if (isSelected) BrightLavender else TextSecondary,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
