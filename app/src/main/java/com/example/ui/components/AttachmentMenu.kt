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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageType

@Composable
fun AttachmentMenu(
    onItemSelected: (MessageType, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
            .testTag("attachment_menu")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentItem(
                    label = "Document",
                    icon = Icons.Default.Description,
                    bgColor = Color(0xFF7F66FF),
                    onClick = {
                        onItemSelected(
                            MessageType.DOCUMENT,
                            "Xer0byte_Bluetooth_Specs.pdf",
                            "2.4 MB"
                        )
                    }
                )

                AttachmentItem(
                    label = "Camera",
                    icon = Icons.Default.CameraAlt,
                    bgColor = Color(0xFFD3396D),
                    onClick = {
                        onItemSelected(
                            MessageType.IMAGE,
                            "Captured via Xer0byte Camera",
                            null
                        )
                    }
                )

                AttachmentItem(
                    label = "Gallery",
                    icon = Icons.Default.Image,
                    bgColor = Color(0xFFAC44CF),
                    onClick = {
                        onItemSelected(
                            MessageType.IMAGE,
                            "Selected from Gallery",
                            null
                        )
                    }
                )

                AttachmentItem(
                    label = "Audio",
                    icon = Icons.Default.Headphones,
                    bgColor = Color(0xFFE07A26),
                    onClick = {
                        onItemSelected(
                            MessageType.VOICE,
                            "Voice Audio Note",
                            "0:32"
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentItem(
                    label = "Location",
                    icon = Icons.Default.LocationOn,
                    bgColor = Color(0xFF1EA05A),
                    onClick = {
                        onItemSelected(
                            MessageType.LOCATION,
                            "Lat: 31.5204° N, Lon: 74.3587° E",
                            null
                        )
                    }
                )

                AttachmentItem(
                    label = "Contact",
                    icon = Icons.Default.Person,
                    bgColor = Color(0xFF009DE2),
                    onClick = {
                        onItemSelected(
                            MessageType.TEXT,
                            "👤 Contact Card: Ghaznain Ahmad (Owner @ Xer0byte)",
                            null
                        )
                    }
                )

                AttachmentItem(
                    label = "Poll",
                    icon = Icons.Default.Poll,
                    bgColor = Color(0xFF00A884),
                    onClick = {
                        onItemSelected(
                            MessageType.TEXT,
                            "📊 Poll: Is Xer0byte Bluetooth offline chat useful without internet? (Yes / Absolutely)",
                            null
                        )
                    }
                )

                // Placeholder for grid balance
                Spacer(modifier = Modifier.size(54.dp))
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag("attach_${label.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(bgColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
