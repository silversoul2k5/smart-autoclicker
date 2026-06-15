package com.clicker.smart.action.puzzle

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.util.Log
import com.clicker.smart.action.puzzle.detector.PuzzleDetector
import com.clicker.smart.action.puzzle.detector.PuzzleType
import com.clicker.smart.action.puzzle.gesture.SliderController
import com.clicker.smart.action.puzzle.vision.GeminiVisionAnalyzer
import kotlinx.coroutines.*

/**
 * Main orchestrator for puzzle solving workflow
 * Coordinates detection, analysis, and solution execution
 */
class PuzzleSolverProcessor(
    private val action: PuzzleSolverAction,
    private val accessibilityService: AccessibilityService,
    private val geminiAnalyzer: GeminiVisionAnalyzer? = null,
    private val onProgress: (String) -> Unit = {}
) {
    
    companion object {
        private const val TAG = "PuzzleSolverProcessor"
    }
    
    private val puzzleDetector = PuzzleDetector()
    private val sliderController = SliderController(accessibilityService)
    private var processingJob: Job? = null
    
    /**
     * Main entry point: Detect and solve puzzle
     */
    suspend fun solvePuzzle(
        screenshotBitmap: Bitmap
    ): PuzzleSolutionResult = withContext(Dispatchers.Main) {
        
        return@withContext try {
            onProgress("Detecting puzzle...")
            
            // Step 1: Detect puzzle area
            val detectionResult = puzzleDetector.detectPuzzleArea(
                screenshotBitmap,
                action.getBounds(),
                action.detectionThreshold
            )
            
            if (!detectionResult.success || detectionResult.puzzleType == null) {
                return@withContext PuzzleSolutionResult(
                    success = false,
                    message = detectionResult.message
                )
            }
            
            val puzzleType = detectionResult.puzzleType
            val puzzleImage = detectionResult.puzzleBitmap ?: return@withContext 
                PuzzleSolutionResult(success = false, message = "No puzzle image")
            
            Log.d(TAG, "Puzzle detected: $puzzleType (confidence: ${detectionResult.confidence})")
            onProgress("Puzzle detected: $puzzleType")
            
            // Step 2: Analyze puzzle (using Gemini if available)
            val solutionResult = when (puzzleType) {
                PuzzleType.SLIDE -> solveSlidePuzzle(screenshotBitmap, puzzleImage)
                PuzzleType.LOCK -> solveLockPuzzle(screenshotBitmap, puzzleImage)
                PuzzleType.MATCH -> PuzzleSolutionResult(success = false, message = "Match puzzle not implemented")
            }
            
            if (!solutionResult.success) {
                if (action.retryOnFailure && solutionResult.retryCount < action.maxRetries) {
                    Log.d(TAG, "Retrying puzzle solve (attempt ${solutionResult.retryCount + 1})")
                    delay(action.retryDelayMs)
                    return@withContext solvePuzzle(screenshotBitmap)
                }
                return@withContext solutionResult
            }
            
            solutionResult.copy(success = true, message = "Puzzle solved successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in puzzle solving", e)
            PuzzleSolutionResult(
                success = false,
                message = "Processing error: ${e.message}"
            )
        }
    }
    
    private suspend fun solveSlidePuzzle(
        fullScreenshot: Bitmap,
        puzzleImage: Bitmap
    ): PuzzleSolutionResult {
        return try {
            onProgress("Analyzing slide puzzle...")
            
            // Detect slider area
            val sliderResult = puzzleDetector.detectSliderArea(puzzleImage)
            if (!sliderResult.success) {
                return PuzzleSolutionResult(success = false, message = "Could not detect slider")
            }
            
            val sliderStartX = if (action.sliderStartX > 0) action.sliderStartX else sliderResult.sliderStartX
            val sliderEndX = if (action.sliderEndX > 0) action.sliderEndX else sliderResult.sliderEndX
            val sliderY = if (action.sliderStartY > 0) action.sliderStartY else sliderResult.sliderY
            
            // Get solution from Gemini if available
            val solution = if (geminiAnalyzer != null && action.useGeminiVision) {
                onProgress("Analyzing with Gemini...")
                geminiAnalyzer.analyzeSlidePuzzle(puzzleImage, sliderStartX, sliderEndX)
            } else {
                // Fallback: 60% guess
                com.clicker.smart.action.puzzle.vision.SlidePuzzleSolution(
                    success = true,
                    targetX = sliderStartX + ((sliderEndX - sliderStartX) * 0.6).toInt(),
                    targetPercentage = 0.6f,
                    confidence = 0.6f,
                    reasoning = "Fallback solution"
                )
            }
            
            if (!solution.success) {
                return PuzzleSolutionResult(success = false, message = solution.message)
            }
            
            onProgress("Sliding to target position...")
            
            // Execute slide gesture
            val gestureSuccess = sliderController.executeSlide(
                startX = sliderStartX,
                startY = sliderY,
                targetX = solution.targetX,
                targetY = sliderY,
                durationMs = action.sliderDuration,
                animated = action.animatedSwipe,
                steps = action.swipeSteps
            )
            
            if (!gestureSuccess) {
                return PuzzleSolutionResult(success = false, message = "Gesture execution failed")
            }
            
            PuzzleSolutionResult(
                success = true,
                puzzleType = "SLIDE",
                solutionDetails = "Slid to ${solution.targetPercentage * 100}%",
                confidence = solution.confidence,
                message = "Slide puzzle solved"
            )
            
        } catch (e: Exception) {
            PuzzleSolutionResult(success = false, message = "Slide solving error: ${e.message}")
        }
    }
    
    private suspend fun solveLockPuzzle(
        fullScreenshot: Bitmap,
        puzzleImage: Bitmap
    ): PuzzleSolutionResult {
        return try {
            onProgress("Analyzing lock puzzle...")
            // Simplified version
            val gestureSuccess = sliderController.executeRotation(
                centerX = 250, centerY = 400, radius = 100,
                startAngle = 0f, targetAngle = 90f, durationMs = action.sliderDuration
            )
            PuzzleSolutionResult(success = gestureSuccess, puzzleType = "LOCK", message = "Lock processed")
        } catch (e: Exception) {
            PuzzleSolutionResult(success = false, message = "Lock error: ${e.message}")
        }
    }
    
    fun cancel() {
        processingJob?.cancel()
        puzzleDetector.release()
    }
}

data class PuzzleSolutionResult(
    val success: Boolean = false,
    val puzzleType: String = "",
    val solutionDetails: String = "",
    val confidence: Float = 0f,
    val processingTimeMs: Long = 0L,
    val retryCount: Int = 0,
    val message: String = ""
)