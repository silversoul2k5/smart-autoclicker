# Quick Reference Card - Gemini 3 Puzzle Solver

## 📋 Files You Need to Create/Modify

```
📁 src/main/java/com/example/smartautoclicker/
├── 📁 services/
│   ├── 🆕 GeminiPuzzleSolverService.kt          (MAIN SOLVER)
│   └── 🆕 AutomationAccessibilityService.kt     (GESTURE EXEC)
├── 📁 ui/
│   ├── 🆕 PuzzleSolverOverlay.kt                (STATUS DISPLAY)
│   └── 📄 action/ActionTypeSelectionFragment.kt (MODIFY)
├── 📁 utils/
│   ├── 🆕 AutomationController.kt               (SWIPE/CLICK)
│   ├── 🆕 ScreenshotCaptureHelper.kt            (SCREENSHOTS)
│   ├── 🆕 PuzzleDetector.kt                     (DETECTION)
│   ├── 🆕 SecureStorage.kt                      (API KEY)
│   └── 🆕 DebugLogger.kt                        (LOGGING)
└── 📁 execution/
    └── 📄 ActionExecutor.kt                     (MODIFY)

📄 AndroidManifest.xml                           (ADD PERMISSIONS)
📄 build.gradle (app module)                      (ADD DEPENDENCIES)
📄 res/xml/accessibility_service_config.xml      (NEW FILE)
```

---

## 🔑 API Key Quick Setup

```
1. Visit: https://aistudio.google.com/app/apikey
2. Click: "Create API key" button
3. Copy: The generated key
4. Store in app using: SecureStorage.saveGeminiApiKey(context, key)
5. Never: Hardcode in source code!
6. Remember: Free tier = 15 req/min, 1M tokens/month
```

---

## 📱 Permissions Required

```xml
<!-- Required in AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
<uses-permission android:name="android.permission.INJECT_EVENTS" />
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
<uses-permission android:name="android.permission.READ_FRAME_BUFFER" />
```

---

## 🛠️ Dependencies to Add

```gradle
// In build.gradle (app level)
dependencies {
    // Google AI Client (CRITICAL)
    implementation 'com.google.ai.client.generativeai:google-generative-ai-android:0.1.2'
    
    // Coroutines (CRITICAL)
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1'
    
    // Secure storage (RECOMMENDED)
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'
    
    // AndroidX (if not present)
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.fragment:fragment-ktx:1.6.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.1'
}
```

---

## 🔄 Integration Points (Where to Hook Into Existing Code)

### 1. Action Type Menu
```kotlin
// In ActionTypeSelectionFragment.kt
when (selectedType) {
    ActionType.CLICK -> { /* existing */ }
    ActionType.SWIPE -> { /* existing */ }
    ActionType.PUZZLE_SOLVER -> {  // ← ADD THIS
        showPuzzleSolverSetup()
    }
}
```

### 2. Action Executor
```kotlin
// In ActionExecutor.kt
when (action.type) {
    ActionType.CLICK -> { /* existing */ }
    ActionType.SWIPE -> { /* existing */ }
    ActionType.PUZZLE_SOLVER -> {  // ← ADD THIS
        executePuzzleSolver(action)
    }
}
```

---

## 📊 Puzzle Types Supported

| Type | Prompt | Parse Logic | Example |
|------|--------|-------------|---------|
| **Binance Security** | Look for yellow piece + gray slot | Regex: `DIRECTION:` `DISTANCE:` | Binance, Crypto exchanges |
| **hCaptcha Tiles** | Look for grid + target image | Regex: `TILE_POSITIONS:` | Traffic lights, fire hydrants |
| **Slider Puzzle** | Look for sliding pieces | Parse: `MOVE_1:` `MOVE_2:` | Sliding tile games |
| **Custom** | Your own prompt | Your own parsing | Your own puzzles |

---

## ⚡ Execution Flow (Step by Step)

```
User clicks "Puzzle Solver"
       ↓
Dialog: "Enter API Key" 
       ↓ (User enters key and clicks OK)
Key stored in SecureStorage
       ↓
Puzzle Solver action created
       ↓
When puzzle appears:
├─ Show overlay: "🔄 Initializing..."
├─ Capture screenshot via PixelCopy
├─ Update overlay: "Encoding puzzle image..."
├─ Convert to Base64
├─ Update overlay: "Sending puzzle to Gemini AI..."
├─ Call Gemini Vision API with image
├─ Update overlay: "Parsing AI response..."
├─ Parse response via regex
├─ Build action objects (Swipe/Click)
├─ Update overlay: "Executing action..."
├─ Call AccessibilityService.performSwipe()
├─ Wait 500ms between actions
├─ Update overlay: "✓ PUZZLE SOLVED" (green)
└─ Auto-hide after 2 seconds
```

