package com.clicker.smart.action.puzzle.vision

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Google Gemini 3.1 Vision API integration
 * Analyzes puzzle images and provides solving strategies
 */
class GeminiVisionAnalyzer(
    private val apiKey: String,
    private val timeoutMs: Long = 10000L
) {
    
    companion object {
        private const val TAG = "GeminiVisionAnalyzer"
        private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/"
        private const val MODEL_ID = "gemini-2.0-flash"
        private const val MAX_IMAGE_SIZE = 4 * 1024 * 1024 // 4MB
    }
    
    private val geminiService: GeminiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiService::class.java)
    }
    
    /**
     * Analyze puzzle image and get solving instructions
     */
    suspend fun analyzePuzzle(
        puzzleImage: Bitmap,
        puzzleType: String = "unknown"
    ): PuzzleAnalysisResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val startTime = System.currentTimeMillis()
            
            // Compress and encode image
            val base64Image = bitmapToBase64(puzzleImage)
            if (base64Image.isEmpty()) {
                return@withContext PuzzleAnalysisResult(
                    success = false,
                    message = "Failed to encode image",
                    processingTime = System.currentTimeMillis() - startTime
                )
            }
            
            // Build analysis prompt
            val prompt = buildAnalysisPrompt(puzzleType)
            
            // Create request
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(
                                inline_data = InlineData(
                                    mime_type = "image/jpeg",
                                    data = base64Image
                                )
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.1f,
                    max_output_tokens = 500
                )
            )
            
            // Send to Gemini API
            val response = geminiService.generateContent(
                model = MODEL_ID,
                key = apiKey,
                request = request
            )
            
            if (!response.isSuccessful || response.body()?.candidates?.isEmpty() == true) {
                Log.e(TAG, "API Error: ${response.code()} - ${response.message()}")
                return@withContext PuzzleAnalysisResult(
                    success = false,
                    message = "API returned error: ${response.code()}",
                    processingTime = System.currentTimeMillis() - startTime
                )
            }
            
            val candidate = response.body()?.candidates?.firstOrNull()
            val analysisText = candidate?.content?.parts?.firstOrNull()?.text ?: ""
            
            Log.d(TAG, "Gemini Analysis:\n$analysisText")
            
            // Parse response to extract solving strategy
            val solution = parseSolutionFromResponse(analysisText, puzzleType)
            
            PuzzleAnalysisResult(
                success = true,
                analysisText = analysisText,
                solutionStrategy = solution,
                confidence = extractConfidence(analysisText),
                processingTime = System.currentTimeMillis() - startTime,
                message = "Analysis successful"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing puzzle", e)
            PuzzleAnalysisResult(
                success = false,
                message = "Analysis error: ${e.localizedMessage}",
                processingTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Get specific solving instructions for slide puzzles
     */
    suspend fun analyzeSlidePuzzle(
        puzzleImage: Bitmap,
        sliderStartX: Int,
        sliderEndX: Int
    ): SlidePuzzleSolution = withContext(Dispatchers.IO) {
        return@withContext try {
            val analysis = analyzePuzzle(puzzleImage, "slide")
            
            if (!analysis.success) {
                return@withContext SlidePuzzleSolution(
                    success = false,
                    message = analysis.message
                )
            }
            
            val solution = analysis.solutionStrategy
            
            // Extract offset percentage from solution text
            val offsetPercentage = when {
                solution.contains("50%", ignoreCase = true) -> 0.5f
                solution.contains("70%", ignoreCase = true) -> 0.7f
                solution.contains("75%", ignoreCase = true) -> 0.75f
                solution.contains("80%", ignoreCase = true) -> 0.8f
                solution.contains("60%", ignoreCase = true) -> 0.6f
                else -> {
                    // Try to parse percentage
                    val regex = Regex("(\\d+(?:\\.\\d+)?)\\s*%")
                    val match = regex.find(solution)
                    (match?.groupValues?.get(1)?.toFloatOrNull() ?: 50f) / 100f
                }
            }
            
            val targetX = sliderStartX + 
                         ((sliderEndX - sliderStartX) * offsetPercentage).toInt()
            
            SlidePuzzleSolution(
                success = true,
                targetX = targetX,
                targetPercentage = offsetPercentage,
                confidence = analysis.confidence,
                reasoning = solution,
                message = "Slide solution calculated"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing slide puzzle", e)
            SlidePuzzleSolution(
                success = false,
                message = "Slide analysis error: ${e.message}"
            )
        }
    }
    
    // Helper functions
    private fun bitmapToBase64(bitmap: Bitmap): String {
        return try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val imageBytes = stream.toByteArray()
            
            if (imageBytes.size > MAX_IMAGE_SIZE) {
                val scaleFactor = kotlin.math.sqrt(
                    MAX_IMAGE_SIZE.toFloat() / imageBytes.size
                ).toInt().coerceAtLeast(1)
                val scaledBitmap = Bitmap.createScaledBitmap(
                    bitmap, 
                    bitmap.width / scaleFactor, 
                    bitmap.height / scaleFactor, 
                    true
                )
                stream.reset()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            } else {
                Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding bitmap", e)
            ""
        }
    }
    
    private fun buildAnalysisPrompt(puzzleType: String): String {
        return when (puzzleType.lowercase()) {
            "slide" -> """
                Analyze this Binance security puzzle image. This is a SLIDE PUZZLE where a 
                yellow/golden puzzle piece must be dragged along a slider to match a slot.
                
                What percentage (0-100%) of the slider length should the piece be moved?
                Look for the TARGET LOCATION and compare it to the CURRENT position.
                
                Respond with the percentage and brief explanation.
                Example: "Move 65% across the slider. The target slot is at the 65% mark."
            """.trimIndent()
            "lock" -> """
                Analyze this Binance security puzzle image. This is a LOCK PUZZLE where a 
                puzzle piece must be rotated/positioned to match a lock shape.
                
                Determine the rotation angle needed (0-360 degrees) to align the piece.
                
                Respond with the angle and explanation.
            """.trimIndent()
            else -> """
                Analyze this Binance security puzzle image carefully.
                Determine:
                1. Puzzle type (slide, lock, or match)
                2. What needs to be moved/arranged
                3. The target configuration
                4. Step-by-step solving instructions
                Be detailed and confident.
            """.trimIndent()
        }
    }
    
    private fun parseSolutionFromResponse(response: String, puzzleType: String): String {
        return response
            .lineSequence()
            .filter { it.isNotBlank() }
            .take(5)
            .joinToString(" ")
            .take(200)
    }
    
    private fun extractConfidence(response: String): Float {
        return when {
            response.contains("confident", ignoreCase = true) -> 0.9f
            response.contains("likely", ignoreCase = true) -> 0.8f
            response.contains("should", ignoreCase = true) -> 0.75f
            response.contains("may", ignoreCase = true) -> 0.6f
            else -> 0.7f
        }
    }
}

// Data classes for Gemini API communication
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Content(val parts: List<Part>)

data class Part(
    val text: String? = null,
    val inline_data: InlineData? = null
)

data class InlineData(
    val mime_type: String,
    val data: String
)

data class GenerationConfig(
    val temperature: Float = 1f,
    val max_output_tokens: Int = 1024
)

data class PuzzleAnalysisResult(
    val success: Boolean = false,
    val analysisText: String = "",
    val solutionStrategy: String = "",
    val confidence: Float = 0f,
    val processingTime: Long = 0L,
    val message: String = ""
)

data class SlidePuzzleSolution(
    val success: Boolean = false,
    val targetX: Int = 0,
    val targetPercentage: Float = 0f,
    val confidence: Float = 0f,
    val reasoning: String = "",
    val message: String = ""
)