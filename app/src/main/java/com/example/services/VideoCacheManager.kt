package com.example.services

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * High-performance local file-based cache for generated video files.
 * Caches videos in the app's internal cache directory (`videos_cache/`),
 * providing instant playback from local disk without re-downloading.
 */
object VideoCacheManager {

    private const val TAG = "VideoCacheManager"
    private const val CACHE_DIR_NAME = "videos_cache"
    private const val MAX_CACHE_SIZE_BYTES = 500L * 1024L * 1024L // 500 MB limit

    /**
     * Returns the dedicated directory for cached videos.
     */
    fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Computes a safe, deterministic filename based on the video URL or ID.
     */
    fun getCacheFileForUrl(context: Context, videoUrl: String): File {
        val hash = hashString(videoUrl)
        return File(getCacheDir(context), "video_$hash.mp4")
    }

    /**
     * Checks if a video has already been completely downloaded to cache.
     */
    fun isCached(context: Context, videoUrl: String): Boolean {
        val file = getCacheFileForUrl(context, videoUrl)
        return file.exists() && file.length() > 0
    }

    /**
     * Gets the local file URI if cached, or null if not yet cached.
     */
    fun getCachedUri(context: Context, videoUrl: String): Uri? {
        val file = getCacheFileForUrl(context, videoUrl)
        return if (file.exists() && file.length() > 0) {
            Uri.fromFile(file)
        } else {
            null
        }
    }

    /**
     * Resolves the best playback URI: returns local cached file URI if available,
     * otherwise parses and returns the remote network URI.
     */
    fun getPlaybackUri(context: Context, videoUrl: String?): Uri {
        if (videoUrl.isNullOrBlank()) {
            return Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
        }

        // If it's already a local file or content URI
        if (videoUrl.startsWith("file://") || videoUrl.startsWith("content://")) {
            return Uri.parse(videoUrl)
        }

        val cached = getCachedUri(context, videoUrl)
        if (cached != null) {
            Log.d(TAG, "Serving instant playback from cache: $cached")
            return cached
        }

        return Uri.parse(videoUrl)
    }

    /**
     * Caches the video from the network to local storage.
     * Uses atomic file rename (`.tmp` to `.mp4`) to avoid corrupt partial files.
     * Reports download progress from 0 to 100.
     */
    suspend fun cacheVideo(
        context: Context,
        videoUrl: String,
        onProgress: (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val targetFile = getCacheFileForUrl(context, videoUrl)
            if (targetFile.exists() && targetFile.length() > 0) {
                withContext(Dispatchers.Main) { onProgress(100) }
                return@withContext Result.success(targetFile)
            }

            trimCacheIfNeeded(context)

            val tempFile = File(getCacheDir(context), "${targetFile.name}.tmp")
            val url = URL(videoUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 20000
                readTimeout = 30000
                instanceFollowRedirects = true
                connect()
            }

            if (connection.responseCode !in 200..299) {
                return@withContext Result.failure(
                    Exception("HTTP ${connection.responseCode} while caching video")
                )
            }

            val totalBytes = connection.contentLengthLong
            val inputStream: InputStream = connection.inputStream
            FileOutputStream(tempFile).use { outputStream ->
                val buffer = ByteArray(16384)
                var bytesRead: Int
                var totalRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (totalBytes > 0) {
                        val progress = ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 99)
                        withContext(Dispatchers.Main) { onProgress(progress) }
                    }
                }
                outputStream.flush()
            }
            inputStream.close()
            connection.disconnect()

            // Atomic rename
            if (tempFile.renameTo(targetFile)) {
                withContext(Dispatchers.Main) { onProgress(100) }
                Log.d(TAG, "Video cached successfully: ${targetFile.absolutePath} (${targetFile.length()} bytes)")
                Result.success(targetFile)
            } else {
                tempFile.delete()
                Result.failure(Exception("Failed to rename temporary cache file"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache video $videoUrl", e)
            Result.failure(e)
        }
    }

    /**
     * Calculates total cached storage size in bytes.
     */
    fun getCacheSizeBytes(context: Context): Long {
        val dir = getCacheDir(context)
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * Formats bytes to readable MB/GB string.
     */
    fun formatCacheSize(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1000) {
            String.format("%.2f GB", mb / 1024.0)
        } else {
            String.format("%.1f MB", mb)
        }
    }

    /**
     * Clears all cached video files.
     */
    fun clearCache(context: Context): Boolean {
        val dir = getCacheDir(context)
        var success = true
        dir.listFiles()?.forEach { file ->
            if (!file.delete()) success = false
        }
        return success
    }

    /**
     * Trims cache if total size exceeds limit using LRU (least recently modified).
     */
    private fun trimCacheIfNeeded(context: Context) {
        val dir = getCacheDir(context)
        val files = dir.listFiles()?.toList() ?: return
        var currentSize = files.sumOf { it.length() }

        if (currentSize > MAX_CACHE_SIZE_BYTES) {
            val sortedByOldest = files.sortedBy { it.lastModified() }
            for (file in sortedByOldest) {
                val len = file.length()
                if (file.delete()) {
                    currentSize -= len
                    if (currentSize <= MAX_CACHE_SIZE_BYTES * 0.75) { // Trim down to 75%
                        break
                    }
                }
            }
        }
    }

    private fun hashString(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray())
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            input.hashCode().toString()
        }
    }
}
