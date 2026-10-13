package com.example.ui.publisher

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FoulTvGlobalConfig
import com.example.data.model.FoulVideo
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

@Composable
fun FoulTvManagerTab(
    videos: List<FoulVideo>,
    globalConfig: FoulTvGlobalConfig,
    onPublishNewVideo: () -> Unit,
    onEditVideo: (FoulVideo) -> Unit,
    onDeleteVideo: (String) -> Unit,
    onUpdateVideoSettings: (String, Boolean, Boolean, Boolean) -> Unit,
    onUpdateGlobalConfig: (Boolean, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var hideViewsAll by remember(globalConfig) { mutableStateOf(globalConfig.hideViewsAll) }
    var hideLikesAll by remember(globalConfig) { mutableStateOf(globalConfig.hideLikesAll) }
    var disableCommentsAll by remember(globalConfig) { mutableStateOf(globalConfig.disableCommentsAll) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with Add Video button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "إدارة قسم فيديوهات FOUL TV",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تحكم شامل في الفيديوهات، الإعجابات، التعليقات، والمشاهدات",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onPublishNewVideo,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("dev_publish_foul_video_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نشر فيديو", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Global Control Settings Box (التحكم العام للكل)
        item {
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(18.dp))
                        Text(
                            text = "الإعدادات العامة لـ FOUL TV (تنطبق على جميع الفيديوهات)",
                            color = BrightLavender,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Global Switch 1: Hide Views Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("إخفاء عدد المشاهدات للكل", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("إخفاء عداد المشاهدات من جميع فيديوهات FOUL TV", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = hideViewsAll,
                            onCheckedChange = {
                                hideViewsAll = it
                                onUpdateGlobalConfig(it, hideLikesAll, disableCommentsAll)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricPurple)
                        )
                    }

                    HorizontalDivider(color = DarkSurfaceBorder)

                    // Global Switch 2: Hide / Disable Likes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("توقيف / إخفاء الإعجابات للكل", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("إخفاء زر وعداد الإعجاب لجميع الفيديوهات", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = hideLikesAll,
                            onCheckedChange = {
                                hideLikesAll = it
                                onUpdateGlobalConfig(hideViewsAll, it, disableCommentsAll)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricPurple)
                        )
                    }

                    HorizontalDivider(color = DarkSurfaceBorder)

                    // Global Switch 3: Disable Comments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("توقيف / إخفاء التعليقات للكل", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("تعطيل إضافة وقراءة التعليقات لجميع الفيديوهات", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = disableCommentsAll,
                            onCheckedChange = {
                                disableCommentsAll = it
                                onUpdateGlobalConfig(hideViewsAll, hideLikesAll, it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricPurple)
                        )
                    }
                }
            }
        }

        // 3. Videos List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الفيديوهات المنشورة (${videos.size})",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تحكم فردي لكل فيديو",
                    color = BrightLavender,
                    fontSize = 11.sp
                )
            }
        }

        // 4. Videos List with per-video controls
        if (videos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                        Text(text = "لا توجد فيديوهات منشورة في FOUL TV حتى الآن", color = TextSecondary, fontSize = 12.5.sp)
                    }
                }
            }
        } else {
            items(videos, key = { it.id }) { video ->
                var localDisableComments by remember(video.isCommentsDisabled) { mutableStateOf(video.isCommentsDisabled) }
                var localDisableLikes by remember(video.isLikesDisabled) { mutableStateOf(video.isLikesDisabled) }
                var localHideViews by remember(video.isViewsHidden) { mutableStateOf(video.isViewsHidden) }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Top info row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail / Fallback
                            Box(
                                modifier = Modifier
                                    .size(width = 72.dp, height = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (video.thumbnailUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = video.thumbnailUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(20.dp))
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
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
                                    Text(text = video.category, color = BrightLavender, fontSize = 10.sp)
                                    Text(text = "•", color = TextMuted, fontSize = 9.sp)
                                    Text(text = "${video.viewsCount} مشاهدة", color = TextSecondary, fontSize = 9.5.sp)
                                    Text(text = "•", color = TextMuted, fontSize = 9.sp)
                                    Text(text = "${video.likesCount} إعجاب", color = TextSecondary, fontSize = 9.5.sp)
                                }
                            }

                            // Actions: Edit / Delete
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { onEditVideo(video) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = BrightLavender, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = { onDeleteVideo(video.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = LiveRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = DarkSurfaceBorder.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Per-video individual toggle controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Toggle 1: Disable comments
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (localDisableComments) "التعليقات: معطلة 🚫" else "التعليقات: مفعّلة ✅",
                                    color = if (localDisableComments) LiveRed else SuccessGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Switch(
                                    checked = !localDisableComments,
                                    onCheckedChange = { isEnabled ->
                                        localDisableComments = !isEnabled
                                        onUpdateVideoSettings(video.id, !isEnabled, localDisableLikes, localHideViews)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen),
                                    modifier = Modifier.height(24.dp)
                                )
                            }

                            // Toggle 2: Disable likes
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (localDisableLikes) "الإعجاب: مخفي 🚫" else "الإعجاب: ظاهر ✅",
                                    color = if (localDisableLikes) LiveRed else SuccessGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Switch(
                                    checked = !localDisableLikes,
                                    onCheckedChange = { isEnabled ->
                                        localDisableLikes = !isEnabled
                                        onUpdateVideoSettings(video.id, localDisableComments, !isEnabled, localHideViews)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen),
                                    modifier = Modifier.height(24.dp)
                                )
                            }

                            // Toggle 3: Hide views
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (localHideViews) "المشاهدات: مخفية 🚫" else "المشاهدات: ظاهرة ✅",
                                    color = if (localHideViews) LiveRed else SuccessGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Switch(
                                    checked = !localHideViews,
                                    onCheckedChange = { isEnabled ->
                                        localHideViews = !isEnabled
                                        onUpdateVideoSettings(video.id, localDisableComments, localDisableLikes, !isEnabled)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
