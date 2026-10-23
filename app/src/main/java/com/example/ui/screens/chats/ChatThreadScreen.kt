package com.example.ui.screens.chats

// TODO: FLAG_SECURE for production window to prevent screenshots in sensitive chat threads
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.data.ChatConversation
import com.example.data.ChatMessage
import com.example.data.ChatPolicies
import com.example.data.MessageStatus
import com.example.ui.components.SoulAvatar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.HairlineDivider
import com.example.ui.theme.HeaderBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.SemanticOnline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChatThreadScreen(
    conversation: ChatConversation,
    onSendMessage: suspend (String) -> Boolean,
    onVideoCall: () -> Unit,
    onLoadOlderMessages: () -> Unit,
    onTypingChanged: (Boolean) -> Unit,
    onOpenAttachment: () -> Unit,
    onViewAttachment: (String, String?) -> Unit,
    onOpenInfo: () -> Unit,
    onOpenOptions: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember(conversation.id) { mutableStateOf("") }
    var isSending by remember(conversation.id) { mutableStateOf(false) }
    var fullScreenImageViewerUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val currentConversation by rememberUpdatedState(conversation)
    val currentMessageCount by rememberUpdatedState(conversation.messages.size)
    val loadOlder by rememberUpdatedState(onLoadOlderMessages)

    DisposableEffect(conversation.id) {
        onDispose { onTypingChanged(false) }
    }

    val listState = rememberLazyListState()
    val user = conversation.user
    val messages = conversation.messages.reversed() // For reverseLayout

    LaunchedEffect(listState, conversation.id) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val current = currentConversation
                if (
                    lastVisibleIndex != null &&
                    current.hasOlderMessages &&
                    !current.isLoadingOlderMessages &&
                    lastVisibleIndex >= currentMessageCount - 2
                ) {
                    loadOlder()
                }
            }
    }

    LaunchedEffect(messages.firstOrNull()?.id) {
        if (
            messages.isNotEmpty() &&
            ChatPolicies.shouldAutoScrollToLatest(listState.firstVisibleItemIndex)
        ) {
            listState.animateScrollToItem(0)
        }
    }

    fun sendDraft() {
        val text = inputText.trim()
        if (text.isBlank() || isSending) return
        scope.launch {
            isSending = true
            try {
                if (onSendMessage(text)) {
                    inputText = ""
                    onTypingChanged(false)
                }
            } finally {
                isSending = false
            }
        }
    }

    // Full-screen image viewer (bg #2A1B3D 95%, close icon)
    if (fullScreenImageViewerUrl != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF22A1B3D))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("full_screen_image_viewer")
        ) {
            AsyncImage(
                model = fullScreenImageViewerUrl,
                contentDescription = "Full Image View",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )

            IconButton(
                onClick = { fullScreenImageViewerUrl = null },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .imePadding()
            .testTag("chat_thread_screen")
    ) {
        // Custom Top Bar: back + avatar (36dp) + name + status + video + more-vert
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBackground)
                .statusBarsPadding()
                .height(60.dp)
                .border(width = 1.dp, color = HairlineDivider)
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenInfo() }
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    SoulAvatar(
                        imageUrl = user.photos.firstOrNull() ?: "",
                        size = 38.dp,
                        showOnlineDot = user.isOnline,
                        isVerified = user.verified
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = user.name,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (user.isOnline) "Online" else "Active recently",
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            color = if (user.isOnline) SemanticOnline else Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onVideoCall,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onOpenOptions,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Chat Options",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Messages LazyColumn reverseLayout
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Typing indicator
            if (conversation.isTyping) {
                item {
                    TypingIndicatorBubble()
                }
            }

            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onImageClick = { fullScreenImageViewerUrl = it },
                    onViewAttachment = onViewAttachment
                )
            }

            if (conversation.isLoadingOlderMessages) {
                item {
                    Text(
                        text = "Loading earlier messages...",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    )
                }
            }

            // Date separator pill centered
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(Color(0xFFFDF2F8))
                            .border(1.dp, HairlineDivider, PillShape)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Today",
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Input bar: attach icon + BasicTextField (pill cream) + send button (scale-in) or mic icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground)
                .border(width = 1.dp, color = HairlineDivider)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenAttachment,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach Media",
                        tint = OrchidPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(PillShape)
                        .background(ElevatedSurface)
                        .border(1.dp, Color(0xFF000000).copy(alpha = 0.1f), PillShape)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Type a message...",
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }

                    BasicTextField(
                        value = inputText,
                        onValueChange = {
                            if (ChatPolicies.isMessageLengthValid(it)) {
                                inputText = it
                                onTypingChanged(it.isNotBlank())
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            color = TextPrimary
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(OrchidPrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                sendDraft()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_field")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (inputText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AuroraHorizontalGradient)
                            .clickable(enabled = !isSending) { sendDraft() }
                            .testTag("chat_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ElevatedSurface)
                            .clickable { onOpenAttachment() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Choose an audio attachment",
                            tint = OrchidPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onImageClick: (String) -> Unit,
    onViewAttachment: (String, String?) -> Unit
) {
    val isOwn = message.isOwn
    val alignment = if (isOwn) Alignment.CenterEnd else Alignment.CenterStart

    val bubbleShape = if (isOwn) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .then(
                    if (isOwn) {
                        Modifier.background(AuroraHorizontalGradient)
                    } else {
                        Modifier
                            .background(Color.White)
                            .border(1.dp, HairlineDivider, bubbleShape)
                    }
                )
                .padding(12.dp)
        ) {
            // Optional Image Message (240dp wide, rounded)
            val attachmentUrl = message.attachmentUrl
            if (attachmentUrl != null) {
                if (message.attachmentType == null || message.attachmentType == "image") {
                    AsyncImage(
                        model = attachmentUrl,
                        contentDescription = "Photo message",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(attachmentUrl) }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isOwn) Color.White.copy(alpha = 0.16f) else ElevatedSurface)
                            .clickable {
                                onViewAttachment(attachmentUrl, message.attachmentType)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (message.attachmentType == "audio") {
                                Icons.Default.PlayArrow
                            } else {
                                Icons.Default.Description
                            },
                            contentDescription = null,
                            tint = if (isOwn) Color.White else OrchidPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (message.attachmentType == "audio") {
                                "Open audio attachment"
                            } else {
                                "Open document"
                            },
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            color = if (isOwn) Color.White else TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (message.text.isNotEmpty()) {
                Text(
                    text = message.text,
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    color = if (isOwn) Color.White else TextPrimary,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.timestamp,
                    fontFamily = InterFontFamily,
                    fontSize = 10.sp,
                    color = if (isOwn) Color.White.copy(alpha = 0.75f) else TextMuted
                )

                if (isOwn) {
                    Spacer(modifier = Modifier.width(4.dp))
                    when (message.status) {
                        MessageStatus.SENT -> {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Sent",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        MessageStatus.DELIVERED -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        MessageStatus.READ -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = Color(0xFF60A5FA), // Blue read checkmark
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot3"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .background(Color.White)
                .border(1.dp, HairlineDivider, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(OrchidPrimary.copy(alpha = dot1Alpha)))
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(OrchidPrimary.copy(alpha = dot2Alpha)))
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(OrchidPrimary.copy(alpha = dot3Alpha)))
            }
        }
    }
}
