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
package com.buzbuz.smartautoclicker.feature.smart.config.ui.action.puzzle

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.buzbuz.smartautoclicker.core.domain.model.action.PuzzleSolver
import com.buzbuz.smartautoclicker.feature.smart.config.domain.EditionRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class PuzzleSolverViewModel @Inject constructor(
    private val editionRepository: EditionRepository,
) : ViewModel() {

    /** The action being configured by the user. */
    private val configuredPuzzle = editionRepository.editionState.editedActionState
        .mapNotNull { it.value as? PuzzleSolver }

    private val editedActionHasChanged: StateFlow<Boolean> =
        editionRepository.editionState.editedActionState
            .map { it.hasChanged }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Tells if the user is currently editing an action. If that's not the case, dialog should be closed. */
    val isEditingAction: Flow<Boolean> = editionRepository.isEditingAction
        .distinctUntilChanged()

    /** The name of the action. */
    val name: Flow<String?> = configuredPuzzle.map { it.name }
    /** Tells if the name is valid or not. */
    val nameError: Flow<Boolean> = configuredPuzzle.map { it.name.isNullOrEmpty() }

    val useGemini: Flow<Boolean> = configuredPuzzle.map { it.useGeminiVision }
    val apiKey: Flow<String> = configuredPuzzle.map { it.geminiApiKey }
    val sliderDuration: Flow<String> = configuredPuzzle.map { it.sliderDuration.toString() }
    val animatedSwipe: Flow<Boolean> = configuredPuzzle.map { it.animatedSwipe }
    val retryOnFailure: Flow<Boolean> = configuredPuzzle.map { it.retryOnFailure }
    val maxRetries: Flow<String> = configuredPuzzle.map { it.maxRetries.toString() }

    /** Tells if the configured action is valid and can be saved. */
    val isValidAction: Flow<Boolean> = combine(nameError, useGemini, apiKey) { nameErr, gemini, key ->
        !nameErr && (!gemini || key.isNotEmpty())
    }

    fun hasUnsavedModifications(): Boolean = editedActionHasChanged.value

    fun setName(name: String) {
        updateEditedAction { it.copy(name = name) }
    }

    fun toggleUseGemini() {
        updateEditedAction { it.copy(useGeminiVision = !it.useGeminiVision) }
    }

    fun setApiKey(key: String) {
        updateEditedAction { it.copy(geminiApiKey = key) }
    }

    fun setSliderDuration(duration: Long?) {
        duration ?: return
        updateEditedAction { it.copy(sliderDuration = duration) }
    }

    fun toggleAnimatedSwipe() {
        updateEditedAction { it.copy(animatedSwipe = !it.animatedSwipe) }
    }

    fun toggleRetryOnFailure() {
        updateEditedAction { it.copy(retryOnFailure = !it.retryOnFailure) }
    }

    fun setMaxRetries(retries: Int?) {
        retries ?: return
        updateEditedAction { it.copy(maxRetries = retries) }
    }

    private fun updateEditedAction(closure: (oldValue: PuzzleSolver) -> PuzzleSolver) {
        (editionRepository.editionState.getEditedAction<PuzzleSolver>())?.let { oldValue ->
            viewModelScope.launch {
                editionRepository.updateEditedAction(closure(oldValue))
            }
        }
    }
}
