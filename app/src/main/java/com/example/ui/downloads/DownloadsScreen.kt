package com.example.ui.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.util.DownloadHelper
import com.example.util.DownloadItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onPlayDownload: (DownloadItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var downloads by remember { mutableStateOf<List<DownloadItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: الكل, 1: المكتملة, 2: قيد التحميل

    fun refreshList() {
        downloads = DownloadHelper.getDownloads(context)
        isLoading = false
    }

    // Initial load
    LaunchedEffect(Unit) {
        refreshList()
    }

    // Auto-poll while downloading or pending items exist
    LaunchedEffect(downloads) {
        val hasActive = downloads.any { it.isRunning || it.isPending || it.isPaused }
        if (hasActive) {
            delay(1200)
            refreshList()
        }
    }

    val filteredDownloads = remember(downloads, selectedFilterTab) {
        when (selectedFilterTab) {
            1 -> downloads.filter { it.isCompleted }
            2 -> downloads.filter { it.isRunning || it.isPending || it.isPaused }
            else -> downloads
        }
    }

    val totalSize = remember(downloads) {
        downloads.sumOf { it.bytesDownloaded }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("downloads_screen")
    ) {
        // Top App Bar
        Surface(
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                            .border(1.dp, DarkSurfaceBorder, CircleShape)
                            .testTag("downloads_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "التنزيلات والمكتبة المحلية",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${downloads.size} عنصر • الحجم المحفوظ: ${DownloadHelper.formatBytes(totalSize)}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Open System Downloads folder
                    IconButton(
                        onClick = { DownloadHelper.openSystemDownloads(context) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "مجلد التنزيلات",
                            tint = VividViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = {
                            isLoading = true
                            refreshList()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Feature Banner: Auto-Resume & Offline Capability
        Surface(
            color = DarkSurfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "التنزيل يحفظ محلياً على الجهاز ويستأنف تلقائياً عند انقطاع وعودة الإنترنت، ويعمل بدون نت فور اكتماله.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "الكل (${downloads.size})",
                "المكتملة (${downloads.count { it.isCompleted }})",
                "قيد التحميل (${downloads.count { it.isRunning || it.isPending || it.isPaused }})"
            )
            tabs.forEachIndexed { index, label ->
                FilterChip(
                    selected = (selectedFilterTab == index),
                    onClick = { selectedFilterTab = index },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selectedFilterTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedFilterTab == index) Color.White else TextSecondary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurface,
                        selectedContainerColor = NeonPurple,
                        labelColor = TextSecondary,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = DarkSurfaceBorder,
                        selectedBorderColor = NeonPurple,
                        enabled = true,
                        selected = (selectedFilterTab == index)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Content: List or Empty State
        if (filteredDownloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(DarkPurpleContainer.copy(alpha = 0.5f), CircleShape)
                                .border(1.dp, NeonPurple.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = if (selectedFilterTab == 0) "لا توجد أي تنزيلات حالياً" else "لا توجد عناصر في هذا القسم",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "يمكنك تنزيل الأفلام وحلقات المسلسلات مباشرة من قسم المكتبة لمشاهدتها في أي وقت دون الحاجة إلى اتصال بالإنترنت.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceVariant,
                            border = BorderStroke(1.dp, DarkSurfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    DownloadHelper.openSystemDownloads(context)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = VividViolet,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "فتح مجلد تنزيلات الجهاز",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDownloads, key = { it.downloadId }) { item ->
                    DownloadItemCard(
                        item = item,
                        onPlay = { onPlayDownload(item) },
                        onRetry = {
                            DownloadHelper.retryDownload(context, item.downloadId)
                            refreshList()
                        },
                        onDelete = {
                            DownloadHelper.deleteDownload(context, item.downloadId)
                            refreshList()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadItemCard(
    item: DownloadItem,
    onPlay: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("download_item_${item.downloadId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Leading Icon + Title
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (item.isCompleted) SuccessGreen.copy(alpha = 0.15f)
                                else if (item.isFailed) LiveRed.copy(alpha = 0.15f)
                                else if (item.isWaitingForNetwork) Color(0xFFFF9800).copy(alpha = 0.15f)
                                else DarkPurpleContainer,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (item.isCompleted) SuccessGreen.copy(alpha = 0.4f)
                                else if (item.isFailed) LiveRed.copy(alpha = 0.4f)
                                else if (item.isWaitingForNetwork) Color(0xFFFF9800).copy(alpha = 0.4f)
                                else NeonPurple.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.isRunning) {
                            CircularProgressIndicator(
                                progress = { if (item.totalBytes > 0) item.bytesDownloaded.toFloat() / item.totalBytes.toFloat() else 0f },
                                color = NeonPurple,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp),
                            )
                        } else if (item.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "مكتمل",
                                tint = SuccessGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        } else if (item.isFailed) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "فشل",
                                tint = LiveRed,
                                modifier = Modifier.size(22.dp)
                            )
                        } else if (item.isWaitingForNetwork) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "بانتظار الإنترنت",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Downloading,
                                contentDescription = "قيد الانتظار",
                                tint = VividViolet,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.statusDescription,
                                color = if (item.isCompleted) SuccessGreen
                                else if (item.isFailed) LiveRed
                                else if (item.isWaitingForNetwork) Color(0xFFFF9800)
                                else VividViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "•",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            val downloadedStr = DownloadHelper.formatBytes(item.bytesDownloaded)
                            val totalStr = if (item.totalBytes > 0) DownloadHelper.formatBytes(item.totalBytes) else ""
                            Text(
                                text = if (totalStr.isNotBlank()) "$downloadedStr / $totalStr" else downloadedStr,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isFailed || item.isPaused) {
                        IconButton(
                            onClick = onRetry,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "إعادة المحاولة",
                                tint = NeonPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Delete Icon
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف التنزيل",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Progress Bar if running or waiting
            if (item.isRunning || (item.totalBytes > 0 && !item.isCompleted && !item.isFailed)) {
                LinearProgressIndicator(
                    progress = { if (item.totalBytes > 0) (item.bytesDownloaded.toFloat() / item.totalBytes.toFloat()).coerceIn(0f, 1f) else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (item.isWaitingForNetwork) Color(0xFFFF9800) else NeonPurple,
                    trackColor = DarkSurfaceVariant,
                )
            }

            // Action Buttons: If completed, show "مشاهدة الآن أوفلاين (بدون إنترنت)" button!
            if (item.isCompleted) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NeonPurple,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlay() }
                        .testTag("play_download_${item.downloadId}")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مشاهدة الآن أوفلاين (بدون إنترنت)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (item.isFailed) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRetry() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = LiveRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إعادة محاولة التنزيل",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
