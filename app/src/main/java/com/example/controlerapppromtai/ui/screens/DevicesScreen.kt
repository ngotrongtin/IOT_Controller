package com.example.controlerapppromtai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controlerapppromtai.data.ConnectionState
import com.example.controlerapppromtai.data.MessageLog
import com.example.controlerapppromtai.data.MessageStatus
import com.example.controlerapppromtai.data.MessageType
import com.example.controlerapppromtai.viewmodel.DevicesViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Devices Screen - Main MQTT control interface
 */
@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel = viewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val messageLogs by viewModel.messageLogs.collectAsState()
    val customMessage by viewModel.customMessage.collectAsState()
    val customTopic by viewModel.customTopic.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Connection Status Card
        ConnectionStatusCard(
            connectionState = connectionState,
            onRefresh = { viewModel.connectToMqtt() },
            onDisconnect = { viewModel.disconnectFromMqtt() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Control Card (Lightbulb)
        QuickControlCard(
            onTurnOn = { viewModel.turnOn() },
            onTurnOff = { viewModel.turnOff() },
            isEnabled = connectionState is ConnectionState.Connected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Message Card
        CustomMessageCard(
            message = customMessage,
            topic = customTopic,
            onMessageChange = { viewModel.updateCustomMessage(it) },
            onTopicChange = { viewModel.updateCustomTopic(it) },
            onSend = { viewModel.sendCustomMessage() },
            isEnabled = connectionState is ConnectionState.Connected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Message Logs Card
        MessageLogsCard(
            logs = messageLogs,
            onClear = { viewModel.clearLogs() }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ConnectionStatusCard(
    connectionState: ConnectionState,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                is ConnectionState.Connected -> MaterialTheme.colorScheme.primaryContainer
                is ConnectionState.Connecting -> MaterialTheme.colorScheme.secondaryContainer
                is ConnectionState.Error -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (connectionState) {
                            is ConnectionState.Connected -> Icons.Default.CheckCircle
                            is ConnectionState.Connecting -> Icons.Default.Refresh
                            is ConnectionState.Error -> Icons.Default.Error
                            else -> Icons.Default.CloudOff
                        },
                        contentDescription = null,
                        tint = when (connectionState) {
                            is ConnectionState.Connected -> MaterialTheme.colorScheme.onPrimaryContainer
                            is ConnectionState.Connecting -> MaterialTheme.colorScheme.onSecondaryContainer
                            is ConnectionState.Error -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Connection Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = when (connectionState) {
                                is ConnectionState.Connected -> "Connected to broker"
                                is ConnectionState.Connecting -> "Connecting..."
                                is ConnectionState.Error -> "Error: ${connectionState.message}"
                                else -> "Disconnected"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalContentColor.current.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (connectionState is ConnectionState.Connected) {
                    OutlinedButton(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disconnect")
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = onRefresh,
                    enabled = connectionState !is ConnectionState.Connecting
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh")
                }
            }
        }
    }
}

@Composable
private fun QuickControlCard(
    onTurnOn: () -> Unit,
    onTurnOff: () -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Lightbulb Control",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onTurnOn,
                    modifier = Modifier.weight(1f),
                    enabled = isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Turn ON")
                }

                OutlinedButton(
                    onClick = onTurnOff,
                    modifier = Modifier.weight(1f),
                    enabled = isEnabled
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Turn OFF")
                }
            }
        }
    }
}

@Composable
private fun CustomMessageCard(
    message: String,
    topic: String,
    onMessageChange: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Custom Message",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = onTopicChange,
                label = { Text("Topic") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Topic,
                        contentDescription = null
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                },
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSend,
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled && message.isNotBlank() && topic.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Message")
            }
        }
    }
}

@Composable
private fun MessageLogsCard(
    logs: List<MessageLog>,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Message Logs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear logs"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No logs yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    logs.take(20).forEach { log ->
                        LogItem(log)
                        if (log != logs.lastOrNull()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: MessageLog) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = when (log.type) {
                MessageType.SENT -> Icons.Default.ArrowUpward
                MessageType.RECEIVED -> Icons.Default.ArrowDownward
                MessageType.SYSTEM -> Icons.Default.Info
            },
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = when (log.status) {
                MessageStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                MessageStatus.FAILED -> MaterialTheme.colorScheme.error
                MessageStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = timeFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

