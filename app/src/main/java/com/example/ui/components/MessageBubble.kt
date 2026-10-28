package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GlassEmeraldGlowBorder
import com.example.ui.theme.GlassSpecularWhiteBorder
import com.example.ui.theme.liquidGlass
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.ui.theme.WhatsAppCheckmarkBlue
import com.example.ui.theme.WhatsAppCheckmarkGray
import com.example.ui.theme.WhatsAppDarkBubbleIncoming
import com.example.ui.theme.WhatsAppDarkBubbleOutgoing
import com.example.ui.theme.WhatsAppLightBubbleIncoming
import com.example.ui.theme.WhatsAppLightBubbleOutgoing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: MessageEntity,
    modifier: Modifier = Modifier
) {
    val isMe = message.isFromMe

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bubbleSurfaceBrush = if (isMe) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF005C4B).copy(alpha = 0.90f),
                Color(0xFF063E33).copy(alpha = 0.80f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFF1F2E3A).copy(alpha = 0.85f),
                Color(0xFF131E27).copy(alpha = 0.75f)
            )
        )
    }

    val bubbleBorderBrush = if (isMe) GlassEmeraldGlowBorder else GlassSpecularWhiteBorder

    val timeString = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 80.dp, max = 300.dp)
                .liquidGlass(
                    shape = bubbleShape,
                    surfaceBrush = bubbleSurfaceBrush,
                    borderBrush = bubbleBorderBrush,
                    borderWidth = 0.8.dp,
                    hasTopSpecularHighlight = true
                )
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .testTag("message_bubble_${message.id}")
        ) {
            // Sender name in group chats if not from me
            if (!isMe && message.senderName != "You") {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            when (message.messageType) {
                MessageType.VOICE -> {
                    VoiceMessageBubbleContent(
                        duration = message.extraData ?: "0:14",
                        isMe = isMe
                    )
                }
                MessageType.IMAGE -> {
                    ImageMessageBubbleContent(
                        caption = message.text,
                        isMe = isMe
                    )
                }
                MessageType.LOCATION -> {
                    LocationMessageBubbleContent(
                        text = message.text
                    )
                }
                MessageType.DOCUMENT -> {
                    DocumentMessageBubbleContent(
                        filename = message.text,
                        size = message.extraData ?: "1.4 MB"
                    )
                }
                else -> {
                    Text(
                        text = message.text,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Timestamp and checkmarks
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeString,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                if (isMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    when (message.status) {
                        MessageStatus.SENDING -> {
                            Text("⏱", fontSize = 10.sp)
                        }
                        MessageStatus.SENT -> {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Sent",
                                tint = WhatsAppCheckmarkGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        MessageStatus.DELIVERED -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = WhatsAppCheckmarkGray,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        MessageStatus.READ -> {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = WhatsAppCheckmarkBlue,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceMessageBubbleContent(duration: String, isMe: Boolean) {
    var isPlaying by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play Voice Note",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Simulated audio waveform bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val heights = listOf(4, 8, 14, 10, 16, 6, 12, 18, 14, 8, 16, 12, 6, 14, 18, 10, 4)
                heights.forEachIndexed { idx, h ->
                    val barColor = if (isPlaying && idx < 8) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(h.dp)
                            .background(barColor, shape = RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = duration,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Note",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ImageMessageBubbleContent(caption: String, isMe: Boolean) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E3A34)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📷 Bluetooth Photo Transfer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("Direct RFCOMM Stream", fontSize = 11.sp, color = Color.LightGray)
            }
        }
        if (caption.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = caption,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LocationMessageBubbleContent(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF00A884).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color(0xFF00A884),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text("Live Location (Bluetooth Mesh)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DocumentMessageBubbleContent(filename: String, size: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF5F66CD).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Document",
                tint = Color(0xFF7F66FF),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(filename, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("DOC • $size", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
