package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.bluetooth.BluetoothConnectionState
import com.example.ui.components.LiquidGlassNavigationRail
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.BluetoothCenterScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatusViewerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.rememberWindowAdaptiveInfo
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp(viewModel: ChatViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val btState by viewModel.bluetoothUiState.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val unreadCount = conversations.sumOf { it.unreadCount }

    // Request Bluetooth and Notification permissions according to Android version
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.refreshPairedDevices()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val adaptiveInfo = rememberWindowAdaptiveInfo(maxWidth = maxWidth, maxHeight = maxHeight)

        // Effective screen to display: On tablets with two-pane chat active, CHAT_DETAIL
        // is embedded directly into HomeScreen's right pane!
        val effectiveScreen = if (adaptiveInfo.isTwoPaneChat && uiState.currentScreen == AppScreen.CHAT_DETAIL) {
            AppScreen.HOME
        } else {
            uiState.currentScreen
        }

        if (adaptiveInfo.useNavRail) {
            // Adaptive Tablet / Landscape layout with Navigation Rail on the left
            Row(modifier = Modifier.fillMaxSize()) {
                LiquidGlassNavigationRail(
                    activeTab = uiState.activeTab,
                    unreadChatsCount = unreadCount,
                    isBluetoothConnected = btState.connectionState == BluetoothConnectionState.CONNECTED,
                    onTabSelected = { viewModel.selectTab(it) },
                    onNavigate = { viewModel.navigateTo(it) }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    RenderScreen(
                        screen = effectiveScreen,
                        viewModel = viewModel,
                        adaptiveInfo = adaptiveInfo
                    )
                }
            }
        } else {
            // Compact Phone / Split-screen layout with bottom navigation dock
            RenderScreen(
                screen = effectiveScreen,
                viewModel = viewModel,
                adaptiveInfo = adaptiveInfo
            )
        }
    }
}

@Composable
private fun RenderScreen(
    screen: AppScreen,
    viewModel: ChatViewModel,
    adaptiveInfo: com.example.ui.theme.WindowAdaptiveInfo
) {
    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            if (targetState == AppScreen.HOME) {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            } else {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            }
        },
        label = "ScreenTransition"
    ) { currentScreen ->
        when (currentScreen) {
            AppScreen.HOME -> HomeScreen(viewModel = viewModel, adaptiveInfo = adaptiveInfo)
            AppScreen.CHAT_DETAIL -> ChatDetailScreen(viewModel = viewModel)
            AppScreen.STATUS_VIEW -> StatusViewerScreen(viewModel = viewModel)
            AppScreen.ACTIVE_CALL -> ActiveCallScreen(viewModel = viewModel)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            AppScreen.BLUETOOTH_CENTER -> BluetoothCenterScreen(viewModel = viewModel)
            AppScreen.ABOUT_XER0BYTE -> SettingsScreen(viewModel = viewModel)
        }
    }
}
