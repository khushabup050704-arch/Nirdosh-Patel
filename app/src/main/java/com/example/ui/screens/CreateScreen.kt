package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.VideoGeneration
import com.example.services.CreditManager
import com.example.ui.components.GenerationStatusOverlay
import com.example.ui.components.PromptInputField
import com.example.ui.components.VideoSettingsPanel
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.RadiantAmber
import com.example.viewmodel.VideoGeneratorViewModel

@Composable
fun CreateScreen(
    viewModel: VideoGeneratorViewModel,
    onPlayVideo: (VideoGeneration) -> Unit,
    modifier: Modifier = Modifier
) {
    val prompt by viewModel.prompt.collectAsState()
    val negativePrompt by viewModel.negativePrompt.collectAsState()
    val mode by viewModel.generationMode.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val quality by viewModel.quality.collectAsState()
    val style by viewModel.style.collectAsState()
    val camera by viewModel.camera.collectAsState()
    val fps by viewModel.fps.collectAsState()

    val activeGen by viewModel.activeGeneration.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    val requiredCredits = CreditManager.calculateRequiredCredits(duration)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("create_screen_scroll"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Active Status Card if generating
        item {
            AnimatedVisibility(visible = activeGen != null) {
                activeGen?.let { currentJob ->
                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                        GenerationStatusOverlay(
                            generation = currentJob,
                            onWatchVideo = { onPlayVideo(it) },
                            onRetry = { viewModel.retryGeneration(it) },
                            onDismiss = { viewModel.activeGeneration.value = null }
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Studio Workspace",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Fine-tune prompts, aspect ratio, camera motion, and visual style.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Prompt Input Field (with Text-to-Video and Image-to-Video)
        item {
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Video Settings Controls
        item {
            VideoSettingsPanel(
                selectedRatio = aspectRatio,
                onRatioSelected = { viewModel.setAspectRatio(it) },
                selectedDuration = duration,
                onDurationSelected = { viewModel.setDuration(it) },
                selectedQuality = quality,
                onQualitySelected = { viewModel.setQuality(it) },
                selectedStyle = style,
                onStyleSelected = { viewModel.setStyle(it) },
                selectedCamera = camera,
                onCameraSelected = { viewModel.setCamera(it) },
                selectedFps = fps,
                onFpsSelected = { viewModel.setFps(it) }
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Main Generate Action Button
        item {
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
                    .testTag("create_generate_button"),
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
                        text = if (isGenerating) "Generating in Progress..." else "Generate Video",
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
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
