package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatConversationEntity
import com.example.data.model.MessageType
import com.example.ui.components.AttachmentMenu
import com.example.ui.components.MessageBubble
import com.example.ui.theme.AmbientLiquidBackdrop
import com.example.ui.theme.GlassEmeraldGlowBorder
import com.example.ui.theme.GlassSpecularWhiteBorder
import com.example.ui.theme.WhatsAppBadgeGreen
import com.example.ui.theme.accessibleTouchTarget
import com.example.ui.theme.liquidGlass
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun ChatDetailScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val chat = uiState.selectedChat ?: return

    ChatDetailContent(
        viewModel = viewModel,
        chat = chat,
        isTwoPane = false,
        onCloseChat = { viewModel.navigateBack() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailContent(
    viewModel: ChatViewModel,
    chat: ChatConversationEntity,
    isTwoPane: Boolean,
    onCloseChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.currentChatMessages.collectAsState()
    // rememberSaveable ensures typed drafts are not lost during screen rotation or split-screen resize
    var inputText by rememberSaveable { mutableStateOf("") }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    BackHandler {
        if (showAttachmentMenu) {
            showAttachmentMenu = false
        } else {
            onCloseChat()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Liquid Glass Chat Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF16232D).copy(alpha = 0.92f),
                                Color(0xFF0F1820).copy(alpha = 0.82f)
                            )
                        )
                    )
                    .then(if (!isTwoPane) Modifier.statusBarsPadding() else Modifier.padding(top = 4.dp))
                    .drawBehind {
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.22f),
                                    WhatsAppBadgeGreen.copy(alpha = 0.40f),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button on phone, Close pane on tablet
                IconButton(
                    onClick = onCloseChat,
                    modifier = Modifier
                        .accessibleTouchTarget()
                        .testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = if (isTwoPane) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if (isTwoPane) "Close conversation pane" else "Back to chat list",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Avatar with prominent green status indicator
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF00A884), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = chat.peerName.firstOrNull()?.uppercaseChar() ?: 'X'
                        Text(
                            text = initial.toString(),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (chat.isOnline) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(13.dp)
                                .background(Color(0xFF090E11), shape = CircleShape)
                                .padding(1.5.dp)
                                .background(WhatsAppBadgeGreen, shape = CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Peer Name & Status
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { }
                ) {
                    Text(
                        text = chat.peerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (chat.isOnline) "🟢 Connected via Bluetooth (Offline)" else "Offline • Last seen recently",
                        fontSize = 11.sp,
                        color = if (chat.isOnline) WhatsAppBadgeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Call Actions
                IconButton(
                    onClick = { viewModel.startCall(chat.peerName, true) },
                    modifier = Modifier
                        .accessibleTouchTarget()
                        .testTag("chat_videocall_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Start Video Call with ${chat.peerName}",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { viewModel.startCall(chat.peerName, false) },
                    modifier = Modifier
                        .accessibleTouchTarget()
                        .testTag("chat_voicecall_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Start Voice Call with ${chat.peerName}",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.accessibleTouchTarget()
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More conversation options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { innerPadding ->
        AmbientLiquidBackdrop(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Authentic wallpaper background with ambient liquid blend
            Image(
                painter = painterResource(id = R.drawable.whatsapp_chat_bg_1792776728648),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.08f
            )

            // Chat area with responsive column constraints
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)
                        .imePadding()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        // Security encryption notice pill
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 540.dp)
                                        .liquidGlass(
                                            shape = RoundedCornerShape(12.dp),
                                            surfaceBrush = Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFF1E2B37).copy(alpha = 0.75f),
                                                    Color(0xFF101921).copy(alpha = 0.60f)
                                                )
                                            ),
                                            borderBrush = GlassSpecularWhiteBorder,
                                            borderWidth = 0.8.dp
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "End-to-End Encrypted",
                                            tint = Color(0xFFFFD279),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Messages are end-to-end encrypted over Xer0byte Bluetooth RFCOMM. Zero internet or SIM required.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFFD279),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Date indicator pill
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "TODAY",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Message bubbles with stable key
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(message = message)
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // Attachment menu drawer if opened
                    if (showAttachmentMenu) {
                        AttachmentMenu(
                            onItemSelected = { type, text, extra ->
                                showAttachmentMenu = false
                                viewModel.sendMessage(text = text, type = type, extraData = extra)
                            }
                        )
                    }

                    // Message Input Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Liquid Glass Rounded text capsule
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .liquidGlass(
                                    shape = RoundedCornerShape(26.dp),
                                    surfaceBrush = Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF1E2D3A).copy(alpha = 0.92f),
                                            Color(0xFF121D25).copy(alpha = 0.84f)
                                        )
                                    ),
                                    borderBrush = GlassSpecularWhiteBorder,
                                    borderWidth = 1.dp
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .accessibleTouchTarget()
                                    .testTag("emoji_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SentimentSatisfied,
                                    contentDescription = "Insert Emoji",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            TextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text(
                                        text = "Message",
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_field"),
                                maxLines = 5
                            )

                            IconButton(
                                onClick = { showAttachmentMenu = !showAttachmentMenu },
                                modifier = Modifier
                                    .accessibleTouchTarget()
                                    .testTag("attach_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = "Attach photo, location, or document",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            if (inputText.isBlank()) {
                                IconButton(
                                    onClick = {
                                        viewModel.sendMessage("📷 Captured photo", MessageType.IMAGE, null)
                                    },
                                    modifier = Modifier
                                        .accessibleTouchTarget()
                                        .testTag("camera_input_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Take camera photo",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Mic / Send button (liquid glass glowing circle with accessible touch target)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .liquidGlass(
                                    shape = CircleShape,
                                    surfaceBrush = Brush.radialGradient(
                                        listOf(
                                            WhatsAppBadgeGreen,
                                            Color(0xFF00A884)
                                        )
                                    ),
                                    borderBrush = GlassSpecularWhiteBorder,
                                    borderWidth = 1.dp
                                )
                                .clickable {
                                    if (inputText.isNotBlank()) {
                                        viewModel.sendMessage(inputText.trim(), MessageType.TEXT, null)
                                        inputText = ""
                                    } else {
                                        viewModel.sendMessage("Voice note", MessageType.VOICE, "0:18")
                                    }
                                }
                                .accessibleTouchTarget()
                                .testTag("send_or_mic_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                                contentDescription = if (inputText.isNotBlank()) "Send message" else "Record voice message",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
