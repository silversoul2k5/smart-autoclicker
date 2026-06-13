# Smart AutoClicker Backup Pre-Installation Solution

## 📦 What You're Getting

This solution enables **automatic scenario loading** on first app install, so users have working scenarios ready to use immediately.

### Files Included

1. **BackupAutoLoader.kt** (Full-featured)
   - Complete implementation with error handling
   - Logging capabilities
   - Multiple load methods
   - ~200 lines with documentation

2. **BackupAutoLoaderSimple.kt** (Minimal)
   - Lightweight version (~30 lines)
   - Essential functionality only
   - Same end result, less overhead
   - **Recommended for most users**

3. **setup.sh** (Automation)
   - Bash script to set up directories
   - Optional but helpful

4. **INSTALLATION_GUIDE.md** (Detailed)
   - Complete step-by-step guide
   - Troubleshooting section
   - Technical details

5. **QUICK_REFERENCE.md** (Fast)
   - 3-step summary
   - File locations table
   - Quick troubleshooting

6. **README.md** (This file)
   - Overview of the solution
   - Quick start guide

---

## ⚡ Quick Start (3 Steps)

### Step 1: Choose Your Implementation

**Option A: Minimal (Recommended)**
```bash
# Use the simple version
cp BackupAutoLoaderSimple.kt YOUR_PROJECT/core/base/src/main/java/.../di/
```

**Option B: Full-Featured**
```bash
# Use the complete version with all features
cp BackupAutoLoader.kt YOUR_PROJECT/core/base/src/main/java/.../di/
```

### Step 2: Place Your Backup
```bash
cp SmartAutoClicker-Backup.zip YOUR_PROJECT/app/src/main/assets/
```

### Step 3: Integrate Into MainActivity
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    
    // Add this ONE line:
    BackupAutoLoaderSimple.load(this)  // or BackupAutoLoader.loadBackupIfFirstRun(this)
}
```

---

## 📋 Complete Checklist

- [ ] Decide between Simple vs Full version
- [ ] Create `assets` folder if missing: `app/src/main/assets/`
- [ ] Copy backup file to `app/src/main/assets/SmartAutoClicker-Backup.zip`
- [ ] Create `di` folder: `core/base/src/main/java/.../core/base/di/`
- [ ] Copy chosen loader (`BackupAutoLoader*.kt`) to `di` folder
- [ ] Add import to MainActivity: `import com.buzbuz.smartautoclicker.core.base.di.*`
- [ ] Add one line to `MainActivity.onCreate()`
- [ ] Verify permissions in `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  ```
- [ ] Build: `./gradlew assembleDebug`
- [ ] Test on device/emulator

---

## 🔍 Which Version Should I Use?

### Use BackupAutoLoaderSimple.kt if:
- ✓ You want minimal code (30 lines)
- ✓ You only need basic backup loading
- ✓ You want to minimize dependencies
- ✓ **You're on a free token plan** ← THIS IS YOU

### Use BackupAutoLoader.kt if:
- You need error logging for debugging
- You want multiple load methods
- You need to reset the auto-load flag
- You want comprehensive documentation inline

---

## 📁 File Structure

After integration, your project should look like:

```
your-smart-autoclicker-project/
│
├── app/
│   └── src/main/
│       ├── assets/
│       │   └── SmartAutoClicker-Backup.zip  ← Your backup
│       ├── java/com/buzbuz/smartautoclicker/activity/
│       │   └── MainActivity.kt  ← Add one line here
│       └── AndroidManifest.xml  ← Verify permissions
│
├── core/
│   └── base/
│       └── src/main/java/com/buzbuz/smartautoclicker/core/base/di/
│           └── BackupAutoLoaderSimple.kt  ← Copy here (or BackupAutoLoader.kt)
│
└── settings.gradle.kts
```

---

## 🚀 How It Works

### First Launch
1. App starts → `MainActivity.onCreate()`
2. `BackupAutoLoaderSimple.load(context)` is called
3. Checks SharedPreferences for "loaded" flag
4. If not set:
   - Copies `SmartAutoClicker-Backup.zip` from assets
   - Places it in app's external storage
   - Sets flag to prevent re-loading
5. App's native backup system detects and imports scenarios
6. User sees scenarios immediately ✓

### Subsequent Launches
- Flag is already set
- `load()` function returns immediately
- No performance impact

---

## 🔧 Implementation Details

### Minimal Version Comparison

| Feature | Simple | Full |
|---------|--------|------|
| Core functionality | ✓ | ✓ |
| Error handling | Basic | Detailed |
| Logging | Minimal | Extensive |
| Reset capability | ✗ | ✓ |
| Custom path loading | ✗ | ✓ |
| Lines of code | ~30 | ~200 |
| Token count | Low | Medium |

---

## 📝 Code Snippet for MainActivity

```kotlin
package com.buzbuz.smartautoclicker.feature.smart.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.buzbuz.smartautoclicker.core.base.di.BackupAutoLoaderSimple

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // ← ADD THIS ONE LINE ←
        BackupAutoLoaderSimple.load(this)
        // ← ADD THIS ONE LINE ←
        
        // ... rest of your code ...
    }
}
```

---

## 🧪 Testing Procedure

### Build
```bash
# Ensure backup file exists
ls app/src/main/assets/SmartAutoClicker-Backup.zip

