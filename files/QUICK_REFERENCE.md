# Smart AutoClicker - Backup Preload: Quick Reference

## TL;DR - 3 Simple Steps

### 1️⃣ Copy Files
```bash
# Copy BackupAutoLoader.kt to your project:
cp BackupAutoLoader.kt \
  app_or_core_module/src/main/java/com/buzbuz/smartautoclicker/core/base/di/

# Copy backup to assets:
cp SmartAutoClicker-Backup.zip \
  app/src/main/assets/
```

### 2️⃣ Add One Line to MainActivity
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    
    // ADD THIS ONE LINE:
    BackupAutoLoader.loadBackupIfFirstRun(this)
    
    // ... rest of code
}
```

### 3️⃣ Verify Permissions in AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## Complete File Locations

```
your-project/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   └── SmartAutoClicker-Backup.zip  ← Copy backup here
│   │   ├── java/.../activity/
│   │   │   └── MainActivity.kt  ← Add one line here
│   │   └── AndroidManifest.xml  ← Verify permissions
│   └── build.gradle
├── core/
│   └── base/
│       └── src/main/java/.../core/base/di/
│           └── BackupAutoLoader.kt  ← Copy loader here
└── settings.gradle
```

---

## Minimal MainActivity Changes

```kotlin
// BEFORE:
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
}

// AFTER:
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    BackupAutoLoader.loadBackupIfFirstRun(this)  // ← ADD THIS
}
```

---

## Minimal AndroidManifest Changes

```xml
<!-- Add these two lines inside <manifest> tag if not present -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

---

## Testing

```bash
# Build
./gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Test:
# 1. Open app
# 2. Grant permissions if asked
# 3. Go to Scenarios tab
# 4. Your backup scenarios should appear
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Scenarios not appearing | Check logcat: `adb logcat \| grep BackupAutoLoader` |
| "File not found" error | Verify `SmartAutoClicker-Backup.zip` is in `app/src/main/assets/` |
| Permission denied | Add permissions to AndroidManifest.xml |
| Build fails | Check BackupAutoLoader.kt is in correct package path |
| Scenarios appear twice | Run `adb shell pm clear com.buzbuz.smartautoclicker` |

---

## How to Find the Right Locations

**Finding where to put MainActivity edit:**
```bash
find . -name "MainActivity.kt" -o -name "MainActivity.java"
# Look for: package com.buzbuz.smartautoclicker.*activity
```

**Finding where to put BackupAutoLoader:**
```bash
find . -path "*/core/base/*/di" -type d
# Or create: core/base/src/main/java/.../core/base/di/
```

**Finding where to put backup:**
```bash
ls -la app/src/main/
# If assets folder doesn't exist, create it:
mkdir -p app/src/main/assets/
```

---

## Timeline

- **Install time**: ~2 minutes
- **Build time**: ~30-60 seconds
- **First app launch**: 5-10 seconds (auto-imports backup)
- **Subsequent launches**: <1 second (normal startup)

---

## What Happens Behind Scenes

```
User installs app
         ↓
MainActivity.onCreate() runs
         ↓
BackupAutoLoader.loadBackupIfFirstRun(this) called
         ↓
Checks SharedPreferences for "backup_auto_loaded" flag
         ↓
If NOT set:
  • Copies SmartAutoClicker-Backup.zip from assets
  • App's native backup system detects it
  • Scenarios extracted & imported into database
  • Sets flag to prevent re-importing
         ↓
If already set:
  • Skip (already loaded)
         ↓
User sees all scenarios in app immediately! ✓
```

---

## File Summary

| File | Purpose | Location |
|------|---------|----------|
| `BackupAutoLoader.kt` | Auto-loader module | `core/base/src/main/java/.../di/` |
| `SmartAutoClicker-Backup.zip` | Backup data | `app/src/main/assets/` |
| `MainActivity.kt` | Add one line | Your existing file |
| `AndroidManifest.xml` | Add permissions | Your existing file |

---

## Import Statement

Add this import to MainActivity (if not auto-imported):
```kotlin
import com.buzbuz.smartautoclicker.core.base.di.BackupAutoLoader
```

---

## For Hilt Users

If using Hilt, you can also add to your Application class:

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BackupAutoLoader.loadBackupIfFirstRun(this)  // ADD HERE
    }
}
```

---

## One-Line Summary

**Copy 2 files, add 1 line of code, verify 2 permissions → Done!**

---

Questions? Check:
1. `BackupAutoLoader.kt` for inline documentation
2. `INSTALLATION_GUIDE.md` for detailed guide
3. Logcat output: `adb logcat | grep -i backup`
