package com.clicker.smart.action.puzzle.detector

import android.graphics.Bitmap
import android.graphics.Rect as AndroidRect
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * OpenCV-based puzzle detection and analysis
 * Handles image processing before API submission
 */
class PuzzleDetector {
    
    companion object {
        private const val TAG = "PuzzleDetector"
        
        // Yellow color range (Binance branding)
        private val LOWER_YELLOW = Scalar(15.0, 100.0, 100.0)   // HSV lower
        private val UPPER_YELLOW = Scalar(35.0, 255.0, 255.0)   // HSV upper
        
        // Color thresholds
        private const val MIN_CONTOUR_AREA = 500.0
        private const val MAX_CONTOUR_AREA = 1000000.0
        private const val CONTOUR_EPSILON_FACTOR = 0.02
    }
    
    private var lastProcessedFrame: Mat? = null
    private var lastDetectionTime: Long = 0L
    
    /**
     * Detect puzzle area and extract relevant image region
     */
    suspend fun detectPuzzleArea(
        bitmap: Bitmap,
        bounds: AndroidRect? = null,
        threshold: Float = 0.7f
    ): PuzzleDetectionResult = withContext(Dispatchers.Default) {
        return@withContext try {
            val startTime = System.currentTimeMillis()
            
            // Convert bitmap to Mat
            val mat = Mat()
            org.opencv.android.Utils.bitmapToMat(bitmap, mat)
            
            // If bounds provided, crop to region
            val cropMat = if (bounds != null) {
                val roi = Rect(bounds.left, bounds.top, 
                               bounds.width(), bounds.height())
                Mat(mat, roi)
            } else {
                mat
            }
            
            // Convert to HSV for better color detection
            val hsvMat = Mat()
            Imgproc.cvtColor(cropMat, hsvMat, Imgproc.COLOR_RGB2HSV)
            
            // Detect yellow puzzle pieces
            val mask = Mat()
            Core.inRangeS(hsvMat, LOWER_YELLOW, UPPER_YELLOW, mask)
            
            // Apply morphological operations
            val kernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_ELLIPSE, Size(5.0, 5.0)
            )
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel)
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel)
            
            // Find contours
            val contours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(mask, contours, Mat(), 
                                Imgproc.RETR_EXTERNAL, 
                                Imgproc.CHAIN_APPROX_SIMPLE)
            
            // Analyze contours to find puzzle piece
            val validContours = contours.filter { contour ->
                val area = Imgproc.contourArea(contour)
                area > MIN_CONTOUR_AREA && area < MAX_CONTOUR_AREA
            }
            
            if (validContours.isEmpty()) {
                return@withContext PuzzleDetectionResult(
                    success = false,
                    message = "No puzzle piece detected",
                    detectionTime = System.currentTimeMillis() - startTime
                )
            }
            
            // Get largest contour (puzzle piece)
            val largestContour = validContours.maxByOrNull { 
                Imgproc.contourArea(it) 
            } ?: return@withContext PuzzleDetectionResult(
                success = false,
                message = "Failed to extract puzzle piece",
                detectionTime = System.currentTimeMillis() - startTime
            )
            
            // Get bounding rectangle
            val boundingRect = Imgproc.boundingRect(largestContour)
            val confidence = validContours.size / 5f  // Heuristic
            
            // Extract puzzle region
            val puzzleRegion = Mat(cropMat, boundingRect)
            val puzzleBitmap = Bitmap.createBitmap(
                puzzleRegion.cols(), puzzleRegion.rows(), 
                Bitmap.Config.ARGB_8888
            )
            org.opencv.android.Utils.matToBitmap(puzzleRegion, puzzleBitmap)
            
            // Detect puzzle type using edge analysis
            val edges = Mat()
            Imgproc.Canny(mask, edges, 100.0, 200.0)
            val edgeCount = Core.countNonZero(edges)
            
            val puzzleType = when {
                edgeCount > 1000 -> PuzzleType.LOCK      // Complex edges
                edgeCount > 500  -> PuzzleType.MATCH     // Moderate edges
                else             -> PuzzleType.SLIDE     // Simple slide
            }
            
            lastProcessedFrame = cropMat.clone()
            lastDetectionTime = System.currentTimeMillis() - startTime
            
            Log.d(TAG, "Puzzle detected: $puzzleType (confidence: $confidence, " +
                    "region: ${boundingRect.width}x${boundingRect.height})")
            
            PuzzleDetectionResult(
                success = true,
                puzzleType = puzzleType,
                puzzleBitmap = puzzleBitmap,
                boundingRect = boundingRect,
                confidence = confidence.coerceIn(0.0f, 1.0f),
                edgeCount = edgeCount,
                detectionTime = lastDetectionTime,
                message = "Puzzle detected successfully"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in puzzle detection", e)
            PuzzleDetectionResult(
                success = false,
                message = "Detection error: ${e.message}",
                detectionTime = System.currentTimeMillis() - lastDetectionTime
            )
        }
    }
    
    /**
     * Detect slider control area for slide puzzles
     */
    suspend fun detectSliderArea(
        bitmap: Bitmap
    ): SliderDetectionResult = withContext(Dispatchers.Default) {
        return@withContext try {
            val mat = Mat()
            org.opencv.android.Utils.bitmapToMat(bitmap, mat)
            
            // Bottom portion likely contains slider
            val bottomMat = Mat(mat, Rect(0, mat.rows() / 2, mat.cols(), mat.rows() / 2))
            
            // Detect horizontal structures (slider track)
            val edges = Mat()
            Imgproc.Canny(bottomMat, edges, 50.0, 150.0)
            
            // Horizontal line detection
            val lines = Mat()
            Imgproc.HoughLinesP(edges, lines, 1.0, Math.PI / 180, 50, 50.0, 10.0)
            
            if (lines.empty()) {
                return@withContext SliderDetectionResult(
                    success = false,
                    message = "No slider detected"
                )
            }
            
            // Find bottom-most horizontal line (slider track)
            var maxY = 0
            var sliderX1 = 0
            var sliderX2 = 0
            
            for (i in 0 until lines.rows()) {
                val line = lines.get(i, 0)
                val y1 = line[1].toInt()
                val y2 = line[3].toInt()
                
                if (maxOf(y1, y2) > maxY) {
                    maxY = maxOf(y1, y2)
                    sliderX1 = line[0].toInt()
                    sliderX2 = line[2].toInt()
                }
            }
            
            SliderDetectionResult(
                success = true,
                sliderStartX = sliderX1,
                sliderEndX = sliderX2,
                sliderY = maxY + mat.rows() / 2,
                message = "Slider detected"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting slider", e)
            SliderDetectionResult(
                success = false,
                message = "Slider detection error: ${e.message}"
            )
        }
    }
    
    fun release() {
        lastProcessedFrame?.release()
        lastProcessedFrame = null
    }
}

enum class PuzzleType { SLIDE, LOCK, MATCH }

data class PuzzleDetectionResult(
    val success: Boolean = false,
    val puzzleType: PuzzleType? = null,
    val puzzleBitmap: Bitmap? = null,
    val boundingRect: org.opencv.core.Rect? = null,
    val confidence: Float = 0f,
    val edgeCount: Int = 0,
    val detectionTime: Long = 0L,
    val message: String = "",
    val errorCode: Int = 0
)

data class SliderDetectionResult(
    val success: Boolean = false,
    val sliderStartX: Int = 0,
    val sliderEndX: Int = 0,
    val sliderY: Int = 0,
    val message: String = ""
)