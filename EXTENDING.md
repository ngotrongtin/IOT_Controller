# Extending the MQTT Controller App

## 🚀 How to Add New Features

This guide shows how to extend the app with new functionality while maintaining the scalable architecture.

---

## 📱 Adding a New Device Type

### Example: Adding a Temperature Sensor

#### 1. Add Data Model (if needed)

```kotlin
// File: app/src/main/java/com/example/controlerapppromtai/data/DeviceModels.kt

data class TemperatureSensor(
    val id: String,
    val name: String,
    val currentTemp: Float,
    val topic: String
)
```

#### 2. Update ViewModel

```kotlin
// File: app/src/main/java/com/example/controlerapppromtai/viewmodel/DevicesViewModel.kt

class DevicesViewModel(application: Application) : AndroidViewModel(application) {
    
    // Add state for temperature
    private val _temperature = MutableStateFlow<Float?>(null)
    val temperature: StateFlow<Float?> = _temperature.asStateFlow()
    
    // Add method to read temperature
    fun subscribeToTemperature(topic: String = "sensors/temperature") {
        mqttManager.subscribe(topic)
    }
    
    // Parse received messages for temperature
    init {
        viewModelScope.launch {
            receivedMessages.collect { messages ->
                messages["sensors/temperature"]?.toFloatOrNull()?.let {
                    _temperature.value = it
                }
            }
        }
    }
}
```

#### 3. Add UI Component

```kotlin
// File: app/src/main/java/com/example/controlerapppromtai/ui/screens/DevicesScreen.kt

@Composable
private fun TemperatureSensorCard(
    temperature: Float?,
    onSubscribe: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Temperature Sensor", style = MaterialTheme.typography.titleMedium)
            
            if (temperature != null) {
                Text(
                    text = "${temperature}°C",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(onClick = onSubscribe) {
                    Text("Subscribe to Updates")
                }
            }
        }
    }
}
```

#### 4. Add to Screen

```kotlin
// In DevicesScreen composable
val temperature by viewModel.temperature.collectAsState()

// Add after QuickControlCard
TemperatureSensorCard(
    temperature = temperature,
    onSubscribe = { viewModel.subscribeToTemperature() }
)
```

---

## 🔔 Adding Notifications

### Step 1: Add Permission

```xml
<!-- File: app/src/main/AndroidManifest.xml -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Step 2: Create Notification Manager

```kotlin
// File: app/src/main/java/com/example/controlerapppromtai/utils/NotificationManager.kt

class NotificationManager(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
        as android.app.NotificationManager
    
    fun showMessageNotification(topic: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("MQTT: $topic")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    companion object {
        private const val CHANNEL_ID = "mqtt_messages"
    }
}
```

### Step 3: Integrate in MqttManager

```kotlin
// In MqttManager, add notification on message arrival
override fun messageArrived(topic: String?, message: MqttMessage?) {
    // ... existing code ...
    
    // Add notification
    notificationManager?.showMessageNotification(topic ?: "", payload)
}
```

---

## 💾 Adding Message History Persistence

### Step 1: Add Room Database Dependencies

```kotlin
// File: app/build.gradle.kts

dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}
```

### Step 2: Create Entity

```kotlin
// File: app/src/main/java/com/example/controlerapppromtai/data/MessageEntity.kt

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val topic: String,
    val message: String,
    val type: String,
    val status: String
)
```

### Step 3: Create DAO

```kotlin
// File: app/src/main/java/com/example/controlerapppromtai/data/MessageDao.kt

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT 100")
    fun getAllMessages(): Flow<List<MessageEntity>>
    
    @Insert
    suspend fun insert(message: MessageEntity)
    
    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
```

### Step 4: Update MqttManager to Save

```kotlin
// In MqttManager
private fun addLog(message: String, type: MessageType, status: MessageStatus) {
    val log = MessageLog(...)
    _messageLogs.value = listOf(log) + _messageLogs.value
    
    // Save to database
    viewModelScope.launch {
        messageDao.insert(log.toEntity())
    }
}
```

---

## 🎨 Adding Dark Theme Toggle

### Step 1: Create Settings Repository

```kotlin
// File: app/src/main/java/com/example/controlerapppromtai/repository/SettingsRepository.kt

class SettingsRepository(context: Context) {
    
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }
}
```

### Step 2: Update Theme

```kotlin
// File: app/src/main/java/com/example/controlerapppromtai/ui/theme/Theme.kt

@Composable
fun ControlerAppPromtAITheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### Step 3: Use in MainActivity

```kotlin
// In MainActivity
val settingsRepository = SettingsRepository(applicationContext)
val isDarkMode by settingsRepository.isDarkMode.collectAsState()

ControlerAppPromtAITheme(isDarkTheme = isDarkMode) {
    MainApp()
}
```

---

## 🔌 Adding Multiple Broker Support

### Step 1: Update Config Model

```kotlin
// File: app/src/main/java/com/example/controlerapppromtai/data/MqttConfig.kt

data class BrokerConfig(
    val id: String,
    val name: String,
    val uri: String,
    val username: String,
    val password: String
)

data class MqttSettings(
    val activeBrokerId: String,
    val brokers: List<BrokerConfig>
)
```

### Step 2: Update MqttManager

```kotlin
class MqttManager {
    private var currentConfig: BrokerConfig? = null
    
    fun connectToBroker(config: BrokerConfig) {
        currentConfig = config
        // Use config.uri, config.username, config.password
        connect()
    }
    
    fun switchBroker(config: BrokerConfig) {
        disconnect()
        connectToBroker(config)
    }
}
```

### Step 3: Add Broker Selection UI

