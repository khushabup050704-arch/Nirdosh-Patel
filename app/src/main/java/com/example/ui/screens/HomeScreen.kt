package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.models.VideoGeneration
import com.example.services.CreditManager
import com.example.ui.components.GenerationStatusOverlay
import com.example.ui.components.PresetChipsRow
import com.example.ui.components.PromptInputField
import com.example.ui.components.VisualQueueSection
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.VideoGeneratorViewModel

@Composable
fun HomeScreen(
    viewModel: VideoGeneratorViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onPlayVideo: (VideoGeneration) -> Unit,
    modifier: Modifier = Modifier
) {
    val prompt by viewModel.prompt.collectAsState()
    val negativePrompt by viewModel.negativePrompt.collectAsState()
    val mode by viewModel.generationMode.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val recentGenerations by viewModel.recentGenerations.collectAsState()
    val activeGen by viewModel.activeGeneration.collectAsState()
    val pendingQueueJobs by viewModel.pendingQueueJobs.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isPollingActive by viewModel.isPollingActive.collectAsState()
    val pollingMessage by viewModel.pollingStatusMessage.collectAsState()

    val requiredCredits = CreditManager.calculateRequiredCredits(duration)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Visual Multi-Job Generation Queue
        item {
            AnimatedVisibility(visible = pendingQueueJobs.isNotEmpty()) {
                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                    VisualQueueSection(
                        pendingJobs = pendingQueueJobs,
                        onWatchVideo = { onPlayVideo(it) },
                        onCancelJob = { viewModel.cancelQueuedJob(it) },
                        isPollingActive = isPollingActive,
                        pollingMessage = pollingMessage
                    )
                }
            }
        }

        // Hero Subtitle & Quick Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                ElectricIndigo.copy(alpha = 0.25f),
                                ElectricViolet.copy(alpha = 0.25f),
                                CyberPink.copy(alpha = 0.15f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Next-Gen AI Video Studio",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyan
                        )
                        Text(
                            text = "Transform thoughts into cinematic 60fps video",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.launchOnboarding() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Tour", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan)
                        }
                        Button(
                            onClick = onNavigateToCreate,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Main Prompt Input Box
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                PromptInputField(
                    prompt = prompt,
                    onPromptChange = { viewModel.setPrompt(it) },
                    negativePrompt = negativePrompt,
                    onNegativePromptChange = { viewModel.setNegativePrompt(it) },
                    mode = mode,
                    onModeChange = { viewModel.setGenerationMode(it) },
                    selectedImageUri = selectedImageUri,
                    onImageSelected = { viewModel.setSelectedImage(it) },
                    onClearPrompt = { viewModel.clearPrompt() },
                    onEnhancePrompt = { viewModel.enhancePrompt() }
                )
            }
        }

        // Generate Video Prominent Action Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { viewModel.startGeneration() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElectricIndigo, ElectricViolet, CyberPink)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("generate_video_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = !isGenerating
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (pendingQueueJobs.isNotEmpty()) "+ Queue Another Video" else "Generate Video",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = "$requiredCredits⚡",
                                color = RadiantAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Example Prompts Chips
        item {
            Spacer(modifier = Modifier.height(10.dp))
            PresetChipsRow(
                onSelectPrompt = { viewModel.selectPresetPrompt(it) }
            )
        }

        // Recent Generations Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Generations",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                TextButton(onClick = onNavigateToHistory) {
                    Text("View All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (recentGenerations.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No recent videos yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Type a prompt above and tap Generate Video!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentGenerations) { video ->
                        RecentVideoCard(
                            video = video,
                            onClick = { onPlayVideo(video) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentVideoCard(
    video: VideoGeneration,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("recent_video_item_${video.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(Color.DarkGray)
            ) {
                if (!video.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = "Video Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Duration badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = video.durationLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = video.prompt,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.style,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ElectricViolet
                    )
                    Text(
                        text = video.resolution,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