---

## 🎯 Status Overlay Messages Cheat Sheet

| Status | Color | What's Happening | Wait Time |
|--------|-------|------------------|-----------|
| 🔄 Initializing... | Blue | Starting process | 1s |
| Encoding puzzle image... | Blue | Converting to Base64 | 1s |
| Sending puzzle to Gemini AI... | Blue | API request in progress | 2-5s |
| Parsing AI response... | Blue | Processing response | 1s |
| Executing 1/1: SWIPE(RIGHT, 250px, 400ms) | Green | Performing gesture | 400-500ms |
| ✓ PUZZLE SOLVED | Green | Success! | 2s then auto-hide |
| ✗ ERROR: [message] | Red | Something failed | 2s then auto-hide |

---

## 🐛 Common Error Messages & Quick Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| "API key not valid" | Invalid/wrong key | Re-generate from aistudio.google.com |
| "No response from Gemini" | API call failed | Check internet, check quota |
| "Could not parse solution" | Response format wrong | Check Gemini response in logs |
| "SYSTEM_ALERT_WINDOW denied" | Permission not granted | Grant in Settings → Permissions |
| "Socket timeout" | Network too slow | Switch to WiFi |
| "Gesture not executing" | Accessibility service disabled | Enable in Settings → Accessibility |
| "App crashes on overlay show" | Bad layout params | Check width/height values |
| "Screenshot is all black" | Capture failed | Add delay, use PixelCopy instead |

---

## 🔍 Debug Commands (Copy-Paste Ready)

```bash
# View live logs
adb logcat | grep -E "GeminiPuzzleSolver|AutomationAccessibility|PuzzleSolver"

# View specific component logs
adb logcat | grep "GeminiPuzzleSolver"

# Export logs to file
adb logcat > puzzle_solver_logs.txt

# Test swipe gesture directly
adb shell input swipe 100 200 300 200 500

# Test tap gesture
adb shell input tap 200 200

# Check if accessibility service is enabled
adb shell settings get secure enabled_accessibility_services

# Pull debug log file
adb pull /sdcard/Documents/smartautoclicker_debug.log

# Check app permissions
adb shell dumpsys package com.example.smartautoclicker | grep -A5 "android.permission"
```

---

## 📝 Code Snippets (Frequently Used)

### Create Puzzle Solver Service
```kotlin
val solver = GeminiPuzzleSolverService(context, apiKey)
val success = solver.solvePuzzle(
    puzzleImage = screenshot,
    puzzleType = "binance_security",
    statusCallback = { status ->
        overlay.updateStatus(status)
    }
)
```

### Create Overlay
```kotlin
val overlay = PuzzleSolverOverlay(context)
overlay.show()
overlay.updateStatus("Processing...")
overlay.addActionLog("Executed swipe: RIGHT 250px")
overlay.setSuccess()
// or
overlay.setError("Failed to solve")
overlay.hide()
```

### Capture Screenshot
```kotlin
val screenshot = ScreenshotCaptureHelper.captureScreen(context)
if (screenshot != null) {
    Log.d(TAG, "Screenshot captured: ${screenshot.width}x${screenshot.height}")
} else {
    Log.e(TAG, "Failed to capture screenshot")
}
```

### Perform Swipe
```kotlin
automationController.performSwipe(
    direction = "RIGHT",
    distance = 250,
    duration = 400
)
```

### Store API Key Securely
```kotlin
// Save
SecureStorage.saveGeminiApiKey(context, "your-api-key-here")

// Retrieve
val apiKey = SecureStorage.getGeminiApiKey(context)

// Check if exists
if (SecureStorage.hasGeminiApiKey(context)) {
    Log.d(TAG, "API key is configured")
}
```

---

## 📊 Performance Targets

| Metric | Target | Critical |
|--------|--------|----------|
| Screenshot capture | < 300ms | Yes |
| Image encoding | < 100ms | Yes |
| API response time | < 5s | Yes |
| Gesture execution | 400-600ms | Yes |
| Total per puzzle | < 7s | Yes |
| Overlay responsiveness | < 100ms | Yes |

---

## ✅ Testing Checklist

```
Project Setup:
□ All code files copied
□ build.gradle updated
□ AndroidManifest.xml updated
□ Accessibility config created

Configuration:
□ API key obtained and stored
□ Permissions granted in device
□ Accessibility service enabled

Functionality:
□ Screenshot captures successfully
□ Gemini API responds to requests
□ Response parsing works correctly
□ Gesture execution performs swipes
□ Overlay displays and updates

Edge Cases:
□ Handles null responses
□ Handles network timeouts
□ Handles invalid API key
□ Handles permission denial
□ Recovers from errors

Performance:
□ API calls complete < 5s
□ Overlay appears instantly
□ Gestures execute smoothly
□ No memory leaks

Deployment:
□ All logs functional
□ Error handling robust
□ User feedback clear
□ Security measures in place
```

