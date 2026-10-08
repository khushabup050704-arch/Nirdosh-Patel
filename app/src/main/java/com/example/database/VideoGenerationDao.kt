package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.models.VideoGeneration
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoGenerationDao {
    @Query("SELECT * FROM video_generations ORDER BY createdAt DESC")
    fun getAllGenerations(): Flow<List<VideoGeneration>>

    @Query("SELECT * FROM video_generations ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentGenerations(limit: Int): Flow<List<VideoGeneration>>

    @Query("SELECT * FROM video_generations WHERE status != 'Completed' AND status != 'Failed' ORDER BY createdAt DESC")
    fun getPendingGenerationsFlow(): Flow<List<VideoGeneration>>

    @Query("SELECT * FROM video_generations WHERE status != 'Completed' AND status != 'Failed' ORDER BY createdAt DESC")
    suspend fun getPendingGenerationsList(): List<VideoGeneration>

    @Query("SELECT * FROM video_generations WHERE id = :id")
    suspend fun getGenerationById(id: String): VideoGeneration?

    @Query("SELECT * FROM video_generations WHERE id = :id")
    fun getGenerationByIdFlow(id: String): Flow<VideoGeneration?>

    @Query("SELECT * FROM video_generations WHERE status = :status ORDER BY createdAt DESC")
    fun getGenerationsByStatus(status: String): Flow<List<VideoGeneration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(videoGeneration: VideoGeneration)

    @Update
    suspend fun update(videoGeneration: VideoGeneration)

    @Delete
    suspend fun delete(videoGeneration: VideoGeneration)

    @Query("DELETE FROM video_generations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE video_generations SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM video_generations")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM video_generations WHERE status = 'Failed'")
    suspend fun getFailedCount(): Int
}
