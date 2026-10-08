package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.models.VideoGeneration
import com.example.services.DownloadAndShareHelper
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.VideoGeneratorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: VideoGeneratorViewModel,
    onPlayVideo: (VideoGeneration) -> Unit,
    onRegenerate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allGenerations by viewModel.allGenerations.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val filterTabs = listOf("All", "Completed", "Processing", "Failed")

    val filteredList = allGenerations.filter { item ->
        val matchesQuery = searchQuery.isBlank() || item.prompt.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (selectedFilterIndex) {
            1 -> item.status == VideoGeneration.STATUS_COMPLETED
            2 -> item.status != VideoGeneration.STATUS_COMPLETED && item.status != VideoGeneration.STATUS_FAILED
            3 -> item.status == VideoGeneration.STATUS_FAILED
            else -> true
        }
        matchesQuery && matchesStatus
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen")
    ) {
        // Top Header & Search Bar
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = "Video Vault & History",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${allGenerations.size} creations saved locally on device",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (searchQuery.isNotEmpty()) ElectricViolet else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                placeholder = { Text("Search by prompt keywords...", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            if (searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Found ${filteredList.size} of ${allGenerations.size} videos for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = ElectricViolet
                    )
                }
            }
        }

        // Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedFilterIndex,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = ElectricViolet,
            divider = {}
        ) {
            filterTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedFilterIndex == index,
                    onClick = { selectedFilterIndex = index },
                    text = {
                        Text(
                            text = tab,
                            fontWeight = if (selectedFilterIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // History Video List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (searchQuery.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No results found",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No videos match \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Try searching with different prompt keywords or clearing the filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No videos in this filter",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { video ->
                    HistoryItemCard(
                        video = video,
                        onPlay = { onPlayVideo(video) },
                        onDownload = {
                            video.videoUrl?.let { url ->
                                DownloadAndShareHelper.downloadVideo(context, url, video.prompt)
                            }
                        },
                        onShare = {
                            DownloadAndShareHelper.shareVideo(context, video.videoUrl, video.prompt)
                        },
                        onRegenerate = { onRegenerate(video.prompt) },
                        onDelete = { viewModel.deleteGeneration(video.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(video) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    video: VideoGeneration,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onRegenerate: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val formattedDate = remember(video.createdAt) { dateFormat.format(Date(video.createdAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onPlay)
            .testTag("history_card_${video.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Thumbnail / Banner area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
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

                // Play Button overlay
                if (video.status == VideoGeneration.STATUS_COMPLETED) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElectricViolet.copy(alpha = 0.85f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                // Status Badge (Top Start)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (video.status) {
                        VideoGeneration.STATUS_COMPLETED -> SuccessGreen.copy(alpha = 0.9f)
                        VideoGeneration.STATUS_FAILED -> ErrorRed.copy(alpha = 0.9f)
                        else -> ElectricViolet.copy(alpha = 0.9f)
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = video.status,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Favorite icon button (Top End)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (video.isFavorite) CyberPink else Color.White
                    )
                }

                // Duration & Resolution tags (Bottom End)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${video.durationLabel} • ${video.resolution}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Card Body Details
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = video.prompt,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Download
                        IconButton(onClick = onDownload, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Video",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Share
                        IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Regenerate
                        IconButton(onClick = onRegenerate, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                tint = ElectricViolet,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ErrorRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
