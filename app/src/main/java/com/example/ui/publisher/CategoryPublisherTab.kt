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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Channel
import com.example.data.model.ChannelCategory
import com.example.data.util.IptvStreamFetcher
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import com.example.ui.viewmodel.PublishUiState
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CategoryPublisherTab(
    categories: List<ChannelCategory>,
    publishUiState: PublishUiState,
    onSaveCategory: (ChannelCategory, Boolean) -> Unit,
    onSaveCategoryWithChannels: ((ChannelCategory, List<Channel>) -> Unit)? = null,
    onSaveChannel: (Channel, Boolean) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onMoveCategoryToTop: (String) -> Unit,
    onMoveCategoryToSecond: (String) -> Unit,
    onMoveCategoryUp: (String) -> Unit,
    onMoveCategoryDown: (String) -> Unit,
    onShowReorderDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Category Basic Info
    var categoryName by remember { mutableStateOf("") }
    var categoryImageUrl by remember { mutableStateOf("") }
    var categoryErrorMsg by remember { mutableStateOf<String?>(null) }

    // Source Mode: 0 = M3U Link, 1 = IPTV Xtream Account, 2 = Manual only
    var sourceMode by remember { mutableIntStateOf(0) }

    // M3U Form State
    var categoryM3uUrl by remember { mutableStateOf("") }

    // IPTV Xtream Form State
    var iptvHost by remember { mutableStateOf("") }
    var iptvUsername by remember { mutableStateOf("") }
    var iptvPassword by remember { mutableStateOf("") }
    var iptvExtension by remember { mutableStateOf("m3u8") }

    // Fetched Channels State
    var isFetchingChannels by remember { mutableStateOf(false) }
    var fetchStatusMessage by remember { mutableStateOf<String?>(null) }
    val fetchedChannels = remember { mutableStateListOf<Channel>() }
    val selectedChannelMap = remember { mutableStateMapOf<String, Boolean>() }
    var channelSearchQuery by remember { mutableStateOf("") }
    var hideUnselectedChannels by remember { mutableStateOf(false) }

    val filteredFetchedChannels = remember(fetchedChannels.toList(), selectedChannelMap.toMap(), channelSearchQuery, hideUnselectedChannels) {
        val q = channelSearchQuery.trim()
        var list = fetchedChannels.toList()
        if (hideUnselectedChannels) {
            list = list.filter { selectedChannelMap[it.id] == true }
        }
        if (q.isNotBlank()) {
            list = list.filter { it.name.contains(q, ignoreCase = true) }
        }
        list
    }

    val selectedCount = selectedChannelMap.count { it.value }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Title
        item {
            Text(
                text = "إضافة تصنيف جديد مع قنوات M3U أو حساب IPTV",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "يمكنك إنشاء التصنيف واستيراد القنوات واختيار قنوات محددة أو نشر الكل مباشرة في التطبيق.",
                color = TextSecondary,
                fontSize = 11.5.sp
            )
        }

        // Category Basic Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. معلومات التصنيف الأساسية:",
                        color = BrightLavender,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("اسم التصنيف (إجباري)") },
                        placeholder = { Text("مثال: قنوات بين سبورت / قنوات SSC / قنوات عربية") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_name_input"),
                        colors = categoryOutlinedTextFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = categoryImageUrl,
                        onValueChange = { categoryImageUrl = it },
                        label = { Text("رابط صورة/أيقونة التصنيف (اختياري)") },
                        placeholder = { Text("https://example.com/icon.png") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = NeonPurple)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_image_input"),
                        colors = categoryOutlinedTextFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (categoryImageUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBackground, RoundedCornerShape(10.dp))
                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(categoryImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "معاينة الصورة",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = "معاينة أيقونة التصنيف",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Source Type Selector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkSurfaceBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. مصدر القنوات لهذا التصنيف:",
                        color = BrightLavender,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Source Mode Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sourceMode == 0,
                            onClick = {
                                sourceMode = 0
                                fetchStatusMessage = null
                            },
                            label = { Text("🔗 رابط M3U", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricPurple,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = sourceMode == 1,
                            onClick = {
                                sourceMode = 1
                                fetchStatusMessage = null
                            },
                            label = { Text("📡 حساب IPTV", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricPurple,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = sourceMode == 2,
                            onClick = {
                                sourceMode = 2
                                fetchStatusMessage = null
                            },
                            label = { Text("✍️ تصنيف يدوي", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricPurple,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Mode 0: M3U Link
                    if (sourceMode == 0) {
                        OutlinedTextField(
                            value = categoryM3uUrl,
                            onValueChange = { categoryM3uUrl = it },
                            label = { Text("رابط قائمة التشغيل M3U") },
                            placeholder = { Text("http://example.com/playlist.m3u أو .m3u8") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null, tint = AccentCyan)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_m3u_url_input"),
                            colors = categoryOutlinedTextFieldColors(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (categoryM3uUrl.isBlank()) {
                                    fetchStatusMessage = "يرجى كتابة أو لصق رابط M3U أولاً"
                                    return@Button
                                }
                                coroutineScope.launch {
                                    isFetchingChannels = true
                                    fetchStatusMessage = "جاري تحميل وقراءة قنوات M3U..."
                                    val cat = categoryName.trim().ifBlank { "عام" }
                                    val list = IptvStreamFetcher.fetchM3uChannels(categoryM3uUrl, cat)
                                    fetchedChannels.clear()
                                    selectedChannelMap.clear()
                                    if (list.isNotEmpty()) {
                                        fetchedChannels.addAll(list)
                                        // Default: select all
                                        list.forEach { selectedChannelMap[it.id] = true }
                                        fetchStatusMessage = "تم العثور على ${list.size} قناة بنجاح!"
                                    } else {
                                        fetchStatusMessage = "لم يتم العثور على قنوات في الرابط أو تعذر الاتصال بالرابط."
                                    }
                                    isFetchingChannels = false
                                }
                            },
                            enabled = !isFetchingChannels && categoryM3uUrl.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("fetch_m3u_channels_button")
                        ) {
                            if (isFetchingChannels) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جاري جلب القنوات...", fontSize = 12.sp, color = Color.White)
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("جلب واستعراض قنوات رابط M3U", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Mode 1: IPTV Xtream Codes Account
                    if (sourceMode == 1) {
                        OutlinedTextField(
                            value = iptvHost,
                            onValueChange = { iptvHost = it },
                            label = { Text("الهوست أو رابط السيرفر (Host)") },
                            placeholder = { Text("http://example.com:8080") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = AccentCyan)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("iptv_host_input"),
                            colors = categoryOutlinedTextFieldColors(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = iptvUsername,
                                onValueChange = { iptvUsername = it },
                                label = { Text("اسم المستخدم") },
                                placeholder = { Text("Username") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BrightLavender)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("iptv_username_input"),
                                colors = categoryOutlinedTextFieldColors(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = iptvPassword,
                                onValueChange = { iptvPassword = it },
                                label = { Text("كلمة المرور") },
                                placeholder = { Text("Password") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = BrightLavender)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("iptv_password_input"),
                                colors = categoryOutlinedTextFieldColors(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Stream extension selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("صيغة البث:", color = TextSecondary, fontSize = 11.5.sp)
                            FilterChip(
                                selected = iptvExtension == "m3u8",
                                onClick = { iptvExtension = "m3u8" },
                                label = { Text("m3u8 (HLS)", fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricPurple,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = iptvExtension == "ts",
                                onClick = { iptvExtension = "ts" },
                                label = { Text("ts (MPEG-TS)", fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricPurple,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        Button(
                            onClick = {
                                if (iptvHost.isBlank() || iptvUsername.isBlank() || iptvPassword.isBlank()) {
                                    fetchStatusMessage = "يرجى تعبئة الهوست واسم المستخدم وكلمة المرور"
                                    return@Button
                                }
                                coroutineScope.launch {
                                    isFetchingChannels = true
                                    fetchStatusMessage = "جاري الاتصال بسيرفر IPTV وجلب القنوات..."
                                    val cat = categoryName.trim().ifBlank { "عام" }
                                    val list = IptvStreamFetcher.fetchXtreamLiveChannels(
                                        host = iptvHost,
                                        username = iptvUsername,
                                        password = iptvPassword,
                                        categoryName = cat,
                                        streamExtension = iptvExtension
                                    )
                                    fetchedChannels.clear()
                                    selectedChannelMap.clear()
                                    if (list.isNotEmpty()) {
                                        fetchedChannels.addAll(list)
                                        // Default: select all
                                        list.forEach { selectedChannelMap[it.id] = true }
                                        fetchStatusMessage = "تم العثور على ${list.size} قناة في حساب IPTV بنجاح!"
                                    } else {
                                        fetchStatusMessage = "تعذر جلب القنوات، تأكد من صحة بيانات الحوست والاشتراك."
                                    }
                                    isFetchingChannels = false
                                }
                            },
                            enabled = !isFetchingChannels && iptvHost.isNotBlank() && iptvUsername.isNotBlank() && iptvPassword.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("fetch_iptv_channels_button")
                        ) {
                            if (isFetchingChannels) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جاري جلب قنوات IPTV...", fontSize = 12.sp, color = Color.White)
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("جلب واستعراض قنوات حساب IPTV", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Status Message
                    if (fetchStatusMessage != null) {
                        Text(
                            text = fetchStatusMessage ?: "",
                            color = if (fetchedChannels.isNotEmpty()) AccentCyan else LiveRed,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Fetched Channels List & Selection Card
        if (fetchedChannels.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131024)),
                    border = BorderStroke(1.2.dp, NeonPurple),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "3. اختيار القنوات للنشر في التطبيق:",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تم تحديد $selectedCount من أصل ${fetchedChannels.size} قناة",
                                    color = AccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = hideUnselectedChannels,
                                    onClick = { hideUnselectedChannels = !hideUnselectedChannels },
                                    label = { Text(if (hideUnselectedChannels) "إخفاء غير المحددة" else "إظهار المحددة فقط", fontSize = 10.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (hideUnselectedChannels) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
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

                                OutlinedButton(
                                    onClick = {
                                        fetchedChannels.forEach { selectedChannelMap[it.id] = true }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, NeonPurple),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحديد الكل", fontSize = 10.sp, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = {
                                        selectedChannelMap.clear()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إلغاء", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }

                        // Search Filter for channels
                        OutlinedTextField(
                            value = channelSearchQuery,
                            onValueChange = { channelSearchQuery = it },
                            placeholder = { Text("بحث في القنوات المستوردة...", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = BrightLavender, modifier = Modifier.size(16.dp))
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = categoryOutlinedTextFieldColors(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Scrollable channels list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredFetchedChannels, key = { it.id }) { channel ->
                                val isSelected = selectedChannelMap[channel.id] ?: false
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF1D1738) else DarkSurface
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) NeonPurple else Color(0xFF24223A)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedChannelMap[channel.id] = !isSelected
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                selectedChannelMap[channel.id] = checked
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ElectricPurple,
                                                uncheckedColor = TextMuted,
                                                checkmarkColor = Color.White
                                            ),
                                            modifier = Modifier.size(20.dp)
                                        )

                                        // Channel Logo / Icon
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DarkSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (channel.imageUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = channel.imageUrl,
                                                    contentDescription = channel.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Tv,
                                                    contentDescription = null,
                                                    tint = BrightLavender,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = channel.name,
                                                color = Color.White,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (channel.servers.size > 1) {
                                                Text(
                                                    text = "✨ ${channel.servers.size} جودات مدمجة: " + channel.servers.joinToString(", ") { it.name.replace("سيرفر ", "") },
                                                    color = AccentCyan,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            } else {
                                                Text(
                                                    text = "سيرفر: ${channel.servers.firstOrNull()?.streamType?.uppercase() ?: "M3U8"}",
                                                    color = TextMuted,
                                                    fontSize = 9.5.sp
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

        // Error Message
        if (categoryErrorMsg != null) {
            item {
                Text(
                    text = categoryErrorMsg ?: "",
                    color = LiveRed,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Publish Action Button
        item {
            val isLoading = (publishUiState is PublishUiState.Loading)
            Button(
                onClick = {
                    if (categoryName.isBlank()) {
                        categoryErrorMsg = "يرجى كتابة اسم التصنيف"
                        return@Button
                    }
                    if (categoryName.trim() == "الكل") {
                        categoryErrorMsg = "اسم التصنيف 'الكل' محجوز مسبقاً، يرجى اختيار اسم آخر"
                        return@Button
                    }
                    categoryErrorMsg = null

                    val catName = categoryName.trim()
                    val selectedChannelsList = fetchedChannels.filter { selectedChannelMap[it.id] == true }

                    val isCustomSelection = selectedChannelsList.isNotEmpty() && (selectedChannelsList.size < fetchedChannels.size || hideUnselectedChannels)

                    if (isCustomSelection) {
                        // Extract and pull ONLY selected channels (keeping clean channel names & servers)
                        val extractedChannels = selectedChannelsList.map { ch ->
                            ch.copy(
                                id = UUID.randomUUID().toString(),
                                category = catName,
                                branchId = ""
                            )
                        }

                        val newCategory = ChannelCategory(
                            id = UUID.randomUUID().toString(),
                            name = catName,
                            imageUrl = categoryImageUrl.trim(),
                            m3uUrl = "", // Empty m3uUrl so unselected channels are completely excluded
                            createdAt = System.currentTimeMillis()
                        )

                        if (onSaveCategoryWithChannels != null) {
                            onSaveCategoryWithChannels(newCategory, extractedChannels)
                        } else {
                            onSaveCategory(newCategory, false)
                            extractedChannels.forEach { ch ->
                                onSaveChannel(ch, false)
                            }
                        }
                    } else {
                        // Compute dynamic M3U URL for the category to load ALL channels
                        val dynamicM3uUrl = when (sourceMode) {
                            0 -> categoryM3uUrl.trim()
                            1 -> {
                                var cleanHost = iptvHost.trim()
                                if (!cleanHost.startsWith("http://", ignoreCase = true) && !cleanHost.startsWith("https://", ignoreCase = true)) {
                                    cleanHost = "http://$cleanHost"
                                }
                                cleanHost = cleanHost.trimEnd('/')
                                val u = iptvUsername.trim()
                                val p = iptvPassword.trim()
                                val ext = iptvExtension.trim().lowercase().ifBlank { "m3u8" }
                                if (cleanHost.isNotBlank() && u.isNotBlank() && p.isNotBlank()) {
                                    "$cleanHost/get.php?username=$u&password=$p&type=m3u_plus&output=$ext"
                                } else {
                                    ""
                                }
                            }
                            else -> ""
                        }

                        val newCategory = ChannelCategory(
                            id = UUID.randomUUID().toString(),
                            name = catName,
                            imageUrl = categoryImageUrl.trim(),
                            m3uUrl = dynamicM3uUrl,
                            createdAt = System.currentTimeMillis()
                        )

                        onSaveCategory(newCategory, false)
                    }

                    // Reset form
                    categoryName = ""
                    categoryImageUrl = ""
                    categoryM3uUrl = ""
                    iptvHost = ""
                    iptvUsername = ""
                    iptvPassword = ""
                    fetchedChannels.clear()
                    selectedChannelMap.clear()
                    fetchStatusMessage = null
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("publish_category_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نشر وحفظ التصنيف الذكي",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Existing Categories Management
        if (categories.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onShowReorderDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPurpleContainer),
                    border = BorderStroke(1.dp, NeonPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("reorder_categories_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تعديل ترتيب ظهور التصنيفات",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            item {
                Text(
                    text = "التصنيفات الحالية (${categories.size})",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(categories, key = { it.id }) { cat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    shape = RoundedCornerShape(12.dp),
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
                            if (cat.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = cat.imageUrl,
                                    contentDescription = cat.name,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(DarkPurpleContainer, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = BrightLavender,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = cat.name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (cat.m3uUrl.isNotBlank()) {
                                    Text(
                                        text = "🔗 رابط M3U",
                                        color = AccentCyan,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onDeleteCategory(cat.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف التصنيف",
                                tint = LiveRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun categoryOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = DarkSurfaceBorder,
    focusedLabelColor = NeonPurple,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = NeonPurple,
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface
)
