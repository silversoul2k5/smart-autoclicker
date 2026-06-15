package com.clicker.smart.action.puzzle.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Controls gesture automation for puzzle solving
 * Uses AccessibilityService for reliable touch simulation
 */
class SliderController(
    private val accessibilityService: AccessibilityService
) {
    
    companion object {
        private const val TAG = "SliderController"
    }
    
    /**
     * Execute slide puzzle solution
     */
    suspend fun executeSlide(
        startX: Int,
        startY: Int,
        targetX: Int,
        targetY: Int = startY,
        durationMs: Long = 500L,
        animated: Boolean = true,
        steps: Int = 20
    ): Boolean = withContext(Dispatchers.Main) {
        return@withContext try {
            Log.d(TAG, "Executing slide: ($startX, $startY) → ($targetX, $targetY)")
            
            val path = Path()
            path.moveTo(startX.toFloat(), startY.toFloat())
            
            if (animated && steps > 1) {
                // Create smooth path with interpolation
                val stepX = (targetX - startX) / (steps - 1)
                val stepY = (targetY - startY) / (steps - 1)
                
                for (i in 1 until steps) {
                    val x = startX + (stepX * i)
                    val y = startY + (stepY * i)
                    path.lineTo(x.toFloat(), y.toFloat())
                }
            }
            
            path.lineTo(targetX.toFloat(), targetY.toFloat())
            
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(
                    path,
                    0L,  // Start immediately
                    durationMs
                ))
                .build()
            
            var result = false
            val callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    result = true
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    result = false
                }
            }
            
            accessibilityService.dispatchGesture(gesture, callback, null)
            
            // Wait for gesture to complete
            kotlinx.coroutines.delay(durationMs + 100)
            
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error executing slide", e)
            false
        }
    }
    
    /**
     * Execute lock puzzle rotation
     */
    suspend fun executeRotation(
        centerX: Int,
        centerY: Int,
        radius: Int,
        startAngle: Float,
        targetAngle: Float,
        durationMs: Long = 500L
    ): Boolean = withContext(Dispatchers.Main) {
        return@withContext try {
            val path = Path()
            val angleStep = (targetAngle - startAngle) / 20
            val startX = centerX + radius * kotlin.math.cos(Math.toRadians(startAngle.toDouble())).toFloat()
            val startY = centerY + radius * kotlin.math.sin(Math.toRadians(startAngle.toDouble())).toFloat()
            
            path.moveTo(startX, startY)
            
            for (i in 1..20) {
                val angle = startAngle + (angleStep * i)
                val x = centerX + radius * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
                val y = centerY + radius * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                path.lineTo(x, y)
            }
            
            val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0L, durationMs)).build()
            accessibilityService.dispatchGesture(gesture, null, null)
            kotlinx.coroutines.delay(durationMs + 100)
            true
        } catch (e: Exception) {
            false
        }
    }
}