package com.example.ui.theme

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthClass {
    COMPACT, // < 600.dp (phones in portrait, split-screen)
    MEDIUM,  // 600.dp - 839.dp (small tablets, foldables, phones in landscape)
    EXPANDED // >= 840.dp (large tablets, desktop, full-width foldables)
}

enum class WindowHeightClass {
    COMPACT, // < 480.dp (landscape phones)
    MEDIUM,  // 480.dp - 899.dp (most devices in portrait)
    EXPANDED // >= 900.dp (tall tablets, desktop displays)
}

@Immutable
data class WindowAdaptiveInfo(
    val widthClass: WindowWidthClass,
    val heightClass: WindowHeightClass,
    val windowWidth: Dp,
    val windowHeight: Dp
) {
    val isCompact: Boolean get() = widthClass == WindowWidthClass.COMPACT
    val isMedium: Boolean get() = widthClass == WindowWidthClass.MEDIUM
    val isExpanded: Boolean get() = widthClass == WindowWidthClass.EXPANDED

    // Use navigation rail when width >= 600.dp AND height is not overly constrained
    val useNavRail: Boolean get() = widthClass != WindowWidthClass.COMPACT && windowHeight >= 440.dp

    // Eligible for two-pane List-Detail chat layout when available width is at least 720.dp
    val isTwoPaneChat: Boolean get() = windowWidth >= 720.dp

    // Adaptive padding recommendations
    val contentPadding: Dp get() = when (widthClass) {
        WindowWidthClass.COMPACT -> 12.dp
        WindowWidthClass.MEDIUM -> 20.dp
        WindowWidthClass.EXPANDED -> 28.dp
    }

    // Max content width for single-pane centered layouts
    val maxContentWidth: Dp get() = when (widthClass) {
        WindowWidthClass.COMPACT -> Dp.Unspecified
        WindowWidthClass.MEDIUM -> 640.dp
        WindowWidthClass.EXPANDED -> 760.dp
    }
}

@Composable
fun rememberWindowAdaptiveInfo(maxWidth: Dp, maxHeight: Dp): WindowAdaptiveInfo {
    return remember(maxWidth, maxHeight) {
        val widthClass = when {
            maxWidth < 600.dp -> WindowWidthClass.COMPACT
            maxWidth < 840.dp -> WindowWidthClass.MEDIUM
            else -> WindowWidthClass.EXPANDED
        }
        val heightClass = when {
            maxHeight < 480.dp -> WindowHeightClass.COMPACT
            maxHeight < 900.dp -> WindowHeightClass.MEDIUM
            else -> WindowHeightClass.EXPANDED
        }
        WindowAdaptiveInfo(
            widthClass = widthClass,
            heightClass = heightClass,
            windowWidth = maxWidth,
            windowHeight = maxHeight
        )
    }
}

/**
 * Ensures minimum accessibility touch target size of 48.dp x 48.dp per Android Material Design 3 guidelines.
 */
fun Modifier.accessibleTouchTarget(minSize: Dp = 48.dp): Modifier = this.sizeIn(minWidth = minSize, minHeight = minSize)
