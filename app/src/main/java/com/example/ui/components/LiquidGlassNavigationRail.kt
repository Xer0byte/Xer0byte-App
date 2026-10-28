package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GlassEmeraldGlowBorder
import com.example.ui.theme.GlassSpecularWhiteBorder
import com.example.ui.theme.WhatsAppBadgeGreen
import com.example.ui.theme.accessibleTouchTarget
import com.example.ui.theme.liquidGlass
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HomeTab

@Composable
fun LiquidGlassNavigationRail(
    activeTab: HomeTab,
    unreadChatsCount: Int,
    isBluetoothConnected: Boolean,
    onTabSelected: (HomeTab) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(88.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(72.dp)
                .liquidGlass(
                    shape = RoundedCornerShape(28.dp),
                    surfaceBrush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF1D2B37).copy(alpha = 0.92f),
                            Color(0xFF0E1720).copy(alpha = 0.88f)
                        )
                    ),
                    borderBrush = GlassSpecularWhiteBorder,
                    borderWidth = 1.dp,
                    hasTopSpecularHighlight = true
                )
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Icon / Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .liquidGlass(
                            shape = CircleShape,
                            surfaceBrush = Brush.radialGradient(
                                listOf(
                                    WhatsAppBadgeGreen.copy(alpha = 0.35f),
                                    Color(0xFF005C4B).copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            ),
                            borderBrush = GlassEmeraldGlowBorder,
                            borderWidth = 1.dp
                        )
                        .clickable { onNavigate(AppScreen.HOME) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.xer0byte_logo_square),
                        contentDescription = "Xer0byte Home",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Xer0byte",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppBadgeGreen
                )
            }

            // Main Navigation Tabs
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RailNavItem(
                    title = "Chats",
                    selected = activeTab == HomeTab.CHATS,
                    selectedIcon = Icons.Filled.Chat,
                    unselectedIcon = Icons.Outlined.Chat,
                    badgeCount = unreadChatsCount,
                    onClick = {
                        onTabSelected(HomeTab.CHATS)
                        onNavigate(AppScreen.HOME)
                    },
                    testTag = "rail_nav_chats"
                )

                RailNavItem(
                    title = "Updates",
                    selected = activeTab == HomeTab.UPDATES,
                    selectedIcon = Icons.Filled.RadioButtonChecked,
                    unselectedIcon = Icons.Outlined.RadioButtonUnchecked,
                    hasDot = true,
                    onClick = {
                        onTabSelected(HomeTab.UPDATES)
                        onNavigate(AppScreen.HOME)
                    },
                    testTag = "rail_nav_updates"
                )

                RailNavItem(
                    title = "Groups",
                    selected = activeTab == HomeTab.COMMUNITIES,
                    selectedIcon = Icons.Filled.Groups,
                    unselectedIcon = Icons.Outlined.Groups,
                    onClick = {
                        onTabSelected(HomeTab.COMMUNITIES)
                        onNavigate(AppScreen.HOME)
                    },
                    testTag = "rail_nav_communities"
                )

                RailNavItem(
                    title = "Calls",
                    selected = activeTab == HomeTab.CALLS,
                    selectedIcon = Icons.Filled.Call,
                    unselectedIcon = Icons.Outlined.Call,
                    onClick = {
                        onTabSelected(HomeTab.CALLS)
                        onNavigate(AppScreen.HOME)
                    },
                    testTag = "rail_nav_calls"
                )
            }

            // Bottom Quick Actions: Bluetooth & Settings
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Bluetooth Center Shortcut with status indicator
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isBluetoothConnected) Color(0xFF005C4B).copy(alpha = 0.40f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                        .clickable { onNavigate(AppScreen.BLUETOOTH_CENTER) }
                        .accessibleTouchTarget()
                        .testTag("rail_bt_shortcut"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = if (isBluetoothConnected) "Bluetooth Connected" else "Bluetooth Center",
                        tint = if (isBluetoothConnected) WhatsAppBadgeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Settings Shortcut
                IconButton(
                    onClick = { onNavigate(AppScreen.SETTINGS) },
                    modifier = Modifier
                        .accessibleTouchTarget()
                        .testTag("rail_settings_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RailNavItem(
    title: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    badgeCount: Int = 0,
    hasDot: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .width(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = WhatsAppBadgeGreen),
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                this.selected = selected
            }
            .accessibleTouchTarget(minSize = 48.dp)
            .padding(vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pill container for icon
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected) WhatsAppBadgeGreen.copy(alpha = 0.22f)
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = WhatsAppBadgeGreen,
                            contentColor = Color.Black
                        ) {
                            Text(
                                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (hasDot) {
                        Badge(
                            containerColor = WhatsAppBadgeGreen,
                            modifier = Modifier.size(6.dp)
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = if (selected) selectedIcon else unselectedIcon,
                    contentDescription = "$title tab, ${if (selected) "Selected" else "Not selected"}",
                    tint = if (selected) WhatsAppBadgeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
