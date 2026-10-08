package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.VideoGeneration
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.SuccessGreen

@Composable
fun VisualQueueSection(
    pendingJobs: List<VideoGeneration>,
    onWatchVideo: (VideoGeneration) -> Unit,
    onCancelJob: (VideoGeneration) -> Unit,
    modifier: Modifier = Modifier,
    isPollingActive: Boolean = true,
    pollingMessage: String? = null
) {
    if (pendingJobs.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_queue_section")
    ) {
        // Queue Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulsing queue indicator dot
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = alpha))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Generation Queue",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElectricViolet.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${pendingJobs.size} active",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricViolet,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPollingActive) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Polling active",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = if (isPollingActive) "Live Polling" else "Multi-Job Pipeline",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (isPollingActive) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Queue Items List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            pendingJobs.forEach { job ->
                QueueJobCard(
                    job = job,
                    onWatch = { onWatchVideo(job) },
                    onCancel = { onCancelJob(job) }
                )
            }
        }
    }
}

@Composable
private fun QueueJobCard(
    job: VideoGeneration,
    onWatch: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = job.status == VideoGeneration.STATUS_COMPLETED
    val isFailed = job.status == VideoGeneration.STATUS_FAILED

    val stageColor = when (job.status) {
        VideoGeneration.STATUS_QUEUED -> RadiantAmber
        VideoGeneration.STATUS_PROCESSING -> ElectricViolet
        VideoGeneration.STATUS_GENERATING -> NeonCyan
        VideoGeneration.STATUS_FINALIZING -> CyberPink
        VideoGeneration.STATUS_COMPLETED -> SuccessGreen
        else -> ErrorRed
    }

    val stepExplanation = when (job.status) {
        VideoGeneration.STATUS_QUEUED -> "Queued in generation cluster..."
        VideoGeneration.STATUS_PROCESSING -> "Analyzing spatial depth & lighting..."
        VideoGeneration.STATUS_GENERATING -> "Synthesizing neural 60fps frames..."
        VideoGeneration.STATUS_FINALIZING -> "Encoding high-bitrate video stream..."
        VideoGeneration.STATUS_COMPLETED -> "Video Ready!"
        else -> job.errorMessage ?: "Generation failed."
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("queue_job_card_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, stageColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Top Row: Status Pill, Progress %, Cancel Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(stageColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = job.status,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = stageColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${job.progress}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isCompleted) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("cancel_queue_job_${job.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel job",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Progress Indicator
            LinearProgressIndicator(
                progress = { (job.progress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = stageColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Prompt snippet
            Text(
                text = "\"${job.prompt}\"",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Step Explanation
            Text(
                text = stepExplanation,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Format Badges & Watch Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QueueBadge(text = job.durationLabel)
                    QueueBadge(text = job.aspectRatio)
                    QueueBadge(text = job.resolution)
                    QueueBadge(text = job.style)
                }

                if (isCompleted) {
                    Button(
                        onClick = onWatch,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
