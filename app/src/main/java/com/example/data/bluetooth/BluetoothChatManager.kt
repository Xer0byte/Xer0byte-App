package com.example.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class PeerSession(
    val address: String,
    val name: String,
    val socket: BluetoothSocket,
    val writer: PrintWriter,
    val readerJob: Job
)

class BluetoothChatManager(private val context: Context) {

    private val tag = "Xer0byteBT"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Standard SPP UUID recognized universally across all Bluetooth stacks
    private val appUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val serviceName = "Xer0byteMultiPeerService"

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    fun getMyDeviceName(): String {
        return try {
            val name = bluetoothAdapter?.name
            if (!name.isNullOrBlank()) name else (Build.MODEL ?: "Xer0byte Device")
        } catch (e: SecurityException) {
            Build.MODEL ?: "Xer0byte Device"
        }
    }

    private val _uiState = MutableStateFlow(
        BluetoothUiState(
            isSupported = bluetoothAdapter != null,
            isEnabled = bluetoothAdapter?.isEnabled == true,
            myDeviceName = getMyDeviceName()
        )
    )
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<BluetoothPacket>(replay = 0)
    val incomingPackets: SharedFlow<BluetoothPacket> = _incomingPackets.asSharedFlow()

    // Multi-peer concurrent active sessions
    private val activeSessions = ConcurrentHashMap<String, PeerSession>()
    // Companion simulation peers for single device testing
    private val companionPeers = ConcurrentHashMap<String, BluetoothPeerDevice>()
    // Cache of recent packet IDs to prevent endless loop relays
    private val seenPacketIds = Collections.synchronizedSet(LinkedHashSet<String>())

    private var serverSocket: BluetoothServerSocket? = null
    private var serverJob: Job? = null
    private var isReceiverRegistered = false

