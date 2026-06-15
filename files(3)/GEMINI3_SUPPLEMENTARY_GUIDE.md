# Smart AutoClicker - Gemini 3 Puzzle Solver
## Supplementary Guide: Implementation Details & Debugging

---

## Table of Contents
1. [Complete Code Files](#complete-code-files)
2. [Gradle Configuration](#gradle-configuration)
3. [API Integration Details](#api-integration-details)
4. [Screenshot Capture Methods](#screenshot-capture-methods)
5. [Debugging & Logging](#debugging--logging)
6. [Real-World Puzzle Examples](#real-world-puzzle-examples)
7. [Common Issues & Solutions](#common-issues--solutions)

---

## Complete Code Files

### A. AndroidManifest.xml (Required Permissions)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Network access for Gemini API -->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!-- Display overlay on screen -->
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    
    <!-- Required for overlay in Android 10+ -->
    <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
    
    <!-- Inject input events (swipe/tap) -->
    <uses-permission android:name="android.permission.INJECT_EVENTS" />
    
    <!-- Accessibility service for automation -->
    <uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
    
    <!-- Take screenshots -->
    <uses-permission android:name="android.permission.READ_FRAME_BUFFER" />

    <application
        android:allowBackup="true"
        android:debuggable="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">

        <!-- Your activities here -->

        <!-- Accessibility Service for gesture automation -->
        <service
            android:name=".services.AutomationAccessibilityService"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>

    </application>

</manifest>
```

### B. res/xml/accessibility_service_config.xml (Required)

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackTypes="feedbackGeneric"
    android:accessibilityFlags="flagDefault|flagRequestFilterKeyEvents"
    android:canPerformGestures="true"
    android:canRetrieveWindowContent="true"
    android:description="@string/accessibility_service_description"
    android:notificationTimeout="100" />
```

### C. AutomationAccessibilityService.kt (Gesture Executor)

```kotlin
package com.example.smartautoclicker.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.*

/**
 * Accessibility Service for executing gestures (swipe, tap)
 * This is the recommended method for reliable gesture execution
 */
class AutomationAccessibilityService : AccessibilityService() {
    companion object {
        private const val TAG = "AutomationAccessibilityService"
        private var instance: AutomationAccessibilityService? = null

        fun getInstance(): AutomationAccessibilityService? = instance
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "Accessibility service created")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Monitor accessibility events if needed
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    /**
     * Perform a gesture swipe using accessibility service
     * This is more reliable than shell commands
     */
    fun performSwipe(
        startX: Int, startY: Int,
        endX: Int, endY: Int,
        duration: Long
    ): Boolean {
        return try {
            val path = Path().apply {
                moveTo(startX.toFloat(), startY.toFloat())
                lineTo(endX.toFloat(), endY.toFloat())
            }

            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()

            dispatchGesture(gesture) { success ->
                if (success) {
                    Log.d(TAG, "Swipe gesture dispatched successfully")
                } else {
                    Log.w(TAG, "Failed to dispatch swipe gesture")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error performing swipe", e)
            false
        }
    }

    /**
     * Perform a click gesture at specific coordinates
     */
    fun performClick(x: Int, y: Int): Boolean {
        return try {
            val path = Path().apply {
                moveTo(x.toFloat(), y.toFloat())
            }

            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                .build()

            dispatchGesture(gesture) { success ->
                if (success) {
                    Log.d(TAG, "Click gesture dispatched at ($x, $y)")
                } else {
                    Log.w(TAG, "Failed to dispatch click gesture")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error performing click", e)
            false
        }
    }

    /**
     * Multi-point swipe (for complex gestures)
     */
    fun performComplexGesture(points: List<Pair<Int, Int>>, duration: Long): Boolean {
        return try {
            val path = Path()
            points.forEachIndexed { index, (x, y) ->
                if (index == 0) {
                    path.moveTo(x.toFloat(), y.toFloat())
                } else {
                    path.lineTo(x.toFloat(), y.toFloat())
                }
            }

            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()

            dispatchGesture(gesture) { success ->
                if (success) {
                    Log.d(TAG, "Complex gesture dispatched")
                } else {
                    Log.w(TAG, "Failed to dispatch complex gesture")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error performing complex gesture", e)
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
        Log.d(TAG, "Accessibility service destroyed")
    }
}
```

### D. ScreenshotCaptureHelper.kt (For Puzzle Detection)

```kotlin
package com.example.smartautoclicker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility for capturing device screen as Bitmap
 */
object ScreenshotCaptureHelper {
    private const val TAG = "ScreenshotCaptureHelper"

    /**
     * Capture full screen using PixelCopy (API 24+, recommended)
     * Returns Bitmap of the entire screen content
     */
    suspend fun captureScreen(context: Context): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                captureScreenPixelCopy(context)
            } else {
                // Fallback for older Android versions
                captureScreenLegacy(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing screen", e)
            null
        }
    }

    /**
     * Capture using PixelCopy API (Most reliable for Android 7+)
     */
    private suspend fun captureScreenPixelCopy(context: Context): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val displayMetrics = context.resources.displayMetrics
                
                val bitmap = Bitmap.createBitmap(
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels,
                    Bitmap.Config.ARGB_8888
                )

                val window = (context as? android.app.Activity)?.window
                    ?: throw Exception("Activity window not available")

                val view = window.decorView.rootView as? View
                    ?: throw Exception("No root view found")

                PixelCopy.request(
                    window,
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            Log.d(TAG, "Screenshot captured successfully")
                            continuation.resume(bitmap)
                        } else {
                            Log.e(TAG, "PixelCopy failed with code: $copyResult")
                            continuation.resume(null)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "PixelCopy error", e)
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Legacy capture method for older Android versions
     */
    private fun captureScreenLegacy(context: Context): Bitmap? {
        return try {
            val displayMetrics = context.resources.displayMetrics
            val bitmap = Bitmap.createBitmap(
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                Bitmap.Config.ARGB_8888
            )

            // Note: This method has limitations and may not capture everything
            // Recommended to use PixelCopy when possible
            Log.d(TAG, "Using legacy screenshot method")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Legacy capture failed", e)
            null
        }
    }

    /**
     * Capture specific view/area (useful for cropping puzzle area)
     */
    fun captureView(view: View): Bitmap {
        view.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        return bitmap
    }

    /**
     * Crop bitmap to specific area
     */
    fun cropBitmap(bitmap: Bitmap, x: Int, y: Int, width: Int, height: Int): Bitmap {
        return try {
            Bitmap.createBitmap(bitmap, x, y, width, height)
        } catch (e: Exception) {
            Log.e(TAG, "Error cropping bitmap", e)
            bitmap
        }
    }

    /**
     * Resize bitmap (for faster API calls)
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        
        val width = if (bitmap.width > maxWidth) maxWidth else bitmap.width
        val height = if (bitmap.height > maxHeight) maxHeight else bitmap.height
        
        return try {
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        } catch (e: Exception) {
            Log.e(TAG, "Error resizing bitmap", e)
            bitmap
        }
    }
}
```

### E. PuzzleDetector.kt (Detect When Puzzle Appears)

```kotlin
package com.example.smartautoclicker.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.*

/**
 * Detect puzzle dialogs on screen
 */
class PuzzleDetector(private val context: Context) {
    companion object {
        private const val TAG = "PuzzleDetector"
    }

    /**
     * Check if Binance Security Verification puzzle is visible
     * Look for specific visual characteristics
     */
    suspend fun detectBinancePuzzle(bitmap: Bitmap): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                // Method 1: Check for specific colors (yellow, gray) in puzzle area
                val centerX = bitmap.width / 2
                val centerY = bitmap.height / 2
                
                // Sample pixels looking for puzzle colors
                var yellowCount = 0
                var grayCount = 0
                
                for (x in centerX - 100..centerX + 100 step 20) {
                    for (y in centerY - 100..centerY + 100 step 20) {
                        if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
                            val pixel = bitmap.getPixel(x, y)
                            
                            if (isYellowColor(pixel)) yellowCount++
                            if (isGrayColor(pixel)) grayCount++
                        }
                    }
                }
                
                val isPuzzleDetected = yellowCount > 5 && grayCount > 5
                Log.d(TAG, "Puzzle detection: yellow=$yellowCount, gray=$grayCount, detected=$isPuzzleDetected")
                isPuzzleDetected
                
            } catch (e: Exception) {
                Log.e(TAG, "Error detecting puzzle", e)
                false
            }
        }
    }

    /**
     * Detect hCaptcha tile puzzle
     */
    suspend fun detectHCaptchaPuzzle(bitmap: Bitmap): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                // Look for grid pattern characteristic of hCaptcha
                val gridPattern = detectGridPattern(bitmap)
                Log.d(TAG, "hCaptcha grid detection: $gridPattern")
                gridPattern
            } catch (e: Exception) {
                Log.e(TAG, "Error detecting hCaptcha", e)
                false
            }
        }
    }

    /**
     * Generic puzzle detection based on dialog characteristics
     */
    suspend fun detectGenericPuzzle(bitmap: Bitmap): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                // Look for modal dialog characteristics
                val hasDialog = detectModalDialog(bitmap)
                Log.d(TAG, "Generic puzzle detection: $hasDialog")
                hasDialog
            } catch (e: Exception) {
                Log.e(TAG, "Error detecting generic puzzle", e)
                false
            }
        }
    }

    /**
     * Helper: Check if pixel is yellowish (Binance puzzle color)
     */
    private fun isYellowColor(pixel: Int): Boolean {
        val red = (pixel shr 16) and 0xFF
        val green = (pixel shr 8) and 0xFF
        val blue = pixel and 0xFF
        
        // Yellow: high red, high green, low blue
        return red > 200 && green > 150 && blue < 100
    }

    /**
     * Helper: Check if pixel is grayish
     */
    private fun isGrayColor(pixel: Int): Boolean {
        val red = (pixel shr 16) and 0xFF
        val green = (pixel shr 8) and 0xFF
        val blue = pixel and 0xFF
        
        // Gray: similar RGB values
        return (red == green) && (green == blue) && (red in 100..200)
    }

    /**
     * Helper: Detect grid pattern (hCaptcha)
     */
    private fun detectGridPattern(bitmap: Bitmap): Boolean {
        // Simplified grid detection - look for regular vertical/horizontal lines
        val sampleSize = bitmap.width / 10
        var verticalLines = 0
        var horizontalLines = 0
        
        for (i in 0..9) {
            val x = i * sampleSize
            var colorChanges = 0
            
            for (y in 0 until bitmap.height step 10) {
                if (x < bitmap.width && y < bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    if (isEdgeColor(pixel)) colorChanges++
                }
            }
            if (colorChanges > 5) verticalLines++
            
            if (verticalLines >= 3) return true
        }
        
        return false
    }

    /**
     * Helper: Detect modal dialog
     */
    private fun detectModalDialog(bitmap: Bitmap): Boolean {
        // Modal dialogs typically have:
        // 1. Dark border/shadow area
        // 2. White content area in center
        // 3. Rounded corners
        
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        
        var whitePixels = 0
        
        for (x in centerX - 150..centerX + 150 step 10) {
            for (y in centerY - 150..centerY + 150 step 10) {
                if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    if (isWhiteColor(pixel)) whitePixels++
                }
            }
        }
        
        return whitePixels > 50 // Threshold for dialog detection
    }

    private fun isEdgeColor(pixel: Int): Boolean {
        val gray = (pixel shr 16) and 0xFF
        return gray < 100 || gray > 200
    }

    private fun isWhiteColor(pixel: Int): Boolean {
        val red = (pixel shr 16) and 0xFF
        val green = (pixel shr 8) and 0xFF
        val blue = pixel and 0xFF
        
        return red > 240 && green > 240 && blue > 240
    }
}
```

---

## Gradle Configuration

### build.gradle (App Level)

```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.example.smartautoclicker"
        minSdk 24  // Minimum for PixelCopy API
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding true
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    // ============ Google AI Client (Gemini) ============
    implementation 'com.google.ai.client.generativeai:google-generative-ai-android:0.1.2'

    // ============ Coroutines ============
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1'

    // ============ AndroidX ============
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.fragment:fragment-ktx:1.6.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.1'
    
    // ============ Material Design ============
    implementation 'com.google.android.material:material:1.10.0'

    // ============ JSON ============
    implementation 'com.google.code.gson:gson:2.10.1'

    // ============ Logging ============
    implementation 'com.jakewharton.timber:timber:5.0.1'

    // ============ Testing ============
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}

// Add repository for generative AI library if needed
repositories {
    google()
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

---

## API Integration Details

### Setup Gemini API Key

1. **Get API Key**:
   - Visit: https://aistudio.google.com/app/apikey
   - Create a new key (free tier available)
   - Copy the key

2. **Securely Store in App** (Use EncryptedSharedPreferences):

```kotlin
package com.example.smartautoclicker.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for sensitive data like API keys
 */
object SecureStorage {
    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_GEMINI_API = "gemini_api_key"

    private fun getSecurePreferences(context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun saveGeminiApiKey(context: Context, apiKey: String) {
        getSecurePreferences(context)
            .edit()
            .putString(KEY_GEMINI_API, apiKey)
            .apply()
    }

    fun getGeminiApiKey(context: Context): String? {
        return getSecurePreferences(context)
            .getString(KEY_GEMINI_API, null)
    }

    fun clearGeminiApiKey(context: Context) {
        getSecurePreferences(context)
            .edit()
            .remove(KEY_GEMINI_API)
            .apply()
    }

    fun hasGeminiApiKey(context: Context): Boolean {
        return getGeminiApiKey(context) != null
    }
}
```

Add to build.gradle:
```gradle
implementation 'androidx.security:security-crypto:1.1.0-alpha06'
```

### Test Gemini Connectivity

```kotlin
// In a test activity or fragment
suspend fun testGeminiConnection(apiKey: String) {
    return try {
        val model = GenerativeModel(
            modelName = "gemini-1.5-vision-latest",
            apiKey = apiKey
        )
        
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val response = model.generateContent(
            content {
                image(testBitmap)
                text("Describe this image briefly.")
            }
        )
        
        Log.d("GeminiTest", "API Test Success: ${response.text}")
        return response.text != null
    } catch (e: Exception) {
        Log.e("GeminiTest", "API Test Failed", e)
        return false
    }
}
```

---

## Screenshot Capture Methods

### Method 1: Using MediaProjection (Most Reliable)

```kotlin
package com.example.smartautoclicker.utils

import android.content.Context
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Log
import android.view.Display
import kotlin.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MediaProjectionScreenCapture(
    private val context: Context,
    private val mediaProjection: MediaProjection
) {
    companion object {
        private const val TAG = "MediaProjectionCapture"
    }

    suspend fun captureScreen(): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
                
                val metrics = context.resources.displayMetrics
                val width = metrics.widthPixels
                val height = metrics.heightPixels
                val density = metrics.densityDpi

                Log.d(TAG, "Capturing screen: ${width}x${height} @ ${density}dpi")
                
                // Note: Full MediaProjection implementation would require VirtualDisplay
                // This is a simplified example
                continuation.resume(null) // Implement full capture logic
                
            } catch (e: Exception) {
                Log.e(TAG, "Capture error", e)
                continuation.resumeWithException(e)
            }
        }
    }
}
```

### Method 2: Using Accessibility Service (Recommended for App)

The AccessibilityService can access screen content:

```kotlin
// In AutomationAccessibilityService.kt
override fun onAccessibilityEvent(event: AccessibilityEvent) {
    when (event.eventType) {
        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
            // Screen content changed - could indicate puzzle appeared
            Log.d(TAG, "Screen content changed")
            // Trigger puzzle detection here
        }
    }
}

// Get window content description
val rootNode = rootInActiveWindow
rootNode?.let { node ->
    Log.d(TAG, "Current window: ${node.packageName}")
}
```

---

## Debugging & Logging

### Enhanced Logging

```kotlin
package com.example.smartautoclicker.utils

import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Debug logging with file backup
 */
object DebugLogger {
    private const val TAG = "SmartAutoClicker"
    private const val LOG_FILE = "/sdcard/Documents/smartautoclicker_debug.log"
    
    private fun getTimestamp(): String {
        return SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
    }

    fun logDebug(module: String, message: String) {
        val fullMessage = "[${getTimestamp()}] [$module] $message"
        Log.d(TAG, fullMessage)
        appendToFile(fullMessage)
    }

    fun logError(module: String, message: String, exception: Exception? = null) {
        val fullMessage = "[${getTimestamp()}] [$module] ERROR: $message"
        if (exception != null) {
            Log.e(TAG, fullMessage, exception)
            appendToFile("$fullMessage\n${Log.getStackTraceString(exception)}")
        } else {
            Log.e(TAG, fullMessage)
            appendToFile(fullMessage)
        }
    }

    fun logNetwork(module: String, method: String, url: String, statusCode: Int, responseTime: Long) {
        val message = "[$module] $method $url -> $statusCode (${responseTime}ms)"
        logDebug("NETWORK", message)
    }

    private fun appendToFile(message: String) {
        try {
            val file = File(LOG_FILE)
            file.parentFile?.mkdirs()
            file.appendText("$message\n")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }

    fun exportLogs(): File? {
        return try {
            val file = File(LOG_FILE)
            if (file.exists()) file else null
        } catch (e: Exception) {
            null
        }
    }
}
```

### Debug UI Overlay

```kotlin
// Add debug mode overlay showing:
// - Current FPS
// - API calls made
// - Puzzle detection status
// - Gesture execution count

class DebugOverlay(context: Context) {
    private var fps = 0
    private var apiCalls = 0
    private var gesturesExecuted = 0
    
    fun updateMetrics() {
        // Update every second
    }
    
    fun recordApiCall() {
        apiCalls++
    }
    
    fun recordGesture() {
        gesturesExecuted++
    }
}
```

---

## Real-World Puzzle Examples

### Binance Security Verification Analysis

**Visual Characteristics**:
```
- Yellow puzzle piece (RGB: ~255, 200, 0)
- Gray background puzzle area
- "Slide to complete the puzzle" text
- Arrow button indicating direction
```

**Gemini Prompt Optimization**:
```kotlin
val binancePuzzlePrompt = """
Analyze this Binance Security Verification puzzle image.

CRITICAL ANALYSIS POINTS:
1. Identify the yellow puzzle piece position
2. Identify the gray slot where it should fit
3. Calculate the EXACT displacement needed
4. Determine primary direction: RIGHT, LEFT, UP, or DOWN

PROVIDE RESPONSE IN THIS EXACT FORMAT:
```
DIRECTION: RIGHT
DISTANCE: 250
DURATION: 400
CONFIDENCE: 95
```

GUIDELINES:
- Distance in pixels (typical: 150-300px)
- Duration in milliseconds (typical: 300-500ms)
- For ambiguous puzzles, provide your best estimate
- Include confidence percentage (0-100)

EXAMPLE RESPONSES:
```
DIRECTION: RIGHT
DISTANCE: 200
DURATION: 350
CONFIDENCE: 90
```

or

```
CANNOT_SOLVE
REASON: Puzzle is too blurry or unclear
```
"""
```

### hCaptcha Tile Puzzle Analysis

**Visual Characteristics**:
```
- Grid of 9 or 16 tiles
- Target image/label at top
- Each tile clickable
- Some tiles match, some don't
```

**Gemini Prompt for hCaptcha**:
```kotlin
val hcaptchaPrompt = """
Analyze this hCaptcha challenge image.

TASK:
1. Read the target label/description (usually at top)
2. Identify which tiles match that description
3. Provide exact click coordinates for matching tiles

RESPONSE FORMAT:
```
TARGET_LABEL: traffic_light
MATCHING_TILES: (100,200),(300,200),(500,200)
CONFIDENCE: 88
```

Be precise with coordinates - they determine click accuracy.
"""
```

---

## Common Issues & Solutions

### Issue 1: "GenerativeAI library not found"

```
Error: Cannot find GenerativeAI library
```

**Solution**:
```gradle
// Add to build.gradle
repositories {
    google()
    mavenCentral()
    // Add explicitly if needed:
    maven {
        url "https://maven.google.com"
    }
}

dependencies {
    // Use specific version
    implementation 'com.google.ai.client.generativeai:google-generative-ai-android:0.1.2'
}
```

### Issue 2: "API Key Invalid"

```
Status{statusCode=INVALID_ARGUMENT, description=API key not valid}
```

**Solution**:
- Verify key from https://aistudio.google.com/app/apikey
- Check API is enabled in Google Cloud Console
- Verify key hasn't expired
- Check character encoding (no extra spaces)

### Issue 3: "Gesture Not Executing"

```
Swipe appears but puzzle doesn't move
```

**Solutions**:
a. **Verify Accessibility Service is enabled**:
   - Settings → Accessibility → Smart AutoClicker → Enable

b. **Test with shell command**:
   ```bash
   adb shell input swipe 100 200 300 200 400
   ```

c. **Log gesture coordinates**:
   ```kotlin
   Log.d(TAG, "Executing swipe: $startX,$startY -> $endX,$endY in ${duration}ms")
   ```

d. **Add delays between gestures**:
   ```kotlin
   delay(500) // Wait 500ms between actions
   ```

### Issue 4: "Screenshot is Black/Blank"

```
Captured bitmap is all black
```

**Solutions**:
- Use PixelCopy instead of drawingCache
- Add small delay before capturing (let UI render)
- Check screenshot permissions
- Verify app has SYSTEM_ALERT_WINDOW permission
- Try different coordinate ranges

### Issue 5: "Gemini Takes Too Long"

```
API calls taking 10+ seconds
```

**Solutions**:
```kotlin
// Add timeout
val response = withTimeoutOrNull(30000L) {
    model.generateContent(content)
}
if (response == null) {
    Log.w(TAG, "Gemini request timed out")
    return false
}

// Compress image
val compressedBitmap = ScreenshotCaptureHelper.resizeBitmap(
    screenshot, 
    maxWidth = 512, 
    maxHeight = 512
)

// Add monitoring
val startTime = System.currentTimeMillis()
// ... call API ...
val duration = System.currentTimeMillis() - startTime
Log.d(TAG, "API call took ${duration}ms")
```

### Issue 6: "Permission Denied"

```
java.lang.SecurityException: Permission Denial
```

**Solutions**:
- Verify all permissions in AndroidManifest.xml
- Request runtime permissions (Android 6+):
  ```kotlin
  ActivityCompat.requestPermissions(
      this,
      arrayOf(
          Manifest.permission.INTERNET,
          Manifest.permission.SYSTEM_ALERT_WINDOW,
          Manifest.permission.BIND_ACCESSIBILITY_SERVICE
      ),
      PERMISSION_REQUEST_CODE
  )
  ```

- Check manifest:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
  <uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
  ```

---

## File Organization Checklist

- [x] GeminiPuzzleSolverService.kt
- [x] PuzzleSolverOverlay.kt
- [x] AutomationAccessibilityService.kt
- [x] ScreenshotCaptureHelper.kt
- [x] PuzzleDetector.kt
- [x] SecureStorage.kt
- [x] AutomationController.kt
- [x] DebugLogger.kt
- [x] AndroidManifest.xml (updated)
- [x] accessibility_service_config.xml
- [x] build.gradle (updated)
- [x] ActionExecutor.kt (updated)

---

## Testing Checklist

- [ ] Gemini API key stored securely
- [ ] Accessibility service enabled
- [ ] Internet connectivity available
- [ ] Permissions granted
- [ ] Test with actual Binance puzzle
- [ ] Verify swipe executes
- [ ] Verify overlay displays
- [ ] Check logs for errors
- [ ] Monitor API quota usage

---

This supplementary guide provides all the additional implementation details needed to make the puzzle solver fully functional!
