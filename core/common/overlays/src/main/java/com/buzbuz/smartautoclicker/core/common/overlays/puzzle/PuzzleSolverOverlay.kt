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
package com.buzbuz.smartautoclicker.core.common.overlays.puzzle

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class PuzzleSolverOverlay(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: LinearLayout? = null
    private var statusText: TextView? = null
    private var actionsText: TextView? = null
    private val actionLog = mutableListOf<String>()

    fun show() {
        if (overlayView != null) return

        overlayView = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                450,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.black))
            alpha = 0.85f
            setPadding(24, 24, 24, 24)

            // Status text
            statusText = TextView(context).apply {
                text = "🔄 Initializing..."
                setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_light))
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
                isSingleLine = false
            }
            addView(statusText)

            // Actions log
            actionsText = TextView(context).apply {
                text = "Waiting for action..."
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                isSingleLine = false
                maxLines = 6
            }
            addView(actionsText)
        }

        val params = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            gravity = Gravity.TOP or Gravity.END
            width = 450
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 20
            y = 150
        }

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            android.util.Log.e("PuzzleOverlay", "Failed to add overlay view", e)
        }
    }

    fun hide() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
            overlayView = null
        }
        actionLog.clear()
    }

    fun updateStatus(message: String) {
        statusText?.text = message
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
            .format(java.util.Date())
        addActionLog("[$timestamp] $message")
    }

    fun addActionLog(action: String) {
        actionLog.add(action)
        if (actionLog.size > 5) {
            actionLog.removeAt(0)
        }
        val logText = actionLog.joinToString("\n")
        actionsText?.text = logText
    }

    fun setSuccess() {
        statusText?.setTextColor(
            ContextCompat.getColor(context, android.R.color.holo_green_light)
        )
        statusText?.text = "✓ PUZZLE SOLVED"
    }

    fun setError(message: String) {
        statusText?.setTextColor(
            ContextCompat.getColor(context, android.R.color.holo_red_light)
        )
        statusText?.text = "✗ ERROR\n$message"
    }
}
