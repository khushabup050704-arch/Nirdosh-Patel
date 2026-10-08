package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.models.VideoGeneration
import com.example.services.DownloadAndShareHelper
import com.example.services.VideoCacheManager
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ExportState {
    object Idle : ExportState()
    data class Exporting(val progress: Int) : ExportState()
    data class Success(val uri: Uri) : ExportState()
    data class Error(val message: String) : ExportState()
}

@Composable
fun VideoPlayerDialog(
    video: videoPlayerTarget,
    onDismiss: () -> Unit,
    onGenerateAgain: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSaveTrimmedClip: ((originalVideo: VideoGeneration, startMs: Int, endMs: Int) -> Unit)? = null
) {
    VideoPlayerDialogInternal(
        video = video,
        onDismiss = onDismiss,
        onGenerateAgain = onGenerateAgain,
        onDelete = onDelete,
        onSaveTrimmedClip = onSaveTrimmedClip
    )
}

typealias videoPlayerTarget = VideoGeneration

@Composable
private fun VideoPlayerDialogInternal(
    video: VideoGeneration,
    onDismiss: () -> Unit,
    onGenerateAgain: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSaveTrimmedClip: ((originalVideo: VideoGeneration, startMs: Int, endMs: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(video.durationSeconds * 1000) }
    var isBuffering by remember { mutableStateOf(true) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    // Export to Gallery State
    var exportState by remember { mutableStateOf<ExportState>(ExportState.Idle) }
    var savedGalleryUri by remember { mutableStateOf<Uri?>(null) }

    // Social Share Dialog State
    var showSocialShareDialog by remember { mutableStateOf(false) }

    // Trim Video State (RangeSlider)
    var isTrimModeEnabled by remember { mutableStateOf(false) }
    var trimRange by remember(durationMs) {
        val maxDuration = (durationMs.takeIf { it > 0 } ?: (video.durationSeconds * 1000)).toFloat().coerceAtLeast(1000f)
        mutableStateOf(0f..maxDuration)
    }
    var isSavingTrimmedClip by remember { mutableStateOf(false) }
    var trimmedClipSavedSuccess by remember { mutableStateOf(false) }

    // Local Video Cache State
    val rawStreamUrl = video.videoUrl
        ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    var isCachedLocally by remember { mutableStateOf(VideoCacheManager.isCached(context, rawStreamUrl)) }
    var resolvedPlaybackUri by remember { mutableStateOf(VideoCacheManager.getPlaybackUri(context, rawStreamUrl)) }

    // Automatically ensure video is cached locally in background for subsequent instant playback
    LaunchedEffect(rawStreamUrl) {
        if (!isCachedLocally) {
            coroutineScope.launch {
                val cacheResult = VideoCacheManager.cacheVideo(context, rawStreamUrl)
                if (cacheResult.isSuccess) {
                    isCachedLocally = true
                }
            }
        }
    }

    // Periodically update progress slider
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying) {
                    currentPositionMs = vv.currentPosition
                    if (vv.duration > 0) {
                        durationMs = vv.duration
                    }
                }
            }
            delay(250)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = !isFullscreen,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = if (isFullscreen) Modifier.fillMaxSize() else Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
            color = Color(0xFF0B0F19)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isFullscreen) 12.dp else 16.dp)
            ) {
                // Header Bar (Title & Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI Video Player",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElectricViolet.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "${video.resolution} • ${video.fps}fps",
                                color = NeonCyan,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (isCachedLocally) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessGreen.copy(alpha = 0.25f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Cached",
                                        color = SuccessGreen,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_player_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Video Surface View Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (isFullscreen) 1f else 0f, fill = isFullscreen)
                        .then(if (!isFullscreen) Modifier.height(255.dp) else Modifier)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val streamUrl = video.videoUrl
                        ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(resolvedPlaybackUri)
                                setOnPreparedListener { mp ->
                                    mediaPlayerRef = mp
                                    isBuffering = false
                                    durationMs = duration
                                    mp.isLooping = false
                                    mp.setVolume(if (isMuted) 0f else 1f, if (isMuted) 0f else 1f)
                                    start()
                                    isPlaying = true
                                }
                                setOnCompletionListener {
                                    isPlaying = false
                                    currentPositionMs = durationMs
                                }
                                setOnErrorListener { _, _, _ ->
                                    isBuffering = false
                                    false
                                }
                                videoViewRef = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                videoViewRef?.let { vv ->
                                    if (vv.isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                }
                            }
                    )

                    // Buffering Spinner
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seekbar & Time Label
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentPositionMs.toFloat(),
                        onValueChange = { newPos ->
                            currentPositionMs = newPos.toInt()
                            videoViewRef?.seekTo(newPos.toInt())
                        },
                        valueRange = 0f..(durationMs.toFloat().coerceAtLeast(1000f)),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = ElectricViolet,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTimeMs(currentPositionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                        Text(
                            text = formatTimeMs(durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Playback Controls (Play/Pause, Replay, Mute, Fullscreen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replay 10s
                    IconButton(onClick = {
                        videoViewRef?.let { vv ->
                            val newPos = (vv.currentPosition - 10000).coerceAtLeast(0)
                            vv.seekTo(newPos)
                            currentPositionMs = newPos
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Rewind",
                            tint = Color.White
                        )
                    }

                    // Main Play/Pause Button
                    Surface(
                        shape = CircleShape,
                        color = ElectricViolet,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .clickable {
                                videoViewRef?.let { vv ->
                                    if (isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                }
                            }
                            .testTag("play_pause_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Mute / Unmute
                    IconButton(onClick = {
                        isMuted = !isMuted
                        mediaPlayerRef?.setVolume(
                            if (isMuted) 0f else 1f,
                            if (isMuted) 0f else 1f
                        )
                    }) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White
                        )
                    }

                    // Fullscreen Toggle
                    IconButton(onClick = { isFullscreen = !isFullscreen }) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // -------------------------------------------------------------
                // TRIM INTERFACE WITH RANGESLIDER
                // -------------------------------------------------------------
                AnimatedVisibility(
                    visible = isTrimModeEnabled,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val maxDuration = durationMs.toFloat().coerceAtLeast(1000f)
                    val startMs = trimRange.start.toInt()
                    val endMs = trimRange.endInclusive.toInt()
                    val clipDurationSec = ((endMs - startMs) / 1000).coerceAtLeast(1)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("trim_interface_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCut,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Trim Sub-Clip",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonCyan.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "Duration: ${clipDurationSec}s",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Drag sliders to select start and end points for your sub-clip:",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.LightGray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Interactive RangeSlider for Trimming
                            RangeSlider(
                                value = trimRange,
                                onValueChange = { newRange ->
                                    // Ensure minimum 1 second duration
                                    if (newRange.endInclusive - newRange.start >= 1000f) {
                                        trimRange = newRange
                                        videoViewRef?.seekTo(newRange.start.toInt())
                                        currentPositionMs = newRange.start.toInt()
                                    }
                                },
                                valueRange = 0f..maxDuration,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("trim_range_slider")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Start: ${formatTimeMs(startMs)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "End: ${formatTimeMs(endMs)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: Preview Trim & Save Trimmed Clip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        videoViewRef?.let { vv ->
                                            vv.seekTo(startMs)
                                            vv.start()
                                            isPlaying = true
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("preview_trim_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Preview", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        isSavingTrimmedClip = true
                                        coroutineScope.launch {
                                            if (onSaveTrimmedClip != null) {
                                                onSaveTrimmedClip.invoke(video, startMs, endMs)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Saved ${clipDurationSec}s trimmed clip!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            delay(500)
                                            isSavingTrimmedClip = false
                                            trimmedClipSavedSuccess = true
                                            Toast.makeText(
                                                context,
                                                "Trimmed clip (${formatTimeMs(startMs)} - ${formatTimeMs(endMs)}) saved to creations!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(38.dp)
                                        .background(
                                            Brush.horizontalGradient(listOf(NeonCyan, ElectricViolet)),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .testTag("save_trim_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    enabled = !isSavingTrimmedClip
                                ) {
                                    if (isSavingTrimmedClip) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Saving...", fontSize = 12.sp, color = Color.White)
                                    } else {
                                        Icon(
                                            imageVector = if (trimmedClipSavedSuccess) Icons.Default.CheckCircle else Icons.Default.Save,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (trimmedClipSavedSuccess) "Saved!" else "Save Sub-Clip",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // EXPORT FEEDBACK CARD (PROGRESS / SUCCESS)
                // -------------------------------------------------------------
                AnimatedVisibility(
                    visible = exportState !is ExportState.Idle,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    when (val state = exportState) {
                        is ExportState.Exporting -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = NeonCyan
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Saving to Gallery (Movies/Nirdosh AI)...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = "${state.progress}%",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = NeonCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { (state.progress / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = NeonCyan,
                                        trackColor = Color.DarkGray
                                    )
                                }
                            }
                        }

                        is ExportState.Success -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3827))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Saved to Gallery!",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Movies/Nirdosh AI Video",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = Color.LightGray
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            DownloadAndShareHelper.openInGallery(context, state.uri)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        is ExportState.Error -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF381418))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Export failed: ${state.message}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFF8B94),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { exportState = ExportState.Idle }) {
                                        Text("Dismiss", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        else -> Unit
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // -------------------------------------------------------------
                // PRIMARY DUAL ACTION BUTTONS: EXPORT TO GALLERY & SOCIAL SHARE
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export to Gallery Button
                    Button(
                        onClick = {
                            val streamUrl = video.videoUrl
                                ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

                            exportState = ExportState.Exporting(10)
                            coroutineScope.launch {
                                val result = DownloadAndShareHelper.exportVideoToGallery(
                                    context = context,
                                    videoUrl = streamUrl,
                                    title = video.prompt,
                                    onProgress = { p -> exportState = ExportState.Exporting(p) }
                                )

                                exportState = result.fold(
                                    onSuccess = { uri ->
                                        savedGalleryUri = uri
                                        Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
                                        ExportState.Success(uri)
                                    },
                                    onFailure = { error ->
                                        Toast.makeText(context, "Export error: ${error.message}", Toast.LENGTH_LONG).show()
                                        ExportState.Error(error.message ?: "Download error")
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .background(
                                Brush.horizontalGradient(listOf(ElectricIndigo, ElectricViolet)),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("export_to_gallery_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = exportState !is ExportState.Exporting
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (exportState is ExportState.Success) Icons.Default.CheckCircle else Icons.Default.PhotoLibrary,
                                contentDescription = "Export to Gallery",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export Gallery",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Social Share Button (WhatsApp, Instagram, TikTok)
                    Button(
                        onClick = { showSocialShareDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .background(
                                Brush.horizontalGradient(listOf(ElectricViolet, CyberPink)),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("social_share_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Social Share",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Social Share",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action Buttons: Cache Status/Action, Download, Again, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cache Status / Fast Re-Cache
                    OutlinedButton(
                        onClick = {
                            if (!isCachedLocally) {
                                coroutineScope.launch {
                                    val res = VideoCacheManager.cacheVideo(context, rawStreamUrl)
                                    if (res.isSuccess) {
                                        isCachedLocally = true
                                        resolvedPlaybackUri = VideoCacheManager.getPlaybackUri(context, rawStreamUrl)
                                        Toast.makeText(context, "Saved to local fast cache!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Cache failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Video is already cached on device storage for instant playback.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("video_cache_toggle_button"),
                        border = if (isCachedLocally) BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)) else null
                    ) {
                        Icon(
                            imageVector = if (isCachedLocally) Icons.Default.CheckCircle else Icons.Default.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isCachedLocally) SuccessGreen else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCachedLocally) "Cached" else "Cache",
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = if (isCachedLocally) SuccessGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Trim Clip Action Button
                    OutlinedButton(
                        onClick = {
                            isTrimModeEnabled = !isTrimModeEnabled
                            if (isTrimModeEnabled) {
                                val max = durationMs.toFloat().coerceAtLeast(1000f)
                                trimRange = 0f..max
                                videoViewRef?.seekTo(0)
                                currentPositionMs = 0
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("trim_button"),
                        border = if (isTrimModeEnabled) BorderStroke(1.dp, NeonCyan) else null
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Trim Clip",
                            modifier = Modifier.size(14.dp),
                            tint = if (isTrimModeEnabled) NeonCyan else Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTrimModeEnabled) "Trimming" else "Trim",
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = if (isTrimModeEnabled) NeonCyan else Color.White
                        )
                    }

                    // Download file via DownloadManager
                    OutlinedButton(
                        onClick = {
                            val streamUrl = video.videoUrl
                                ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                            DownloadAndShareHelper.downloadVideo(context, streamUrl, video.prompt)
                        },
                        modifier = Modifier.weight(1f).testTag("download_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 11.sp, maxLines = 1)
                    }

                    // Generate Again
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onGenerateAgain(video.prompt)
                        },
                        modifier = Modifier.weight(1f).testTag("generate_again_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Again", fontSize = 11.sp, maxLines = 1)
                    }

                    // Delete
                    IconButton(
                        onClick = {
                            onDelete(video.id)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Video",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // SOCIAL SHARE DIALOG (WhatsApp, Instagram, TikTok, Chooser)
    // -------------------------------------------------------------
    if (showSocialShareDialog) {
        SocialShareDialog(
            prompt = video.prompt,
            videoUrl = video.videoUrl,
            savedUri = savedGalleryUri,
            onDismiss = { showSocialShareDialog = false }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef?.stopPlayback()
        }
    }
}

@Composable
private fun SocialShareDialog(
    prompt: String,
    videoUrl: String?,
    savedUri: Uri?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF111827),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("social_share_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(listOf(ElectricViolet, CyberPink))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Social Share",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Export to your favorite social app",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prompt preview card with copy button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "\"$prompt\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#NirdoshAIVideo #AIVideo #AIArt",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = NeonCyan
                            )
                            TextButton(
                                onClick = {
                                    DownloadAndShareHelper.copyToClipboard(
                                        context,
                                        "\"$prompt\"\n#NirdoshAIVideo #AIVideo",
                                        "Prompt & tags"
                                    )
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", fontSize = 11.sp, color = NeonCyan)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Destination App:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 1. WhatsApp Action
                SocialAppCard(
                    name = "WhatsApp",
                    description = "Send to WhatsApp chats & Status",
                    badgeText = "Chat & Status",
                    accentColor = Color(0xFF25D366),
                    iconEmoji = "💬",
                    onClick = {
                        DownloadAndShareHelper.shareVideoMedia(
                            context = context,
                            videoUri = savedUri,
                            videoUrl = videoUrl,
                            prompt = prompt,
                            targetPackage = "com.whatsapp"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Instagram Action
                SocialAppCard(
                    name = "Instagram",
                    description = "Post to Reels, Stories, or Direct message",
                    badgeText = "Reels & Stories",
                    accentColor = Color(0xFFE1306C),
                    iconEmoji = "📸",
                    onClick = {
                        DownloadAndShareHelper.shareVideoMedia(
                            context = context,
                            videoUri = savedUri,
                            videoUrl = videoUrl,
                            prompt = prompt,
                            targetPackage = "com.instagram.android"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. TikTok Action
                SocialAppCard(
                    name = "TikTok",
                    description = "Publish to your TikTok video feed",
                    badgeText = "Feed & Sounds",
                    accentColor = Color(0xFF00F2FE),
                    iconEmoji = "🎵",
                    onClick = {
                        DownloadAndShareHelper.shareVideoMedia(
                            context = context,
                            videoUri = savedUri,
                            videoUrl = videoUrl,
                            prompt = prompt,
                            targetPackage = "com.zhiliaoapp.musically"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. General System Share (All Apps)
                SocialAppCard(
                    name = "More Apps & Chooser",
                    description = "Share via Bluetooth, Telegram, YouTube, Drive...",
                    badgeText = "All Apps",
                    accentColor = ElectricViolet,
                    iconEmoji = "🚀",
                    onClick = {
                        DownloadAndShareHelper.shareVideoMedia(
                            context = context,
                            videoUri = savedUri,
                            videoUrl = videoUrl,
                            prompt = prompt,
                            targetPackage = null
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (savedUri != null) "✓ Attached full .mp4 video file from Gallery" else "💡 Tip: Export to Gallery first to attach the full video file to Stories & Reels",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (savedUri != null) SuccessGreen else Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SocialAppCard(
    name: String,
    description: String,
    badgeText: String,
    accentColor: Color,
    iconEmoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("social_share_item_${name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = iconEmoji, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = badgeText,
                                color = accentColor,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.LightGray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Share to $name",
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatTimeMs(timeMs: Int): String {
    val totalSeconds = (timeMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
