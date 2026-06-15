package com.clicker.smart.action.puzzle.vision

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path

/**
 * Retrofit service interface for Google Gemini API
 */
interface GeminiService {
    
    @POST("models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") key: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null
)

data class Candidate(
    val content: CandidateContent? = null,
    val finishReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

data class CandidateContent(
    val parts: List<ResponsePart>? = null,
    val role: String? = null
)

data class ResponsePart(val text: String? = null)
data class SafetyRating(val category: String? = null, val probability: String? = null)
data class PromptFeedback(val blockReason: String? = null, val safetyRatings: List<SafetyRating>? = null)