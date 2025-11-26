package com.example.controlerapppromtai.data

import com.example.controlerapppromtai.BuildConfig

/**
 * MQTT Configuration data class
 * Stores broker connection details from BuildConfig
 */
data class MqttConfig(
    val brokerUri: String = BuildConfig.MQTT_BROKER_URI,
    val username: String = BuildConfig.MQTT_USERNAME,
    val password: String = BuildConfig.MQTT_PASSWORD,
    val defaultTopic: String = BuildConfig.MQTT_TOPIC_STATE,
    val clientId: String = "AndroidClient_${System.currentTimeMillis()}",
    val cleanSession: Boolean = true,
    val connectionTimeout: Int = 30,
    val keepAliveInterval: Int = 60,
    val automaticReconnect: Boolean = true
)
