package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantAmber
import com.example.viewmodel.VideoGeneratorViewModel

@Composable
fun ProfileScreen(
    viewModel: VideoGeneratorViewModel,
    onGetCreditsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSafetyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 14.dp
        )
    ) {
        // Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Badge
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ElectricIndigo, ElectricViolet, CyberPink))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.avatarEmoji ?: "🎬",
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user?.name ?: "Creator",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user?.email ?: "creator@nirdoshvideo.ai",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row (Generations & Remaining Credits)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MovieCreation,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${user?.totalGenerations ?: 0}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Generations",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = RadiantAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${user?.credits ?: 0}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Available Credits",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Get More Credits" Prominent Button
                    Button(
                        onClick = onGetCreditsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                Brush.horizontalGradient(listOf(ElectricIndigo, ElectricViolet, CyberPink)),
                                RoundedCornerShape(14.dp)
                            )
                            .testTag("get_credits_profile_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = RadiantAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Get More Credits", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Settings and Account Actions Section
        item {
            Text(
                text = "Preferences & Security",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ProfileOptionRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Explore Feature Tour",
                        subtitle = "Prompt enhancer & image-to-video guide",
                        onClick = { viewModel.launchOnboarding() }
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Edit,
                        title = "Edit Profile Details",
                        subtitle = "Name and contact email",
                        onClick = { showEditProfileDialog = true }
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Settings,
                        title = "Video API & Backend Config",
                        subtitle = "Configure VIDEO_API_URL and VIDEO_API_KEY",
                        onClick = onSettingsClick
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Shield,
                        title = "Safety & Content Moderation",
                        subtitle = "Review acceptable use and policies",
                        onClick = onSafetyPolicyClick
                    )
                    ProfileOptionRow(
                        icon = Icons.Default.Storage,
                        title = "Local Video Cache",
                        subtitle = "Storage: ${viewModel.getLocalCacheSizeFormatted()} • Tap to clear",
                        onClick = { viewModel.clearLocalVideoCache() }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = user?.name ?: "",
            currentEmail = user?.email ?: "",
            onSave = { n, e ->
                viewModel.updateUserProfile(n, e)
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

@Composable
private fun ProfileOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricViolet,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentEmail: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var emailInput by remember { mutableStateOf(currentEmail) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave(nameInput, emailInput) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
