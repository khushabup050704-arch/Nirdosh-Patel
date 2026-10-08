package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.models.GenerationMode
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptInputField(
    prompt: String,
    onPromptChange: (String) -> Unit,
    negativePrompt: String,
    onNegativePromptChange: (String) -> Unit,
    mode: GenerationMode,
    onModeChange: (GenerationMode) -> Unit,
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    onClearPrompt: () -> Unit,
    onEnhancePrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var isNegativePromptExpanded by remember { mutableStateOf(false) }

    // Standard Android Photo Picker (zero-permission, fully Play-compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    val negativePresets = listOf(
        "blurry", "distorted face", "extra fingers", "watermark",
        "low quality", "bad anatomy", "text", "flickering"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Generation Mode Switcher (Text to Video vs Image to Video)
            TabRow(
                selectedTabIndex = if (mode == GenerationMode.TEXT_TO_VIDEO) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = mode == GenerationMode.TEXT_TO_VIDEO,
                    onClick = { onModeChange(GenerationMode.TEXT_TO_VIDEO) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Text to Video", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("tab_text_to_video")
                )
                Tab(
                    selected = mode == GenerationMode.IMAGE_TO_VIDEO,
                    onClick = { onModeChange(GenerationMode.IMAGE_TO_VIDEO) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Image to Video", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("tab_image_to_video")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Image-to-Video Upload Picker Container
            AnimatedVisibility(visible = mode == GenerationMode.IMAGE_TO_VIDEO) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = "Source Image",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(14.dp)
                                )
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected source image",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                            ) {
                                Text(
                                    text = "Change Image",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    } else {
                        // Empty Image Placeholder Box
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload Image",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap to choose an image from gallery",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "JPEG, PNG, WEBP supported",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Prompt Label & Tools Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mode == GenerationMode.IMAGE_TO_VIDEO) "Animation Instructions" else "Prompt Description",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Paste Button
                    TextButton(
                        onClick = {
                            val clipboardText = clipboardManager.getText()?.text
                            if (!clipboardText.isNullOrBlank()) {
                                onPromptChange(
                                    if (prompt.isBlank()) clipboardText else "$prompt $clipboardText"
                                )
                            }
                        },
                        modifier = Modifier.testTag("paste_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste", fontSize = 12.sp)
                    }

                    // Clear Button
                    if (prompt.isNotBlank()) {
                        IconButton(
                            onClick = onClearPrompt,
                            modifier = Modifier.size(30.dp).testTag("clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Prompt Outlined Text Field
            OutlinedTextField(
                value = prompt,
                onValueChange = { if (it.length <= 1500) onPromptChange(it) },
                placeholder = {
                    Text(
                        text = if (mode == GenerationMode.IMAGE_TO_VIDEO)
                            "Make the character slowly turn toward the camera while the hair moves naturally in the wind..."
                        else
                            "Describe the video you want to create... (e.g. A cinematic drone shot flying over the Himalayas at sunrise)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("prompt_input_field"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricViolet,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Character Counter & Prompt Enhancer Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${prompt.length} / 1500",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (prompt.length > 1300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Prompt Enhancer Button
                Button(
                    onClick = onEnhancePrompt,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(ElectricIndigo, ElectricViolet, CyberPink)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("enhance_prompt_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Enhance",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enhance Prompt",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Negative Prompt Collapsible Section
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isNegativePromptExpanded = !isNegativePromptExpanded }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Negative Prompt (Optional)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (negativePrompt.isNotBlank() && !isNegativePromptExpanded) {
                            Text(
                                text = negativePrompt.take(40) + "...",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isNegativePromptExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle negative prompt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isNegativePromptExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = negativePrompt,
                        onValueChange = onNegativePromptChange,
                        placeholder = {
                            Text(
                                "Describe what should not appear in the video... (e.g. blurry, distorted, watermark)",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .testTag("negative_prompt_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        negativePresets.forEach { preset ->
                            val isIncluded = negativePrompt.contains(preset)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isIncluded) ElectricViolet.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isIncluded) androidx.compose.foundation.BorderStroke(1.dp, ElectricViolet) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (!isIncluded) {
                                            val newText = if (negativePrompt.isBlank()) preset else "$negativePrompt, $preset"
                                            onNegativePromptChange(newText)
                                        }
                                    }
                            ) {
                                Text(
                                    text = "+ $preset",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (isIncluded) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
