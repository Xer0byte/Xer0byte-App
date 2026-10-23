package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.bluetooth.BluetoothConnectionState
import com.example.ui.theme.WhatsAppBadgeGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun WhatsAppTopBar(
    viewModel: ChatViewModel,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState.value
    val btState = viewModel.bluetoothUiState.value

    if (uiState.isSearching) {
        // WhatsApp Search bar
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleSearch() },
                modifier = Modifier.testTag("search_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search chats, messages…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_input_field"),
                singleLine = true
            )
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.xer0byte_logo_square),
                        contentDescription = "Xer0byte Logo",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xer0byte",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("app_brand_title")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF005C4B).copy(alpha = 0.35f), shape = CircleShape)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock",
                                tint = WhatsAppBadgeGreen,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Bluetooth P2P",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppBadgeGreen
                            )
                        }
                    }
                }
                Text(
                    text = when {
                        btState.totalConnected > 0 -> "🟢 ${btState.totalConnected} Connected Peer(s): ${btState.connectedPeers.joinToString { it.name }} (Zero Internet)"
                        btState.connectionState == BluetoothConnectionState.CONNECTED -> "🟢 Connected to ${btState.connectedDeviceName ?: "Peer"} (Zero Internet)"
                        btState.connectionState == BluetoothConnectionState.CONNECTING -> "🟡 Linking Bluetooth RFCOMM…"
                        btState.connectionState == BluetoothConnectionState.LISTENING -> "🔵 Ready for peers • Ghaznain Ahmad"
                        else -> "⚪ 100% Offline • Bluetooth Only"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Quick Bluetooth icon button
            IconButton(
                onClick = { onNavigate(AppScreen.BLUETOOTH_CENTER) },
                modifier = Modifier.testTag("bluetooth_center_btn")
            ) {
                Icon(
                    imageVector = when (btState.connectionState) {
                        BluetoothConnectionState.CONNECTED -> Icons.Default.BluetoothConnected
                        BluetoothConnectionState.CONNECTING -> Icons.Default.BluetoothSearching
                        else -> Icons.Default.Bluetooth
                    },
                    contentDescription = "Bluetooth Status",
                    tint = if (btState.connectionState == BluetoothConnectionState.CONNECTED) {
                        WhatsAppBadgeGreen
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Camera icon
            IconButton(
                onClick = { },
                modifier = Modifier.testTag("camera_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search icon
            IconButton(
                onClick = { viewModel.toggleSearch() },
                modifier = Modifier.testTag("search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Overflow menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("more_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Xer0byte Bluetooth Center") },
                        onClick = {
                            menuExpanded = false
                            onNavigate(AppScreen.BLUETOOTH_CENTER)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New group") },
                        onClick = { menuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Linked devices") },
                        onClick = {
                            menuExpanded = false
                            onNavigate(AppScreen.BLUETOOTH_CENTER)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Starred messages") },
                        onClick = { menuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            menuExpanded = false
                            onNavigate(AppScreen.SETTINGS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("About Xer0byte (Owner: Ghaznain Ahmad)") },
                        onClick = {
                            menuExpanded = false
                            onNavigate(AppScreen.ABOUT_XER0BYTE)
                        }
                    )
                }
            }
        }
    }
}
