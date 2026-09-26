package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    @Query("SELECT * FROM jarvis_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<JarvisMessageEntity>>

    @Query("SELECT * FROM jarvis_messages ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessage(): JarvisMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: JarvisMessageEntity): Long

    @Query("DELETE FROM jarvis_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM jarvis_messages")
    suspend fun clearAllMessages()

    @Query("SELECT COUNT(*) FROM jarvis_messages")
    suspend fun getMessageCount(): Int
}
