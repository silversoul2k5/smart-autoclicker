/*
 * Copyright (C) 2025 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.application

import android.app.Application
import android.util.Log
import com.buzbuz.smartautoclicker.ComponentConfig
import com.buzbuz.smartautoclicker.core.base.data.AppComponentsManager
import com.buzbuz.smartautoclicker.core.display.config.DisplayConfigManager
import com.buzbuz.smartautoclicker.core.domain.IRepository
import com.buzbuz.smartautoclicker.core.dumb.domain.IDumbRepository
import com.buzbuz.smartautoclicker.feature.backup.domain.BackupRepository
import com.buzbuz.smartautoclicker.feature.backup.domain.Backup
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SmartAutoClickerApplication : Application() {

    @Inject lateinit var appComponentsManager: AppComponentsManager
    @Inject lateinit var backupRepository: BackupRepository
    @Inject lateinit var smartRepository: IRepository
    @Inject lateinit var dumbRepository: IDumbRepository
    @Inject lateinit var displayConfigManager: DisplayConfigManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        val componentConfig = ComponentConfig
        appComponentsManager.apply {
            registerOriginalAppId(componentConfig.ORIGINAL_APP_ID)
            registerSmartAutoClickerService(componentConfig.smartAutoClickerService)
            registerScenarioActivity(componentConfig.scenarioActivity)
        }

        DynamicColors.applyToActivitiesIfAvailable(this)
        preloadDefaultBackupIfNeeded()
    }

    private fun preloadDefaultBackupIfNeeded() {
        applicationScope.launch {
            val preferences = getSharedPreferences(DEFAULT_PRELOAD_PREFS, MODE_PRIVATE)
            if (preferences.getBoolean(KEY_DEFAULT_PRELOAD_DONE, false)) return@launch

            val hasSmartScenarios = smartRepository.scenarios.first().isNotEmpty()
            val hasDumbScenarios = dumbRepository.dumbScenarios.first().isNotEmpty()
            if (hasSmartScenarios || hasDumbScenarios) {
                preferences.edit().putBoolean(KEY_DEFAULT_PRELOAD_DONE, true).apply()
                return@launch
            }

            val result = backupRepository.restoreBundledDefaultBackup(displayConfigManager.displayConfig.sizePx)
            preferences.edit().putBoolean(KEY_DEFAULT_PRELOAD_DONE, true).apply()

            if (result.successCount > 0) {
                Log.i(TAG, "Default automation preload completed: $result")
            } else {
                Log.e(TAG, "Default automation preload failed: $result")
            }
        }
    }
}

private const val TAG = "SmartAutoClickerApp"
private const val DEFAULT_PRELOAD_PREFS = "default_preload"
private const val KEY_DEFAULT_PRELOAD_DONE = "default_preload_done"
