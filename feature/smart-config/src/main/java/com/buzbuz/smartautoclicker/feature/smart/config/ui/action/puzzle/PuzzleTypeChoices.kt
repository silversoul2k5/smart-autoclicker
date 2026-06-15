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
package com.buzbuz.smartautoclicker.feature.smart.config.ui.action.puzzle

import com.buzbuz.smartautoclicker.core.common.overlays.dialog.implementation.DialogChoice
import com.buzbuz.smartautoclicker.feature.smart.config.R

sealed class PuzzleTypeChoice(
    val type: String,
    title: Int,
    description: Int,
) : DialogChoice(
    title = title,
    description = description,
    iconId = null,
) {
    data object Binance : PuzzleTypeChoice(
        type = "binance_security",
        title = R.string.item_puzzle_solver_type_binance,
        description = R.string.item_puzzle_solver_type_binance_desc,
    )
    data object HCaptcha : PuzzleTypeChoice(
        type = "hcaptcha",
        title = R.string.item_puzzle_solver_type_hcaptcha,
        description = R.string.item_puzzle_solver_type_hcaptcha_desc,
    )
    data object Slider : PuzzleTypeChoice(
        type = "slider_puzzle",
        title = R.string.item_puzzle_solver_type_slider,
        description = R.string.item_puzzle_solver_type_slider_desc,
    )

    companion object {
        val allChoices = listOf(Binance, HCaptcha, Slider)

        fun fromType(type: String): PuzzleTypeChoice =
            allChoices.find { it.type == type } ?: Binance
    }
}
