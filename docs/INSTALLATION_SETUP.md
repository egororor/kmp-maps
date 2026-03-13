## ⚙️ Installation

### 📦 Dependency configuration (with Gradle Version Catalogs)

Add libraries to `gradle/libs.versions.toml`:

```toml
[versions]
kmpMaps = "0.8.1"

[libraries]
# For native map (Apple Maps on iOS, Google Maps on Android)
swmansion-kmpMaps-core = { module = "com.swmansion.kmpmaps:core", version.ref = "kmpMaps" }

# For Google Maps (Google Maps on both platforms)
swmansion-kmpMaps-googleMaps = { module = "com.swmansion.kmpmaps:google-maps", version.ref = "kmpMaps" }
```

Then, in your shared module `build.gradle.kts`, pick one of the following:

Option A — Core (Apple Maps on iOS, Google Maps on Android):

```kotlin
dependencies {
    implementation(libs.swmansion.kmpMaps.core)
}
```

Option B — Google Maps (Google Maps on both platforms):

```kotlin
dependencies {
    implementation(libs.swmansion.kmpMaps.googleMaps)
}
```

## ☁️ Google Cloud API Setup

For using Google Maps you have to generate your API Key and setup Google Cloud API.
Visit our [dedicated document](https://github.com/software-mansion/kmp-maps/blob/main/docs/GOOGLE_CLOUD_API_SETUP.md) for more info.

## 🤖 Android Setup

To use Google Maps on Android, you need to configure your API key in `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY" />
```

### 🔐 Permissions

To display the user's location on the map, you need to declare and request location permissions.
Add the following permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## 🍎 iOS Setup

### Apple Maps (Core)

No extra iOS setup is required beyond location permission (if you need user location).

### Google Maps (Add-on)

Follow the dedicated guide for CocoaPods setup and API key configuration: [Google Maps iOS Setup](https://github.com/software-mansion/kmp-maps/blob/main/docs/GOOGLE_MAPS_IOS_SETUP.md)

### 🔐 Permissions

To display the user's location on the map, you need to declare location permissions:
Add the following key to your `Info.plist`:

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>Allow this app to use your location</string>
```
### 🖥️ Desktop Setup

The JVM desktop implementation uses `compose-webview-multiplatform` (v1.9.40+), which internally uses JCEF (Java Chromium Embedded Framework). We recommend following the setup guide in the official [compose-webview-multiplatform repository](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/README.desktop.md).

**Important:** When running with **JetBrains Runtime (JBR)**, the bundled JCEF will be used automatically. No additional downloads or `kcef-bundle` folder is needed.

JCEF must be initialized via KCEF before the map is displayed:

```kotlin
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun main() = application {
    Window(title = "KMP Maps - Desktop", onCloseRequest = ::exitApplication) {
        var initialized by remember { mutableStateOf(false) }
        var restartRequired by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(
                    builder = {
                        // When running with JBR, JCEF is bundled
                        // No installDir needed
                        progress { onInitialized { initialized = true } }
                        settings { noSandbox = true }
                    },
                    onError = { it?.printStackTrace() },
                    onRestartRequired = { restartRequired = true },
                )
            }
        }

        if (restartRequired) {
            Text("Restart required to complete initialization.")
        } else if (initialized) {
            App()
        } else {
            Text("Initializing Map Engine...")
        }

        DisposableEffect(Unit) {
            onDispose {
                KCEF.disposeBlocking()
            }
        }
    }
}
```

**Note:** If you're NOT using JBR, KCEF will automatically download CEF binaries to a `kcef-bundle` folder on first run.

For a full example, refer to [main.kt](https://github.com/software-mansion/kmp-maps/blob/main/sample/src/jvmMain/kotlin/com/swmansion/kmpmaps/sample/main.kt).

To use Google Maps on desktop, you also need to initialize a valid API key globally before rendering the map:

```kotlin
MapConfiguration.initialize(googleMapsApiKey = "YOUR_API_KEY")
```