# Build
./gradlew assembleDebug
```

### Install
```bash
# Install clean
adb uninstall com.buzbuz.smartautoclicker
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Verify
1. Open the app
2. Grant storage permissions if prompted
3. Navigate to "Scenarios" section
4. **✓ Your backup scenarios should appear automatically**
5. Click any scenario to verify it works

### Debug (if not working)
```bash
# View logs
adb logcat | grep -i backup

# Check file was copied
adb shell ls -la /data/data/com.buzbuz.smartautoclicker/files/

# Check SharedPreferences
# Use Android Studio's Device File Explorer:
# /data/data/com.buzbuz.smartautoclicker/shared_prefs/backup.xml
```

---

## ❌ Common Issues & Solutions

### Issue: Scenarios not appearing
**Solution**: 
- Check logcat for errors: `adb logcat | grep -i backup`
- Verify backup file exists: `ls app/src/main/assets/SmartAutoClicker-Backup.zip`
- Clear app data: `adb shell pm clear com.buzbuz.smartautoclicker`
- Reinstall app

### Issue: Build fails - "Cannot find BackupAutoLoader"
**Solution**:
- Verify file is in correct location
- Check package name matches: `com.buzbuz.smartautoclicker.core.base.di`
- Rebuild project: `./gradlew clean assembleDebug`

### Issue: "File not found" error in logcat
**Solution**:
- Backup filename must be exactly: `SmartAutoClicker-Backup.zip`
- Must be in: `app/src/main/assets/`
- Check folder name is `assets` (not `asset`)

### Issue: Permission denied
**Solution**:
- Add to AndroidManifest.xml:
  ```xml
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  ```
- Grant runtime permissions when prompted

### Issue: Scenarios imported twice
**Solution**:
- Clear app data: `adb shell pm clear com.buzbuz.smartautoclicker`
- Shared preferences is saving wrong value - check `backup.xml`

---

## 💡 Tips & Tricks

### View SharedPreferences
```bash
adb shell cat /data/data/com.buzbuz.smartautoclicker/shared_prefs/backup.xml
```

### Force Reload (for testing)
```kotlin
// Add this to clear the flag for re-testing:
context.getSharedPreferences("backup", Context.MODE_PRIVATE)
    .edit()
    .clear()
    .apply()
```

### Extract Backup Contents
```bash
# See what scenarios are in your backup:
unzip -l SmartAutoClicker-Backup.zip
```

### Monitor Import in Real-time
```bash
adb logcat -f -t 100 | grep -i "scenario\|import\|backup"
```

---

## 📚 Documentation Files

- **QUICK_REFERENCE.md** - Fast overview and troubleshooting table
- **INSTALLATION_GUIDE.md** - Detailed step-by-step with all options
- **BackupAutoLoader.kt** - Full-featured version with inline docs
- **BackupAutoLoaderSimple.kt** - Minimal version, perfect for you
- **setup.sh** - Automated setup helper

---

## ✅ Success Criteria

You'll know it's working when:

1. ✓ App builds without errors
2. ✓ App installs on device/emulator
3. ✓ First launch: scenarios appear in list
4. ✓ Subsequent launches: scenarios still there
5. ✓ Can open and edit scenarios
6. ✓ Can run scenarios
7. ✓ No permission errors in logcat

---

## 🎯 Time Estimate

- **Setup time**: 2-5 minutes
- **Build time**: 30-60 seconds
- **First app launch**: 5-10 seconds (one-time import)
- **Normal launches**: <2 seconds

---

## 📞 Need Help?

1. Check **QUICK_REFERENCE.md** for fast troubleshooting
2. Check **INSTALLATION_GUIDE.md** for detailed steps
3. Review inline code comments in **BackupAutoLoader*.kt**
4. Check logcat output: `adb logcat | grep -i backup`
5. Verify file paths and package names match exactly

---

## 🎉 You're Ready!

You now have everything needed to integrate backup pre-installation into your Smart AutoClicker app. Choose the minimal version, follow the 3 steps, and your app will be ready to ship with working scenarios!

**Recommended approach for you:**
1. Use `BackupAutoLoaderSimple.kt` (minimal code)
2. Follow the 3-step Quick Start
3. Test on device
4. Deploy to users

Good luck! 🚀
