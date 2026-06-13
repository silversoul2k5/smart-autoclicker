/*
 * MINIMAL ALTERNATIVE: BackupAutoLoaderSimple.kt
 * 
 * Use this if you want the smallest possible code footprint.
 * This is a lightweight version with minimal features.
 * 
 * Token Count: ~50% less than full version
 * Features: Essential only - copy backup & mark as done
 * 
 * For most users, this is sufficient.
 */

package com.buzbuz.smartautoclicker.core.base.di

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object BackupAutoLoaderSimple {
    private const val TAG = "BackupAutoLoader"
    private const val PREF_NAME = "backup"
    private const val PREF_KEY = "loaded"
    private const val BACKUP_NAME = "SmartAutoClicker-Backup.zip"
    
    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        if (prefs.getBoolean(PREF_KEY, false)) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Copy from assets
                val input = context.assets.open(BACKUP_NAME)
                val output = File(context.getExternalFilesDir(null), BACKUP_NAME)
                output.parentFile?.mkdirs()
                input.use { inp ->
                    output.outputStream().use { out ->
                        inp.copyTo(out)
                    }
                }
                
                // Mark as loaded
                prefs.edit().putBoolean(PREF_KEY, true).apply()
                Log.d(TAG, "Backup loaded: ${output.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Backup load failed", e)
            }
        }
    }
}

/*
 * USAGE:
 * 
 * In MainActivity.onCreate():
 * 
 *     BackupAutoLoaderSimple.load(this)
 */
