package com.example.ui.publisher

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.data.model.ChannelBranch
import com.example.data.model.ChannelCategory
import com.example.data.model.ChannelServer
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
import java.util.UUID

@Composable
fun BranchPublisherTab(
    branches: List<ChannelBranch>,
    categories: List<ChannelCategory>,
    channels: List<Channel>,
    onSaveBranch: (ChannelBranch, Boolean) -> Unit,
    onDeleteBranch: (String) -> Unit,
    onAddChannelsToBranch: (String, List<String>, String) -> Unit,
    onRemoveChannelFromBranch: (String, String) -> Unit,
    onSaveChannel: (Channel, Boolean) -> Unit,
    onPlayChannel: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current branch being managed for channels
    var managingBranchId by remember { mutableStateOf<String?>(null) }
    val currentManagingBranch = branches.find { it.id == managingBranchId }

    // Branch form state
    var editingBranch by remember { mutableStateOf<ChannelBranch?>(null) }
    var branchName by remember { mutableStateOf("") }
    var branchImageUrl by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var branchErrorMsg by remember { mutableStateOf<String?>(null) }

    // Direct Channel Creation Dialog inside Branch
    var showDirectPublishDialog by remember { mutableStateOf(false) }

    // Pick Existing Channels Dialog inside Branch
    var showPickExistingDialog by remember { mutableStateOf(false) }

    // Initialize or reset form
    fun resetBranchForm() {
        editingBranch = null
        branchName = ""
        branchImageUrl = ""
        selectedCategoryName = categories.firstOrNull()?.name ?: ""
        selectedCategoryId = categories.firstOrNull()?.id ?: ""
        branchErrorMsg = null
    }

    LaunchedEffect(categories) {
        if (selectedCategoryName.isBlank() && categories.isNotEmpty()) {
            selectedCategoryName = categories.first().name
            selectedCategoryId = categories.first().id
        }
    }

    // SUB-VIEW 1: Managing Channels Inside a Specific Branch
    if (currentManagingBranch != null) {
        val branchChannels = channels.filter { ch ->
            currentManagingBranch.channelIds.contains(ch.id) || ch.branchId == currentManagingBranch.id
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Back Button and Branch details
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonPurple, AccentCyan))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { managingBranchId = null },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkPurpleContainer),
                                border = BorderStroke(1.dp, NeonPurple),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "رجوع",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "رجوع لقائمة الفروع",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonPurple.copy(alpha = 0.2f),
                                border = BorderStroke(0.8.dp, NeonPurple)
                            ) {
                                Text(
                                    text = "التصنيف: ${currentManagingBranch.categoryName}",
                                    color = BrightLavender,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurface,
                                border = BorderStroke(1.dp, DarkSurfaceBorder),
                                modifier = Modifier.size(54.dp)
                            ) {
                                if (currentManagingBranch.imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = currentManagingBranch.imageUrl,
                                        contentDescription = currentManagingBranch.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AddBusiness,
                                            contentDescription = null,
                                            tint = NeonPurple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Column {
                                Text(
                                    text = "إدارة قنوات الفرع: ${currentManagingBranch.name}",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${branchChannels.size} قنوات موجودة حالياً داخل هذا الفرع",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons: 1. Publish Channel Directly, 2. Pick from Existing Channels
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Button 1: Publish new channel directly into branch
                    Button(
                        onClick = { showDirectPublishDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = "نشر قناة للفرع",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Button 2: Pick from existing published channels
                    OutlinedButton(
                        onClick = { showPickExistingDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AccentCyan
                        ),
                        border = BorderStroke(1.2.dp, AccentCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = "أخذ قنوات منشورة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Channels List Header
            item {
                Text(
                    text = "قنوات الفرع (${branchChannels.size})",
                    color = BrightLavender,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (branchChannels.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = BrightLavender.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "لا توجد قنوات داخل هذا الفرع حتى الآن",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اضغط على 'نشر قناة للفرع' أو 'أخذ قنوات منشورة' لإضافة القنوات فوراً",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(branchChannels, key = { it.id }) { channel ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(0.8.dp, DarkSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkSurfaceVariant,
                                    border = BorderStroke(0.8.dp, DarkSurfaceBorder),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    if (channel.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = channel.imageUrl,
                                            contentDescription = channel.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.LiveTv,
                                                contentDescription = null,
                                                tint = BrightLavender,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = channel.name,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${channel.servers.size} سيرفرات • ${channel.category}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Preview play button
                                IconButton(
                                    onClick = { onPlayChannel(channel) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل",
                                        tint = AccentCyan
                                    )
                                }

                                // Remove from branch button
                                IconButton(
                                    onClick = {
                                        onRemoveChannelFromBranch(currentManagingBranch.id, channel.id)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "إزالة من الفرع",
                                        tint = LiveRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // DIALOG: Direct Channel Publishing inside branch
        if (showDirectPublishDialog) {
            DirectChannelPublishDialog(
                branch = currentManagingBranch,
                onDismiss = { showDirectPublishDialog = false },
                onPublish = { newChannel ->
                    // Save channel with branch info
                    onSaveChannel(newChannel, false)
                    // Link to branch
                    onAddChannelsToBranch(currentManagingBranch.id, listOf(newChannel.id), currentManagingBranch.name)
                    showDirectPublishDialog = false
                }
            )
        }

        // DIALOG: Pick Existing Channels to Add into branch
        if (showPickExistingDialog) {
            PickExistingChannelsDialog(
                allChannels = channels,
                alreadyInBranchIds = currentManagingBranch.channelIds,
                onDismiss = { showPickExistingDialog = false },
                onAddChannels = { selectedIds ->
                    onAddChannelsToBranch(currentManagingBranch.id, selectedIds, currentManagingBranch.name)
                    showPickExistingDialog = false
                }
            )
        }

        return
    }

    // SUB-VIEW 2: List of Branches & New Branch Form
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title & Form Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(DarkPurpleContainer, CircleShape)
                                .border(1.dp, NeonPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddBusiness,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (editingBranch != null) "تعديل بيانات الفرع" else "نشر فرع جديد داخل تصنيف",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Field 1: Branch Name
                    OutlinedTextField(
                        value = branchName,
                        onValueChange = {
                            branchName = it
                            branchErrorMsg = null
                        },
                        label = { Text("اسم الفرع (مثال: قنوات الكأس الرياضية)") },
                        placeholder = { Text("أدخل اسم الفرع") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("branch_name_input"),
                        colors = branchTextFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Field 2: Branch Image URL
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = branchImageUrl,
                            onValueChange = {
                                branchImageUrl = it
                                branchErrorMsg = null
                            },
                            label = { Text("رابط صورة / شعار الفرع") },
                            placeholder = { Text("https://example.com/branch_logo.png") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null, tint = NeonPurple)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("branch_image_input"),
                            colors = branchTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Live image preview
                        if (branchImageUrl.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkSurfaceVariant,
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    AsyncImage(
                                        model = branchImageUrl,
                                        contentDescription = "معاينة صورة الفرع",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text(
                                    text = "معاينة شعار الفرع",
                                    color = BrightLavender,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Field 3: Select Parent Category
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "التصنيف التابع له هذا الفرع:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (categories.isEmpty()) {
                            Text(
                                text = "تنبيه: لا توجد تصنيفات حالياً. يرجى إنشاء تصنيف أولاً من تبويب 'نشر تصنيف'.",
                                color = LiveRed,
                                fontSize = 11.sp
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(categories, key = { it.id }) { cat ->
                                    val isSelected = (selectedCategoryName.trim().equals(cat.name.trim(), ignoreCase = true))
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedCategoryName = cat.name
                                            selectedCategoryId = cat.id
                                        },
                                        label = {
                                            Text(
                                                text = cat.name,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElectricPurple,
                                            selectedLabelColor = Color.White,
                                            containerColor = DarkSurfaceVariant,
                                            labelColor = TextSecondary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Error Message
                    if (branchErrorMsg != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = LiveRed, modifier = Modifier.size(16.dp))
                            Text(text = branchErrorMsg ?: "", color = LiveRed, fontSize = 11.sp)
                        }
                    }

                    // Action Buttons (Save / Cancel)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val trimmedName = branchName.trim()
                                if (trimmedName.isBlank()) {
                                    branchErrorMsg = "يرجى كتابة اسم الفرع"
                                    return@Button
                                }
                                if (selectedCategoryName.isBlank()) {
                                    branchErrorMsg = "يرجى تحديد التصنيف التابع له الفرع"
                                    return@Button
                                }

                                val branchToSave = (editingBranch ?: ChannelBranch()).copy(
                                    name = trimmedName,
                                    imageUrl = branchImageUrl.trim(),
                                    categoryId = selectedCategoryId,
                                    categoryName = selectedCategoryName.trim()
                                )

                                onSaveBranch(branchToSave, editingBranch != null)
                                resetBranchForm()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (editingBranch != null) "تحديث بيانات الفرع" else "نشر وحفظ الفرع",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (editingBranch != null) {
                            OutlinedButton(
                                onClick = { resetBranchForm() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إلغاء التعديل", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section: Existing Branches List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الفروع المنشورة (${branches.size})",
                    color = BrightLavender,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (branches.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "لا توجد فروع منشورة حالياً",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اكتب اسم الفرع ورابط الصورة أعلاه لنشر أول فرع داخل تصنيف.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(branches, key = { it.id }) { branch ->
                val branchChannelsCount = channels.count { ch ->
                    branch.channelIds.contains(ch.id) || ch.branchId == branch.id
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkSurfaceVariant,
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    if (branch.imageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = branch.imageUrl,
                                            contentDescription = branch.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AddBusiness,
                                                contentDescription = null,
                                                tint = NeonPurple,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = branch.name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = DarkPurpleContainer,
                                            border = BorderStroke(0.6.dp, NeonPurple)
                                        ) {
                                            Text(
                                                text = branch.categoryName.ifBlank { "عام" },
                                                color = BrightLavender,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "$branchChannelsCount قنوات",
                                            color = AccentCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Edit branch info
                                IconButton(
                                    onClick = {
                                        editingBranch = branch
                                        branchName = branch.name
                                        branchImageUrl = branch.imageUrl
                                        selectedCategoryName = branch.categoryName
                                        selectedCategoryId = branch.categoryId
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = BrightLavender, modifier = Modifier.size(18.dp))
                                }

                                // Delete branch
                                IconButton(
                                    onClick = { onDeleteBranch(branch.id) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = LiveRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Prominent "إدارة قنوات الفرع وإضافة قنوات" Button
                        Button(
                            onClick = { managingBranchId = branch.id },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1735)),
                            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = null,
                                    tint = NeonPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "إدارة قنوات الفرع وإضافة قنوات ($branchChannelsCount)",
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

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Dialog to publish a brand new channel directly into a branch.
 */
@Composable
private fun DirectChannelPublishDialog(
    branch: ChannelBranch,
    onDismiss: () -> Unit,
    onPublish: (Channel) -> Unit
) {
    var channelName by remember { mutableStateOf("") }
    var channelImageUrl by remember { mutableStateOf("") }
    var serverName by remember { mutableStateOf("HD") }
    var serverUrl by remember { mutableStateOf("") }
    var serverType by remember { mutableStateOf("m3u8") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.2.dp, NeonPurple),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "نشر قناة جديدة للفرع (${branch.name})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }

                HorizontalDivider(color = DarkSurfaceBorder)

                OutlinedTextField(
                    value = channelName,
                    onValueChange = {
                        channelName = it
                        errorMsg = null
                    },
                    label = { Text("اسم القناة") },
                    placeholder = { Text("مثال: الكأس 1 HD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = branchTextFieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = channelImageUrl,
                    onValueChange = { channelImageUrl = it },
                    label = { Text("رابط لوغو القناة") },
                    placeholder = { Text("https://example.com/logo.png") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = branchTextFieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = {
                        serverUrl = it
                        errorMsg = null
                    },
                    label = { Text("رابط بث القناة (m3u8 / ts / مباشر)") },
                    placeholder = { Text("http://example.com/live/stream.m3u8") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = branchTextFieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("SD", "HD", "FHD", "4K").forEach { sName ->
                        FilterChip(
                            selected = serverName == sName,
                            onClick = { serverName = sName },
                            label = { Text(sName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(text = errorMsg ?: "", color = LiveRed, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (channelName.isBlank()) {
                                errorMsg = "يرجى كتابة اسم القناة"
                                return@Button
                            }
                            if (serverUrl.isBlank()) {
                                errorMsg = "يرجى إدخال رابط البث"
                                return@Button
                            }
                            val server = ChannelServer(
                                id = UUID.randomUUID().toString(),
                                name = serverName,
                                streamUrl = serverUrl.trim(),
                                streamType = serverType
                            )
                            val newChannel = Channel(
                                id = UUID.randomUUID().toString(),
                                name = channelName.trim(),
                                imageUrl = channelImageUrl.trim(),
                                category = branch.categoryName.ifBlank { "كرة قدم" },
                                branchId = branch.id,
                                branchName = branch.name,
                                servers = listOf(server),
                                createdAt = System.currentTimeMillis()
                            )
                            onPublish(newChannel)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("نشر وإضافة للفرع", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إلغاء", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Dialog to pick existing published channels and add them to a branch.
 */
@Composable
private fun PickExistingChannelsDialog(
    allChannels: List<Channel>,
    alreadyInBranchIds: List<String>,
    onDismiss: () -> Unit,
    onAddChannels: (List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }

    val filtered = remember(allChannels, searchQuery) {
        if (searchQuery.isBlank()) {
            allChannels
        } else {
            allChannels.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.2.dp, AccentCyan),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 580.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "أخذ قنوات من المنشورات وإضافتها للفرع",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن قناة بالاسم...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = branchTextFieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "حدد القنوات المراد إضافتها (${selectedIds.size} محددة):",
                    color = BrightLavender,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.id }) { ch ->
                        val isAlreadyInBranch = alreadyInBranchIds.contains(ch.id)
                        val isChecked = selectedIds.contains(ch.id) || isAlreadyInBranch

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChecked) DarkPurpleContainer.copy(alpha = 0.5f) else DarkSurfaceVariant,
                            border = BorderStroke(0.6.dp, if (isChecked) NeonPurple else DarkSurfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isAlreadyInBranch) {
                                        if (selectedIds.contains(ch.id)) {
                                            selectedIds.remove(ch.id)
                                        } else {
                                            selectedIds.add(ch.id)
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkSurface,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        if (ch.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = ch.imageUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.LiveTv, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = ch.name,
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${ch.category} • ${ch.servers.size} سيرفرات",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (isAlreadyInBranch) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SuccessGreen.copy(alpha = 0.2f),
                                        border = BorderStroke(0.6.dp, SuccessGreen)
                                    ) {
                                        Text(
                                            text = "موجودة بالفرع",
                                            color = SuccessGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Checkbox(
                                        checked = selectedIds.contains(ch.id),
                                        onCheckedChange = { checked ->
                                            if (checked) selectedIds.add(ch.id) else selectedIds.remove(ch.id)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = NeonPurple,
                                            uncheckedColor = DarkSurfaceBorder
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (selectedIds.isNotEmpty()) {
                                onAddChannels(selectedIds.toList())
                            }
                        },
                        enabled = selectedIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "إضافة ${selectedIds.size} قنوات للفرع",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إلغاء", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun branchTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = DarkSurfaceBorder,
    focusedLabelColor = NeonPurple,
    unfocusedLabelColor = TextMuted,
    cursorColor = NeonPurple,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = DarkSurfaceVariant,
    unfocusedContainerColor = DarkSurfaceVariant
)
