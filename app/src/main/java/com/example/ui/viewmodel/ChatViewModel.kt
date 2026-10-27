package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.bluetooth.BluetoothChatManager
import com.example.data.bluetooth.BluetoothConnectionState
import com.example.data.bluetooth.BluetoothPeerDevice
import com.example.data.bluetooth.BluetoothUiState
import com.example.data.local.AppDatabase
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.StatusUpdateEntity
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    CHAT_DETAIL,
    STATUS_VIEW,
    ACTIVE_CALL,
    SETTINGS,
    BLUETOOTH_CENTER,
    ABOUT_XER0BYTE
}

enum class HomeTab {
    CHATS,
    UPDATES,
    COMMUNITIES,
    CALLS
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val activeTab: HomeTab = HomeTab.CHATS,
    val selectedChat: ChatConversationEntity? = null,
    val selectedStatus: StatusUpdateEntity? = null,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val activeCallPeer: String = "",
    val isVideoCall: Boolean = false,
    val callDurationSeconds: Int = 0,
    val isCallMuted: Boolean = false,
    val isSpeakerOn: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val bluetoothManager = BluetoothChatManager(application)
    private val repository = ChatRepository(database, bluetoothManager)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val bluetoothUiState: StateFlow<BluetoothUiState> = bluetoothManager.uiState

    val conversations: StateFlow<List<ChatConversationEntity>> = repository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statuses: StateFlow<List<StatusUpdateEntity>> = repository.getAllStatuses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallLogEntity>> = repository.getAllCallLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentChatMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val currentChatMessages: StateFlow<List<MessageEntity>> = _currentChatMessages.asStateFlow()

    private var messageCollectionJob: Job? = null
    private var callTimerJob: Job? = null

    fun selectTab(tab: HomeTab) {
        _uiState.update { it.copy(activeTab = tab, isSearching = false, searchQuery = "") }
    }

    fun openChat(conversation: ChatConversationEntity) {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.CHAT_DETAIL,
                selectedChat = conversation,
                isSearching = false,
                searchQuery = ""
            )
        }
        viewModelScope.launch {
            repository.clearUnread(conversation.id)
        }
        observeMessages(conversation.id)
    }

    private fun observeMessages(chatId: String) {
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            repository.getMessagesForChat(chatId).collect { msgs ->
                _currentChatMessages.value = msgs
            }
        }
    }

    fun sendMessage(text: String, type: MessageType = MessageType.TEXT, extraData: String? = null) {
        val chat = _uiState.value.selectedChat ?: return
        if (text.isBlank() && type == MessageType.TEXT) return

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chat.id,
                peerName = chat.peerName,
                text = text,
                type = type,
                extraData = extraData
            )
        }
    }

    fun navigateBack(): Boolean {
        val current = _uiState.value.currentScreen
        return if (current != AppScreen.HOME) {
            _uiState.update { it.copy(currentScreen = AppScreen.HOME) }
            true
        } else {
            false
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun toggleSearch() {
        _uiState.update { it.copy(isSearching = !it.isSearching, searchQuery = "") }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun startBluetoothDiscovery() {
        bluetoothManager.startDiscovery()
    }

    fun cancelBluetoothDiscovery() {
        bluetoothManager.cancelDiscovery()
    }

    fun connectToBluetoothDevice(device: BluetoothPeerDevice) {
        bluetoothManager.connectToDevice(device.address, device.name)
    }

    fun refreshPairedDevices() {
        bluetoothManager.refreshPairedDevices()
    }

    fun openStatus(status: StatusUpdateEntity) {
        _uiState.update { it.copy(currentScreen = AppScreen.STATUS_VIEW, selectedStatus = status) }
    }

    fun postStatus(text: String, caption: String = "") {
        viewModelScope.launch {
            repository.postStatus(text, caption)
        }
    }

    fun startCall(peerName: String, isVideo: Boolean) {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.ACTIVE_CALL,
                activeCallPeer = peerName,
                isVideoCall = isVideo,
                callDurationSeconds = 0,
                isCallMuted = false,
                isSpeakerOn = false
            )
        }
        startCallTimer()
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(callDurationSeconds = it.callDurationSeconds + 1) }
            }
        }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isCallMuted = !it.isCallMuted) }
    }

    fun toggleSpeaker() {
        _uiState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun endCall() {
        callTimerJob?.cancel()
        val duration = _uiState.value.callDurationSeconds
        val formattedDuration = String.format("%02d:%02d", duration / 60, duration % 60)
        val peer = _uiState.value.activeCallPeer
        val isVideo = _uiState.value.isVideoCall

        viewModelScope.launch {
            repository.addCallLog(
                peerName = if (peer.isNotBlank()) peer else "Bluetooth Peer",
                callType = if (isVideo) "VIDEO" else "AUDIO",
                direction = "OUTGOING",
                duration = formattedDuration
            )
        }

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.HOME,
                activeCallPeer = "",
                callDurationSeconds = 0
            )
        }
    }

    fun simulateNearbyNodeConnection() {
        viewModelScope.launch {
            bluetoothManager.connectCompanionNode(
                name = "Ghaznain Ahmad (Xer0byte)",
                address = "00:1A:7D:DA:71:01"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        messageCollectionJob?.cancel()
        bluetoothManager.cleanup()
    }
}
