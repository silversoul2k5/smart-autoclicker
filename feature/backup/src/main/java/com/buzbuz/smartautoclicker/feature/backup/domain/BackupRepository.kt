/*
 * Copyright (C) 2024 Kevin Buzeau
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
package com.buzbuz.smartautoclicker.feature.backup.domain

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.util.Log

import com.buzbuz.smartautoclicker.core.database.ClickDatabase
import com.buzbuz.smartautoclicker.core.database.entity.ActionEntity
import com.buzbuz.smartautoclicker.core.database.entity.ActionType
import com.buzbuz.smartautoclicker.core.database.entity.ClickPositionType
import com.buzbuz.smartautoclicker.core.database.entity.CompleteActionEntity
import com.buzbuz.smartautoclicker.core.database.entity.CompleteEventEntity
import com.buzbuz.smartautoclicker.core.database.entity.CompleteScenario
import com.buzbuz.smartautoclicker.core.database.entity.ConditionEntity
import com.buzbuz.smartautoclicker.core.database.entity.ConditionType
import com.buzbuz.smartautoclicker.core.database.entity.EventEntity
import com.buzbuz.smartautoclicker.core.database.entity.EventType
import com.buzbuz.smartautoclicker.core.database.entity.ScenarioEntity
import com.buzbuz.smartautoclicker.core.domain.IRepository
import com.buzbuz.smartautoclicker.core.domain.model.AND
import com.buzbuz.smartautoclicker.core.domain.model.IN_AREA
import com.buzbuz.smartautoclicker.core.dumb.data.database.DumbDatabase
import com.buzbuz.smartautoclicker.core.dumb.domain.IDumbRepository
import com.buzbuz.smartautoclicker.feature.backup.data.BackupEngine
import com.buzbuz.smartautoclicker.feature.backup.data.BackupProgress
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dumbDatabase: DumbDatabase,
    private val dumbRepository: IDumbRepository,
    private val smartDatabase: ClickDatabase,
    private val smartRepository: IRepository,
) {

    private val backupEngine: BackupEngine = BackupEngine(
        appDataDir = context.filesDir,
        contentResolver = context.contentResolver,
    )

    /**
     * Create a backup of the provided scenario into the provided file.
     *
     * @param zipFileUri the uri of the file to write the backup into. Must be retrieved using the DocumentProvider.
     * @param dumbScenarios the dumb scenarios to backup.
     * @param smartScenarios the smart scenarios to backup.
     * @param screenSize the size of this device screen.
     *
     * @return a flow on the backup creation progress.
     */
    fun createScenarioBackup(
        zipFileUri: Uri,
        dumbScenarios: List<Long>,
        smartScenarios: List<Long>,
        screenSize: Point,
    ) = channelFlow {
        launch {
            backupEngine.createBackup(
                zipFileUri = zipFileUri,
                dumbScenarios = dumbScenarios.mapNotNull {
                    dumbDatabase.dumbScenarioDao().getDumbScenariosWithAction(it)
                },
                smartScenarios = smartScenarios.mapNotNull {
                    smartDatabase.scenarioDao().getCompleteScenario(it)
                },
                screenSize = screenSize,
                progress = BackupProgress(
                    onError = { send(Backup.Error) },
                    onProgressChanged = { current, max -> send(Backup.Loading(current, max)) },
                    onCompleted = { dumbs, smarts, failureCount, compatWarning ->
                        send(Backup.Completed(
                            successCount = dumbs.size + smarts.size,
                            failureCount = failureCount,
                            compatWarning = compatWarning,
                        ))
                    }
                )
            )
        }
    }

    /**
     * Restore a backup of scenarios from the provided file.
     *
     * @param zipFileUri the uri of the file to read the backup from. Must be retrieved using the DocumentProvider.
     * @param screenSize the size of this device screen.
     *
     * @return a flow on the backup import progress.
     */
    fun restoreScenarioBackup(zipFileUri: Uri, screenSize: Point) = channelFlow {
        launch {
            backupEngine.loadBackup(
                zipFileUri,
                screenSize,
                BackupProgress(
                    onError = { send(Backup.Error) },
                    onProgressChanged = { current, max -> send(Backup.Loading(current, max)) },
                    onVerification = { send(Backup.Verification) },
                    onCompleted = { dumbs, smarts, failureCount, compatWarning ->
                        var totalFailures = failureCount

                        val dumbsSuccess = dumbs.toMutableList()
                        dumbs.forEach { completeScenario ->
                            if (dumbRepository.addDumbScenarioCopy(completeScenario) == null) {
                                dumbsSuccess.remove(completeScenario)
                                totalFailures++
                            }
                        }

                        val smartsSuccess = smarts.toMutableList()
                        smarts.forEach { completeScenario ->
                            if (smartRepository.addScenarioCopy(completeScenario) == null) {
                                smartsSuccess.remove(completeScenario)
                                totalFailures++
                            }
                        }

                        send(Backup.Completed(
                            successCount = dumbsSuccess.size + smartsSuccess.size,
                            failureCount = totalFailures,
                            compatWarning = compatWarning,
                        ))
                    }
                )
            )
        }
    }

    suspend fun restoreBundledDefaultBackup(screenSize: Point): Backup.Completed {
        var result: Backup = Backup.Error

        try {
            backupEngine.loadBackup(
                inputStream = context.assets.open(DEFAULT_BACKUP_ASSET),
                screenSize = screenSize,
                progress = BackupProgress(
                    onError = { result = Backup.Error },
                    onProgressChanged = { _, _ -> },
                    onVerification = {},
                    onCompleted = { dumbs, smarts, failureCount, compatWarning ->
                        var totalFailures = failureCount

                        val dumbsSuccess = dumbs.toMutableList()
                        dumbs.forEach { completeScenario ->
                            if (dumbRepository.addDumbScenarioCopy(completeScenario) == null) {
                                dumbsSuccess.remove(completeScenario)
                                totalFailures++
                            }
                        }

                        val smartsSuccess = smarts.toMutableList()
                        smarts.forEach { completeScenario ->
                            if (smartRepository.addScenarioCopy(completeScenario) == null) {
                                smartsSuccess.remove(completeScenario)
                                totalFailures++
                            }
                        }

                        result = Backup.Completed(
                            successCount = dumbsSuccess.size + smartsSuccess.size,
                            failureCount = totalFailures,
                            compatWarning = compatWarning,
                        )
                    },
                )
            )
        } catch (ex: Exception) {
            Log.e(TAG, "Bundled default backup restore failed", ex)
            result = Backup.Error
        }

        val completed = result as? Backup.Completed
        if (completed != null && completed.successCount > 0) return completed

        Log.w(TAG, "Bundled default backup produced no scenarios, creating fallback scenario.")
        return createFallbackScenario()
    }

    private suspend fun createFallbackScenario(): Backup.Completed {
        return if (copyFallbackConditionImage() && smartRepository.addScenarioCopy(createFallbackCompleteScenario()) != null) {
            Backup.Completed(successCount = 1, failureCount = 0, compatWarning = false)
        } else {
            Backup.Completed(successCount = 0, failureCount = 1, compatWarning = false)
        }
    }

    private fun copyFallbackConditionImage(): Boolean =
        try {
            val outputFile = File(context.filesDir, FALLBACK_CONDITION_PATH)
            if (!outputFile.exists()) {
                ZipInputStream(context.assets.open(DEFAULT_BACKUP_ASSET)).use { zipStream ->
                    generateSequence { zipStream.nextEntry }
                        .firstOrNull { zipEntry -> zipEntry.name == FALLBACK_BACKUP_ENTRY }
                        ?: return false

                    outputFile.outputStream().use { outputStream ->
                        zipStream.copyTo(outputStream)
                    }
                }
            }
            true
        } catch (ioEx: IOException) {
            Log.e(TAG, "Can't copy fallback condition image", ioEx)
            false
        }

    private fun createFallbackCompleteScenario(): CompleteScenario =
        CompleteScenario(
            scenario = ScenarioEntity(
                id = 0,
                name = "Binance Red Packet Preset",
                detectionQuality = 763,
                randomize = true,
            ),
            events = listOf(
                CompleteEventEntity(
                    event = EventEntity(
                        id = FALLBACK_EVENT_ID,
                        scenarioId = FALLBACK_SCENARIO_ID,
                        name = "Claim When Available",
                        conditionOperator = AND,
                        priority = 0,
                        type = EventType.IMAGE_EVENT,
                        keepDetecting = true,
                    ),
                    actions = listOf(
                        CompleteActionEntity(
                            action = ActionEntity(
                                id = 0,
                                eventId = FALLBACK_EVENT_ID,
                                name = "Tap claim",
                                type = ActionType.CLICK,
                                clickPositionType = ClickPositionType.ON_DETECTED_CONDITION,
                                clickOnConditionId = FALLBACK_CONDITION_ID,
                                pressDuration = 50,
                            ),
                            intentExtras = emptyList(),
                            eventsToggle = emptyList(),
                        )
                    ),
                    conditions = listOf(
                        ConditionEntity(
                            id = FALLBACK_CONDITION_ID,
                            eventId = FALLBACK_EVENT_ID,
                            name = "Yellow Claim Button",
                            type = ConditionType.ON_IMAGE_DETECTED,
                            priority = 0,
                            path = FALLBACK_CONDITION_PATH,
                            areaLeft = 0,
                            areaTop = 0,
                            areaRight = 167,
                            areaBottom = 163,
                            threshold = 15,
                            detectionType = IN_AREA,
                            shouldBeDetected = true,
                            detectionAreaLeft = 381,
                            detectionAreaTop = 368,
                            detectionAreaRight = 684,
                            detectionAreaBottom = 1507,
                            detectionIntervalMs = 500,
                            requiredDetectionCount = 1,
                            resetAfterTrigger = true,
                        )
                    ),
                )
            )
        )
}

private const val TAG = "BackupRepository"
private const val DEFAULT_BACKUP_ASSET = "SmartAutoClicker-Backup (1).zip"
private const val FALLBACK_BACKUP_ENTRY = "1/BinanceClaim_-477295430.png"
private const val FALLBACK_CONDITION_PATH = "BinanceClaim_-477295430.png"
private const val FALLBACK_SCENARIO_ID = 1L
private const val FALLBACK_EVENT_ID = 1L
private const val FALLBACK_CONDITION_ID = 1L
