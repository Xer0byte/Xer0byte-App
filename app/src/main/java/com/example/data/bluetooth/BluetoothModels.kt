package com.example.data.bluetooth

enum class BluetoothConnectionState {
    DISCONNECTED,
    LISTENING,
    CONNECTING,
    CONNECTED
}

data class BluetoothPeerDevice(
    val name: String,
    val address: String,
    val isBonded: Boolean = false,
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false
)

data class BluetoothUiState(
    val isSupported: Boolean = true,
    val isEnabled: Boolean = false,
    val isDiscovering: Boolean = false,
    val connectionState: BluetoothConnectionState = BluetoothConnectionState.DISCONNECTED,
    val connectedDeviceName: String? = null,
    val connectedDeviceAddress: String? = null,
    val connectedPeers: List<BluetoothPeerDevice> = emptyList(),
    val pairedDevices: List<BluetoothPeerDevice> = emptyList(),
    val scannedDevices: List<BluetoothPeerDevice> = emptyList(),
    val statusMessage: String = "Xer0byte Bluetooth Offline Engine Ready"
) {
    val totalConnected: Int get() = connectedPeers.size
    val isAnyConnected: Boolean get() = connectedPeers.isNotEmpty() || connectionState == BluetoothConnectionState.CONNECTED
}

data class BluetoothPacket(
    val id: String,
    val type: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val extra: String? = null,
    val targetAddress: String? = null
)
