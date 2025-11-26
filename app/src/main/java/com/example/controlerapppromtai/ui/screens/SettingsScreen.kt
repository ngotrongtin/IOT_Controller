package com.example.controlerapppromtai.ui.screens

import androidx.compose.foundation.layout.*
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

/**
 * Settings Screen - App configuration and preferences
 */
@Composable
fun SettingsScreen() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoConnectEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var keepAliveInterval by remember { mutableStateOf(60f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // MQTT Settings Section
        SettingsSectionHeader(
            icon = Icons.Default.Cloud,
            title = "MQTT Settings"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                icon = Icons.Default.Link,
                title = "Auto Connect",
                description = "Automatically connect to broker on app start",
                trailing = {
                    Switch(
                        checked = autoConnectEnabled,
                        onCheckedChange = { autoConnectEnabled = it }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsItem(
                icon = Icons.Default.Timer,
                title = "Keep Alive Interval",
                description = "${keepAliveInterval.toInt()} seconds",
                trailing = {}
            )

            Slider(
                value = keepAliveInterval,
                onValueChange = { keepAliveInterval = it },
                valueRange = 30f..120f,
                steps = 8,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Settings Section
        SettingsSectionHeader(
            icon = Icons.Default.Settings,
            title = "App Settings"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "Enable push notifications for events",
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                description = "Use dark theme",
                trailing = {
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        SettingsSectionHeader(
            icon = Icons.Default.Info,
            title = "About"
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            SettingsItem(
                icon = Icons.Default.AppShortcut,
                title = "App Version",
                description = "1.0.0"
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsItem(
                icon = Icons.Default.Code,
                title = "Build",
                description = "Release Build"
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsItem(
                icon = Icons.Default.Storage,
                title = "SDK Version",
                description = "Android 14 (API 36)"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Danger Zone
        SettingsSectionHeader(
            icon = Icons.Default.Warning,
            title = "Danger Zone"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { /* Clear cache */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Cache")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { /* Reset settings */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        trailing()
    }
}

