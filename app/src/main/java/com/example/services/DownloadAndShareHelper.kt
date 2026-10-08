package com.example.services

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object DownloadAndShareHelper {

    /**
     * Exports and saves the generated video directly into the local Android Gallery
     * under the 'Movies/Nirdosh AI Video' collection so that it is instantly indexed
     * and visible in the device's Gallery / Google Photos app.
     */
    suspend fun exportVideoToGallery(
        context: Context,
        videoUrl: String,
        title: String,
        onProgress: (Int) -> Unit = {}
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_\\-\\s]"), "")
                .trim()
                .take(30)
                .ifEmpty { "Nirdosh_AI" }
            val fileName = "${sanitizedTitle}_${System.currentTimeMillis()}.mp4"

            val url = URL(videoUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 20000
                readTimeout = 30000
                instanceFollowRedirects = true
                connect()
            }

            if (connection.responseCode !in 200..299) {
                return@withContext Result.failure(
                    Exception("Server returned HTTP ${connection.responseCode}")
                )
            }

            val totalBytes = connection.contentLengthLong
            val inputStream: InputStream = connection.inputStream
            val resolver = context.contentResolver

            val savedUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.TITLE, sanitizedTitle)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_MOVIES}/Nirdosh AI Video"
                    )
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                    put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                    put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                }

                val itemUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("Could not create MediaStore entry for video")

                try {
                    resolver.openOutputStream(itemUri)?.use { outputStream ->
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
                    } ?: throw IllegalStateException("Unable to open output stream")

                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(itemUri, contentValues, null, null)
                    withContext(Dispatchers.Main) { onProgress(100) }
                    itemUri
                } catch (e: Exception) {
                    resolver.delete(itemUri, null, null)
                    throw e
                }
            } else {
                @Suppress("DEPRECATION")
                val moviesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "Nirdosh AI Video"
                )
                if (!moviesDir.exists()) moviesDir.mkdirs()
                val targetFile = File(moviesDir, fileName)

                FileOutputStream(targetFile).use { outputStream ->
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

                withContext(Dispatchers.Main) { onProgress(100) }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetFile.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
                Uri.fromFile(targetFile)
            }

            inputStream.close()
            connection.disconnect()
            Result.success(savedUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Opens the saved video in the device's native Gallery or video viewer app.
     */
    fun openInGallery(context: Context, videoUri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(videoUri, "video/mp4")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open Video in Gallery"))
        } catch (e: Exception) {
            Toast.makeText(context, "Saved to Gallery (Movies/Nirdosh AI Video)", Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadVideo(context: Context, videoUrl: String, title: String) {
        try {
            val uri = Uri.parse(videoUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle(title.take(30).ifEmpty { "Nirdosh_AI_Video" })
                setDescription("Downloading generated AI video")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "Nirdosh_AI_${System.currentTimeMillis()}.mp4"
                )
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            manager?.enqueue(request)
            Toast.makeText(context, "Download started! Check notifications.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareVideo(context: Context, videoUrl: String?, prompt: String) {
        shareVideoMedia(context, null, videoUrl, prompt)
    }

    /**
     * Shares video directly to social apps (WhatsApp, Instagram, TikTok, etc.)
     * using the Android Share Intent. If a local Uri is provided, attaches the actual
     * video file so the recipient app can immediately play and post it.
     */
    fun shareVideoMedia(
        context: Context,
        videoUri: Uri?,
        videoUrl: String?,
        prompt: String,
        targetPackage: String? = null
    ) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                if (videoUri != null) {
                    type = "video/mp4"
                    putExtra(Intent.EXTRA_STREAM, videoUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                }
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "AI Video generated with Nirdosh AI Video"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "🎬 Watch this video made with Nirdosh AI Video!\n\n" +
                            "\"$prompt\"\n\n" +
                            (if (!videoUrl.isNullOrEmpty() && videoUri == null) "$videoUrl\n\n" else "") +
                            "#NirdoshAIVideo #AIVideo #GenerativeAI"
                )
                if (!targetPackage.isNullOrBlank()) {
                    setPackage(targetPackage)
                }
            }

            if (!targetPackage.isNullOrBlank()) {
                val packageManager = context.packageManager
                val isInstalled = packageManager.getLaunchIntentForPackage(targetPackage) != null
                if (isInstalled) {
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(shareIntent)
                    return
                }
            }

            val chooserTitle = "Share to WhatsApp, Instagram, TikTok..."
            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Prompt") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
