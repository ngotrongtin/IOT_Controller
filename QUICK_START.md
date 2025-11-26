# Quick Start Guide - MQTT Controller App

## 🚀 Getting Started in 3 Steps

### Step 1: Open Project
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to `ControlerAppPromtAI` folder
4. Click "OK"

### Step 2: Sync Dependencies
When Android Studio opens the project:
1. Wait for automatic Gradle sync (or click "Sync Now" if prompted)
2. This will download all dependencies including MQTT libraries
3. Should complete in 1-3 minutes depending on internet speed

### Step 3: Run the App
1. Connect an Android device or start an emulator (Android 7.0+)
2. Click the green "Run" button (or press Shift+F10)
3. Select your device/emulator
4. Wait for build to complete
5. App will launch automatically

## 📱 Using the App

### First Launch
1. App opens on **Home Screen** with welcome message
2. Tap **Devices** in bottom navigation

### Devices Screen (MQTT Control)
**Auto-Connect**: The app automatically connects to the MQTT broker when you open Devices screen

#### Connection Controls
- 🔄 **Refresh Button**: Reconnect to broker
- ☁️ **Disconnect Button**: Disconnect from broker
- Status indicator shows: Connected (green) / Connecting (yellow) / Error (red) / Disconnected (gray)

#### Lightbulb Quick Controls
- ⚡ **Turn ON**: Sends "on" message to "lightbulb" topic
- 🌙 **Turn OFF**: Sends "off" message to "lightbulb" topic
- Buttons disabled when not connected

#### Custom Message
1. Enter your topic (default: "lightbulb")
2. Type your message
3. Click **Send Message**
4. Message published to broker

#### Message Logs
- View all MQTT activity in real-time
- ⬆️ Sent messages (green on success)
- ⬇️ Received messages
- ℹ️ System messages (connection events)
- 🗑️ Clear logs button

### Other Screens
- **Home**: Welcome and feature overview
- **Settings**: App configuration (UI only, functional settings can be added)
- **Profile**: User profile (demo data)

## 🔧 Configuration

### MQTT Broker Settings
Already configured in `local.properties`:
```
MQTT_BROKER_URI=ssl://5878f5ab7d3244d7899daea48f01b9d4.s1.eu.hivemq.cloud:8883
MQTT_USERNAME=tinngo
MQTT_PASSWORD=4n.8au7g3jP9B_s
MQTT_TOPIC_STATE=lightbulb
```

### To Change MQTT Settings
1. Open `local.properties`
2. Modify the MQTT configuration values
3. Rebuild the app (Build → Rebuild Project)

## ✅ Verify Everything Works

1. **Check Auto-Connect**
   - Open app → Go to Devices
   - Should see "Connecting..." then "Connected to broker"

2. **Test Quick Controls**
   - Wait for connection
   - Click "Turn ON"
   - Check logs for success message

3. **Test Custom Message**
   - Enter a topic
   - Type a message
   - Click Send
   - Verify in logs

## 🐛 Troubleshooting

### App Won't Build
- **Solution**: File → Invalidate Caches → Invalidate and Restart
- Or: Delete `.gradle` and `build` folders, then sync again

### Connection Fails
- **Check**: Internet connection
- **Check**: MQTT credentials in `local.properties`
- **Check**: Firewall not blocking port 8883
- **Try**: Click Refresh button

### Gradle Sync Issues
- **Solution**: 
  ```bash
  cd ControlerAppPromtAI
  ./gradlew clean
  ./gradlew build
  ```

### BuildConfig Errors (Red in IDE)
- **Normal**: BuildConfig is generated during build
- **Solution**: Build the project (Build → Make Project)
- Errors will disappear after successful build

### MQTT Library Not Found
- **Solution**: Check internet connection and sync Gradle again
- Or: File → Project Structure → Verify SDK location

## 📊 Expected Behavior

### When App Starts
1. Displays Home screen with animations
2. Bottom navigation bar visible
3. Material3 theme applied

### When Opening Devices Screen
1. Immediately shows "Connecting..." status
2. Within 2-5 seconds: Changes to "Connected"
3. Quick control buttons become enabled
4. Log shows "Connected to broker" message
5. Auto-subscribes to "lightbulb" topic

### When Sending Messages
1. Message appears in logs immediately
2. Status shows as pending
3. Changes to success (green arrow) when confirmed
4. Or failed (red) if error occurs

## 🎨 UI Features

- **Material3 Design**: Modern, clean interface
- **Responsive Layout**: Works on phones and tablets
- **Color-Coded Status**: Easy to understand at a glance
- **Smooth Animations**: Professional feel
- **Bottom Navigation**: Easy screen switching
- **Scrollable Content**: All screens support scrolling

## 📱 System Requirements

- Android 7.0 (API 24) or higher
- Internet connection
- ~50MB storage space

## 🎯 Test Checklist

- [ ] App builds successfully
- [ ] App launches without crash
- [ ] Home screen displays correctly
- [ ] Navigation works (all 4 tabs)
- [ ] Auto-connect works on Devices screen
- [ ] Connection status shows "Connected"
- [ ] Turn ON button sends message
- [ ] Turn OFF button sends message
- [ ] Custom message can be sent
- [ ] Logs update in real-time
- [ ] Refresh button reconnects
- [ ] No crashes during normal use

## 💡 Tips

1. **Keep Logs Visible**: They show what's happening
2. **Wait for Connection**: Buttons disabled until connected
3. **Watch Status Indicator**: Color shows connection state
4. **Refresh if Needed**: Use refresh button if connection drops

## 🆘 Getting Help

If something doesn't work:
1. Check the logs in app
2. Check Android Studio Logcat
3. Verify MQTT credentials
4. Check internet connection
5. Try rebuilding project

## 🎉 Success!

When you see:
- ✅ "Connected to broker" in green
- ✅ Message logs updating
- ✅ Buttons enabled
- ✅ Messages sending successfully

**Your MQTT Controller App is working perfectly!**

---

**Enjoy controlling your IoT devices! 🚀**

