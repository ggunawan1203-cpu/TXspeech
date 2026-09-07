package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale
import java.util.UUID

class DeviceTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isIndonesianSupported = MutableStateFlow(false)
    val isIndonesianSupported: StateFlow<Boolean> = _isIndonesianSupported.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var onSpeechDoneCallback: (() -> Unit)? = null
    private var onSpeechErrorCallback: ((String) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val idLocale = Locale("id", "ID")
            var langResult = tts?.setLanguage(idLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Try fallback Locale("in", "ID")
                val fallbackLocale = Locale("in", "ID")
                langResult = tts?.setLanguage(fallbackLocale)
            }

            val supported = langResult != TextToSpeech.LANG_MISSING_DATA &&
                    langResult != TextToSpeech.LANG_NOT_SUPPORTED

            _isIndonesianSupported.value = supported
            _isInitialized.value = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeechDoneCallback?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeechErrorCallback?.invoke("Terjadi kesalahan saat memproses suara perangkat")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    onSpeechErrorCallback?.invoke("Kesalahan TTS perangkat (kode: $errorCode)")
                }
            })
        } else {
            Log.e("DeviceTtsManager", "Gagal menginisialisasi TextToSpeech perangkat")
            _isInitialized.value = false
        }
    }

    fun speak(
        text: String,
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f,
        onDone: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        this.onSpeechDoneCallback = onDone
        this.onSpeechErrorCallback = onError

        tts?.let { engine ->
            engine.setPitch(pitch.coerceIn(0.5f, 2.0f))
            engine.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            val utteranceId = UUID.randomUUID().toString()
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } ?: run {
            onError?.invoke("Engine TTS perangkat belum siap")
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun synthesizeToFile(
        text: String,
        outputFile: File,
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f,
        onComplete: (Boolean) -> Unit
    ) {
        tts?.let { engine ->
            engine.setPitch(pitch.coerceIn(0.5f, 2.0f))
            engine.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            val utteranceId = UUID.randomUUID().toString()
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }

            val listener = object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    if (id == utteranceId) onComplete(true)
                }
                override fun onError(id: String?) {
                    if (id == utteranceId) onComplete(false)
                }
            }
            engine.setOnUtteranceProgressListener(listener)
            engine.synthesizeToFile(text, params, outputFile, utteranceId)
        } ?: onComplete(false)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
