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
package com.buzbuz.smartautoclicker.core.processing.puzzle

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.Base64

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

class GeminiPuzzleSolverService(
    private val apiKey: String
) {
    companion object {
        private const val TAG = "GeminiPuzzleSolver"
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    private var generativeModel: GenerativeModel? = null
    
    init {
        try {
            generativeModel = GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = apiKey
            )
            Log.d(TAG, "Gemini model initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Gemini model", e)
            throw e
        }
    }

    /**
     * Main solve function - analyzes puzzle image using Gemini
     */
    suspend fun solvePuzzle(
        puzzleImage: Bitmap,
        puzzleType: String = "binance_security",
        statusCallback: (String) -> Unit = {}
    ): List<PuzzleAction> = withContext(Dispatchers.IO) {
        try {
            statusCallback("Encoding puzzle image...")
            val compressedBitmap = resizeBitmap(puzzleImage, 512, 512)
            val base64Image = encodeImageToBase64(compressedBitmap)
            Log.d(TAG, "Image encoded, size: ${base64Image.length} chars")

            val prompt = buildPromptForPuzzleType(puzzleType)
            statusCallback("Sending puzzle to Gemini AI...")

            val response = queryGemini(base64Image, prompt)
            Log.d(TAG, "Gemini response: $response")

            if (response.isEmpty()) {
                statusCallback("ERROR: No response from Gemini")
                return@withContext emptyList()
            }

            statusCallback("Parsing AI response...")
            val actions = parseGeminiResponse(response, puzzleType)
            
            if (actions.isEmpty()) {
                statusCallback("ERROR: Could not parse solution from AI response")
                Log.w(TAG, "No valid actions parsed from response: $response")
            }

            return@withContext actions

        } catch (e: Exception) {
            Log.e(TAG, "Error in solvePuzzle", e)
            statusCallback("ERROR: ${e.message}")
            emptyList()
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) return bitmap
        
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        var width = maxWidth
        var height = (maxWidth / aspectRatio).toInt()
        
        if (height > maxHeight) {
            height = maxHeight
            width = (maxHeight * aspectRatio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun buildPromptForPuzzleType(puzzleType: String): String {
        return when (puzzleType.lowercase()) {
            "binance_security", "security_verification" -> {
                """
Analyze this Binance Security Verification puzzle image.

CRITICAL ANALYSIS POINTS:
1. Identify the yellow puzzle piece position
2. Identify the gray slot where it should fit
3. Calculate the EXACT displacement needed
4. Determine primary direction: RIGHT, LEFT, UP, or DOWN

PROVIDE RESPONSE IN THIS EXACT FORMAT:
```
DIRECTION: RIGHT
DISTANCE: 250
DURATION: 400
CONFIDENCE: 95
```

GUIDELINES:
- Distance in pixels (typical: 150-300px)
- Duration in milliseconds (typical: 300-500ms)
- For ambiguous puzzles, provide your best estimate
- Include confidence percentage (0-100)
                """.trimIndent()
            }
            "hcaptcha", "hcaptcha_tile" -> {
                """
Analyze this hCaptcha challenge image.

TASK:
1. Read the target label/description (usually at top)
2. Identify which tiles match that description
3. Provide exact click coordinates for matching tiles

RESPONSE FORMAT:
```
TARGET_LABEL: traffic_light
MATCHING_TILES: (100,200),(300,200),(500,200)
CONFIDENCE: 88
```

Be precise with coordinates - they determine click accuracy.
                """.trimIndent()
            }
            else -> {
                """
Analyze this puzzle image and describe:
1. What type of puzzle is it?
2. What action is required to solve it?
3. Provide specific coordinates, directions, or steps needed.

Be precise and technical. Format your response clearly.
                """.trimIndent()
            }
        }
    }

    private suspend fun queryGemini(base64Image: String, prompt: String): String {
        val model = generativeModel ?: throw Exception("Gemini model not initialized")
        
        val response = model.generateContent(
            content {
                // The library might expect the bytes directly if using the Android SDK helper
                image(decodeBase64ToBitmap(base64Image))
                text(prompt)
            }
        )

        return response.text ?: ""
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun parseGeminiResponse(
        response: String,
        puzzleType: String
    ): List<PuzzleAction> {
        val actions = mutableListOf<PuzzleAction>()

        return when (puzzleType.lowercase()) {
            "binance_security", "security_verification" -> {
                val directionMatch = Regex("DIRECTION:\\s*([A-Z_]+)", RegexOption.IGNORE_CASE)
                    .find(response)?.groupValues?.getOrNull(1)?.trim() ?: return emptyList()
                
                val distanceMatch = Regex("DISTANCE:\\s*(\\d+)").find(response)
                    ?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 200
                
                val durationMatch = Regex("DURATION:\\s*(\\d+)").find(response)
                    ?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 400

                if (directionMatch.isNotEmpty()) {
                    actions.add(
                        PuzzleAction.Swipe(
                            direction = directionMatch.uppercase(),
                            distance = distanceMatch,
                            duration = durationMatch
                        )
                    )
                }
                actions
            }
            "hcaptcha", "hcaptcha_tile" -> {
                val positionsMatch = Regex("MATCHING_TILES:\\s*(.+?)(?=CONFIDENCE:|$)")
                    .find(response)?.groupValues?.getOrNull(1)?.trim()
                
                if (!positionsMatch.isNullOrEmpty()) {
                    val positions = Regex("\\((\\d+),(\\d+)\\)")
                        .findAll(positionsMatch)
                        .map { match ->
                            val x = match.groupValues[1].toInt()
                            val y = match.groupValues[2].toInt()
                            Pair(x, y)
                        }
                        .toList()

                    positions.forEach { (x, y) ->
                        actions.add(PuzzleAction.Click(x, y))
                    }
                }
                actions
            }
            else -> actions
        }
    }

    sealed class PuzzleAction {
        data class Swipe(
            val direction: String,
            val distance: Int,
            val duration: Int
        ) : PuzzleAction() {
            override fun toString() = "SWIPE($direction, ${distance}px, ${duration}ms)"
        }

        data class Click(
            val x: Int,
            val y: Int
        ) : PuzzleAction() {
            override fun toString() = "CLICK($x, $y)"
        }
    }
}
