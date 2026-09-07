package com.example.data.repository

import android.content.Context
import com.example.data.local.SpeechDatabase
import com.example.data.local.SpeechHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class SpeechRepository(private val context: Context) {
    private val dao = SpeechDatabase.getInstance(context).speechHistoryDao()

    val allHistory: Flow<List<SpeechHistoryEntity>> = dao.getAllHistory()
    val favorites: Flow<List<SpeechHistoryEntity>> = dao.getFavorites()

    suspend fun insertHistory(
        title: String,
        text: String,
        voiceId: String,
        voiceName: String,
        styleId: String,
        styleLabel: String,
        engineType: String,
        audioBytes: ByteArray?,
        durationMs: Long
    ): SpeechHistoryEntity = withContext(Dispatchers.IO) {
        val audioFilePath = if (audioBytes != null && audioBytes.isNotEmpty()) {
            saveAudioToFile(audioBytes)
        } else {
            null
        }

        val entity = SpeechHistoryEntity(
            title = title.ifBlank { text.take(30) + if (text.length > 30) "..." else "" },
            text = text,
            voiceId = voiceId,
            voiceName = voiceName,
            styleId = styleId,
            styleLabel = styleLabel,
            engineType = engineType,
            audioFilePath = audioFilePath,
            durationMs = durationMs
        )
        val id = dao.insert(entity)
        entity.copy(id = id)
    }

    suspend fun toggleFavorite(item: SpeechHistoryEntity) = withContext(Dispatchers.IO) {
        dao.update(item.copy(isFavorite = !item.isFavorite))
    }

    suspend fun deleteHistory(item: SpeechHistoryEntity) = withContext(Dispatchers.IO) {
        item.audioFilePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
        }
        dao.delete(item)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }

    private fun saveAudioToFile(bytes: ByteArray): String {
        val audioDir = File(context.filesDir, "audio_recordings").apply { mkdirs() }
        val file = File(audioDir, "speech_${UUID.randomUUID()}.wav")
        FileOutputStream(file).use { fos ->
            fos.write(bytes)
        }
        return file.absolutePath
    }
}
