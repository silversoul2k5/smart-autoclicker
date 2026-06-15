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
package com.buzbuz.smartautoclicker.core.domain.model.action

import com.buzbuz.smartautoclicker.core.base.interfaces.Identifiable
import com.buzbuz.smartautoclicker.core.base.identifier.Identifier
import com.buzbuz.smartautoclicker.core.base.interfaces.Completable
import com.buzbuz.smartautoclicker.core.base.interfaces.Prioritizable

/** Base for for all possible actions for an Event. */
sealed class Action : Identifiable, Completable, Prioritizable {

    /** The identifier of the event for this action. */
    abstract val eventId: Identifier
    /** The name of the action. */
    abstract val name: String?

    /** @return true if this action is complete and can be transformed into its entity. */
    override fun isComplete(): Boolean = name != null

    abstract fun hashCodeNoIds(): Int

    /** @return creates a deep copy of this action. */
    abstract fun deepCopy(): Action

    fun copyBase(
        id: Identifier = this.id,
        eventId: Identifier = this.eventId,
        name: String? = this.name,
        priority: Int = this.priority,
    ): Action =
        when (this) {
            is Click -> copy(id = id, eventId = eventId, name = name, priority = priority)
            is ChangeCounter -> copy(id = id, eventId = eventId, name = name, priority = priority)
            is Intent -> copy(id = id, eventId = eventId, name = name, priority = priority)
            is Pause -> copy(id = id, eventId = eventId, name = name, priority = priority)
            is Swipe -> copy(id = id, eventId = eventId, name = name, priority = priority)
            is ToggleEvent -> copy(id = id, eventId = eventId, name = name, priority = priority)
            is Notification -> copy(id = id, eventId = eventId, name = name, priority = priority)
            is SystemAction -> copy(id = id, eventId = eventId, name = name, priority = priority)
            is SetText -> copy(id = id, eventId = eventId, name = name, priority = priority)
            is PuzzleSolver -> copy(id = id, eventId = eventId, name = name, priority = priority)
        }
}

/** Action that automatically solves puzzles. */
data class PuzzleSolver(
    override val id: Identifier,
    override val eventId: Identifier,
    override val name: String?,
    override var priority: Int,

    val puzzleType: String = "binance_security",
    val detectionTimeoutMs: Long = 5000L,
    val detectionThreshold: Float = 0.7f,
    val puzzleAreaBounds: String? = null,
    val useGeminiVision: Boolean = true,
    val geminiApiKey: String = "",
    val geminiTimeoutMs: Long = 3000L,
    val sliderStartX: Int = 100,
    val sliderStartY: Int = 0,
    val sliderEndX: Int = 500,
    val sliderDuration: Long = 500L,
    val animatedSwipe: Boolean = true,
    val swipeSteps: Int = 20,
    val retryOnFailure: Boolean = true,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000L,
    val validateAfterSolve: Boolean = true,
    val successIndicatorColor: String = "#00FF00",
    val successTimeoutMs: Long = 2000L,
) : Action() {

    override fun hashCodeNoIds(): Int =
        (name?.hashCode() ?: 0) + detectionTimeoutMs.hashCode() + detectionThreshold.hashCode() +
                (puzzleAreaBounds?.hashCode() ?: 0) + useGeminiVision.hashCode() + geminiApiKey.hashCode() +
                geminiTimeoutMs.hashCode() + sliderStartX.hashCode() + sliderStartY.hashCode() +
                sliderEndX.hashCode() + sliderDuration.hashCode() + animatedSwipe.hashCode() +
                swipeSteps.hashCode() + retryOnFailure.hashCode() + maxRetries.hashCode() +
                retryDelayMs.hashCode() + validateAfterSolve.hashCode() +
                successIndicatorColor.hashCode() + successTimeoutMs.hashCode()

    override fun deepCopy(): Action = copy()
}
