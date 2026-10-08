package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "video_generations")
data class VideoGeneration(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "user_default",
    val prompt: String,
    val negativePrompt: String = "",
    val durationSeconds: Int = 30,
    val durationLabel: String = "30 seconds",
    val aspectRatio: String = "16:9",
    val resolution: String = "1080p",
    val style: String = "Cinematic",
    val camera: String = "Cinematic",
    val fps: Int = 30,
    val status: String = STATUS_QUEUED, // Queued, Processing, Generating, Finalizing, Completed, Failed
    val progress: Int = 0, // 0 - 100
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val inputImageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val isFavorite: Boolean = false
) {
    companion object {
        const val STATUS_QUEUED = "Queued"
        const val STATUS_PROCESSING = "Processing"
        const val STATUS_GENERATING = "Generating"
        const val STATUS_FINALIZING = "Finalizing"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_FAILED = "Failed"
    }

    val isFinished: Boolean
        get() = status == STATUS_COMPLETED || status == STATUS_FAILED

    val isSuccess: Boolean
        get() = status == STATUS_COMPLETED
}
