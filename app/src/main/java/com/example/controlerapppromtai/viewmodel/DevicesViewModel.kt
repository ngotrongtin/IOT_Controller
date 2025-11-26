package com.example.controlerapppromtai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controlerapppromtai.data.ConnectionState
import com.example.controlerapppromtai.data.MessageLog
import com.example.controlerapppromtai.repository.MqttManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Devices Screen
 * Manages MQTT connection and message operations
 */
class DevicesViewModel(application: Application) : AndroidViewModel(application) {

    private val mqttManager = MqttManager.getInstance(application)

    // MQTT Connection State
    val connectionState: StateFlow<ConnectionState> = mqttManager.connectionState

    // Message Logs
    val messageLogs: StateFlow<List<MessageLog>> = mqttManager.messageLogs

    // Received Messages
    val receivedMessages: StateFlow<Map<String, String>> = mqttManager.receivedMessages

    // Custom message and topic
    private val _customMessage = MutableStateFlow("")
    val customMessage: StateFlow<String> = _customMessage.asStateFlow()

    private val _customTopic = MutableStateFlow("lightbulb")
    val customTopic: StateFlow<String> = _customTopic.asStateFlow()

    init {
        // Auto-connect when ViewModel is created
        connectToMqtt()
    }

    /**
     * Connect to MQTT broker
     */
    fun connectToMqtt() {
        viewModelScope.launch {
            mqttManager.connect()
        }
    }

    /**
     * Disconnect from MQTT broker
     */
    fun disconnectFromMqtt() {
        viewModelScope.launch {
            mqttManager.disconnect()
        }
    }

    /**
     * Turn on the lightbulb
     */
    fun turnOn() {
        mqttManager.publish("lightbulb", "on")
    }

    /**
     * Turn off the lightbulb
     */
    fun turnOff() {
        mqttManager.publish("lightbulb", "off")
    }

    /**
     * Send custom message
     */
    fun sendCustomMessage() {
        if (_customMessage.value.isNotBlank() && _customTopic.value.isNotBlank()) {
            mqttManager.publish(_customTopic.value, _customMessage.value)
            _customMessage.value = "" // Clear after sending
        }
    }

    /**
     * Update custom message
     */
    fun updateCustomMessage(message: String) {
        _customMessage.value = message
    }

    /**
     * Update custom topic
     */
    fun updateCustomTopic(topic: String) {
        _customTopic.value = topic
    }

    /**
     * Clear message logs
     */
    fun clearLogs() {
        mqttManager.clearLogs()
    }

    override fun onCleared() {
        super.onCleared()
        // Optionally disconnect when ViewModel is cleared
        // mqttManager.disconnect()
    }
}

