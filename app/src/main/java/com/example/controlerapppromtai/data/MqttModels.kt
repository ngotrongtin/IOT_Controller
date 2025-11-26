package com.example.controlerapppromtai.data

/**
 * Represents the connection state of MQTT client
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Represents a message log entry
 */
data class MessageLog(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val topic: String,
    val message: String,
    val type: MessageType,
    val status: MessageStatus = MessageStatus.PENDING
)

enum class MessageType {
    SENT,
    RECEIVED,
    SYSTEM
}

enum class MessageStatus {
    PENDING,
    SUCCESS,
    FAILED
}

