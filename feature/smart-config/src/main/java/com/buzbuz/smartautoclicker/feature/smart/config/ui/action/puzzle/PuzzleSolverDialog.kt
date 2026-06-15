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

import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.buzbuz.smartautoclicker.core.common.overlays.dialog.implementation.DialogChoice
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.implementation.MultiChoiceDialog
import com.buzbuz.smartautoclicker.core.common.overlays.base.viewModels
import com.buzbuz.smartautoclicker.core.common.overlays.dialog.OverlayDialog
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.DialogNavigationButton
import com.buzbuz.smartautoclicker.core.ui.bindings.dialogs.setButtonEnabledState
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setChecked
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setLabel
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setOnClickListener
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setOnTextChangedListener
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setText
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setError
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setTitle
import com.buzbuz.smartautoclicker.core.ui.bindings.fields.setDescription
import com.buzbuz.smartautoclicker.feature.smart.config.R
import com.buzbuz.smartautoclicker.feature.smart.config.databinding.DialogConfigActionPuzzleSolverBinding
import com.buzbuz.smartautoclicker.feature.smart.config.di.ScenarioConfigViewModelsEntryPoint
import com.buzbuz.smartautoclicker.feature.smart.config.ui.action.OnActionConfigCompleteListener
import com.buzbuz.smartautoclicker.feature.smart.config.ui.common.dialogs.showCloseWithoutSavingDialog

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch

class PuzzleSolverDialog(
    private val listener: OnActionConfigCompleteListener,
) : OverlayDialog(R.style.ScenarioConfigTheme) {

    /** The view model for this dialog. */
    private val viewModel: PuzzleSolverViewModel by viewModels(
        entryPoint = ScenarioConfigViewModelsEntryPoint::class.java,
        creator = { puzzleSolverViewModel() },
    )

    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogConfigActionPuzzleSolverBinding

    override fun onCreateView(): ViewGroup {
        viewBinding = DialogConfigActionPuzzleSolverBinding.inflate(LayoutInflater.from(context)).apply {
            layoutTopBar.apply {
                dialogTitle.setText(R.string.item_puzzle_solver_title)

                buttonDismiss.setDebouncedOnClickListener { back() }
                buttonSave.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener { onSaveButtonClicked() }
                }
                buttonDelete.apply {
                    visibility = View.VISIBLE
                    setDebouncedOnClickListener { onDeleteButtonClicked() }
                }
            }

            fieldName.apply {
                setLabel(R.string.generic_name)
                setOnTextChangedListener { viewModel.setName(it.toString()) }
                textField.filters = arrayOf<InputFilter>(
                    InputFilter.LengthFilter(context.resources.getInteger(R.integer.name_max_length))
                )
            }
            hideSoftInputOnFocusLoss(fieldName.textField)

            fieldPuzzleType.apply {
                setOnClickListener { showPuzzleTypeSelectionDialog() }
            }

            fieldUseGemini.apply {
                setTitle("Use Gemini Vision API")
                setOnClickListener { viewModel.toggleUseGemini() }
            }

            fieldApiKey.apply {
                setLabel(R.string.generic_config) // Or add specific string
                setOnTextChangedListener { viewModel.setApiKey(it.toString()) }
            }

            fieldSliderDuration.apply {
                setLabel(R.string.input_field_label_click_press_duration)
                setOnTextChangedListener { 
                    viewModel.setSliderDuration(it.toString().toLongOrNull())
                }
            }

            fieldAnimatedSwipe.apply {
                setTitle("Animated Swipe")
                setOnClickListener { viewModel.toggleAnimatedSwipe() }
            }

            fieldRetryOnFailure.apply {
                setTitle("Retry on Failure")
                setOnClickListener { viewModel.toggleRetryOnFailure() }
            }

            fieldMaxRetries.apply {
                setLabel(R.string.generic_modify)
                setOnTextChangedListener {
                    viewModel.setMaxRetries(it.toString().toIntOrNull())
                }
            }
        }

        return viewBinding.root
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.isEditingAction.collect(::onActionEditingStateChanged) }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.name.collect(viewBinding.fieldName::setText) }
                launch { viewModel.nameError.collect(viewBinding.fieldName::setError) }
                launch { viewModel.puzzleType.collect(::updatePuzzleType) }
                launch { viewModel.useGemini.collect(::updateUseGemini) }
                launch { viewModel.apiKey.collect(viewBinding.fieldApiKey::setText) }
                launch { viewModel.sliderDuration.collect(::updateSliderDuration) }
                launch { viewModel.animatedSwipe.collect(viewBinding.fieldAnimatedSwipe::setChecked) }
                launch { viewModel.retryOnFailure.collect(::updateRetryOnFailure) }
                launch { viewModel.maxRetries.collect(::updateMaxRetries) }
                launch { viewModel.isValidAction.collect(::updateSaveButton) }
            }
        }
    }

    private fun updateUseGemini(useGemini: Boolean) {
        viewBinding.fieldUseGemini.setChecked(useGemini)
        viewBinding.fieldApiKey.root.visibility = if (useGemini) View.VISIBLE else View.GONE
        viewBinding.dividerGemini.visibility = if (useGemini) View.VISIBLE else View.GONE
    }

    private fun updatePuzzleType(type: String) {
        val choice = PuzzleTypeChoice.fromType(type)
        viewBinding.fieldPuzzleType.apply {
            setTitle(context.getString(choice.title))
            setDescription(choice.description?.let { context.getString(it) })
        }
    }

    private fun showPuzzleTypeSelectionDialog() {
        overlayManager.navigateTo(
            context = context,
            newOverlay = MultiChoiceDialog(
                theme = R.style.ScenarioConfigTheme,
                dialogTitleText = R.string.dialog_title_action_type,
                choices = PuzzleTypeChoice.allChoices,
                onChoiceSelected = { choice -> viewModel.setPuzzleType(choice.type) }
            )
        )
    }

    private fun updateSliderDuration(duration: String) {
        viewBinding.fieldSliderDuration.setText(duration, InputType.TYPE_CLASS_NUMBER)
    }

    private fun updateRetryOnFailure(retry: Boolean) {
        viewBinding.fieldRetryOnFailure.setChecked(retry)
        viewBinding.fieldMaxRetries.root.visibility = if (retry) View.VISIBLE else View.GONE
        viewBinding.dividerRetry.visibility = if (retry) View.VISIBLE else View.GONE
    }

    private fun updateMaxRetries(retries: String) {
        viewBinding.fieldMaxRetries.setText(retries, InputType.TYPE_CLASS_NUMBER)
    }

    private fun updateSaveButton(isValid: Boolean) {
        viewBinding.layoutTopBar.setButtonEnabledState(DialogNavigationButton.SAVE, isValid)
    }

    override fun back() {
        if (viewModel.hasUnsavedModifications()) {
            context.showCloseWithoutSavingDialog {
                listener.onDismissClicked()
                super.back()
            }
            return
        }

        listener.onDismissClicked()
        super.back()
    }

    private fun onSaveButtonClicked() {
        listener.onConfirmClicked()
        super.back()
    }

    private fun onDeleteButtonClicked() {
        listener.onDeleteClicked()
        super.back()
    }

    private fun onActionEditingStateChanged(isEditingAction: Boolean) {
        if (!isEditingAction) {
            Log.e(TAG, "Closing PuzzleSolverDialog because there is no action edited")
            finish()
        }
    }
}

private const val TAG = "PuzzleSolverDialog"
