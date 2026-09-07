package com.example.audio

import android.util.Base64
import android.util.Log
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

class GeminiTtsService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Synthesizes natural Indonesian speech using Gemini AI Speech Generation
     */
    suspend fun generateSpeechAudio(
        text: String,
        geminiVoiceName: String,
        stylePrompt: String
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Kunci Gemini API belum diatur. Silakan atur di Secrets Panel AI Studio, atau gunakan Mode Suara Perangkat.")
            )
        }

        val prompt = "Anda adalah pengisi suara (voice actor) profesional penutur asli Bahasa Indonesia (native speaker). " +
                "Karakter suara Anda: $stylePrompt. " +
                "Tolong ucapkan kalimat Bahasa Indonesia berikut dengan artikulasi jernih, intonasi alami, ritme bicara luwes, dan penghayatan yang hidup:\n\n\"$text\""

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val partsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                }
                put(JSONObject().apply {
                    put("parts", partsArray)
                })
            }
            put("contents", contentsArray)

            val generationConfig = JSONObject().apply {
                val modalities = JSONArray().apply {
                    put("AUDIO")
                }
                put("responseModalities", modalities)

                val speechConfig = JSONObject().apply {
                    val voiceConfig = JSONObject().apply {
                        val prebuiltVoiceConfig = JSONObject().apply {
                            put("voiceName", geminiVoiceName)
                        }
                        put("prebuiltVoiceConfig", prebuiltVoiceConfig)
                    }
                    put("voiceConfig", voiceConfig)
                }
                put("speechConfig", speechConfig)
            }
            put("generationConfig", generationConfig)
        }

        // Try primary TTS model first, then fallback
        val models = listOf("gemini-2.5-flash-preview-tts", "gemini-2.5-flash-native-audio-preview-12-2025")
        var lastError: Exception? = null

        for (model in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errObj = JSONObject(responseBody).optJSONObject("error")
                        errObj?.optString("message") ?: "HTTP ${response.code}: $responseBody"
                    } catch (_: Exception) {
                        "HTTP ${response.code}"
                    }
                    lastError = Exception(errorMsg)
                    Log.w("GeminiTtsService", "Model $model failed: $errorMsg, trying fallback...")
                    continue
                }

                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    lastError = Exception("Respons kosong dari Gemini")
                    continue
                }

                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts == null || parts.length() == 0) {
                    lastError = Exception("Kandidat suara tidak ditemukan")
                    continue
                }

                var base64Data: String? = null
                var mimeType: String? = null

                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    val inlineData = part.optJSONObject("inlineData")
                    if (inlineData != null) {
                        base64Data = inlineData.optString("data")
                        mimeType = inlineData.optString("mimeType")
                        break
                    }
                }

                if (!base64Data.isNullOrBlank()) {
                    val rawBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val formattedWav = PcmToWavUtil.ensureWavOrCompressed(rawBytes)
                    return@withContext Result.success(formattedWav)
                } else {
                    lastError = Exception("Data audio inline tidak ditemukan dalam respons")
                }
            } catch (e: Exception) {
                lastError = e
                Log.e("GeminiTtsService", "Error with model $model: ${e.message}", e)
            }
        }

        Result.failure(lastError ?: Exception("Gagal menghasilkan suara dengan AI Gemini."))
    }

    /**
     * Polishes Indonesian text with conversational prosody, natural pauses, and expressive punctuation
     */
    suspend fun enhanceIndonesianText(rawText: String, styleName: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Kunci Gemini API belum diatur.")
            )
        }

        val prompt = "Sempurnakan kalimat Bahasa Indonesia berikut agar terdengar sangat ekspresif, hidup, dan natural saat dibacakan oleh Text-to-Speech dengan gaya bicara \"$styleName\". " +
                "Tambahkan tanda baca seperti koma untuk jeda napas yang pas, titik, atau tanda seru, serta pilihan kata yang luwes tanpa mengubah makna aslinya. " +
                "Hanya balas dengan hasil teks Bahasa Indonesia yang telah disempurnakan tanpa penjelasan tambahan.\n\nTeks asli:\n$rawText"

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
            })
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val json = JSONObject(body)
            val text = json.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text.trim().removeSurrounding("\""))
            } else {
                Result.failure(Exception("Hasil penyempurnaan kosong"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
