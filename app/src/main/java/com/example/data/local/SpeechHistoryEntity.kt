package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speech_history")
data class SpeechHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val text: String,
    val voiceId: String,
    val voiceName: String,
    val styleId: String,
    val styleLabel: String,
    val engineType: String, // "AI Studio" or "Perangkat (TTS)"
    val audioFilePath: String?,
    val durationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
