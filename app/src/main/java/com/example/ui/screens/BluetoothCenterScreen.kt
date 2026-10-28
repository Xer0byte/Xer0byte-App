package com.example.ui.screens

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.AmbientLiquidBackdrop
import com.example.ui.theme.GlassEmeraldGlowBorder
import com.example.ui.theme.GlassLiquidSurfaceActive
import com.example.ui.theme.GlassLiquidSurfaceDark
import com.example.ui.theme.GlassSpecularWhiteBorder
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.liquidGlass
import com.example.data.bluetooth.BluetoothConnectionState
import com.example.data.bluetooth.BluetoothPeerDevice
import com.example.ui.theme.WhatsAppBadgeGreen
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun BluetoothCenterScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val btState by viewModel.bluetoothUiState.collectAsState()

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF16232D).copy(alpha = 0.88f),
                                Color(0xFF0F1820).copy(alpha = 0.78f)
                            )
                        )
                    )
                    .statusBarsPadding()
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
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("bt_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Xer0byte Bluetooth Center",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Offline Peer-to-Peer RFCOMM Sockets",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
            // Status Card
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    surfaceBrush = if (btState.connectionState == BluetoothConnectionState.CONNECTED) GlassLiquidSurfaceActive else GlassLiquidSurfaceDark,
                    borderBrush = if (btState.connectionState == BluetoothConnectionState.CONNECTED) GlassEmeraldGlowBorder else GlassSpecularWhiteBorder,
                    borderWidth = 1.dp
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        if (btState.connectionState == BluetoothConnectionState.CONNECTED) WhatsAppBadgeGreen else MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (btState.connectionState == BluetoothConnectionState.CONNECTED) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                    contentDescription = "BT",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = when (btState.connectionState) {
                                        BluetoothConnectionState.CONNECTED -> "Connected to Peer"
                                        BluetoothConnectionState.CONNECTING -> "Connecting…"
                                        BluetoothConnectionState.LISTENING -> "Listening for Nearby Devices"
                                        BluetoothConnectionState.DISCONNECTED -> "Bluetooth Ready"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = btState.statusMessage,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (btState.connectedDeviceName != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF005C4B).copy(alpha = 0.25f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("Peer: ${btState.connectedDeviceName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Address: ${btState.connectedDeviceAddress ?: "N/A"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Status: End-to-End Encrypted via RFCOMM", fontSize = 11.sp, color = WhatsAppBadgeGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!btState.isEnabled) {
                            Button(
                                onClick = {
                                    try {
                                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                        context.startActivity(enableBtIntent)
                                    } catch (e: Exception) {
                                        // fallback
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15), contentColor = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("turn_on_bluetooth_btn")
                            ) {
                                Icon(Icons.Default.Bluetooth, contentDescription = "Turn On", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Turn On Device Bluetooth", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Actions row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (btState.isDiscovering) {
                                        viewModel.cancelBluetoothDiscovery()
                                    } else {
                                        viewModel.startBluetoothDiscovery()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppBadgeGreen, contentColor = Color.Black),
                                modifier = Modifier.weight(1f).testTag("scan_devices_btn")
                            ) {
                                if (btState.isDiscovering) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scanning…", fontSize = 12.sp)
                                } else {
                                    Icon(imageVector = Icons.Default.BluetoothSearching, contentDescription = "Scan", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scan Nearby", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                                        }
                                        context.startActivity(discoverableIntent)
                                    } catch (e: Exception) {
                                        // fallback
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("make_discoverable_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Sensors, contentDescription = "Discoverable", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Discoverable", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Quick Peer Simulation for testing on single device
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 Single Device / Emulator Testing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "If you don't have a second phone right now, tap below to link with Ghaznain Ahmad (Founder Xer0byte) companion node for real-time offline chat simulation.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.simulateNearbyNodeConnection() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().testTag("simulate_peer_btn")
                        ) {
                            Text("Connect to Ghaznain Ahmad (Xer0byte Companion)")
                        }
                    }
                }
            }

            // Paired Devices
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paired Devices",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { viewModel.refreshPairedDevices() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }

            if (btState.pairedDevices.isEmpty()) {
                item {
                    Text(
                        text = "No paired devices found. Tap Scan Nearby to discover Bluetooth peers.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(btState.pairedDevices) { device ->
                    val isConn = btState.connectedPeers.any { it.address == device.address } || btState.connectedDeviceAddress == device.address
                    DeviceRow(
                        device = device,
                        isConnected = isConn,
                        onConnect = { viewModel.connectToBluetoothDevice(device) }
                    )
                }
            }

            // Scanned Devices
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Discovered Devices",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (btState.scannedDevices.isEmpty()) {
                item {
                    Text(
                        text = if (btState.isDiscovering) "Searching for Bluetooth signals…" else "Tap 'Scan Nearby' to find other phones running Xer0byte.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(btState.scannedDevices) { device ->
                    val isConn = btState.connectedPeers.any { it.address == device.address } || btState.connectedDeviceAddress == device.address
                    DeviceRow(
                        device = device,
                        isConnected = isConn,
                        onConnect = { viewModel.connectToBluetoothDevice(device) }
                    )
                }
            }
        }
    }
}
}

@Composable
private fun DeviceRow(
    device: BluetoothPeerDevice,
    isConnected: Boolean,
    onConnect: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        surfaceBrush = if (isConnected) GlassLiquidSurfaceActive else GlassLiquidSurfaceDark,
        borderBrush = if (isConnected) GlassEmeraldGlowBorder else GlassSpecularWhiteBorder,
        borderWidth = 1.dp,
        onClick = onConnect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = "Device",
                    tint = if (isConnected) WhatsAppBadgeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isConnected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(10.dp)
                            .background(Color(0xFF090E11), shape = CircleShape)
                            .padding(1.dp)
                            .background(WhatsAppBadgeGreen, shape = CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isConnected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(WhatsAppBadgeGreen, shape = CircleShape)
                        )
                    }
                }
                Text(
                    text = device.address,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isConnected) {
                Box(
                    modifier = Modifier
                        .liquidGlass(
                            shape = RoundedCornerShape(8.dp),
                            surfaceBrush = Brush.horizontalGradient(
                                listOf(
                                    WhatsAppBadgeGreen.copy(alpha = 0.30f),
                                    Color(0xFF005C4B).copy(alpha = 0.40f)
                                )
                            ),
                            borderBrush = GlassEmeraldGlowBorder,
                            borderWidth = 0.8.dp
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("Connected 🟢", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WhatsAppBadgeGreen)
                }
            } else {
                OutlinedButton(
                    onClick = onConnect,
                    modifier = Modifier.height(34.dp).testTag("connect_${device.address}")
                ) {
                    Text("Connect", fontSize = 11.sp)
                }
            }
        }
    }
}
