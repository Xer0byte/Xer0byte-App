package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.bluetooth.BluetoothConnectionState
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatConversationEntity
import com.example.data.model.StatusUpdateEntity
import com.example.ui.components.WhatsAppBottomNav
import com.example.ui.components.WhatsAppTopBar
import com.example.ui.theme.WhatsAppBadgeGreen
import com.example.ui.theme.WhatsAppCheckmarkBlue
import com.example.ui.theme.WhatsAppGreenLight
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.HomeTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val btState by viewModel.bluetoothUiState.collectAsState()

    val unreadCount = conversations.sumOf { it.unreadCount }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WhatsAppTopBar(
                viewModel = viewModel,
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        bottomBar = {
            WhatsAppBottomNav(
                activeTab = uiState.activeTab,
                unreadChatsCount = unreadCount,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        floatingActionButton = {
            when (uiState.activeTab) {
                HomeTab.CHATS -> {
                    FloatingActionButton(
                        onClick = { viewModel.navigateTo(AppScreen.BLUETOOTH_CENTER) },
                        containerColor = WhatsAppBadgeGreen,
                        contentColor = Color.Black,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier.testTag("fab_new_chat")
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "New Chat / Connect Peer")
                    }
                }
                HomeTab.UPDATES -> {
                    Column(horizontalAlignment = Alignment.End) {
                        SmallFloatingActionButton(
                            onClick = { viewModel.postStatus("Xer0byte Bluetooth status: Mesh connected") },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp).testTag("fab_text_status")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Text Status")
                        }
                        FloatingActionButton(
                            onClick = { viewModel.postStatus("Offline Update via Ghaznain Ahmad's Xer0byte Node") },
                            containerColor = WhatsAppBadgeGreen,
                            contentColor = Color.Black,
                            modifier = Modifier.testTag("fab_camera_status")
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera Status")
                        }
                    }
                }
                HomeTab.CALLS -> {
                    FloatingActionButton(
                        onClick = { viewModel.startCall("Ghaznain Ahmad", false) },
                        containerColor = WhatsAppBadgeGreen,
                        contentColor = Color.Black,
                        modifier = Modifier.testTag("fab_new_call")
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Start Bluetooth Call")
                    }
                }
                else -> {}
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                when (uiState.activeTab) {
                    HomeTab.CHATS -> {
                        ChatsTabContent(
                            conversations = conversations,
                            searchQuery = uiState.searchQuery,
                            btState = btState,
                            onChatClicked = { viewModel.openChat(it) },
                            onBluetoothBannerClicked = { viewModel.navigateTo(AppScreen.BLUETOOTH_CENTER) }
                        )
                    }
                    HomeTab.UPDATES -> {
                        UpdatesTabContent(
                            statuses = statuses,
                            onStatusClicked = { viewModel.openStatus(it) },
                            onAddStatus = { viewModel.postStatus("Available on Xer0byte Bluetooth") }
                        )
                    }
                    HomeTab.COMMUNITIES -> {
                        CommunitiesTabContent(
                            onOpenCommunity = {
                                val meshChat = conversations.find { it.id == "xer0byte_mesh_group" }
                                if (meshChat != null) viewModel.openChat(meshChat)
                            }
                        )
                    }
                    HomeTab.CALLS -> {
                        CallsTabContent(
                            callLogs = callLogs,
                            onStartCall = { peer, isVideo -> viewModel.startCall(peer, isVideo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatsTabContent(
    conversations: List<ChatConversationEntity>,
    searchQuery: String,
    btState: com.example.data.bluetooth.BluetoothUiState,
    onChatClicked: (ChatConversationEntity) -> Unit,
    onBluetoothBannerClicked: () -> Unit
) {
    val filteredChats = if (searchQuery.isBlank()) {
        conversations
    } else {
        conversations.filter {
            it.peerName.contains(searchQuery, ignoreCase = true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Warning if Bluetooth is turned off
        if (!btState.isEnabled) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF854D0E).copy(alpha = 0.25f))
                        .clickable(onClick = onBluetoothBannerClicked)
                        .padding(14.dp)
                        .testTag("bluetooth_off_warning")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth Off",
                            tint = Color(0xFFFACC15),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bluetooth is currently off",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Turn on Bluetooth to chat offline with nearby peers",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "TURN ON",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFACC15)
                        )
                    }
                }
            }
        }

        // Bluetooth Quick Connection Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (btState.connectionState == BluetoothConnectionState.CONNECTED) {
                            Color(0xFF005C4B).copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable(onClick = onBluetoothBannerClicked)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("bt_quick_banner")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth Status",
                        tint = if (btState.connectionState == BluetoothConnectionState.CONNECTED) WhatsAppBadgeGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (btState.connectionState == BluetoothConnectionState.CONNECTED) {
                                "🟢 Connected: ${btState.connectedPeers.joinToString { it.name }.ifEmpty { btState.connectedDeviceName ?: "Peer" }}"
                            } else {
                                "Xer0byte Offline Bluetooth Mesh"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (btState.connectionState == BluetoothConnectionState.CONNECTED) {
                                "Direct RFCOMM socket active • Zero Internet • Range ~100m"
                            } else {
                                "Account: ${btState.myDeviceName} • Tap to pair PC & Mobile"
                            },
                            fontSize = 11.sp,
                            color = if (btState.connectionState == BluetoothConnectionState.CONNECTED) WhatsAppBadgeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "MANAGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Archived row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = "Archived",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Archived",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Chats list
        items(filteredChats, key = { it.id }) { chat ->
            ChatItemRow(
                chat = chat,
                onClick = { onChatClicked(chat) }
            )
        }

        // Footer notice
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Encrypted",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Your offline messages are end-to-end encrypted by Xer0byte",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatItemRow(
    chat: ChatConversationEntity,
    onClick: () -> Unit
) {
    val timeFormatted = remember(chat.lastMessageTimestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(chat.lastMessageTimestamp))
    }

    val avatarColors = listOf(
        Color(0xFF00A884),
        Color(0xFF007ACC),
        Color(0xFF8E24AA),
        Color(0xFFE65100),
        Color(0xFF00897B),
        Color(0xFF5E35B1)
    )
    val avatarBg = avatarColors[chat.avatarColorIndex % avatarColors.size]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("chat_item_${chat.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(avatarBg, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val initial = chat.peerName.firstOrNull()?.uppercaseChar() ?: 'X'
            Text(
                text = initial.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Prominent glowing green dot when connected via Bluetooth
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                        .padding(2.dp)
                        .background(WhatsAppBadgeGreen, shape = CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Chat Info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.peerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chat.isOnline) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(WhatsAppBadgeGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ONLINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppBadgeGreen
                            )
                        }
                    }
                }
                Text(
                    text = timeFormatted,
                    fontSize = 12.sp,
                    color = if (chat.unreadCount > 0) WhatsAppBadgeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Read status",
                    tint = WhatsAppCheckmarkBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = chat.lastMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (chat.isPinned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }

                if (chat.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(WhatsAppBadgeGreen, shape = CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${chat.unreadCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdatesTabContent(
    statuses: List<StatusUpdateEntity>,
    onStatusClicked: (StatusUpdateEntity) -> Unit,
    onAddStatus: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // My Status
        item {
            Text(
                text = "Status",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddStatus)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("my_status_row"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF00A884), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("M", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .background(WhatsAppBadgeGreen, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Status",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "My status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap to add status update via Xer0byte",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Recent Updates
        item {
            Text(
                text = "Recent updates",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(statuses) { status ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStatusClicked(status) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("status_item_${status.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status ring circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (status.isViewed) MaterialTheme.colorScheme.surfaceVariant else WhatsAppBadgeGreen,
                            shape = CircleShape
                        )
                        .padding(2.5.dp)
                        .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                        .padding(2.dp)
                        .background(Color(0xFF075E54), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = status.userName.firstOrNull() ?: 'U'
                    Text(
                        text = initial.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = status.userName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Today, 10:15 AM",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Channels section
        item {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Channels",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Explore",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF00A884), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("X", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Xer0byte Official by Ghaznain Ahmad",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "New update: Bluetooth mesh offline protocol v2.4 released!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunitiesTabContent(
    onOpenCommunity: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onOpenCommunity)
                    .padding(16.dp)
                    .testTag("community_mesh_card")
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF00A884), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Groups, contentDescription = "Community", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Xer0byte Offline Network",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Founder: Ghaznain Ahmad • Bluetooth Hub",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Direct peer-to-peer offline group communication across all nearby Bluetooth devices within range. Zero SIM, Zero WiFi, Zero Internet cost.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .background(WhatsAppBadgeGreen, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("ENTER COMMUNITY CHAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun CallsTabContent(
    callLogs: List<CallLogEntity>,
    onStartCall: (String, Boolean) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Create call link
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartCall("Xer0byte Peer", false) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("create_call_link"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(WhatsAppBadgeGreen, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Create Link",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Create Bluetooth call link",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Share direct audio connection with offline peer",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Recent",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(callLogs) { call ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("call_log_${call.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF007ACC), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = call.peerName.firstOrNull() ?: 'P'
                    Text(initial.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = call.peerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (call.direction) {
                                "INCOMING" -> Icons.Default.CallReceived
                                "OUTGOING" -> Icons.Default.CallMade
                                else -> Icons.Default.CallMissed
                            },
                            contentDescription = call.direction,
                            tint = if (call.direction == "MISSED") Color(0xFFE53935) else WhatsAppBadgeGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Today, 09:30 AM • ${call.duration}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { onStartCall(call.peerName, call.callType == "VIDEO") },
                    modifier = Modifier.testTag("call_btn_${call.id}")
                ) {
                    Icon(
                        imageVector = if (call.callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Call",
                        tint = WhatsAppBadgeGreen
                    )
                }
            }
        }
    }
}
