package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioExportManager
import com.example.audio.AudioPlayerManager
import com.example.audio.DeviceTtsManager
import com.example.audio.GeminiTtsService
import com.example.data.ExpressiveStyle
import com.example.data.ExpressiveStyles
import com.example.data.SamplePreset
import com.example.data.VoicePersona
import com.example.data.VoicePersonas
import com.example.data.local.SpeechHistoryEntity
import com.example.data.repository.SpeechRepository
import com.example.ui.components.TtsEngineType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SpeechViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SpeechRepository(application)
    private val geminiTtsService = GeminiTtsService(application)
    val deviceTtsManager = DeviceTtsManager(application)
    val audioPlayer = AudioPlayerManager(application)

    val historyList: StateFlow<List<SpeechHistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _inputText = MutableStateFlow(
        "Halo! Selamat datang di Suara AI. Ketik teks apa saja di sini, dan dengarkan bagaimana kecerdasan buatan menyuarakannya secara alami dan ekspresif."
    )
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedVoice = MutableStateFlow(VoicePersonas.defaultPersona)
    val selectedVoice: StateFlow<VoicePersona> = _selectedVoice.asStateFlow()

    private val _selectedStyle = MutableStateFlow(ExpressiveStyles.defaultStyle)
    val selectedStyle: StateFlow<ExpressiveStyle> = _selectedStyle.asStateFlow()

    private val _engineType = MutableStateFlow(
        if (geminiTtsService.isApiKeyConfigured()) TtsEngineType.AI_STUDIO else TtsEngineType.DEVICE_TTS
    )
    val engineType: StateFlow<TtsEngineType> = _engineType.asStateFlow()

    private val _isApiKeyConfigured = MutableStateFlow(geminiTtsService.isApiKeyConfigured())
    val isApiKeyConfigured: StateFlow<Boolean> = _isApiKeyConfigured.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isEnhancing = MutableStateFlow(false)
    val isEnhancing: StateFlow<Boolean> = _isEnhancing.asStateFlow()

    private val _lastGeneratedBytes = MutableStateFlow<ByteArray?>(null)
    val lastGeneratedBytes: StateFlow<ByteArray?> = _lastGeneratedBytes.asStateFlow()

    private val _currentSavedEntity = MutableStateFlow<SpeechHistoryEntity?>(null)
    val currentSavedEntity: StateFlow<SpeechHistoryEntity?> = _currentSavedEntity.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    fun getCustomApiKey(): String = geminiTtsService.getApiKey()

    fun saveCustomApiKey(key: String) {
        geminiTtsService.saveCustomApiKey(key)
        val isConfigured = geminiTtsService.isApiKeyConfigured()
        _isApiKeyConfigured.value = isConfigured
        if (isConfigured) {
            _engineType.value = TtsEngineType.AI_STUDIO
        }
        viewModelScope.launch {
            _snackbarMessage.emit("Kunci API Gemini pribadi berhasil disimpan!")
        }
    }

    fun clearCustomApiKey() {
        geminiTtsService.clearCustomApiKey()
        _isApiKeyConfigured.value = geminiTtsService.isApiKeyConfigured()
        viewModelScope.launch {
            _snackbarMessage.emit("Kunci API telah dihapus.")
        }
    }

    fun updateInputText(newText: String) {
        _inputText.value = newText
    }

    fun selectVoice(persona: VoicePersona) {
        _selectedVoice.value = persona
    }

    fun selectStyle(style: ExpressiveStyle) {
        _selectedStyle.value = style
        _pitch.value = style.defaultPitch
        _speechRate.value = style.defaultRate
    }

    fun setEngineType(engine: TtsEngineType) {
        _engineType.value = engine
    }

    fun updatePitch(newPitch: Float) {
        _pitch.value = newPitch
    }

    fun updateSpeechRate(newRate: Float) {
        _speechRate.value = newRate
    }

    fun loadPreset(preset: SamplePreset) {
        _inputText.value = preset.text
        VoicePersonas.list.find { it.id == preset.recommendedVoiceId }?.let { selectVoice(it) }
        ExpressiveStyles.list.find { it.id == preset.recommendedStyleId }?.let { selectStyle(it) }
        viewModelScope.launch {
            _snackbarMessage.emit("Memuat skrip: ${preset.title}")
        }
    }

    /**
     * Polishes text with AI to add natural pauses and emotional nuance
     */
    fun enhanceText() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _isEnhancing.value = true
            val result = geminiTtsService.enhanceIndonesianText(text, _selectedStyle.value.label)
            _isEnhancing.value = false
            result.onSuccess { enhanced ->
                _inputText.value = enhanced
                _snackbarMessage.emit("Teks berhasil disempurnakan dengan jeda alami!")
            }.onFailure { err ->
                _snackbarMessage.emit("Catatan: Gunakan API Key di Secrets Panel untuk fitur AI enhancer.")
            }
        }
    }

    /**
     * Preview sample voice clip
     */
    fun previewVoice(persona: VoicePersona) {
        selectVoice(persona)
        val previewText = "Halo, saya ${persona.name}. Karakter suara saya ${persona.timbre.lowercase()}."
        speakDirectText(previewText)
    }

    private fun speakDirectText(text: String) {
        if (_engineType.value == TtsEngineType.AI_STUDIO && _isApiKeyConfigured.value) {
            viewModelScope.launch {
                _isGenerating.value = true
                val result = geminiTtsService.generateSpeechAudio(
                    text = text,
                    geminiVoiceName = _selectedVoice.value.geminiVoiceName,
                    stylePrompt = _selectedStyle.value.promptInstruction
                )
                _isGenerating.value = false
                result.onSuccess { audioBytes ->
                    _lastGeneratedBytes.value = audioBytes
                    audioPlayer.playBytes(audioBytes)
                }.onFailure {
                    // fallback to device
                    deviceTtsManager.speak(text, _pitch.value, _speechRate.value)
                }
            }
        } else {
            deviceTtsManager.speak(text, _pitch.value, _speechRate.value)
        }
    }

    /**
     * Main action: Speaks the user input text using the selected engine
     */
    fun speakCurrentText() {
        val text = _inputText.value.trim()
        if (text.isBlank()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Silakan masukkan teks Bahasa Indonesia terlebih dahulu")
            }
            return
        }

        // Stop any current playback first
        audioPlayer.stop()
        deviceTtsManager.stop()

        if (_engineType.value == TtsEngineType.AI_STUDIO) {
            generateWithGeminiAi(text)
        } else {
            speakWithDeviceTts(text)
        }
    }

    private fun generateWithGeminiAi(text: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            val result = geminiTtsService.generateSpeechAudio(
                text = text,
                geminiVoiceName = _selectedVoice.value.geminiVoiceName,
                stylePrompt = _selectedStyle.value.promptInstruction
            )
            _isGenerating.value = false

            result.onSuccess { bytes ->
                _lastGeneratedBytes.value = bytes
                audioPlayer.playBytes(bytes)
                // Save to repository automatically
                val saved = repository.insertHistory(
                    title = text.take(35) + if (text.length > 35) "..." else "",
                    text = text,
                    voiceId = _selectedVoice.value.id,
                    voiceName = _selectedVoice.value.name,
                    styleId = _selectedStyle.value.id,
                    styleLabel = _selectedStyle.value.label,
                    engineType = "AI Studio (Gemini)",
                    audioBytes = bytes,
                    durationMs = audioPlayer.durationMs.value
                )
                _currentSavedEntity.value = saved
                _snackbarMessage.emit("Suara AI berhasil dibuat!")
            }.onFailure { err ->
                val errorDesc = err.message ?: "Gagal menghasilkan audio"
                _snackbarMessage.emit("$errorDesc. Memutar dengan TTS Perangkat...")
                // Fallback to Device TTS so user gets speech anyway!
                speakWithDeviceTts(text)
            }
        }
    }

    private fun speakWithDeviceTts(text: String) {
        val cacheFile = File(getApplication<Application>().cacheDir, "temp_tts_${System.currentTimeMillis()}.wav")
        deviceTtsManager.speak(
            text = text,
            pitch = _pitch.value,
            speechRate = _speechRate.value,
            onDone = {
                viewModelScope.launch {
                    _currentSavedEntity.value = repository.insertHistory(
                        title = text.take(35) + if (text.length > 35) "..." else "",
                        text = text,
                        voiceId = _selectedVoice.value.id,
                        voiceName = "${_selectedVoice.value.name} (TTS)",
                        styleId = _selectedStyle.value.id,
                        styleLabel = _selectedStyle.value.label,
                        engineType = "Perangkat (Offline)",
                        audioBytes = null,
                        durationMs = 0L
                    )
                }
            },
            onError = { errMsg ->
                viewModelScope.launch {
                    _snackbarMessage.emit(errMsg)
                }
            }
        )
    }

    fun toggleSaveCurrentAudio() {
        val current = _currentSavedEntity.value ?: return
        viewModelScope.launch {
            repository.toggleFavorite(current)
            _currentSavedEntity.value = current.copy(isFavorite = !current.isFavorite)
            _snackbarMessage.emit(
                if (!current.isFavorite) "Disimpan ke Favorit!" else "Dihapus dari Favorit"
            )
        }
    }

    fun playHistoryItem(item: SpeechHistoryEntity) {
        audioPlayer.stop()
        deviceTtsManager.stop()

        item.audioFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                _currentSavedEntity.value = item
                _inputText.value = item.text
                audioPlayer.playFile(path)
                return
            }
        }

        // If file doesn't exist, re-speak text
        _inputText.value = item.text
        speakCurrentText()
    }

    fun toggleFavorite(item: SpeechHistoryEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun deleteHistoryItem(item: SpeechHistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistory(item)
            if (_currentSavedEntity.value?.id == item.id) {
                _currentSavedEntity.value = null
            }
            _snackbarMessage.emit("Riwayat dihapus")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _currentSavedEntity.value = null
            _snackbarMessage.emit("Semua riwayat dibersihkan")
        }
    }

    /**
     * Downloads currently generated audio to public device Downloads folder
     */
    fun downloadCurrentAudio() {
        val bytes = _lastGeneratedBytes.value
            ?: _currentSavedEntity.value?.audioFilePath?.let { path ->
                val f = File(path)
                if (f.exists()) f.readBytes() else null
            }

        if (bytes == null || bytes.isEmpty()) {
            viewModelScope.launch {
                _snackbarMessage.emit("Belum ada audio yang dapat diunduh. Ucapkan teks dengan Suara AI terlebih dahulu.")
            }
            return
        }

        viewModelScope.launch {
            val result = AudioExportManager.saveAudioToDownloads(
                context = getApplication(),
                audioBytes = bytes,
                baseFileName = _selectedVoice.value.name
            )
            result.onSuccess { msg ->
                _snackbarMessage.emit("Berhasil! $msg")
            }.onFailure { err ->
                _snackbarMessage.emit("Gagal mengunduh: ${err.message ?: "Terjadi kesalahan"}")
            }
        }
    }

    /**
     * Shares currently generated audio to other apps (WhatsApp, Telegram, etc.)
     */
    fun shareCurrentAudio() {
        val bytes = _lastGeneratedBytes.value
        val entityPath = _currentSavedEntity.value?.audioFilePath

        if (bytes != null && bytes.isNotEmpty()) {
            AudioExportManager.shareAudio(
                context = getApplication(),
                audioBytes = bytes,
                title = _selectedVoice.value.name
            )
        } else if (!entityPath.isNullOrBlank() && File(entityPath).exists()) {
            AudioExportManager.shareAudioFile(
                context = getApplication(),
                filePath = entityPath,
                title = _selectedVoice.value.name
            )
        } else {
            viewModelScope.launch {
                _snackbarMessage.emit("Belum ada file audio untuk dibagikan. Ucapkan teks terlebih dahulu.")
            }
        }
    }

    /**
     * Downloads an audio item from history
     */
    fun downloadHistoryAudio(item: SpeechHistoryEntity) {
        val path = item.audioFilePath
        if (path == null || !File(path).exists()) {
            viewModelScope.launch {
                _snackbarMessage.emit("File rekaman ini tidak ditemukan di memori.")
            }
            return
        }

        viewModelScope.launch {
            val bytes = File(path).readBytes()
            val result = AudioExportManager.saveAudioToDownloads(
                context = getApplication(),
                audioBytes = bytes,
                baseFileName = item.voiceName
            )
            result.onSuccess { msg ->
                _snackbarMessage.emit("Berhasil! $msg")
            }.onFailure { err ->
                _snackbarMessage.emit("Gagal mengunduh: ${err.message}")
            }
        }
    }

    /**
     * Shares an audio item from history
     */
    fun shareHistoryAudio(item: SpeechHistoryEntity) {
        val path = item.audioFilePath
        if (path == null || !File(path).exists()) {
            viewModelScope.launch {
                _snackbarMessage.emit("File rekaman ini tidak ditemukan di memori.")
            }
            return
        }

        AudioExportManager.shareAudioFile(
            context = getApplication(),
            filePath = path,
            title = item.voiceName
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        deviceTtsManager.shutdown()
    }
}
