package com.clicker.smart.action.puzzle

import android.graphics.Rect
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Action model for puzzle solving operations
 * Integrates with existing Smart AutoClicker action system
 */
@Entity(tableName = "puzzle_solver_actions")
data class PuzzleSolverAction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "Solve Puzzle",
    val description: String = "",
    
    // Detection settings
    val detectionTimeoutMs: Long = 5000L,  // Max wait time
    val detectionThreshold: Float = 0.7f,   // Confidence threshold
    val puzzleAreaBounds: String = "",      // JSON serialized Rect
    
    // Gemini API settings
    val useGeminiVision: Boolean = true,
    val geminiApiKey: String = "",
    val geminiTimeoutMs: Long = 3000L,
    
    // Gesture settings
    val sliderStartX: Int = 100,
    val sliderStartY: Int = 0,             // Auto-detect if 0
    val sliderEndX: Int = 500,
    val sliderDuration: Long = 500L,       // Milliseconds
    val animatedSwipe: Boolean = true,
    val swipeSteps: Int = 20,              // Smoothness
    
    // Behavior settings
    val retryOnFailure: Boolean = true,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000L,
    
    // Validation
    val validateAfterSolve: Boolean = true,
    val successIndicatorColor: String = "#00FF00",  // Green checkmark
    val successTimeoutMs: Long = 2000L,
    
    // Logging
    val enableLogging: Boolean = true,
    val captureSolution: Boolean = false,  // Save solution images
    val solutionDirectory: String = "",
    
    // Execution control
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    
    fun getBounds(): Rect? = try {
        if (puzzleAreaBounds.isEmpty()) null
        else {
            val parts = puzzleAreaBounds.split(",")
            Rect(parts[0].toInt(), parts[1].toInt(), 
                 parts[2].toInt(), parts[3].toInt())
        }
    } catch (e: Exception) { null }
    
    fun setBounds(rect: Rect) {
        puzzleAreaBounds = "${rect.left},${rect.top},${rect.right},${rect.bottom}"
    }
    
    companion object {
        fun default() = PuzzleSolverAction(
            name = "Solve Binance Puzzle",
            description = "Automatically solve Binance security verification puzzles",
            useGeminiVision = true,
            retryOnFailure = true
        )
    }
}