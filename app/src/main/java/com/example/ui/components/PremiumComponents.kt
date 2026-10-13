package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.language.AppLanguage
import com.example.ui.language.LocalAppStrings
import com.example.ui.theme.*

@Composable
fun PremiumHeader(
    currentLanguage: AppLanguage = AppLanguage.AR,
    onOpenLanguageSelector: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    onOpenDeveloper: () -> Unit = {},
    telegramUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    // Firebase live pulsing heartbeat animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo & Title Section with premium styling
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Glowing radial background for logo
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(VividViolet.copy(alpha = 0.35f), Color.Transparent),
                                radius = 60f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.foul_app_icon_pure_1789914149620),
                        contentDescription = "Foul Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.2.dp, VividViolet, RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "FOUL",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Heartbeat pulsing light
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .scale(pulseScale)
                                    .background(SuccessGreen.copy(alpha = pulseAlpha), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(SuccessGreen, CircleShape)
                            )
                        }
                        Text(
                            text = strings.liveOnline,
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Top Bar Controls (Telegram, Language Switcher, Downloads, Developer Settings)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // 1. Official Telegram Button
                Surface(
                    shape = CircleShape,
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
                            context.startActivity(intent)
                        }
                        .testTag("top_bar_telegram_channel_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_telegram_official),
                            contentDescription = strings.telegramChannel,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // 2. Dynamic Language Switcher Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonPurple, VividViolet))),
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenLanguageSelector() }
                        .testTag("top_bar_language_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = currentLanguage.flag,
                            fontSize = 13.sp
                        )
                        Text(
                            text = currentLanguage.nativeName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3. Downloads Icon Button
                Surface(
                    shape = CircleShape,
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onOpenDownloads() }
                        .testTag("top_bar_downloads_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = strings.downloads,
                            tint = BrightLavender,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // 4. Developer / Publisher Panel Button (Exclusively visible in Developer builds, stripped in User Release)
                if (!com.example.ui.viewmodel.FootballViewModel.IS_USER_RELEASE_BUILD) {
                    Surface(
                        shape = CircleShape,
                        color = ElectricPurple.copy(alpha = 0.25f),
                        border = BorderStroke(1.2.dp, NeonPurple),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onOpenDeveloper() }
                            .testTag("top_bar_developer_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "لوحة المطور",
                                tint = BrightLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
        // Clean subtle divider underneath the top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.6.dp)
                .background(DarkSurfaceBorder.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun PremiumBottomNav(
    currentIndex: Int,
    onTabSelected: (Int) -> Unit,
    hideMatchesSchedule: Boolean = false,
    hideFoulTv: Boolean = false,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black) // Pure black as requested
    ) {
        // Sleek divider at the top of the pure black bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF242424),
                            NeonPurple.copy(alpha = 0.4f),
                            Color(0xFF242424),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val baseTabs = mutableListOf(
                Quadruple(Icons.Default.Home, strings.navHome, "nav_tab_home", 0),
                Quadruple(Icons.Default.LiveTv, "IPTV", "nav_tab_iptv", 1)
            )

            if (!hideFoulTv) {
                baseTabs.add(Quadruple(Icons.Default.PlayCircle, "FOUL TV", "nav_tab_foul_tv", 2))
            }

            baseTabs.add(Quadruple(Icons.Default.VideoLibrary, strings.navLibrary, "nav_tab_library", 3))

            if (!hideMatchesSchedule) {
                baseTabs.add(Quadruple(Icons.Default.SportsSoccer, strings.navMatches, "nav_tab_matches", 4))
            }

            baseTabs.forEach { (icon, label, tag, targetIndex) ->
                val isSelected = currentIndex == targetIndex

                // High-fidelity active coloring
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) BrightLavender else TextSecondary,
                    animationSpec = tween(250), label = "contentColor"
                )

                // Spring scale feedback on selection
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ), label = "scale"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(targetIndex) }
                        .scale(scale)
                        .testTag(tag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        // Soft active glow ring back layer
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(NeonPurple.copy(alpha = 0.25f), Color.Transparent),
                                            radius = 45f
                                        )
                                    )
                            )
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = label,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )

                    // Capsule neon slider indicator
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(if (isSelected) 16.dp else 0.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(NeonPurple, BrightLavender)
                                )
                            )
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val badgeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeAlpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(LiveRed.copy(alpha = badgeAlpha))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(Color.White, CircleShape)
        )
        Text(
            text = "مباشر",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PremiumSectionHeader(
    title: String,
    icon: ImageVector,
    count: Int? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp)
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
                // Vertical colored line
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(13.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(NeonPurple, BrightLavender)
                            )
                        )
                )

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrightLavender,
                    modifier = Modifier.size(15.dp)
                )

                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (count != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .border(0.5.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = if (title.contains("قناة") || title.contains("قنوات")) "$count قناة" else "$count",
                        color = BrightLavender,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (subtitle != null) {
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 9.dp, top = 1.dp)
            )
        }
    }
}
