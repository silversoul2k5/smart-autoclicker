# Smart AutoClicker - Backup Pre-Installation Guide

## Overview
This guide explains how to integrate the `SmartAutoClicker-Backup.zip` backup into the Smart AutoClicker app so that scenarios are preloaded and ready to launch immediately upon installation.

## Solution Architecture

The solution uses a two-part approach:
1. **BackupAutoLoader.kt** - A helper module that detects and loads the backup on first app run
2. **Integration Steps** - Simple modifications to existing app startup code

## Files Provided

- `BackupAutoLoader.kt` - Main auto-loader module (copy to `core/base/di/` folder)

## Installation Steps

### Step 1: Prepare the Backup File
Rename your backup: `SmartAutoClicker-Backup (1).zip` → `SmartAutoClicker-Backup.zip`

### Step 2: Choose Installation Location

**Option A: Asset Bundle (Recommended)**
- Copy `SmartAutoClicker-Backup.zip` to `app/src/main/assets/`
- Backup loads automatically from APK
- No additional permissions needed

**Option B: External Storage**
- Copy to app's external files directory before first launch
- Provides flexibility for updates
- Requires storage permissions

**Option C: Programmatic Download**
- Download from server/cloud storage
- Most flexible but requires internet
- Requires network permissions

### Step 3: Integrate BackupAutoLoader

#### File: `core/base/src/main/java/com/buzbuz/smartautoclicker/core/base/di/BackupAutoLoader.kt`
Copy the provided `BackupAutoLoader.kt` to this location (create di folder if it doesn't exist)

#### File: `app/src/main/AndroidManifest.xml`
Ensure these permissions are present:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

#### File: `app/src/main/java/com/buzbuz/smartautoclicker/activity/MainActivity.kt`
Add this code in the `onCreate()` method (after `setContentView`):

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    
    // Auto-load backup scenarios on first app run
    BackupAutoLoader.loadBackupIfFirstRun(this)
    
    // ... rest of your onCreate code
}
```

#### Alternative: Application Class
If your app uses a custom Application class (for Hilt, etc.):

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Auto-load backup scenarios on first app run
        BackupAutoLoader.loadBackupIfFirstRun(this)
    }
}
```

### Step 4: Build and Test

```bash
# Ensure backup file is in assets
ls app/src/main/assets/SmartAutoClicker-Backup.zip

# Build the app
./gradlew assembleDebug
# or
./gradlew assembleRelease

# Install on device/emulator
adb install -r app/build/outputs/apk/*/app-*.apk

# Test:
# 1. Open the app
# 2. Grant required permissions if prompted
# 3. Navigate to "Scenarios" section
# 4. Your backup scenarios should appear automatically
# 5. Click any scenario to edit/run it
```

## How It Works

### First Run
1. `BackupAutoLoader.loadBackupIfFirstRun()` is called
2. Checks if backup has already been loaded (via SharedPreferences)
3. Copies `SmartAutoClicker-Backup.zip` from assets to app storage
4. App's native backup restoration system detects and imports the scenarios
5. Sets flag in SharedPreferences to prevent re-importing

### Subsequent Runs
1. Flag is already set, so no action is taken
2. App launches normally with imported scenarios available
3. Users can continue using or modifying their scenarios

## Technical Details

### Permissions Required
- `READ_EXTERNAL_STORAGE` - To read the backup file
- `WRITE_EXTERNAL_STORAGE` - To write extracted scenarios

### Storage Locations Used
- **Assets**: `app/src/main/assets/SmartAutoClicker-Backup.zip`
- **App Storage**: `context.getExternalFilesDir(null)/SmartAutoClicker-Backup.zip`
- **Shared Preferences**: `backup_prefs` with key `backup_auto_loaded`

### Thread Safety
- Uses `CoroutineScope(Dispatchers.IO)` for non-blocking operation
- Doesn't block UI thread
- Safe to call from main thread

## Troubleshooting

### Backup Not Loading
1. Check if `SmartAutoClicker-Backup.zip` exists in `app/src/main/assets/`
2. Verify file name matches exactly (case-sensitive)
3. Check logcat for error messages: `grep "BackupAutoLoader" logcat.txt`
4. Clear app data: `adb shell pm clear com.buzbuz.smartautoclicker`
5. Reinstall app fresh

### Scenarios Not Appearing
1. Check app logs for import errors
2. Verify backup file is valid (can open with zip tools)
3. Try loading manually: Settings → Backup → Restore from File
4. Reset auto-load flag:
   ```kotlin
   BackupAutoLoader.resetAutoLoadFlag(context)
   ```

### Permission Errors
1. Ensure permissions are declared in `AndroidManifest.xml`
2. For Android 6.0+, grant runtime permissions when prompted
3. Check device Settings → Apps → Smart AutoClicker → Permissions

## Advanced Options

### Manual Reset for Testing
```kotlin
// In any activity/fragment:
BackupAutoLoader.resetAutoLoadFlag(this)
// Next app restart will reload the backup
```

### Load from Custom Path
```kotlin
val backupFile = BackupAutoLoader.loadBackupFromPath(
    context,
    "/path/to/backup/SmartAutoClicker-Backup.zip"
)
```

### Load Multiple Backups
Modify `BackupAutoLoader` to handle multiple backup files:
1. Create multiple zip files in assets
2. Loop through each and call loader for each
3. Adjust SharedPreferences key for each backup

## Implementation Checklist

- [ ] Backup file renamed to `SmartAutoClicker-Backup.zip`
- [ ] Backup file copied to `app/src/main/assets/`
- [ ] `BackupAutoLoader.kt` copied to `core/base/di/` (or appropriate location)
- [ ] Permissions added to `AndroidManifest.xml`
- [ ] Integration code added to `MainActivity.onCreate()` or `Application.onCreate()`
- [ ] App builds successfully
- [ ] App installs without errors
- [ ] Scenarios appear in app on first launch
- [ ] Scenarios persist after app restart

## Build Variants

The solution works with all build flavors (fDroid, PlayStore, etc.):
- Paste `BackupAutoLoader.kt` once in a shared location
- Call from any activity/application class
- Backup file in assets is flavor-agnostic

## Size Considerations

- Backup file will be embedded in APK
- Example: 1MB backup → 1MB larger APK
- Users still have option to delete scenarios if needed

## Conclusion

This solution provides a seamless experience where users get working scenarios immediately upon installation. No manual backup restoration is needed.

For questions or issues, check:
1. Logcat output
2. SharedPreferences using Android Studio's Device Explorer
3. File existence in Device File Explorer
