package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.models.CreditTransaction
import com.example.models.User
import com.example.models.VideoGeneration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [VideoGeneration::class, CreditTransaction::class, User::class],
    version = 1,
    exportSchema = false
)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoGenerationDao(): VideoGenerationDao
    abstract fun creditTransactionDao(): CreditTransactionDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: VideoDatabase? = null

        fun getInstance(context: Context): VideoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VideoDatabase::class.java,
                    "nirdosh_ai_video.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default user and welcome sample video
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.userDao().insertOrUpdate(
                                    User(
                                        id = "user_default",
                                        name = "Alex Vance",
                                        email = "creator@nirdoshvideo.ai",
                                        credits = 50,
                                        totalGenerations = 1,
                                        isAdmin = true
                                    )
                                )
                                database.creditTransactionDao().insert(
                                    CreditTransaction(
                                        userId = "user_default",
                                        amount = 50,
                                        type = "bonus",
                                        description = "Welcome Creator Bonus credits"
                                    )
                                )
                                // Add a gorgeous starter generation
                                database.videoGenerationDao().insert(
                                    VideoGeneration(
                                        id = "sample_video_1",
                                        userId = "user_default",
                                        prompt = "A cinematic drone shot flying over snow-covered mountain peaks at sunrise, golden sunlight reflecting on misty valleys, ultra realistic 8k, smooth camera motion.",
                                        negativePrompt = "blurry, low quality, distortion, flickering",
                                        durationSeconds = 30,
                                        durationLabel = "30 seconds",
                                        aspectRatio = "16:9",
                                        resolution = "1080p",
                                        style = "Cinematic",
                                        camera = "Drone",
                                        fps = 30,
                                        status = VideoGeneration.STATUS_COMPLETED,
                                        progress = 100,
                                        // A reliable high-speed CDN MP4 video and high-res thumbnail
                                        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                                        thumbnailUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
                                        createdAt = System.currentTimeMillis() - 3600000L
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
