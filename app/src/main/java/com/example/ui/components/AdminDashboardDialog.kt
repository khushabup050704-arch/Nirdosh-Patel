package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.SuccessGreen

@Composable
fun AdminDashboardDialog(
    totalGenerations: Int,
    failedJobsCount: Int,
    userCredits: Int,
    onGrantCredits: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var rateLimitEnabled by remember { mutableStateOf(true) }
    var accountFlagged by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().testTag("admin_dashboard_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Dashboard",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Analytics Cards Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminMetricCard(
                        title = "Generations",
                        value = "$totalGenerations",
                        icon = Icons.Default.VideoLibrary,
                        tint = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Failed Jobs",
                        value = "$failedJobsCount",
                        icon = Icons.Default.Error,
                        tint = if (failedJobsCount > 0) ErrorRed else SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Credits",
                        value = "$userCredits",
                        icon = Icons.Default.Bolt,
                        tint = RadiantAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manage Credits
                Text(
                    text = "Manage User Credits",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onGrantCredits(50) },
                        modifier = Modifier.weight(1f).testTag("grant_50_credits")
                    ) {
                        Text("+50 Credits")
                    }
                    Button(
                        onClick = { onGrantCredits(200) },
                        modifier = Modifier.weight(1f).testTag("grant_200_credits"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                    ) {
                        Text("+200 Credits")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rate limiting & Abusive Account Control
                Text(
                    text = "System Governance & Safety",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rate Limiter (5 requests/min)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = { rateLimitEnabled = !rateLimitEnabled }) {
                                Icon(
                                    imageVector = if (rateLimitEnabled) Icons.Default.Done else Icons.Default.Close,
                                    tint = if (rateLimitEnabled) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    contentDescription = "Toggle rate limit"
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account Status: ${if (accountFlagged) "Suspended" else "Active / Good Standing"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (accountFlagged) ErrorRed else MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { accountFlagged = !accountFlagged }) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    tint = if (accountFlagged) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                    contentDescription = "Flag account"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
