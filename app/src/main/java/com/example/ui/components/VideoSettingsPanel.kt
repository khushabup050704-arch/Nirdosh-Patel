package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AspectRatio
import com.example.models.CameraMotion
import com.example.models.DurationOption
import com.example.models.FpsOption
import com.example.models.QualityOption
import com.example.models.StyleOption
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoSettingsPanel(
    selectedRatio: AspectRatio,
    onRatioSelected: (AspectRatio) -> Unit,
    selectedDuration: DurationOption,
    onDurationSelected: (DurationOption) -> Unit,
    selectedQuality: QualityOption,
    onQualitySelected: (QualityOption) -> Unit,
    selectedStyle: StyleOption,
    onStyleSelected: (StyleOption) -> Unit,
    selectedCamera: CameraMotion,
    onCameraSelected: (CameraMotion) -> Unit,
    selectedFps: FpsOption,
    onFpsSelected: (FpsOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Video Parameters & Styling",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Aspect Ratio
            SettingsSectionHeader(
                icon = Icons.Default.AspectRatio,
                title = "Aspect Ratio",
                subtitle = selectedRatio.iconDescription
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AspectRatio.values().forEach { ratio ->
                    val isSelected = ratio == selectedRatio
                    SelectablePill(
                        label = ratio.label,
                        isSelected = isSelected,
                        onClick = { onRatioSelected(ratio) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ratio_${ratio.label}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Duration & Credits
            SettingsSectionHeader(
                icon = Icons.Default.Timer,
                title = "Duration",
                subtitle = "Uses ${selectedDuration.creditsRequired} credits"
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DurationOption.values().forEach { dur ->
                    val isSelected = dur == selectedDuration
                    SelectablePillWithBadge(
                        label = dur.label,
                        badge = "${dur.creditsRequired}⚡",
                        isSelected = isSelected,
                        onClick = { onDurationSelected(dur) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("duration_${dur.seconds}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Quality & Resolution
            SettingsSectionHeader(
                icon = Icons.Default.HighQuality,
                title = "Quality",
                subtitle = selectedQuality.resolutionString
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QualityOption.values().forEach { qual ->
                    SelectablePill(
                        label = qual.label,
                        isSelected = qual == selectedQuality,
                        onClick = { onQualitySelected(qual) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quality_${qual.label}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Style Option
            SettingsSectionHeader(
                icon = Icons.Default.Palette,
                title = "Visual Style",
                subtitle = selectedStyle.label
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StyleOption.values().forEach { st ->
                    SelectablePill(
                        label = st.label,
                        isSelected = st == selectedStyle,
                        onClick = { onStyleSelected(st) },
                        modifier = Modifier.testTag("style_${st.label}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Camera Motion
            SettingsSectionHeader(
                icon = Icons.Default.CameraAlt,
                title = "Camera Motion",
                subtitle = selectedCamera.label
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CameraMotion.values().forEach { cam ->
                    SelectablePill(
                        label = cam.label,
                        isSelected = cam == selectedCamera,
                        onClick = { onCameraSelected(cam) },
                        modifier = Modifier.testTag("camera_${cam.label}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Frame Rate (FPS)
            SettingsSectionHeader(
                icon = Icons.Default.Speed,
                title = "Frame Rate (FPS)",
                subtitle = "${selectedFps.fpsValue} fps"
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FpsOption.values().forEach { f ->
                    SelectablePill(
                        label = "${f.fpsValue} FPS",
                        isSelected = f == selectedFps,
                        onClick = { onFpsSelected(f) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fps_${f.fpsValue}")
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectablePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) ElectricViolet else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SelectablePillWithBadge(
    label: String,
    badge: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) ElectricViolet else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = badge,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) RadiantAmber else NeonCyan
            )
        }
    }
}
