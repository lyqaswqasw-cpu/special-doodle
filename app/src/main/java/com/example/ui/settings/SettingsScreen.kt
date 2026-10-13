package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.downloads.DownloadsScreen
import com.example.ui.theme.BrightLavender
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkPurpleContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import com.example.util.DownloadItem

@Composable
fun SettingsScreen(
    onOpenDeveloperPanel: () -> Unit = {},
    onOpenBranchPublisher: () -> Unit = onOpenDeveloperPanel,
    channelsCount: Int,
    xtremeAccountsCount: Int,
    onPlayDownload: (DownloadItem) -> Unit = {},
    modifier: Modifier = Modifier,
    appVersion: String = "4.0.0"
) {
    val context = LocalContext.current
    val telegramUrl = "https://t.me/jdj_q"
    var isDownloadsOpen by remember { mutableStateOf(false) }

    if (isDownloadsOpen) {
        DownloadsScreen(
            onBack = { isDownloadsOpen = false },
            onPlayDownload = onPlayDownload,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title & Header (Premium)
        com.example.ui.components.PremiumSectionHeader(
            title = "الإعدادات العامة",
            icon = Icons.Default.Settings,
            count = null,
            subtitle = "تخصيص المشاهدة، المكتبة، وقناة التواصل"
        )

        // 1. TELEGRAM OFFICIAL SOCIAL SECTION (Section 12)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, TelegramBlue.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
                    context.startActivity(intent)
                }
                .testTag("settings_telegram_channel_card")
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(TelegramBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_telegram_official),
                            contentDescription = "Telegram",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "قناة التيلجرام الرسمية",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "t.me/jdj_q • آخر التحديثات والترددات",
                            color = TelegramBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open",
                    tint = BrightLavender,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 2. التحميلات والمكتبة (Downloads & Library)
        SettingsSectionHeader(title = "المكتبة والتحميلات")
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsItemRow(
                    icon = Icons.Default.Storage,
                    iconColor = BrightLavender,
                    title = "التنزيلات والمحتوى غير المتصل",
                    subtitle = "إدارة وعرض وتنزيل الأفلام والمسلسلات محلياً",
                    badgeText = "إدارة التنزيلات",
                    onClick = { isDownloadsOpen = true }
                )
            }
        }

        // 3. مشغل الفيديو المدمج (Presentation Only - No logic changes)
        SettingsSectionHeader(title = "مشغل الفيديو والبث المباشر")
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsItemRow(
                    icon = Icons.Default.Tv,
                    iconColor = BrightLavender,
                    title = "محرك المشغل المدمج",
                    subtitle = "Android Media3 ExoPlayer مع دعم HLS / TS المباشر",
                    badgeText = "نشط",
                    badgeColor = SuccessGreen,
                    onClick = {}
                )
                HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.7.dp)
                SettingsItemRow(
                    icon = Icons.Default.Tv,
                    iconColor = BrightLavender,
                    title = "الجودة وتدوير الشاشة",
                    subtitle = "كشف تلقائي للجودات الأصلية وتدوير سريع للشاشة الكاملة",
                    onClick = {}
                )
            }
        }

        // قسم إدارة ونشر الفروع (New Section in Settings)
        SettingsSectionHeader(title = "نشر الفروع والقنوات")
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(listOf(ElectricPurple, NeonPurple, BrightLavender))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsItemRow(
                    icon = Icons.Default.AddBusiness,
                    iconColor = BrightLavender,
                    title = "نشر فرع جديد",
                    subtitle = "إضافة فرع داخل تصنيف، تعيين صورة، وتضمين قنوات داخل الفرع وبشكل مرتب جداً",
                    badgeText = "إدارة الفروع",
                    badgeColor = ElectricPurple,
                    onClick = onOpenBranchPublisher
                )
            }
        }

        // حول التطبيق والدعم (App Info & Support)
        SettingsSectionHeader(title = "حول التطبيق")
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsItemRow(
                    icon = Icons.Default.Info,
                    iconColor = BrightLavender,
                    title = "إصدار التطبيق",
                    subtitle = "Foul v$appVersion",
                    badgeText = "v$appVersion",
                    onClick = {}
                )
                HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.7.dp)
                SettingsItemRow(
                    icon = Icons.Default.VpnKey,
                    iconColor = Color(0xFF00E676),
                    title = "أمان وحماية التطبيق",
                    subtitle = "نسخة المطور - مؤمنة ومحمية بكلمة مرور مخصصة (2812246216200810)",
                    badgeText = "المطور ⚙️",
                    badgeColor = Color(0xFF00E676),
                    onClick = onOpenDeveloperPanel
                )
                HorizontalDivider(color = DarkSurfaceBorder, thickness = 0.7.dp)
                SettingsItemRow(
                    icon = Icons.Default.SupportAgent,
                    iconColor = BrightLavender,
                    title = "الدعم الفني والشكاوى",
                    subtitle = "تواصل مباشر مع فريق التطوير عبر قناة التيلجرام الرسمية",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
                        context.startActivity(intent)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = BrightLavender,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = ElectricPurple,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                    .border(0.8.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = badgeText,
                        color = if (badgeColor == SuccessGreen) SuccessGreen else BrightLavender,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = BrightLavender.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
