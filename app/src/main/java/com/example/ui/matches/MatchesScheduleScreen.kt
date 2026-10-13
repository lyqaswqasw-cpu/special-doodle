package com.example.ui.matches

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.data.matches.MatchItem
import com.example.data.matches.MatchLeagueHeader
import com.example.data.matches.MatchStatus
import com.example.data.matches.MatchSummaryDetail
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BrightLavender
import com.example.ui.theme.DarkPurpleContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DateItem(
    val dateYyyyMmDd: String,
    val dayName: String,
    val dayNumber: String,
    val monthName: String,
    val isToday: Boolean
)

@Composable
fun MatchesScheduleScreen(
    matches: List<MatchItem>,
    selectedDate: String,
    selectedFilter: String,
    isLoading: Boolean,
    errorMessage: String?,
    selectedMatchDetail: MatchItem?,
    matchSummary: MatchSummaryDetail?,
    isSummaryLoading: Boolean,
    onSelectDate: (String) -> Unit,
    onSelectFilter: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenMatchDetail: (MatchItem) -> Unit,
    onCloseMatchDetail: () -> Unit,
    onRefreshSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    // If a match is clicked, display the dedicated full-screen page with back arrow
    if (selectedMatchDetail != null) {
        MatchDetailFullScreen(
            match = selectedMatchDetail,
            summary = matchSummary,
            isLoading = isSummaryLoading,
            onBack = onCloseMatchDetail,
            onRefresh = onRefreshSummary,
            modifier = modifier
        )
        return
    }

    var isCalendarPickerOpen by remember { mutableStateOf(false) }

    // Pulsing transition for live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_fixtures")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlphaFixtures"
    )

    // Calculate dates list around selected date (3 days back, today, 4 days forward)
    val dateItems = remember {
        val list = mutableListOf<DateItem>()
        val cal = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -2)
        for (i in 0..7) {
            val date = cal.time
            val yyyyMmDd = SimpleDateFormat("yyyyMMdd", Locale.US).format(date)
            val isToday = yyyyMmDd == todayStr

            val dayName = when {
                isToday -> "اليوم"
                i == 0 -> SimpleDateFormat("E", Locale("ar")).format(date)
                i == 1 -> "أمس"
                i == 3 -> "غداً"
                else -> SimpleDateFormat("E", Locale("ar")).format(date)
            }
            val dayNum = SimpleDateFormat("d", Locale("ar")).format(date)
            val month = SimpleDateFormat("MMM", Locale("ar")).format(date)

            list.add(DateItem(yyyyMmDd, dayName, dayNum, month, isToday))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Filter matches (Clean leagues without Gulf Cup)
    val filteredMatches = remember(matches, selectedFilter) {
        when (selectedFilter) {
            "all" -> matches
            "live" -> matches.filter { it.status == MatchStatus.LIVE }
            else -> matches.filter { it.leagueId == selectedFilter }
        }
    }

    // Group by League
    val groupedByLeague = remember(filteredMatches) {
        val map = linkedMapOf<String, MutableList<MatchItem>>()
        for (m in filteredMatches) {
            map.getOrPut(m.leagueName) { mutableListOf() }.add(m)
        }
        map.map { (leagueName, list) ->
            val first = list.first()
            MatchLeagueHeader(
                id = first.leagueId,
                name = leagueName,
                logo = first.leagueLogo,
                isGulfCup = false,
                matches = list
            )
        }
    }

    val liveCount = remember(matches) { matches.count { it.status == MatchStatus.LIVE } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // 1. TOP HEADER (No "جدول المباريات" text; only live indicator, calendar icon, and refresh button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Live Count Pill (if any matches are live)
            if (liveCount > 0) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = LiveRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, LiveRed)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(LiveRed.copy(alpha = pulseAlpha), CircleShape)
                        )
                        Text(
                            text = "$liveCount مباشر الآن",
                            color = LiveRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Right: Calendar Picker & Refresh Buttons (Clean, visible, compact)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Calendar Picker Icon Button (Small, clear, in the top bar only)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF141424),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isCalendarPickerOpen = true }
                        .testTag("matches_top_calendar_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "اختيار التاريخ من التقويم",
                            tint = BrightLavender,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Refresh Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF141424),
                    border = BorderStroke(1.dp, Color(0xFF24243A)),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onRefresh() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = AccentCyan,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(15.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث",
                                tint = AccentCyan,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. DATES SELECTOR STRIP (Compact horizontal scroll of days, WITHOUT calendar pill)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(dateItems, key = { it.dateYyyyMmDd }) { item ->
                val isSelected = item.dateYyyyMmDd == selectedDate
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) ElectricPurple else Color(0xFF12121E),
                    label = "dateBg"
                )
                val borderColor = if (isSelected) BrightLavender else DarkSurfaceBorder

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = bgColor,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectDate(item.dateYyyyMmDd) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.dayName,
                            color = if (isSelected) Color.White else if (item.isToday) AccentCyan else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected || item.isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "${item.dayNumber} ${item.monthName}",
                            color = if (isSelected) Color.White else Color(0xFFD0D0E0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. COMPACT FILTER CHIPS (All, Live, League IDs — Gulf Cup removed)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                CompactFilterChip(
                    title = "الكل (${matches.size})",
                    isSelected = selectedFilter == "all",
                    onClick = { onSelectFilter("all") }
                )
            }

            if (liveCount > 0) {
                item {
                    CompactFilterChip(
                        title = "🔴 مباشر ($liveCount)",
                        isSelected = selectedFilter == "live",
                        highlightColor = LiveRed,
                        onClick = { onSelectFilter("live") }
                    )
                }
            }

            val leagues = listOf(
                Pair("eng.1", "الدوري الإنجليزي"),
                Pair("esp.1", "الدوري الإسباني"),
                Pair("uefa.champions", "دوري أبطال أوروبا"),
                Pair("ksa.1", "دوري روشن السعودي"),
                Pair("ita.1", "الدوري الإيطالي"),
                Pair("ger.1", "الدوري الألماني"),
                Pair("fra.1", "الدوري الفرنسي"),
                Pair("afc.champions", "دوري أبطال آسيا")
            )

            for ((id, name) in leagues) {
                item(key = id) {
                    CompactFilterChip(
                        title = name,
                        isSelected = selectedFilter == id,
                        onClick = { onSelectFilter(id) }
                    )
                }
            }
        }

        // 4. MAIN MATCHES LIST
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                isLoading && matches.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = NeonPurple,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "جاري تحميل جدول المباريات والنتائج...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                filteredMatches.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = DarkPurpleContainer,
                                    border = BorderStroke(1.dp, NeonPurple),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.SportsSoccer,
                                            contentDescription = null,
                                            tint = BrightLavender,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "لا توجد مباريات مجدولة لهذا التاريخ",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "اختر تاريخاً آخر من الشريط العلوي، أو اضغط تحديث لجلب أحدث المواعيد.",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )

                                Button(
                                    onClick = onRefresh,
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحديث", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        groupedByLeague.forEach { leagueGroup ->
                            item(key = "header_${leagueGroup.name}") {
                                CompactLeagueHeaderCard(leagueGroup = leagueGroup)
                            }

                            items(leagueGroup.matches, key = { it.id }) { match ->
                                CompactMatchCardItem(
                                    match = match,
                                    pulseAlpha = pulseAlpha,
                                    onClick = { onOpenMatchDetail(match) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
    }

    // 5. CALENDAR PICKER DIALOG
    if (isCalendarPickerOpen) {
        CalendarPickerDialog(
            initialDateYyyyMmDd = selectedDate,
            onDismiss = { isCalendarPickerOpen = false },
            onDateSelected = { pickedDate ->
                isCalendarPickerOpen = false
                onSelectDate(pickedDate)
            }
        )
    }
}

// -------------------------------------------------------------
// COMPACT LEAGUE HEADER (Slim, elegant bar)
// -------------------------------------------------------------
@Composable
fun CompactLeagueHeaderCard(leagueGroup: MatchLeagueHeader) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF12121E),
        border = BorderStroke(1.dp, Color(0xFF222234)),
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (leagueGroup.logo.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = leagueGroup.logo,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(
                    text = leagueGroup.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFF1A1A2E),
                modifier = Modifier.size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${leagueGroup.matches.size}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPACT MATCH CARD (Small, neat, no oversized fonts or buttons)
// -------------------------------------------------------------
@Composable
fun CompactMatchCardItem(
    match: MatchItem,
    pulseAlpha: Float,
    onClick: () -> Unit
) {
    val isLive = match.status == MatchStatus.LIVE
    val borderColor = if (isLive) LiveRed.copy(alpha = 0.8f) else DarkSurfaceBorder

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLive) Color(0xFF17111D) else DarkSurfaceVariant
        ),
        border = BorderStroke(if (isLive) 1.2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("match_card_${match.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            // Main Teams & Score Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team
                Row(
                    modifier = Modifier.weight(1.1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompactTeamLogo(logoUrl = match.homeTeamLogo, teamName = match.displayHomeName)
                    Text(
                        text = match.displayHomeName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Center Score or Scheduled Time Area
                Column(
                    modifier = Modifier.weight(0.8f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    when (match.status) {
                        MatchStatus.LIVE -> {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = LiveRed.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, LiveRed)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(LiveRed.copy(alpha = pulseAlpha), CircleShape)
                                    )
                                    Text(
                                        text = match.statusText.ifBlank { "مباشر" },
                                        color = LiveRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "${match.homeScore ?: "0"} - ${match.awayScore ?: "0"}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        MatchStatus.FINISHED -> {
                            Text(
                                text = "انتهت",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${match.homeScore ?: "0"} - ${match.awayScore ?: "0"}",
                                color = Color(0xFFE2E2F0),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        MatchStatus.SCHEDULED -> {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = DarkPurpleContainer,
                                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = match.formattedTime.ifBlank { "قادمة" },
                                    color = BrightLavender,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "VS",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Away Team
                Row(
                    modifier = Modifier.weight(1.1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.displayAwayName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CompactTeamLogo(logoUrl = match.awayTeamLogo, teamName = match.displayAwayName)
                }
            }

            // Subtle Footer Info: Broadcaster or Stadium (if present)
            if (match.broadcaster.isNotBlank() || match.venue.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (match.broadcaster.isNotBlank()) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = match.broadcaster, color = AccentCyan, fontSize = 9.sp, maxLines = 1)
                    } else if (match.venue.isNotBlank()) {
                        Icon(Icons.Default.Stadium, contentDescription = null, tint = TextMuted, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = match.venue, color = TextMuted, fontSize = 9.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPACT TEAM LOGO
// -------------------------------------------------------------
@Composable
fun CompactTeamLogo(logoUrl: String, teamName: String) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF161626),
        border = BorderStroke(1.dp, Color(0xFF28283E)),
        modifier = Modifier.size(28.dp)
    ) {
        if (logoUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model = logoUrl,
                contentDescription = teamName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                error = {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = teamName.take(1).uppercase(),
                            color = BrightLavender,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = teamName.take(1).uppercase(),
                    color = BrightLavender,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// COMPACT FILTER CHIP
// -------------------------------------------------------------
@Composable
fun CompactFilterChip(
    title: String,
    isSelected: Boolean,
    highlightColor: Color? = null,
    onClick: () -> Unit
) {
    val activeColor = highlightColor ?: ElectricPurple
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.25f) else Color(0xFF131320),
        border = BorderStroke(
            1.dp,
            if (isSelected) activeColor else DarkSurfaceBorder
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            color = if (isSelected) (highlightColor ?: BrightLavender) else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

// -------------------------------------------------------------
// CALENDAR PICKER DIALOG (Month navigation, Arabic weekdays & days grid)
// -------------------------------------------------------------
@Composable
fun CalendarPickerDialog(
    initialDateYyyyMmDd: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var viewingYear by remember {
        val c = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
            val d = sdf.parse(initialDateYyyyMmDd)
            if (d != null) c.time = d
        } catch (_: Exception) {}
        mutableStateOf(c.get(Calendar.YEAR))
    }

    var viewingMonth by remember {
        val c = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
            val d = sdf.parse(initialDateYyyyMmDd)
            if (d != null) c.time = d
        } catch (_: Exception) {}
        mutableStateOf(c.get(Calendar.MONTH))
    }

    val todayCal = remember { Calendar.getInstance() }
    val todayYyyyMmDd = remember { SimpleDateFormat("yyyyMMdd", Locale.US).format(todayCal.time) }

    val monthName = remember(viewingYear, viewingMonth) {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, viewingYear)
            set(Calendar.MONTH, viewingMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        SimpleDateFormat("MMMM yyyy", Locale("ar")).format(c.time)
    }

    val daysInMonth = remember(viewingYear, viewingMonth) {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, viewingYear)
            set(Calendar.MONTH, viewingMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        val startOffset = firstDayOfWeek % 7
        Pair(maxDays, startOffset)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13131F)),
            border = BorderStroke(1.2.dp, NeonPurple.copy(alpha = 0.5f)),
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
                // Header: Title & Close
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
                            shape = RoundedCornerShape(8.dp),
                            color = DarkPurpleContainer,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = BrightLavender,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "اختيار التاريخ من التقويم",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }

                // Month Navigation Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A2B), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            if (viewingMonth == 0) {
                                viewingMonth = 11
                                viewingYear -= 1
                            } else {
                                viewingMonth -= 1
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "الشهر السابق", tint = AccentCyan)
                    }

                    Text(
                        text = monthName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            if (viewingMonth == 11) {
                                viewingMonth = 0
                                viewingYear += 1
                            } else {
                                viewingMonth += 1
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "الشهر القادم", tint = AccentCyan)
                    }
                }

                // Weekday Headers (Saturday to Friday)
                val weekDays = listOf("سبت", "أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (day in weekDays) {
                        Text(
                            text = day,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Days Grid
                val (maxDays, startOffset) = daysInMonth
                val totalCells = ((maxDays + startOffset + 6) / 7) * 7
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (row in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNum = cellIndex - startOffset + 1
                                if (dayNum in 1..maxDays) {
                                    val dateStr = String.format(Locale.US, "%04d%02d%02d", viewingYear, viewingMonth + 1, dayNum)
                                    val isSelected = dateStr == initialDateYyyyMmDd
                                    val isToday = dateStr == todayYyyyMmDd

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isSelected -> ElectricPurple
                                            isToday -> Color(0xFF1E2838)
                                            else -> Color.Transparent
                                        },
                                        border = if (isToday && !isSelected) BorderStroke(1.dp, AccentCyan) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onDateSelected(dateStr)
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$dayNum",
                                                color = when {
                                                    isSelected -> Color.White
                                                    isToday -> AccentCyan
                                                    else -> Color(0xFFD6D6E6)
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }

                // Quick Shortcuts Row (Today, Yesterday, Tomorrow)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val yestStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(calYesterday.time)

                    val calTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                    val tomStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(calTomorrow.time)

                    Button(
                        onClick = { onDateSelected(todayYyyyMmDd) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("اليوم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onDateSelected(yestStr) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C2C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("أمس", fontSize = 11.sp, color = TextSecondary)
                    }

                    Button(
                        onClick = { onDateSelected(tomStr) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C2C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("غداً", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
