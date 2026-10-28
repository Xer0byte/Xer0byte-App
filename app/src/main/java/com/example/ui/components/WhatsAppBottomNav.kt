package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppBadgeGreen
import com.example.ui.theme.WhatsAppGreenLight
import com.example.ui.viewmodel.HomeTab

import androidx.compose.foundation.border
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GlassEmeraldGlowBorder
import com.example.ui.theme.GlassSpecularWhiteBorder
import com.example.ui.theme.liquidGlass

@Composable
fun WhatsAppBottomNav(
    activeTab: HomeTab,
    unreadChatsCount: Int,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(28.dp),
                    surfaceBrush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF1D2B37).copy(alpha = 0.90f),
                            Color(0xFF0E1720).copy(alpha = 0.85f)
                        )
                    ),
                    borderBrush = GlassSpecularWhiteBorder,
                    borderWidth = 1.dp,
                    hasTopSpecularHighlight = true
                )
                .height(66.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                title = "Chats",
                selected = activeTab == HomeTab.CHATS,
                selectedIcon = Icons.Filled.Chat,
                unselectedIcon = Icons.Outlined.Chat,
                badgeCount = unreadChatsCount,
                onClick = { onTabSelected(HomeTab.CHATS) },
                testTag = "nav_chats_tab"
            )

            NavItem(
                title = "Updates",
                selected = activeTab == HomeTab.UPDATES,
                selectedIcon = Icons.Filled.RadioButtonChecked,
                unselectedIcon = Icons.Outlined.RadioButtonUnchecked,
                hasDot = true,
                onClick = { onTabSelected(HomeTab.UPDATES) },
                testTag = "nav_updates_tab"
            )

            NavItem(
                title = "Communities",
                selected = activeTab == HomeTab.COMMUNITIES,
                selectedIcon = Icons.Filled.Groups,
                unselectedIcon = Icons.Outlined.Groups,
                onClick = { onTabSelected(HomeTab.COMMUNITIES) },
                testTag = "nav_communities_tab"
            )

            NavItem(
                title = "Calls",
                selected = activeTab == HomeTab.CALLS,
                selectedIcon = Icons.Filled.Call,
                unselectedIcon = Icons.Outlined.Call,
                onClick = { onTabSelected(HomeTab.CALLS) },
                testTag = "nav_calls_tab"
            )
        }
    }
}

@Composable
private fun NavItem(
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
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = WhatsAppBadgeGreen),
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(34.dp)
                .then(
                    if (selected) {
                        Modifier
                            .liquidGlass(
                                shape = RoundedCornerShape(18.dp),
                                surfaceBrush = Brush.horizontalGradient(
                                    listOf(
                                        WhatsAppBadgeGreen.copy(alpha = 0.25f),
                                        Color(0xFF005C4B).copy(alpha = 0.35f)
                                    )
                                ),
                                borderBrush = GlassEmeraldGlowBorder,
                                borderWidth = 1.dp
                            )
                            .padding(horizontal = 14.dp)
                    } else {
                        Modifier.padding(horizontal = 8.dp)
                    }
                )
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = title,
                tint = if (selected) WhatsAppBadgeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(WhatsAppBadgeGreen, shape = CircleShape)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else "$badgeCount",
                        fontSize = 10.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (hasDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(7.dp)
                        .background(WhatsAppBadgeGreen, shape = CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) WhatsAppBadgeGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

