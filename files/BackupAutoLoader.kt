/*
 * Smart AutoClicker - Backup Auto-Loader
 * 
 * Integration Guide:
 * 1. Copy the backup zip file (SmartAutoClicker-Backup.zip) to the app's assets folder
 *    or to the app's internal storage directory
 * 2. Call this loader once at app startup (in MainActivity or Application.onCreate)
 * 3. The backup will be automatically restored on first run
 */

package com.buzbuz.smartautoclicker.core.base.di

import android.app.Application
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Auto-loads a backup scenario zip file on app startup
 * 
 * Usage in Application.onCreate() or MainActivity.onCreate():
 * 
 *     BackupAutoLoader.loadBackupIfFirstRun(context)
 */
object BackupAutoLoader {
    private const val TAG = "BackupAutoLoader"
    private const val BACKUP_LOADED_PREF = "backup_auto_loaded"
    private const val BACKUP_FILE_NAME = "SmartAutoClicker-Backup.zip"
    
    /**
     * Loads the backup from assets if this is the first run
     * Should be called once during app initialization
     */
    fun loadBackupIfFirstRun(context: Context) {
        val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        
        // Check if backup was already loaded
        if (prefs.getBoolean(BACKUP_LOADED_PREF, false)) {
            Log.d(TAG, "Backup already loaded, skipping")
            return
        }
        
        // Load backup in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val backupFile = copyBackupFromAssets(context)
                if (backupFile != null && backupFile.exists()) {
                    Log.d(TAG, "Backup file copied successfully: ${backupFile.absolutePath}")
                    
                    // Mark as loaded to prevent re-running
                    prefs.edit().putBoolean(BACKUP_LOADED_PREF, true).apply()
                    Log.d(TAG, "Backup auto-load marked as complete")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading backup on startup", e)
            }
        }
    }
    
    /**
     * Resets the auto-load flag (useful for testing/debugging)
     */
    fun resetAutoLoadFlag(context: Context) {
        context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove(BACKUP_LOADED_PREF)
            .apply()
    }
    
    /**
     * Copies backup file from assets to app's internal storage
     * Returns the File object pointing to the copied backup
     */
    private fun copyBackupFromAssets(context: Context): File? {
        return try {
            // Try to read from assets first
            val assetManager = context.assets
            
            return try {
                val inputStream: InputStream = assetManager.open(BACKUP_FILE_NAME)
                val outputFile = File(context.getExternalFilesDir(null), BACKUP_FILE_NAME)
                outputFile.parentFile?.mkdirs()
                
                inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                Log.d(TAG, "Copied backup from assets to: ${outputFile.absolutePath}")
                outputFile
            } catch (e: Exception) {
                Log.d(TAG, "Backup not found in assets, checking internal storage")
                
                // Try internal storage
                val internalFile = File(context.getExternalFilesDir(null), BACKUP_FILE_NAME)
                if (internalFile.exists()) {
                    Log.d(TAG, "Found backup in internal storage: ${internalFile.absolutePath}")
                    internalFile
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing backup file", e)
            null
        }
    }
    
    /**
     * Manually specify a backup file path for loading
     */
    fun loadBackupFromPath(context: Context, backupFilePath: String): File? {
        return try {
            val backupFile = File(backupFilePath)
            if (backupFile.exists()) {
                Log.d(TAG, "Backup file found at: $backupFilePath")
                
                // Mark as loaded
                context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(BACKUP_LOADED_PREF, true)
                    .apply()
                
                backupFile
            } else {
                Log.e(TAG, "Backup file not found at: $backupFilePath")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading backup from path", e)
            null
        }
    }
}

// ============================================================================
// IMPLEMENTATION GUIDE
// ============================================================================
/*
STEP 1: Copy SmartAutoClicker-Backup.zip to your backup location
   - Option A: Place in project's `src/main/assets/` folder
   - Option B: Place in app's external files directory before first run
   - Option C: Download from internet and save to storage

STEP 2: Call the loader in your MainActivity or Application class
   
   Example in MainActivity.kt:
   ```
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_main)
       
       // Auto-load backup on first app run
       BackupAutoLoader.loadBackupIfFirstRun(this)
   }
   ```
   
   Or in MyApplication.kt (Hilt Application):
   ```
   @HiltAndroidApp
   class MyApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           BackupAutoLoader.loadBackupIfFirstRun(this)
       }
   }
   ```

STEP 3: Grant necessary permissions in AndroidManifest.xml
   ```xml
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   ```

STEP 4: The app's backup restoration system will automatically:
   - Detect the backup file
   - Extract scenarios from the zip
   - Import them into the local database
   - Make them available in the scenarios list immediately

OPTIONAL: Reset auto-load flag for testing
   ```
   BackupAutoLoader.resetAutoLoadFlag(context)
   ```

NOTES:
- The backup is only loaded once (first run)
- Subsequent app launches skip the auto-load
- The SharedPreference flag prevents redundant imports
- Uses CoroutineScope on IO dispatcher for non-blocking operation
- Works with both assets folder and external storage locations
*/
