package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun ActiveCallScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        viewModel.endCall()
    }

    val minutes = uiState.callDurationSeconds / 60
    val seconds = uiState.callDurationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1E1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.Center)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "End-to-end encrypted (Xer0byte BT RFCOMM)",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                Text(
                    text = uiState.activeCallPeer,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (uiState.callDurationSeconds > 0) formattedDuration else "Connecting via Bluetooth…",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Big Avatar
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(Color(0xFF00A884).copy(alpha = 0.3f), shape = CircleShape)
                    .padding(8.dp)
                    .background(Color(0xFF00A884), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initial = uiState.activeCallPeer.firstOrNull()?.uppercaseChar() ?: 'X'
                Text(
                    text = initial.toString(),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Call Action Buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CallActionButton(
                        icon = if (uiState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeUp,
                        label = "Speaker",
                        isActive = uiState.isSpeakerOn,
                        onClick = { viewModel.toggleSpeaker() }
                    )

                    CallActionButton(
                        icon = if (uiState.isVideoCall) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        label = "Video",
                        isActive = uiState.isVideoCall,
                        onClick = { }
                    )

                    CallActionButton(
                        icon = if (uiState.isCallMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = "Mute",
                        isActive = uiState.isCallMuted,
                        onClick = { viewModel.toggleMute() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // End call button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEA0038))
                        .clickable { viewModel.endCall() }
                        .testTag("end_call_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color.White.copy(alpha = 0.2f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, color = Color.White)
    }
}
