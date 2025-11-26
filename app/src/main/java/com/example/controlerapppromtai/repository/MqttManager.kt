package com.example.controlerapppromtai.repository

import android.content.Context
import android.util.Log
import com.example.controlerapppromtai.data.ConnectionState
import com.example.controlerapppromtai.data.MessageLog
import com.example.controlerapppromtai.data.MessageStatus
import com.example.controlerapppromtai.data.MessageType
import com.example.controlerapppromtai.data.MqttConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
 * MQTT Manager - Singleton repository using MqttAsyncClient
 */
class MqttManager private constructor(
    private val context: Context,
    private val config: MqttConfig = MqttConfig()
) {

    companion object {
        private const val TAG = "MqttManager"

        @Volatile
        private var instance: MqttManager? = null

        fun getInstance(context: Context): MqttManager {
            return instance ?: synchronized(this) {
                instance ?: MqttManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // Use MqttAsyncClient (no Android service / LocalBroadcastManager)
    private var mqttClient: MqttAsyncClient? = null
    private val persistence = MemoryPersistence()

    // coroutine scope for flow updates
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messageLogs = MutableStateFlow<List<MessageLog>>(emptyList())
    val messageLogs: StateFlow<List<MessageLog>> = _messageLogs.asStateFlow()

    private val _receivedMessages = MutableStateFlow<Map<String, String>>(emptyMap())
    val receivedMessages: StateFlow<Map<String, String>> = _receivedMessages.asStateFlow()

    /**
     * Connect to MQTT broker
     */
    fun connect() {
        // Avoid concurrent connect attempts
        if (_connectionState.value is ConnectionState.Connecting ||
            _connectionState.value is ConnectionState.Connected) {
            Log.d(TAG, "Already connecting or connected")
            return
        }

        _connectionState.value = ConnectionState.Connecting
        addLog("Connecting to broker...", MessageType.SYSTEM)

        try {
            // Create client if null or if not the same instance
            if (mqttClient == null) {
                mqttClient = MqttAsyncClient(config.brokerUri, config.clientId, persistence)
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    scope.launch {
                        _connectionState.value = ConnectionState.Connected
                        addLog(
                            if (reconnect) "Reconnected to broker" else "Connected to broker",
                            MessageType.SYSTEM,
                            MessageStatus.SUCCESS
                        )
                    }
                    // subscribe after connect (call on any thread is fine)
                    try {
                        subscribe(config.defaultTopic)
                    } catch (t: Throwable) {
                        Log.e(TAG, "Auto-subscribe error", t)
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    scope.launch {
                        _connectionState.value = ConnectionState.Disconnected
                        addLog(
                            "Connection lost: ${cause?.message ?: "Unknown error"}",
                            MessageType.SYSTEM,
                            MessageStatus.FAILED
                        )
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (topic == null || message == null) return
                    val payload = String(message.payload)
                    // update flows on coroutine scope to be thread-safe
                    scope.launch {
                        _receivedMessages.update { prev -> prev + (topic to payload) }
                        addLog("[$topic] $payload", MessageType.RECEIVED, MessageStatus.SUCCESS)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "Delivery complete")
                }
            })

            val options = MqttConnectOptions().apply {
                userName = config.username
                password = config.password.toCharArray()
                isCleanSession = config.cleanSession
                connectionTimeout = config.connectionTimeout
                keepAliveInterval = config.keepAliveInterval
                isAutomaticReconnect = config.automaticReconnect
            }

            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    // Note: connectComplete will also be called via callback
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    scope.launch {
                        _connectionState.value = ConnectionState.Error(
                            exception?.message ?: "Connection failed"
                        )
                        addLog("Connection failed: ${exception?.message}", MessageType.SYSTEM, MessageStatus.FAILED)
                    }
                    Log.e(TAG, "Connection failure", exception)
                }
            })

        } catch (e: Exception) {
            scope.launch {
                _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
                addLog("Error: ${e.message}", MessageType.SYSTEM, MessageStatus.FAILED)
            }
            Log.e(TAG, "Connection error", e)
        }
    }

    /**
     * Disconnect from MQTT broker
     */
    fun disconnect() {
        try {
            mqttClient?.let { client ->
                if (client.isConnected) {
                    client.disconnect(null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            scope.launch {
                                _connectionState.value = ConnectionState.Disconnected
                                addLog("Disconnected from broker", MessageType.SYSTEM, MessageStatus.SUCCESS)
                            }
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            addLog("Disconnect failed: ${exception?.message}", MessageType.SYSTEM, MessageStatus.FAILED)
                        }
                    })
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error", e)
        }
    }

    /**
     * Publish a message to a topic
     */
    fun publish(topic: String, message: String, qos: Int = 1) {
        if (_connectionState.value !is ConnectionState.Connected) {
            addLog("Cannot publish: Not connected", MessageType.SYSTEM, MessageStatus.FAILED)
            return
        }

        try {
            val mqttMessage = MqttMessage().apply {
                payload = message.toByteArray()
                this.qos = qos
                isRetained = false
            }

            mqttClient?.publish(topic, mqttMessage, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    addLog("[$topic] $message", MessageType.SENT, MessageStatus.SUCCESS)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    addLog("Publish failed: ${exception?.message}", MessageType.SENT, MessageStatus.FAILED)
                }
            })
        } catch (e: Exception) {
            addLog("Publish error: ${e.message}", MessageType.SYSTEM, MessageStatus.FAILED)
            Log.e(TAG, "Publish error", e)
        }
    }

    /**
     * Subscribe to a topic
     */
    fun subscribe(topic: String, qos: Int = 1) {
        if (_connectionState.value !is ConnectionState.Connected) {
            return
        }

        try {
            mqttClient?.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    addLog("Subscribed to: $topic", MessageType.SYSTEM, MessageStatus.SUCCESS)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    addLog("Subscribe failed: ${exception?.message}", MessageType.SYSTEM, MessageStatus.FAILED)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Subscribe error", e)
        }
    }

    /**
     * Unsubscribe from a topic
     */
//    fun unsubscribe(topic: String) {
//        try {
//            mqttClient?.unsubscribe(topic)
//        } catch (e: Exception) {
//            Log.e(TAG, "Unsubscribe error", e)
//        }
//    }

    /**
     * Clear message logs
     */
    fun clearLogs() {
        _messageLogs.value = emptyList()
    }

    /**
     * Add a log entry (keeps last 100)
     */
    private fun addLog(message: String, type: MessageType, status: MessageStatus = MessageStatus.PENDING) {
        val log = MessageLog(
            topic = config.defaultTopic,
            message = message,
            type = type,
            status = status
        )

        // update on coroutine scope
        scope.launch {
            _messageLogs.update { prev ->
                val newList = listOf(log) + prev
                if (newList.size > 100) newList.take(100) else newList
            }
        }
    }

    /**
     * Check if connected
     */
    fun isConnected(): Boolean {
        return mqttClient?.isConnected == true
    }
}
