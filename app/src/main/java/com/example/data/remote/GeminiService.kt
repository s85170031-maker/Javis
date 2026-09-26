package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateJarvisResponse(
        userPrompt: String,
        recentHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("GEMINI_API_KEY is not configured in AI Studio Secrets.")
            )
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // Append recent conversation context
            for ((role, text) in recentHistory) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role.equals("USER", ignoreCase = true)) "user" else "model")
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", text)
                partsArray.put(partObj)
                turnObj.put("parts", partsArray)
                contentsArray.put(turnObj)
            }

            // Append current user prompt
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            val currentPart = JSONObject()
            currentPart.put("text", userPrompt)
            currentParts.put(currentPart)
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            // System Instruction for JARVIS persona
            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            val sysPart = JSONObject()
            sysPart.put(
                "text",
                "You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the refined AI assistant created by Tony Stark. " +
                        "Address the user as 'Sir' or 'Madam'. Speak with British politeness, understated wit, and crisp intelligence. " +
                        "You are fully bilingual in English and Hindi (including Hinglish). If the user addresses you in Hindi or Hinglish, answer politely in Hindi or Hinglish; otherwise answer in English. " +
                        "Keep your responses concise and natural for voice synthesis (1 to 3 punchy sentences), unless explicitly instructed to give a detailed technical report."
            )
            sysParts.put(sysPart)
            systemInstruction.put("parts", sysParts)

            val payload = JSONObject()
            payload.put("contents", contentsArray)
            payload.put("systemInstruction", systemInstruction)

            val configObj = JSONObject()
            configObj.put("temperature", 0.7)
            configObj.put("maxOutputTokens", 500)
            payload.put("generationConfig", configObj)

            val requestBody = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Jarvis Link Error (${response.code}): $bodyString")
                )
            }

            val responseJson = JSONObject(bodyString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    if (text.isNotBlank()) {
                        return@withContext Result.success(text.trim())
                    }
                }
            }

            Result.failure(Exception("Jarvis received empty response from core link."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
