import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}


// Load MQTT configuration from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { input -> localProperties.load(input) }
}

android {
    namespace = "com.example.controlerapppromtai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.controlerapppromtai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add MQTT configuration to BuildConfig
        buildConfigField("String", "MQTT_BROKER_URI", "\"${localProperties.getProperty("MQTT_BROKER_URI", "")}\"")
        buildConfigField("String", "MQTT_USERNAME", "\"${localProperties.getProperty("MQTT_USERNAME", "")}\"")
        buildConfigField("String", "MQTT_PASSWORD", "\"${localProperties.getProperty("MQTT_PASSWORD", "")}\"")
        buildConfigField("String", "MQTT_TOPIC_STATE", "\"${localProperties.getProperty("MQTT_TOPIC_STATE", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // MQTT Client
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}