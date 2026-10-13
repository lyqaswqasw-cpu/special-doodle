package com.example.ui.matches

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.matches.MatchEvent
import com.example.data.matches.MatchItem
import com.example.data.matches.MatchPlayer
import com.example.data.matches.MatchStatRow
import com.example.data.matches.MatchStatus
import com.example.data.matches.MatchSummaryDetail
import com.example.data.matches.MatchTeamRoster
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

@Composable
fun MatchDetailFullScreen(
    match: MatchItem,
    summary: MatchSummaryDetail?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back button to return to match schedule
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: الأحداث, 1: الإحصائيات, 2: التشكيلة

    val isLive = match.status == MatchStatus.LIVE

    // Pulsing animation for live badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_detail")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlphaDetail"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // 1. TOP APP BAR WITH BACK ARROW BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Back Button (Distinct, 48dp touch target)
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF161626),
                    border = BorderStroke(1.dp, Color(0xFF28283E)),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onBack() }
                        .testTag("match_detail_back_button")
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

                // League info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (match.leagueLogo.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = match.leagueLogo,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Column {
                        Text(
                            text = match.leagueName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (match.round.isNotBlank()) {
                            Text(
                                text = match.round,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Refresh Button
            Surface(
                shape = CircleShape,
                color = Color(0xFF161626),
                border = BorderStroke(1.dp, Color(0xFF28283E)),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { onRefresh() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. HERO SCOREBOARD CARD
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131322)),
            border = BorderStroke(
                if (isLive) 1.5.dp else 1.dp,
                if (isLive) LiveRed.copy(alpha = 0.8f) else DarkSurfaceBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Home Team
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TeamLogoBox(logoUrl = match.homeTeamLogo, teamName = match.displayHomeName, size = 46)
                        Text(
                            text = match.displayHomeName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Score & Status Center Area
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        when (match.status) {
                            MatchStatus.LIVE -> {
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = LiveRed.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, LiveRed)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(LiveRed.copy(alpha = pulseAlpha), CircleShape)
                                        )
                                        Text(
                                            text = match.statusText.ifBlank { "مباشر" },
                                            color = LiveRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = "${match.homeScore ?: "0"} - ${match.awayScore ?: "0"}",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            MatchStatus.FINISHED -> {
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = Color(0xFF1E1E2E),
                                    border = BorderStroke(1.dp, Color(0xFF32324A))
                                ) {
                                    Text(
                                        text = "انتهت",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = "${match.homeScore ?: "0"} - ${match.awayScore ?: "0"}",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            MatchStatus.SCHEDULED -> {
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = DarkPurpleContainer,
                                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = match.formattedTime.ifBlank { "قادمة" },
                                        color = BrightLavender,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = "VS",
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Away Team
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TeamLogoBox(logoUrl = match.awayTeamLogo, teamName = match.displayAwayName, size = 46)
                        Text(
                            text = match.displayAwayName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Venue & Referee Info Footer
                val venueText = summary?.venue?.ifBlank { match.venue } ?: match.venue
                val refereeText = summary?.referee ?: ""
                if (venueText.isNotBlank() || refereeText.isNotBlank() || match.broadcaster.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C0C16), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (venueText.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Stadium, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                Text(text = venueText, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        if (refereeText.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Sports, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(12.dp))
                                Text(text = "الحكم: $refereeText", color = AccentCyan, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // 3. TABS SELECTOR (الأحداث والمجريات | الإحصائيات | التشكيلة)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Pair("الأحداث والمجريات", Icons.Default.Timeline),
                Pair("الإحصائيات", Icons.Default.Analytics),
                Pair("التشكيلة في الملعب", Icons.Default.Groups)
            )

            tabs.forEachIndexed { index, (title, icon) ->
                val isSelected = selectedTab == index
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) ElectricPurple else Color(0xFF141422),
                    label = "tabBg"
                )
                val borderColor = if (isSelected) BrightLavender else DarkSurfaceBorder

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = bgColor,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedTab = index }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. TAB CONTENT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                isLoading && summary == null -> {
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
                                text = "جاري جلب تفاصيل وتشكيلة المباراة الرسمية...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                else -> {
                    when (selectedTab) {
                        0 -> TwoSidedMatchEventsTab(
                            events = summary?.events ?: emptyList(),
                            homeTeamName = match.displayHomeName,
                            homeTeamLogo = match.homeTeamLogo,
                            awayTeamName = match.displayAwayName,
                            awayTeamLogo = match.awayTeamLogo,
                            matchStatus = match.status
                        )
                        1 -> MatchStatisticsTab(
                            statistics = summary?.statistics ?: emptyList(),
                            homeTeamName = match.displayHomeName,
                            awayTeamName = match.displayAwayName,
                            matchStatus = match.status
                        )
                        2 -> TacticalPitchLineupsTab(
                            homeRoster = summary?.homeRoster,
                            awayRoster = summary?.awayRoster,
                            homeTeamName = match.displayHomeName,
                            homeTeamLogo = match.homeTeamLogo,
                            awayTeamName = match.displayAwayName,
                            awayTeamLogo = match.awayTeamLogo,
                            matchStatus = match.status
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: TWO-SIDED SEPARATED EVENTS TIMELINE (مقسمة لكل فريق بشكل طولي)
// -------------------------------------------------------------
@Composable
fun TwoSidedMatchEventsTab(
    events: List<MatchEvent>,
    homeTeamName: String,
    homeTeamLogo: String,
    awayTeamName: String,
    awayTeamLogo: String,
    matchStatus: MatchStatus
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = if (matchStatus == MatchStatus.SCHEDULED) "المباراة لم تبدأ بعد" else "لا توجد أهداف أو بطاقات مسجلة حالياً",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "سيتم تحديث مجريات اللقاء من أهداف وبطاقات وتبديلات لحظة بلحظة.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Dual Team Column Headers (Split into two distinct sides)
            item(key = "two_sided_headers") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131322), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home Team Header
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompactTeamLogo(logoUrl = homeTeamLogo, teamName = homeTeamName)
                        Text(
                            text = homeTeamName,
                            color = BrightLavender,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Center Timeline Indicator
                    Box(
                        modifier = Modifier
                            .background(DarkPurpleContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "الدقيقة", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Away Team Header
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = awayTeamName,
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        CompactTeamLogo(logoUrl = awayTeamLogo, teamName = awayTeamName)
                    }
                }
            }

            // Events List
            items(events, key = { it.id }) { event ->
                TwoSidedEventRow(event = event)
            }
        }
    }
}

@Composable
fun TwoSidedEventRow(event: MatchEvent) {
    val isHome = event.isHomeTeam

    val (typeColor, typeIcon, typeLabel) = when (event.type) {
        "goal" -> Triple(Color(0xFF00E676), "⚽", "هدف")
        "red-card" -> Triple(LiveRed, "🟥", "طرد (بطاقة حمراء)")
        "yellow-card" -> Triple(Color(0xFFFFD600), "🟨", "بطاقة صفراء")
        "substitution" -> Triple(AccentCyan, "🔄", "تبديل")
        "penalty" -> Triple(Color(0xFFFF9100), "🎯", "ركلة جزاء")
        else -> Triple(BrightLavender, "📌", "حدث")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // HOME SIDE EVENT (or empty spacer if away)
        if (isHome) {
            Box(modifier = Modifier.weight(1f)) {
                ProfessionalEventCard(
                    event = event,
                    typeColor = typeColor,
                    typeIcon = typeIcon,
                    typeLabel = typeLabel,
                    isHomeSide = true
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // CENTER MINUTE BADGE
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF18182C),
            border = BorderStroke(1.dp, typeColor.copy(alpha = 0.8f)),
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = event.minute.ifBlank { "•" },
                    color = typeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // AWAY SIDE EVENT (or empty spacer if home)
        if (!isHome) {
            Box(modifier = Modifier.weight(1f)) {
                ProfessionalEventCard(
                    event = event,
                    typeColor = typeColor,
                    typeIcon = typeIcon,
                    typeLabel = typeLabel,
                    isHomeSide = false
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ProfessionalEventCard(
    event: MatchEvent,
    typeColor: Color,
    typeIcon: String,
    typeLabel: String,
    isHomeSide: Boolean
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141424)),
        border = BorderStroke(1.dp, typeColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = if (isHomeSide) Alignment.Start else Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Type header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = typeIcon, fontSize = 11.sp)
                Text(
                    text = typeLabel,
                    color = typeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Player names and substitution details
            if (event.type == "substitution" && (event.playerIn.isNotBlank() || event.playerOut.isNotBlank())) {
                if (event.playerIn.isNotBlank()) {
                    Text(
                        text = "🟢 ${event.playerIn}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (event.playerOut.isNotBlank()) {
                    Text(
                        text = "🔴 ${event.playerOut}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                if (event.playerName.isNotBlank()) {
                    Text(
                        text = event.playerName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (event.assistName.isNotBlank()) {
                    Text(
                        text = "صناعة: ${event.assistName}",
                        color = AccentCyan,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (event.playerName.isBlank() && event.text.isNotBlank()) {
                    Text(
                        text = event.text,
                        color = Color(0xFFD0D0E0),
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: STATISTICS (الاستحواذ، التسديدات، التسلل، الركنيات...)
// -------------------------------------------------------------
@Composable
fun MatchStatisticsTab(
    statistics: List<MatchStatRow>,
    homeTeamName: String,
    awayTeamName: String,
    matchStatus: MatchStatus
) {
    if (statistics.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = if (matchStatus == MatchStatus.SCHEDULED) "المباراة لم تبدأ بعد" else "لا توجد إحصائيات متاحة حالياً",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تتوفر إحصائيات الاستحواذ والتسديدات والتمريرات فور انطلاق المباراة.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Team Name Column Headers
            item(key = "stats_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = homeTeamName,
                        color = BrightLavender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "المؤشر",
                        color = TextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = awayTeamName,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            items(statistics, key = { it.name }) { stat ->
                StatBarCard(stat = stat)
            }
        }
    }
}

@Composable
fun StatBarCard(stat: MatchStatRow) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141422)),
        border = BorderStroke(1.dp, Color(0xFF24243A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.homeValue,
                    color = BrightLavender,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = stat.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = stat.awayValue,
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E1E2E))
            ) {
                val homeWeight = stat.homeProgress.coerceIn(0.05f, 0.95f)
                val awayWeight = 1f - homeWeight

                Box(
                    modifier = Modifier
                        .weight(homeWeight)
                        .height(6.dp)
                        .background(BrightLavender)
                )

                Spacer(modifier = Modifier.width(2.dp))

                Box(
                    modifier = Modifier
                        .weight(awayWeight)
                        .height(6.dp)
                        .background(AccentCyan)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: TACTICAL SOCCER PITCH (تشكيلة الفريقين معاً على نفس الملعب - نصفين)
// -------------------------------------------------------------
@Composable
fun TacticalPitchLineupsTab(
    homeRoster: MatchTeamRoster?,
    awayRoster: MatchTeamRoster?,
    homeTeamName: String,
    homeTeamLogo: String,
    awayTeamName: String,
    awayTeamLogo: String,
    matchStatus: MatchStatus
) {
    if (homeRoster == null && awayRoster == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = if (matchStatus == MatchStatus.SCHEDULED) "لم يتم الإعلان عن التشكيلة الرسمية بعد" else "تشكيلة الفريقين غير متاحة حالياً",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "عادةً ما يتم الإعلان عن التشكيلة الرسمية قبل انطلاق المباراة بساعة واحدة.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pitch Header with Formations
            item(key = "pitch_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131322), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Home Formation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompactTeamLogo(logoUrl = homeTeamLogo, teamName = homeTeamName)
                        Column {
                            Text(text = homeTeamName, color = BrightLavender, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "الخطة: ${homeRoster?.formation?.ifBlank { "4-3-3" } ?: "4-3-3"}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = "ملعب المباراة",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Away Formation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = awayTeamName, color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "الخطة: ${awayRoster?.formation?.ifBlank { "4-2-3-1" } ?: "4-2-3-1"}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        CompactTeamLogo(logoUrl = awayTeamLogo, teamName = awayTeamName)
                    }
                }
            }

            // SOCCER PITCH WITH BOTH TEAMS
            item(key = "tactical_pitch_surface") {
                SoccerPitchDualTeams(
                    homeStarters = homeRoster?.starters ?: emptyList(),
                    homeFormation = homeRoster?.formation ?: "4-3-3",
                    awayStarters = awayRoster?.starters ?: emptyList(),
                    awayFormation = awayRoster?.formation ?: "4-2-3-1"
                )
            }

            // SUBSTITUTES & BENCH SECTION
            item(key = "bench_section") {
                BenchAndSubstitutionsSection(
                    homeSubs = homeRoster?.substitutes ?: emptyList(),
                    homeTeamName = homeTeamName,
                    awaySubs = awayRoster?.substitutes ?: emptyList(),
                    awayTeamName = awayTeamName
                )
            }
        }
    }
}

/**
 * High-performance Realistic Green Soccer Pitch with tactical player nodes for BOTH teams.
 */
@Composable
fun SoccerPitchDualTeams(
    homeStarters: List<MatchPlayer>,
    homeFormation: String,
    awayStarters: List<MatchPlayer>,
    awayFormation: String
) {
    val homeRows = remember(homeStarters, homeFormation) {
        splitStartersIntoFormationRows(homeStarters, homeFormation, isHomeTeam = true)
    }
    val awayRows = remember(awayStarters, awayFormation) {
        splitStartersIntoFormationRows(awayStarters, awayFormation, isHomeTeam = false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF13361E))
            .border(2.dp, Color(0xFF2D7344), RoundedCornerShape(16.dp))
    ) {
        // 1. PITCH GRASS AND LINES CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidth = 2.dp.toPx()
            val lineColor = Color.White.copy(alpha = 0.55f)
            val darkStripeColor = Color(0xFF102E19)
            val lightStripeColor = Color(0xFF143B21)

            // Alternating grass stripes
            val stripeCount = 10
            val stripeHeight = h / stripeCount
            for (i in 0 until stripeCount) {
                drawRect(
                    color = if (i % 2 == 0) lightStripeColor else darkStripeColor,
                    topLeft = Offset(0f, i * stripeHeight),
                    size = Size(w, stripeHeight)
                )
            }

            // Outer pitch border
            val padding = 12.dp.toPx()
            drawRoundRect(
                color = lineColor,
                topLeft = Offset(padding, padding),
                size = Size(w - padding * 2, h - padding * 2),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(strokeWidth)
            )

            // Center Halfway Line
            drawLine(
                color = lineColor,
                start = Offset(padding, h / 2f),
                end = Offset(w - padding, h / 2f),
                strokeWidth = strokeWidth
            )

            // Center Circle & Center Spot
            drawCircle(
                color = lineColor,
                radius = 42.dp.toPx(),
                center = Offset(w / 2f, h / 2f),
                style = Stroke(strokeWidth)
            )
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(w / 2f, h / 2f)
            )

            // Top Penalty Box (Home Goal Area)
            val boxWidth = w * 0.58f
            val boxHeight = h * 0.16f
            drawRect(
                color = lineColor,
                topLeft = Offset((w - boxWidth) / 2f, padding),
                size = Size(boxWidth, boxHeight),
                style = Stroke(strokeWidth)
            )
            // Top Goal Area (Small 6-yard box)
            val smallBoxWidth = w * 0.32f
            val smallBoxHeight = h * 0.065f
            drawRect(
                color = lineColor,
                topLeft = Offset((w - smallBoxWidth) / 2f, padding),
                size = Size(smallBoxWidth, smallBoxHeight),
                style = Stroke(strokeWidth)
            )

            // Bottom Penalty Box (Away Goal Area)
            drawRect(
                color = lineColor,
                topLeft = Offset((w - boxWidth) / 2f, h - padding - boxHeight),
                size = Size(boxWidth, boxHeight),
                style = Stroke(strokeWidth)
            )
            // Bottom Goal Area (Small 6-yard box)
            drawRect(
                color = lineColor,
                topLeft = Offset((w - smallBoxWidth) / 2f, h - padding - smallBoxHeight),
                size = Size(smallBoxWidth, smallBoxHeight),
                style = Stroke(strokeWidth)
            )
        }

        // 2. PLAYERS ON PITCH (Top Half: Home Team | Bottom Half: Away Team)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP HALF: HOME TEAM (GK at top -> Def -> Mid -> Fwd near center)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                homeRows.forEach { rowPlayers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowPlayers.forEach { player ->
                            PitchPlayerNode(player = player, accentColor = BrightLavender)
                        }
                    }
                }
            }

            // BOTTOM HALF: AWAY TEAM (Fwd near center -> Mid -> Def -> GK at bottom)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                awayRows.forEach { rowPlayers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowPlayers.forEach { player ->
                            PitchPlayerNode(player = player, accentColor = AccentCyan)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual tactical player node positioned on the pitch.
 * Shows player photo from ESPN if available, or a sleek generic jersey placeholder with jersey number.
 */
@Composable
fun PitchPlayerNode(player: MatchPlayer, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(62.dp)
    ) {
        // Player Avatar with Jersey Number
        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF10101C),
                border = BorderStroke(1.5.dp, accentColor),
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
            ) {
                if (player.headshotUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = player.headshotUrl,
                        contentDescription = player.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = {
                            GenericPlayerAvatar(jersey = player.jersey, accentColor = accentColor)
                        }
                    )
                } else {
                    GenericPlayerAvatar(jersey = player.jersey, accentColor = accentColor)
                }
            }

            // Subbed In / Out tiny status dots
            if (player.subbedIn) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp)
                        .background(Color(0xFF00E676), CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            } else if (player.subbedOut) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp)
                        .background(LiveRed, CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Name Pill (Clean translucent background)
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.75f),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Text(
                text = player.name.substringAfterLast(" ").ifBlank { player.name },
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

/**
 * High quality fallback generic player avatar with jersey number.
 */
@Composable
fun GenericPlayerAvatar(jersey: String, accentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181828)),
        contentAlignment = Alignment.Center
    ) {
        if (jersey.isNotBlank()) {
            Text(
                text = jersey,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Splits the list of 11 starters into tactical rows according to formation:
 * E.g. "4-3-3" -> [1 GK, 4 DEF, 3 MID, 3 FWD]
 */
private fun splitStartersIntoFormationRows(
    starters: List<MatchPlayer>,
    formation: String,
    isHomeTeam: Boolean
): List<List<MatchPlayer>> {
    if (starters.isEmpty()) return emptyList()

    val gk = starters.find { it.position.contains("حارس") || it.position.contains("Goalkeeper", ignoreCase = true) }
        ?: starters.firstOrNull()

    val fieldPlayers = starters.filter { it != gk }

    val rawCounts = formation.split("-", " ").mapNotNull { it.trim().toIntOrNull() }
    val counts = if (rawCounts.isNotEmpty() && rawCounts.sum() in 9..11) {
        rawCounts
    } else {
        listOf(4, 3, 3)
    }

    val rows = mutableListOf<List<MatchPlayer>>()
    var cursor = 0

    for (count in counts) {
        val row = fieldPlayers.drop(cursor).take(count)
        if (row.isNotEmpty()) {
            rows.add(row)
            cursor += count
        }
    }

    // Append remaining players if any
    if (cursor < fieldPlayers.size) {
        rows.add(fieldPlayers.drop(cursor))
    }

    return if (isHomeTeam) {
        // Home: GK first, then DEF, MID, FWD
        listOf(listOfNotNull(gk)) + rows
    } else {
        // Away: FWD first, MID, DEF, GK last (towards bottom goal)
        rows.reversed() + listOf(listOfNotNull(gk))
    }
}

// -------------------------------------------------------------
// BENCH & SUBSTITUTIONS SECTION
// -------------------------------------------------------------
@Composable
fun BenchAndSubstitutionsSection(
    homeSubs: List<MatchPlayer>,
    homeTeamName: String,
    awaySubs: List<MatchPlayer>,
    awayTeamName: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131322)),
        border = BorderStroke(1.dp, Color(0xFF24243A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(16.dp))
                    Text(
                        text = "دكة البدلاء والتبديلات الرسمية",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Split into 2 columns for Home and Away Bench
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Home Bench Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = homeTeamName, color = BrightLavender, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    homeSubs.forEach { player ->
                        CompactBenchPlayerCard(player = player, accentColor = BrightLavender)
                    }
                }

                // Away Bench Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = awayTeamName, color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    awaySubs.forEach { player ->
                        CompactBenchPlayerCard(player = player, accentColor = AccentCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun CompactBenchPlayerCard(player: MatchPlayer, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF18182A),
        border = BorderStroke(1.dp, Color(0xFF26263E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Photo / Jersey
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10101C),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                    modifier = Modifier.size(24.dp)
                ) {
                    if (player.headshotUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = player.headshotUrl,
                            contentDescription = player.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = player.jersey.ifBlank { "•" }, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = player.jersey.ifBlank { "•" }, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (player.position.isNotBlank()) {
                        Text(
                            text = player.position,
                            color = TextMuted,
                            fontSize = 8.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            if (player.subbedIn) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF00E676).copy(alpha = 0.15f),
                    border = BorderStroke(0.8.dp, Color(0xFF00E676))
                ) {
                    Text(
                        text = "🟢 شارك",
                        color = Color(0xFF00E676),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TeamLogoBox(logoUrl: String, teamName: String, size: Int = 46) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF161626),
        border = BorderStroke(1.dp, Color(0xFF2C2C42)),
        modifier = Modifier.size(size.dp)
    ) {
        if (logoUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model = logoUrl,
                contentDescription = teamName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                error = {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = teamName.take(2).uppercase(),
                            color = BrightLavender,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = teamName.take(2).uppercase(),
                    color = BrightLavender,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
