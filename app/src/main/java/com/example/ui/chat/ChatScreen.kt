package com.example.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.data.model.ChatMessage
import com.example.data.model.ChatUser
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private val AVAILABLE_EMOJIS = listOf("❤️", "🔥", "😂", "👍", "👏", "😮")

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    currentUser: ChatUser?,
    channel: Channel? = null,
    allChannels: List<Channel> = emptyList(),
    onSelectChannel: ((Channel) -> Unit)? = null,
    onSendMessage: (text: String, replyTo: ChatMessage?) -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
    onToggleReaction: (messageId: String, emoji: String) -> Unit,
    onRequireAuth: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    isCompactLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var messageToDeleteId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Safe insets: keyboard and navigation bars calculations
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isKeyboardOpen = imeBottom > 0.dp
    val bottomInsetPadding = if (isKeyboardOpen) 0.dp else navBottom

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-scroll to bottom when keyboard opens so conversation remains visible
    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Delete Confirmation Dialog
    if (messageToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { messageToDeleteId = null },
            title = { Text("حذف الرسالة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذه الرسالة؟", fontSize = 12.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = messageToDeleteId
                        if (id != null) {
                            onDeleteMessage(id)
                        }
                        messageToDeleteId = null
                    }
                ) {
                    Text("حذف", color = LiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDeleteId = null }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(14.dp)
        )
    }

    val currentChannelName = channel?.name?.ifBlank { "القناة" } ?: "دردشة البث"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("chat_section_container")
    ) {
        // TOP BAR: Back Arrow + Channel Title + Live indicator + Auth Profile badge
        Surface(
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Arrow Button + Channel Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = if (isCompactLandscape) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isCompactLandscape) "إغلاق الدردشة" else "رجوع للقنوات",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(SuccessGreen, CircleShape)
                            )
                            Text(
                                text = "دردشة: $currentChannelName",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "دردشة مباشرة نشطة • ${currentUser?.fullName ?: "مشاهد"}",
                            color = BrightLavender,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Direct User Identity Chip (No login needed)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("chat_user_profile_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Text(
                            text = currentUser?.fullName ?: "مشاهد مباشر",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // CHANNEL SWITCHER ROW: Quick switch between channel chats with 1 tap (hidden in compact landscape to maximize message space)
        if (!isCompactLandscape && allChannels.isNotEmpty() && onSelectChannel != null) {
            Surface(
                color = DarkSurfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(0.5.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_channel_switcher_row")
                ) {
                    items(allChannels, key = { it.id }) { ch ->
                        val isCurrent = (ch.id == channel?.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCurrent) VividViolet else DarkSurface,
                            border = BorderStroke(1.dp, if (isCurrent) NeonPurple else DarkSurfaceBorder),
                            modifier = Modifier
                                .clickable { onSelectChannel(ch) }
                                .testTag("chat_channel_chip_${ch.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                                Text(
                                    text = ch.name,
                                    color = if (isCurrent) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // MESSAGES LIST
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                            tint = NeonPurple.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "لا توجد رسائل بعد في دردشة $currentChannelName",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "كن أول من يشارك ويبدأ المحادثة الآن!",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isOwnMessage = currentUser != null && currentUser.username == msg.authorUsername

                        ChatMessageItem(
                            message = msg,
                            isOwnMessage = isOwnMessage,
                            currentUsername = currentUser?.username,
                            onReply = {
                                replyingToMessage = msg
                            },
                            onDelete = {
                                messageToDeleteId = msg.id
                            },
                            onToggleReaction = { emoji ->
                                if (currentUser == null) {
                                    onRequireAuth()
                                } else {
                                    onToggleReaction(msg.id, emoji)
                                }
                            }
                        )
                    }
                }
            }
        }

        // BOTTOM CHAT INPUT DOCK: Takes the place of the bottom navigation bar with unified styling & safe insets
        Surface(
            color = DarkSurface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            border = BorderStroke(0.8.dp, DarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .testTag("chat_bottom_input_dock")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomInsetPadding)
            ) {
                // REPLY PREVIEW BANNER (Integrated seamlessly within the dock)
                AnimatedVisibility(
                    visible = replyingToMessage != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    val replyMsg = replyingToMessage
                    if (replyMsg != null) {
                        Surface(
                            color = DarkSurfaceVariant,
                            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Reply,
                                        contentDescription = null,
                                        tint = NeonPurple,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "الرد على ${replyMsg.authorFullName}",
                                            color = NeonPurple,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = replyMsg.text,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { replyingToMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "إلغاء الرد",
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // INPUT ROW: Text field + Send/Login Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    text = "اكتب رسالة في $currentChannelName...",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            enabled = true,
                            shape = RoundedCornerShape(22.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                disabledBorderColor = DarkSurfaceBorder,
                                disabledTextColor = TextMuted,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground,
                                disabledContainerColor = DarkBackground,
                                cursorColor = NeonPurple
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("chat_message_input_field")
                        )
                    }

                    // Direct Send Button (Circular, elevated, vibrant)
                    Surface(
                        shape = CircleShape,
                        color = if (inputText.isNotBlank()) VividViolet else DarkSurfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (inputText.isNotBlank()) NeonPurple else DarkSurfaceBorder
                        ),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    val textToSend = inputText.trim()
                                    val replyTo = replyingToMessage
                                    inputText = ""
                                    replyingToMessage = null
                                    onSendMessage(textToSend, replyTo)
                                    coroutineScope.launch {
                                        if (messages.isNotEmpty()) {
                                            listState.animateScrollToItem(messages.size - 1)
                                        }
                                    }
                                }
                            }
                            .testTag("chat_send_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                tint = if (inputText.isNotBlank()) Color.White else TextMuted,
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
private fun ChatMessageItem(
    message: ChatMessage,
    isOwnMessage: Boolean,
    currentUsername: String?,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    onToggleReaction: (String) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isReactionMenuOpen by remember { mutableStateOf(false) }

    // Dynamic color derived from author username for nice aesthetic diversity
    val avatarColor = remember(message.authorUsername) {
        val colors = listOf(
            Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF3B82F6),
            Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF06B6D4)
        )
        val hash = message.authorUsername.hashCode().absoluteValue
        colors[hash % colors.size]
    }

    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    // Swipe-to-reply gesture
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(message.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX.absoluteValue > 50f) {
                            onReply()
                        }
                        offsetX = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val newOffset = offsetX + dragAmount
                        offsetX = newOffset.coerceIn(-90f, 90f)
                    }
                )
            }
    ) {
        // Swipe indicator icon behind
        if (offsetX.absoluteValue > 15f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = if (offsetX > 0) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "رد",
                    tint = NeonPurple,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // The Message Row with animated offset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) },
            horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isOwnMessage) {
                // Compact Avatar
                Surface(
                    shape = CircleShape,
                    color = avatarColor.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, avatarColor.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .size(26.dp)
                        .padding(top = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = message.authorFullName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Message Bubble Column
            Column(
                horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                // Author row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = message.authorFullName,
                        color = if (isOwnMessage) BrightLavender else TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 9.sp
                    )

                    // Delete button if own message
                    if (isOwnMessage) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(18.dp).testTag("delete_message_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الرسالة",
                                tint = LiveRed.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Main Bubble
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isOwnMessage) 12.dp else 2.dp,
                        bottomEnd = if (isOwnMessage) 2.dp else 12.dp
                    ),
                    color = if (isOwnMessage) DarkPurpleContainer else DarkSurfaceVariant,
                    border = BorderStroke(1.dp, if (isOwnMessage) NeonPurple.copy(alpha = 0.4f) else DarkSurfaceBorder),
                    modifier = Modifier
                        .padding(vertical = 1.dp)
                        .testTag("chat_bubble_${message.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        // QUOTED REPLY BANNER (Shows who was replied to)
                        if (!message.replyToText.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DarkBackground.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(18.dp)
                                            .background(NeonPurple, RoundedCornerShape(2.dp))
                                    )
                                    Column {
                                        Text(
                                            text = "↩️ رد على ${message.replyToAuthor ?: "رسالة"}:",
                                            color = NeonPurple,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = com.example.data.security.AppSecurityGuard.sanitizeComment(message.replyToText),
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Message Text (Protected and sanitized against abusive words & attacker defacement)
                        val cleanMessageText = remember(message.text) {
                            com.example.data.security.AppSecurityGuard.sanitizeComment(message.text)
                        }
                        Text(
                            text = cleanMessageText,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // REACTIONS ROW & ACTION BUTTONS
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // Existing reactions badges
                    message.reactions.forEach { (emoji, users) ->
                        if (users.isNotEmpty()) {
                            val userHasReacted = currentUsername != null && users.contains(currentUsername)
                            val badgeBg = if (userHasReacted) VividViolet.copy(alpha = 0.35f) else DarkSurface
                            val badgeBorder = if (userHasReacted) NeonPurple else DarkSurfaceBorder

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeBg,
                                border = BorderStroke(1.dp, badgeBorder),
                                modifier = Modifier
                                    .clickable { onToggleReaction(emoji) }
                                    .testTag("reaction_badge_${message.id}_$emoji")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(text = emoji, fontSize = 10.sp)
                                    Text(
                                        text = "${users.size}",
                                        color = if (userHasReacted) BrightLavender else TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Add reaction button (+)
                    Box {
                        IconButton(
                            onClick = { isReactionMenuOpen = !isReactionMenuOpen },
                            modifier = Modifier.size(20.dp).testTag("add_reaction_button_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddReaction,
                                contentDescription = "إضافة تفاعل",
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        // Compact Reaction Emoji Picker Popover
                        if (isReactionMenuOpen) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = DarkSurface,
                                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f)),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .padding(top = 22.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    AVAILABLE_EMOJIS.forEach { emoji ->
                                        Text(
                                            text = emoji,
                                            fontSize = 15.sp,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable {
                                                    onToggleReaction(emoji)
                                                    isReactionMenuOpen = false
                                                }
                                                .padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Direct Reply button (in addition to swipe)
                    IconButton(
                        onClick = onReply,
                        modifier = Modifier.size(20.dp).testTag("reply_button_${message.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = "رد على الرسالة",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
