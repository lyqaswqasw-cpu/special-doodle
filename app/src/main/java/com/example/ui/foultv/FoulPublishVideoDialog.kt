package com.example.ui.foultv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoulVideo
import com.example.ui.theme.BrightLavender
import com.example.ui.theme.DarkBackground
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

val FOUL_TV_CATEGORIES = listOf(
    "أهداف وملخصات",
    "مباريات كاملة",
    "مهارات ولقطات",
    "دوري أبطال أوروبا",
    "الدوري الإنجليزي",
    "الدوري الإسباني",
    "الدوري السعودي",
    "الكرة العربية",
    "تحليلات ومؤتمرات",
    "منوعات رياضية"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoulPublishVideoDialog(
    isOpen: Boolean,
    editingVideo: FoulVideo? = null,
    onDismiss: () -> Unit,
    onSaveVideo: (FoulVideo, Boolean) -> Unit,
    onDeleteVideo: ((String) -> Unit)? = null
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = editingVideo != null

    var title by remember(editingVideo) { mutableStateOf(editingVideo?.title ?: "") }
    var description by remember(editingVideo) { mutableStateOf(editingVideo?.description ?: "") }
    var videoUrl by remember(editingVideo) { mutableStateOf(editingVideo?.videoUrl ?: "") }
    var thumbnailUrl by remember(editingVideo) { mutableStateOf(editingVideo?.thumbnailUrl ?: "") }
    var duration by remember(editingVideo) { mutableStateOf(editingVideo?.duration ?: "") }
    var category by remember(editingVideo) { mutableStateOf(editingVideo?.category ?: FOUL_TV_CATEGORIES.first()) }
    var authorName by remember(editingVideo) { mutableStateOf(editingVideo?.authorName ?: "FOUL TV") }
    var tagsText by remember(editingVideo) { mutableStateOf(editingVideo?.tags?.joinToString(", ") ?: "") }

    var isCommentsDisabled by remember(editingVideo) { mutableStateOf(editingVideo?.isCommentsDisabled ?: false) }
    var isLikesDisabled by remember(editingVideo) { mutableStateOf(editingVideo?.isLikesDisabled ?: false) }
    var isViewsHidden by remember(editingVideo) { mutableStateOf(editingVideo?.isViewsHidden ?: false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(DarkSurfaceVariant, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isEdit) "تعديل فيديو FOUL TV" else "نشر فيديو جديد في FOUL TV",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "نشر فيديوهات احترافية مثل يوتيوب للمشاهدين",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Surface(
                    color = LiveRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Video Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                label = { Text("عنوان الفيديو (إلزامي)") },
                placeholder = { Text("مثال: ملخص وأهداف الكلاسيكو ريال مدريد وبرشلونة") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedLabelColor = BrightLavender,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("foul_video_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Video Stream URL
            OutlinedTextField(
                value = videoUrl,
                onValueChange = {
                    videoUrl = it
                    errorMessage = null
                },
                label = { Text("رابط الفيديو (MP4 / M3U / M3U8 / Direct Stream)") },
                placeholder = { Text("https://example.com/stream.m3u8 أو .m3u") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedLabelColor = BrightLavender,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("foul_video_url_input")
            )

            if (videoUrl.contains(".m3u", ignoreCase = true) || videoUrl.contains(".m3u8", ignoreCase = true)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.15f),
                    border = BorderStroke(0.8.dp, SuccessGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Text(
                            text = "رابط M3U / M3U8 متوافق مع مشغل FOUL TV ⚡",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thumbnail Image URL
            OutlinedTextField(
                value = thumbnailUrl,
                onValueChange = { thumbnailUrl = it },
                label = { Text("رابط صورة الغلاف / الصورة المصغرة (اختياري)") },
                placeholder = { Text("https://example.com/thumbnail.jpg") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedLabelColor = BrightLavender,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("وصف الفيديو وتفاصيل النشر (اختياري)") },
                placeholder = { Text("أبرز ما جاء في اللقاء والتشكيلة وأهداف المباراة...") },
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedLabelColor = BrightLavender,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Duration & Channel Name in Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("المدة (مثلاً: 12:40)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedLabelColor = BrightLavender,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("اسم القناة الناشرة") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedLabelColor = BrightLavender,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tags
            OutlinedTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                label = { Text("الكلمات المفتاحية والوسوم (مفصولة بفواصل)") },
                placeholder = { Text("أهداف, ريال مدريد, دوري الأبطال, 2026") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedLabelColor = BrightLavender,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selector Pills
            Text(
                text = "اختر تصنيف الفيديو:",
                color = BrightLavender,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(FOUL_TV_CATEGORIES.size) { index ->
                        val cat = FOUL_TV_CATEGORIES[index]
                        val isSelected = category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricPurple,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(1.dp, if (isSelected) BrightLavender else DarkSurfaceBorder),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Developer Control Switches for this specific video
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "خيارات التحكم الخاصة بالفيديو (للمطور)",
                        color = BrightLavender,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Disable comments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("توقيف / إخفاء التعليقات", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("منع المستخدمين من إضافة أو رؤية التعليقات على هذا الفيديو", color = TextMuted, fontSize = 10.5.sp)
                        }
                        Switch(
                            checked = isCommentsDisabled,
                            onCheckedChange = { isCommentsDisabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricPurple)
                        )
                    }

                    // 2. Disable / Hide likes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("إخفاء أيقونة / عدد الإعجابات", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("إخفاء زر وعداد الإعجاب عن المشاهدين لهذا الفيديو", color = TextMuted, fontSize = 10.5.sp)
                        }
                        Switch(
                            checked = isLikesDisabled,
                            onCheckedChange = { isLikesDisabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricPurple)
                        )
                    }

                    // 3. Hide Views count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("إخفاء عدد المشاهدات", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("إخفاء عداد المشاهدات من واجهة كارت الفيديو", color = TextMuted, fontSize = 10.5.sp)
                        }
                        Switch(
                            checked = isViewsHidden,
                            onCheckedChange = { isViewsHidden = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricPurple)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isEdit && onDeleteVideo != null && editingVideo != null) {
                    OutlinedButton(
                        onClick = {
                            onDeleteVideo(editingVideo.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveRed),
                        border = BorderStroke(1.dp, LiveRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .weight(0.8f)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حذف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "يرجى كتابة عنوان الفيديو أولاً"
                            return@Button
                        }
                        if (videoUrl.isBlank()) {
                            errorMessage = "يرجى إدخال رابط الفيديو (MP4 / M3U8 / Stream)"
                            return@Button
                        }

                        val parsedTags = tagsText.split(",", "،")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        val videoToSave = if (isEdit && editingVideo != null) {
                            editingVideo.copy(
                                title = title.trim(),
                                description = description.trim(),
                                videoUrl = videoUrl.trim(),
                                thumbnailUrl = thumbnailUrl.trim(),
                                duration = duration.trim(),
                                category = category.trim(),
                                authorName = authorName.trim().ifBlank { "FOUL TV" },
                                isCommentsDisabled = isCommentsDisabled,
                                isLikesDisabled = isLikesDisabled,
                                isViewsHidden = isViewsHidden,
                                tags = parsedTags
                            )
                        } else {
                            FoulVideo(
                                title = title.trim(),
                                description = description.trim(),
                                videoUrl = videoUrl.trim(),
                                thumbnailUrl = thumbnailUrl.trim(),
                                duration = duration.trim(),
                                category = category.trim(),
                                authorName = authorName.trim().ifBlank { "FOUL TV" },
                                isCommentsDisabled = isCommentsDisabled,
                                isLikesDisabled = isLikesDisabled,
                                isViewsHidden = isViewsHidden,
                                tags = parsedTags
                            )
                        }

                        isSaving = true
                        onSaveVideo(videoToSave, isEdit)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1.5f)
                        .testTag("submit_foul_video_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = if (isEdit) Icons.Default.Save else Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEdit) "حفظ التعديلات في Firebase" else "نشر الفيديو في FOUL TV",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