```kotlin
@Composable
fun BrokerSelectorDialog(
    brokers: List<BrokerConfig>,
    onSelect: (BrokerConfig) -> Unit
) {
    // Dialog with list of brokers
}
```

---

## 📊 Adding Statistics Dashboard

### Step 1: Create Stats Model

```kotlin
data class MqttStatistics(
    val totalMessagesSent: Int,
    val totalMessagesReceived: Int,
    val connectionUptime: Long,
    val lastConnectionTime: Long,
    val averageResponseTime: Long
)
```

### Step 2: Track in MqttManager

```kotlin
class MqttManager {
    private var stats = MutableStateFlow(MqttStatistics(...))
    
    private fun incrementSentMessages() {
        stats.value = stats.value.copy(
            totalMessagesSent = stats.value.totalMessagesSent + 1
        )
    }
}
```

### Step 3: Create Stats Screen

```kotlin
@Composable
fun StatisticsScreen(stats: MqttStatistics) {
    LazyColumn {
        item { StatCard("Messages Sent", stats.totalMessagesSent) }
        item { StatCard("Messages Received", stats.totalMessagesReceived) }
        item { StatCard("Uptime", formatUptime(stats.connectionUptime)) }
    }
}
```

---

## 🔄 Adding Offline Queue

### Step 1: Create Queue Model

```kotlin
data class QueuedMessage(
    val id: String = UUID.randomUUID().toString(),
    val topic: String,
    val message: String,
    val queuedAt: Long = System.currentTimeMillis()
)
```

### Step 2: Implement Queue in MqttManager

```kotlin
class MqttManager {
    private val messageQueue = mutableListOf<QueuedMessage>()
    
    fun publish(topic: String, message: String) {
        if (isConnected()) {
            // Send immediately
            mqttClient?.publish(...)
        } else {
            // Queue for later
            messageQueue.add(QueuedMessage(topic, message))
        }
    }
    
    private fun processQueue() {
        messageQueue.forEach { msg ->
            publish(msg.topic, msg.message)
        }
        messageQueue.clear()
    }
}
```

---

## 🌍 Adding Multiple Topics Subscription

### Step 1: Update State

```kotlin
class DevicesViewModel {
    private val _subscribedTopics = MutableStateFlow<Set<String>>(emptySet())
    val subscribedTopics: StateFlow<Set<String>> = _subscribedTopics.asStateFlow()
    
    fun subscribeToTopic(topic: String) {
        mqttManager.subscribe(topic)
        _subscribedTopics.value = _subscribedTopics.value + topic
    }
    
    fun unsubscribeFromTopic(topic: String) {
        mqttManager.unsubscribe(topic)
        _subscribedTopics.value = _subscribedTopics.value - topic
    }
}
```

### Step 2: Add UI

```kotlin
@Composable
fun TopicSubscriptionManager(
    subscribedTopics: Set<String>,
    onSubscribe: (String) -> Unit,
    onUnsubscribe: (String) -> Unit
) {
    var newTopic by remember { mutableStateOf("") }
    
    Column {
        // Input for new topic
        Row {
            OutlinedTextField(
                value = newTopic,
                onValueChange = { newTopic = it },
                label = { Text("Topic") }
            )
            Button(onClick = { 
                onSubscribe(newTopic)
                newTopic = ""
            }) {
                Text("Subscribe")
            }
        }
        
        // List of subscribed topics
        subscribedTopics.forEach { topic ->
            TopicItem(topic, onUnsubscribe = { onUnsubscribe(topic) })
        }
    }
}
```

---

## 🔐 Adding Certificate-Based Authentication

### Step 1: Update MqttConfig

```kotlin
data class MqttConfig(
    // ... existing fields ...
    val useCertAuth: Boolean = false,
    val certificatePath: String? = null,
    val privateKeyPath: String? = null
)
```

### Step 2: Update SSL Socket Factory

```kotlin
private fun createSSLSocketFactoryWithCert(): SSLSocketFactory {
    val keyStore = KeyStore.getInstance("PKCS12")
    context.assets.open(config.certificatePath!!).use {
        keyStore.load(it, config.password.toCharArray())
    }
    
    val kmf = KeyManagerFactory.getInstance("X509")
    kmf.init(keyStore, config.password.toCharArray())
    
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(kmf.keyManagers, null, null)
    
    return sslContext.socketFactory
}
```

---

## 📱 Adding Widget Support

### Step 1: Create Widget Provider

```kotlin
class MqttWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_mqtt)
            views.setTextViewText(R.id.status_text, "Connected")
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
```

### Step 2: Add Widget Layout

```xml
<!-- res/layout/widget_mqtt.xml -->
<LinearLayout>
    <TextView android:id="@+id/status_text" />
    <Button android:id="@+id/toggle_button" android:text="Toggle" />
</LinearLayout>
```

---

## 🎯 Best Practices for Extensions

### 1. Follow Existing Patterns
- Use StateFlow for state
- Keep business logic in ViewModel
- UI components stay pure (no logic)

### 2. Maintain Separation of Concerns
- Data models in `data/`
- Business logic in `viewmodel/`
- Repository in `repository/`
- UI in `ui/screens/`

### 3. Keep It Testable
- Inject dependencies
- Use interfaces where appropriate
- Keep functions small and focused

### 4. Document Your Changes
- Add KDoc comments
- Update README if needed
- Create examples

### 5. Consider Performance
- Use LazyColumn for lists
- Avoid unnecessary recompositions
- Use remember and derivedStateOf

---

## 📚 Additional Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Eclipse Paho MQTT](https://www.eclipse.org/paho/)
- [Material Design 3](https://m3.material.io/)
- [Android Architecture Components](https://developer.android.com/topic/architecture)

---

**Happy Coding! 🚀**