    private val bluetoothReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    device?.let { dev ->
                        try {
                            val name = dev.name ?: dev.address ?: "Bluetooth Device"
                            val address = dev.address ?: return@let
                            val peer = BluetoothPeerDevice(
                                name = name,
                                address = address,
                                isBonded = dev.bondState == BluetoothDevice.BOND_BONDED,
                                isConnected = isPeerConnected(address)
                            )
                            _uiState.update { state ->
                                if (state.scannedDevices.none { it.address == address }) {
                                    state.copy(scannedDevices = state.scannedDevices + peer)
                                } else {
                                    state
                                }
                            }
                        } catch (e: SecurityException) {
                            Log.w(tag, "Permission missing for device discovery info", e)
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    refreshPairedDevices()
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _uiState.update { it.copy(isDiscovering = true, statusMessage = "Scanning for nearby Xer0byte peers…") }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _uiState.update { it.copy(isDiscovering = false, statusMessage = "Scan completed. Select devices to connect.") }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    val isEnabled = state == BluetoothAdapter.STATE_ON
                    _uiState.update { it.copy(isEnabled = isEnabled) }
                    if (isEnabled) {
                        refreshPairedDevices()
                        startServer()
                    } else {
                        disconnectAll()
                    }
                }
            }
        }
    }

    init {
        registerReceivers()
        refreshPairedDevices()
        if (bluetoothAdapter?.isEnabled == true) {
            startServer()
        }
    }

    private fun registerReceivers() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
            try {
                context.registerReceiver(bluetoothReceiver, filter)
                isReceiverRegistered = true
            } catch (e: Exception) {
                Log.e(tag, "Failed to register Bluetooth broadcast receiver", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        val adapter = bluetoothAdapter ?: return
        try {
            val bonded = adapter.bondedDevices?.map { dev ->
                BluetoothPeerDevice(
                    name = dev.name ?: "Peer (${dev.address.takeLast(4)})",
                    address = dev.address,
                    isBonded = true,
                    isConnected = isPeerConnected(dev.address)
                )
            } ?: emptyList()

            _uiState.update {
                it.copy(
                    isEnabled = adapter.isEnabled,
                    pairedDevices = bonded
                )
            }
        } catch (e: SecurityException) {
            Log.w(tag, "Permission missing to read paired devices", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        val adapter = bluetoothAdapter ?: return
        try {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            _uiState.update { it.copy(scannedDevices = emptyList()) }
            val started = adapter.startDiscovery()
            if (!started) {
                _uiState.update { it.copy(statusMessage = "Cannot start discovery. Check Bluetooth permissions.") }
            }
        } catch (e: SecurityException) {
            _uiState.update { it.copy(statusMessage = "Bluetooth Scan Permission Required") }
        }
    }

    @SuppressLint("MissingPermission")
    fun cancelDiscovery() {
        try {
            bluetoothAdapter?.cancelDiscovery()
        } catch (e: SecurityException) {
            Log.w(tag, "SecurityException on cancelDiscovery", e)
        }
    }

    fun isPeerConnected(address: String): Boolean {
        return activeSessions.containsKey(address) ||
                companionPeers.containsKey(address) ||
                activeSessions.values.any { it.name.equals(address, ignoreCase = true) } ||
                companionPeers.values.any { it.name.equals(address, ignoreCase = true) }
    }

    @SuppressLint("MissingPermission")
    fun startServer() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return

        serverJob?.cancel()
        serverJob = scope.launch {
            try {
                // Try listening on insecure RFCOMM (Windows PC friendly) and standard RFCOMM
                serverSocket = try {
                    adapter.listenUsingInsecureRfcommWithServiceRecord(serviceName, appUuid)
                } catch (e: Exception) {
                    try {
                        adapter.listenUsingRfcommWithServiceRecord(serviceName, appUuid)
                    } catch (e2: Exception) {
                        null
                    }
                }

                _uiState.update {
                    it.copy(
                        connectionState = if (activeSessions.isNotEmpty()) BluetoothConnectionState.CONNECTED else BluetoothConnectionState.LISTENING,
                        statusMessage = "Xer0byte server listening for incoming Bluetooth connections…"
                    )
                }

                // Continuous server listening loop to accept multiple incoming connections!
                while (isActive && serverSocket != null) {
                    val socket = try {
                        serverSocket?.accept()
                    } catch (e: Exception) {
                        null
                    }
                    if (socket != null) {
                        val remoteName = try { socket.remoteDevice?.name ?: "Remote Peer" } catch (e: Exception) { "Remote Peer" }
                        val remoteAddress = try { socket.remoteDevice?.address ?: UUID.randomUUID().toString() } catch (e: Exception) { UUID.randomUUID().toString() }
                        addPeerSession(socket, remoteName, remoteAddress)
                    }
                }
            } catch (e: SecurityException) {
                Log.w(tag, "Server listen permission denied", e)
            } catch (e: Exception) {
                Log.e(tag, "Server socket exception in listening loop", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String, deviceName: String) {
        val adapter = bluetoothAdapter ?: return
        cancelDiscovery()

        scope.launch {
            _uiState.update {
                it.copy(
                    connectionState = BluetoothConnectionState.CONNECTING,
                    statusMessage = "Connecting to $deviceName over Bluetooth…"
                )
            }
            // Allow Bluetooth chip hardware to settle after discovery
            kotlinx.coroutines.delay(200)

            try {
                val device = adapter.getRemoteDevice(address)
                var socket: BluetoothSocket? = null
                var lastException: Exception? = null

                // Strategy 1: Insecure RFCOMM with Service Record (Top choice for Windows PC & Android)
                try {
                    val s = device.createInsecureRfcommSocketToServiceRecord(appUuid)
                    s.connect()
                    socket = s
                } catch (e: Exception) {
                    lastException = e
                    Log.w(tag, "Strategy 1 (Insecure RFCOMM) failed for $address: ${e.message}")
                }

                // Strategy 2: Standard Secure RFCOMM
                if (socket == null || !socket.isConnected) {
                    try {
                        val s = device.createRfcommSocketToServiceRecord(appUuid)
                        s.connect()
                        socket = s
                    } catch (e: Exception) {
                        lastException = e
                        Log.w(tag, "Strategy 2 (Secure RFCOMM) failed for $address: ${e.message}")
                    }
                }

                // Strategy 3: Insecure Reflection Channel 1 (Standard Windows COM port)
                if (socket == null || !socket.isConnected) {
                    try {
                        val m = device.javaClass.getMethod("createInsecureRfcommSocket", Int::class.javaPrimitiveType)
                        val s = m.invoke(device, 1) as BluetoothSocket
                        s.connect()
                        socket = s
                    } catch (e: Exception) {
                        lastException = e
                        Log.w(tag, "Strategy 3 (Insecure Channel 1) failed: ${e.message}")
                    }
                }

                // Strategy 4: Standard Reflection Channel 1
                if (socket == null || !socket.isConnected) {
                    try {
                        val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                        val s = m.invoke(device, 1) as BluetoothSocket
                        s.connect()
                        socket = s
                    } catch (e: Exception) {
                        lastException = e
                        Log.w(tag, "Strategy 4 (Channel 1) failed: ${e.message}")
                    }
                }

                // Strategy 5: Insecure Reflection Channel 2
                if (socket == null || !socket.isConnected) {
                    try {
                        val m = device.javaClass.getMethod("createInsecureRfcommSocket", Int::class.javaPrimitiveType)
                        val s = m.invoke(device, 2) as BluetoothSocket
                        s.connect()
                        socket = s
                    } catch (e: Exception) {
                        lastException = e
                    }
                }

                if (socket != null && socket.isConnected) {
                    addPeerSession(socket, deviceName, address)
                } else {
                    throw (lastException ?: IllegalStateException("Could not establish Bluetooth socket connection"))
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to connect to device $address", e)
                _uiState.update {
                    it.copy(
                        connectionState = if (activeSessions.isNotEmpty()) BluetoothConnectionState.CONNECTED else BluetoothConnectionState.DISCONNECTED,
                        statusMessage = "Connection to $deviceName failed. Ensure device is paired & Bluetooth is ON on PC/Phone."
                    )
                }
            }
        }
    }

    private fun triggerHapticFeedback() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(120, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (e: Exception) {
            // Ignore if vibration unsupported
        }
    }

    private fun addPeerSession(socket: BluetoothSocket, deviceName: String, address: String) {
        // Remove existing session for same address if any
        activeSessions[address]?.let { old ->
            old.readerJob.cancel()
            try { old.writer.close(); old.socket.close() } catch (e: Exception) {}
        }

        val outputStream = socket.outputStream
        val writer = PrintWriter(BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"), 8192), true)

        val readerJob = scope.launch(Dispatchers.IO) {
            try {
                val inputStream = socket.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"), 8192)

                while (isActive && socket.isConnected) {
                    val line = reader.readLine() ?: break
                    if (line.isNotBlank()) {
                        handleInboundLine(line, deviceName, address)
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Connection lost with peer $deviceName ($address)", e)
            } finally {
                removePeerSession(address)
            }
        }

        val session = PeerSession(address, deviceName, socket, writer, readerJob)
        activeSessions[address] = session
        triggerHapticFeedback()
        updateConnectedPeersState(justConnectedName = deviceName)
    }

    private fun handleInboundLine(line: String, deviceName: String, address: String) {
        try {
            val json = JSONObject(line)
            val packetId = json.getString("id")

            // Check if already processed
            if (seenPacketIds.contains(packetId)) {
                return
            }
            if (seenPacketIds.size > 2000) {
                seenPacketIds.clear()
            }
            seenPacketIds.add(packetId)

            val packet = BluetoothPacket(
                id = packetId,
                type = json.optString("type", "TEXT"),
                senderName = json.optString("senderName", deviceName),
                text = json.optString("text", ""),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                extra = json.optString("extra", null),
                targetAddress = json.optString("targetAddress", null)
            )

            // Emit to local UI/Repository
            scope.launch {
                _incomingPackets.emit(packet)
            }

            // Mesh Relay: If packet is for group or broadcast, relay to other peers!
            if (packet.targetAddress == null || packet.targetAddress == "all" || packet.targetAddress == "mesh") {
                relayPacketToOtherPeers(line, excludeAddress = address)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse inbound JSON: $line", e)
        }
    }

    private fun relayPacketToOtherPeers(rawLine: String, excludeAddress: String) {
        activeSessions.forEach { (addr, session) ->
            if (addr != excludeAddress && session.socket.isConnected) {
                try {
                    session.writer.println(rawLine)
                    session.writer.flush()
                } catch (e: Exception) {
                    Log.w(tag, "Failed to relay packet to $addr", e)
                }
            }
        }
    }

    private fun removePeerSession(address: String) {
        val removed = activeSessions.remove(address)
        try {
            removed?.writer?.close()
            removed?.socket?.close()
        } catch (e: Exception) {}
        updateConnectedPeersState()
    }

    private fun updateConnectedPeersState(justConnectedName: String? = null) {
        val sessionPeers = activeSessions.values.map {
            BluetoothPeerDevice(
                name = it.name,
                address = it.address,
                isBonded = true,
                isConnected = true
            )
        }
        val companionList = companionPeers.values.toList()
        val peersList = (sessionPeers + companionList).distinctBy { it.address }

        _uiState.update { state ->
            val isConn = peersList.isNotEmpty()
            state.copy(
                connectionState = if (isConn) BluetoothConnectionState.CONNECTED else BluetoothConnectionState.LISTENING,
                connectedDeviceName = peersList.firstOrNull()?.name,
                connectedDeviceAddress = peersList.firstOrNull()?.address,
                connectedPeers = peersList,
                lastConnectionEvent = if (justConnectedName != null) "🟢 Connected to $justConnectedName! (Bluetooth Active)" else state.lastConnectionEvent,
                statusMessage = if (isConn) {
                    "Connected to ${peersList.size} peer(s): ${peersList.joinToString { it.name }} (Zero Internet)"
                } else {
                    "Listening for Bluetooth connections (Ready for peers)"
                }
            )
        }
    }

    fun connectCompanionNode(name: String = "Ghaznain Ahmad (Founder)", address: String = "00:1A:7D:DA:71:01") {
        companionPeers[address] = BluetoothPeerDevice(
            name = name,
            address = address,
            isBonded = true,
            isConnected = true
        )
        triggerHapticFeedback()
        updateConnectedPeersState(justConnectedName = name)

        // Send initial greeting packet
        scope.launch {
            kotlinx.coroutines.delay(400)
            _incomingPackets.emit(
                BluetoothPacket(
                    id = UUID.randomUUID().toString(),
                    type = "TEXT",
                    senderName = name,
                    text = "🟢 Connected! Bluetooth link established. Xer0byte offline mesh is active (Zero Internet / SIM).",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun sendPacket(packet: BluetoothPacket, targetAddress: String? = null): Boolean = withContext(Dispatchers.IO) {
        seenPacketIds.add(packet.id)
        val myName = _uiState.value.myDeviceName
        val effectiveSender = if (packet.senderName.isNotBlank() && packet.senderName != "Me") packet.senderName else myName
        val json = JSONObject().apply {
            put("id", packet.id)
            put("type", packet.type)
            put("senderName", effectiveSender)
            put("text", packet.text)
            put("timestamp", packet.timestamp)
            put("extra", packet.extra ?: "")
            put("targetAddress", targetAddress ?: packet.targetAddress ?: "all")
        }
        val line = json.toString()

        var sentAny = false

        if (targetAddress != null && activeSessions.containsKey(targetAddress)) {
            val session = activeSessions[targetAddress]
            if (session != null && session.socket.isConnected) {
                try {
                    session.writer.println(line)
                    session.writer.flush()
                    sentAny = true
                } catch (e: Exception) {
                    Log.e(tag, "Error sending packet to $targetAddress", e)
                }
            }
        } else {
            // Broadcast to all connected peers
            activeSessions.values.forEach { session ->
                if (session.socket.isConnected) {
                    try {
                        session.writer.println(line)
                        session.writer.flush()
                        sentAny = true
                    } catch (e: Exception) {
                        Log.e(tag, "Error broadcasting to ${session.address}", e)
                    }
                }
            }
        }

        // Handle companion simulation automated acknowledgment
        if (companionPeers.isNotEmpty() && packet.type != "ACK") {
            sentAny = true
            scope.launch {
                // Immediate delivery ACK receipt
                _incomingPackets.emit(
                    BluetoothPacket(
                        id = UUID.randomUUID().toString(),
                        type = "ACK",
                        senderName = companionPeers.values.firstOrNull()?.name ?: "Ghaznain Ahmad",
                        text = "ACK",
                        timestamp = System.currentTimeMillis(),
                        extra = packet.id
                    )
                )
                kotlinx.coroutines.delay(800)
                val replyText = when (packet.type) {
                    "IMAGE" -> "📷 Photo received offline via Bluetooth RFCOMM! (Zero Internet)"
                    "VOICE" -> "🎙️ Audio note received and verified! Audio waveform active."
                    "LOCATION" -> "📍 Location coordinates received and pinned offline."
                    "DOCUMENT" -> "📄 Document received and saved locally."
                    else -> "Received: \"${packet.text}\" 👍 Transmitted 100% offline via Bluetooth socket. — Ghaznain Ahmad"
                }
                _incomingPackets.emit(
                    BluetoothPacket(
                        id = UUID.randomUUID().toString(),
                        type = "TEXT",
                        senderName = companionPeers.values.firstOrNull()?.name ?: "Ghaznain Ahmad",
                        text = replyText,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        return@withContext sentAny
    }

    fun simulateInboundPacket(packet: BluetoothPacket) {
        scope.launch {
            _incomingPackets.emit(packet)
        }
    }

    fun disconnectAll() {
        activeSessions.values.forEach { session ->
            session.readerJob.cancel()
            try { session.writer.close(); session.socket.close() } catch (e: Exception) {}
        }
        activeSessions.clear()
        companionPeers.clear()
        updateConnectedPeersState()
    }

    fun cleanup() {
        disconnectAll()
        serverJob?.cancel()
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.w(tag, "Error closing server socket", e)
        }
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(bluetoothReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {
                Log.w(tag, "Failed to unregister receiver", e)
            }
        }
    }
}
