package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeechHistoryDao {
    @Query("SELECT * FROM speech_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<SpeechHistoryEntity>>

    @Query("SELECT * FROM speech_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<SpeechHistoryEntity>>

    @Query("SELECT * FROM speech_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SpeechHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SpeechHistoryEntity): Long

    @Update
    suspend fun update(item: SpeechHistoryEntity)

    @Delete
    suspend fun delete(item: SpeechHistoryEntity)

    @Query("DELETE FROM speech_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM speech_history")
    suspend fun clearAll()
}