---

## 🎯 Success Indicators (You'll Know It's Working When...)

### Visual Indicators
- ✅ Floating overlay appears top-right of screen
- ✅ Overlay shows blue status while processing
- ✅ Overlay shows green "✓ PUZZLE SOLVED" on success
- ✅ Puzzle piece/element moves after swipe
- ✅ Puzzle dialog closes after solving

### Log Indicators
```
✅ "Gemini model initialized successfully"
✅ "Accessibility service created"
✅ "Screenshot captured successfully"
✅ "Image encoded, size: XXXXX chars"
✅ "Gemini response: DIRECTION: RIGHT DISTANCE: 250..."
✅ "Executed swipe: RIGHT 250px in 400ms"
✅ "Puzzle solved successfully"
```

### Device Indicators
```
✅ No freezing or lag
✅ No crashes or exceptions
✅ Smooth gesture execution
✅ Clear status updates
✅ Responsive UI
```

---

## 🔒 Security Checklist

```
API Key:
□ Never hardcoded
□ Stored with EncryptedSharedPreferences
□ Not logged in production
□ User can clear from settings

Permissions:
□ Only request what's needed
□ Use runtime permissions
□ Inform user why needed

Network:
□ HTTPS only (automatic)
□ No sensitive data in requests
□ No API keys in responses logged

Code:
□ Input validation
□ Error handling
□ No sensitive data in exceptions
□ Secure fallback paths
```

---

## 📞 Quick Help Reference

**Symptom: Nothing happens**
→ Check logcat for errors
→ Verify API key is valid
→ Enable accessibility service
→ Run: `adb logcat | grep GeminiPuzzleSolver`

**Symptom: Overlay doesn't appear**
→ Grant SYSTEM_ALERT_WINDOW permission
→ Check overlay creation in logcat
→ Verify window manager is initialized

**Symptom: Gemini error**
→ Check API key
→ Verify internet connection
→ Check API quota (console.cloud.google.com)
→ Screenshot size may be too large

**Symptom: Gesture doesn't work**
→ Enable Accessibility Service
→ Test with: `adb shell input swipe 100 200 300 200 500`
→ Check if app has required permissions
→ Verify accessibility service is active

**Symptom: App crashes**
→ Check logcat for full stack trace
→ Verify nullability checks
→ Check for permission denial
→ Ensure context is valid (not activity after destroy)

---

## 🚀 Quick Start (If You're Impatient)

```
1. Get API Key: https://aistudio.google.com/app/apikey
2. Copy code files from main guide
3. Update build.gradle with dependencies
4. Update AndroidManifest.xml with permissions
5. Run: gradlew build
6. Test with real puzzle
7. Debug any issues with included guides
8. Deploy!
```

---

## 📚 Document Cross-References

| Need | See Document | Section |
|------|--------------|---------|
| Complete overview | README_START_HERE.md | All |
| Main implementation | GEMINI3_PUZZLE_SOLVER_GUIDE.md | Steps 1-5 |
| Detailed components | GEMINI3_SUPPLEMENTARY_GUIDE.md | Complete Code Files |
| Quick setup | QUICK_IMPLEMENTATION_CHECKLIST.md | Phase 1-3 |
| Why it failed before | ANALYSIS_PREVIOUS_FAILURE.md | All |
| Troubleshooting | QUICK_IMPLEMENTATION_CHECKLIST.md | Common Issues section |
| Security | GEMINI3_SUPPLEMENTARY_GUIDE.md | SecureStorage section |
| Performance | QUICK_IMPLEMENTATION_CHECKLIST.md | Performance Metrics |

---

## 💡 Pro Tips

1. **Always check logcat first** - 90% of issues are visible there
2. **Test components separately** - Don't test everything at once
3. **Use a real puzzle** - Testing with fake images might not work
4. **Monitor your API quota** - Free tier can run out
5. **Optimize image size** - Smaller = faster = cheaper
6. **Cache solutions** - Avoid repeated API calls for same puzzle
7. **Log everything** - Makes debugging much easier
8. **Read error messages carefully** - They usually tell you exactly what's wrong

---

## 🎓 Learning Resources

- **Gemini API Docs**: https://ai.google.dev/docs
- **Android Accessibility**: https://developer.android.com/guide/topics/ui/accessibility
- **Kotlin Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html
- **PixelCopy API**: https://developer.android.com/reference/android/view/PixelCopy
- **GestureDescription**: https://developer.android.com/reference/android/accessibilityservice/GestureDescription

---

Print this page or save as reference while implementing!
