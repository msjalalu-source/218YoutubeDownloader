package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MediaEntity
import com.example.ui.components.MediaItemCard
import com.example.ui.theme.YouTubeRed

@Composable
fun DownloadsScreen(
    activeDownloads: List<MediaEntity>,
    completedDownloads: List<MediaEntity>,
    onPlayMedia: (MediaEntity, List<MediaEntity>) -> Unit,
    onToggleFavorite: (MediaEntity) -> Unit,
    onAddToPlaylist: (MediaEntity) -> Unit,
    onDeleteMedia: (String) -> Unit,
    onPauseDownload: (String) -> Unit,
    onResumeDownload: (MediaEntity) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableStateOf(0) }
    val filters = listOf("সবগুলো", "অডিও গান (MP3)", "ভিডিও (MP4)", "পছন্দনীয় গান")

    val filteredList = when (selectedFilterIndex) {
        1 -> completedDownloads.filter { it.mediaType == "AUDIO" }
        2 -> completedDownloads.filter { it.mediaType == "VIDEO" }
        3 -> completedDownloads.filter { it.isFavorite }
        else -> completedDownloads
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // YouTube-style Library Storage Overview Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(YouTubeRed.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DownloadDone,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ডাউনলোড ও অফলাইন লাইব্রেরি",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "কোনো ডেটা বা বিজ্ঞাপন ছাড়াই উপভোগ করুন",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${completedDownloads.size} আইটেম",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = YouTubeRed
                        )
                    }
                }
            }
        }

        // Active Downloads Section if any
        if (activeDownloads.isNotEmpty()) {
            item {
                Text(
                    text = "ডাউনলোড হচ্ছে (${activeDownloads.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = YouTubeRed,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            items(activeDownloads) { media ->
                MediaItemCard(
                    media = media,
                    onClickPlay = {},
                    onToggleFavorite = { onToggleFavorite(media) },
                    onAddToPlaylist = { onAddToPlaylist(media) },
                    onDelete = { onDeleteMedia(media.id) },
                    onPauseDownload = { onPauseDownload(media.id) },
                    onResumeDownload = { onResumeDownload(media) }
                )
            }
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(filters.indices.toList()) { index ->
                    val isSelected = selectedFilterIndex == index
                    Surface(
                        onClick = { selectedFilterIndex = index },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("download_filter_$index")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = filters[index],
                                color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Empty state or Completed items list
        if (completedDownloads.isEmpty() && activeDownloads.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VideoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো অফলাইন মিডিয়া ডাউনলোড নেই",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "হোম পেজের যেকোনো ভিডিওতে ডাউনলোড আইকনে চাপ দিন বা লিংক পেস্ট করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToHome,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ইউটিউব হোমে যান", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "এই ফিল্টারে কোনো অফলাইন মিডিয়া নেই",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredList) { media ->
                MediaItemCard(
                    media = media,
                    onClickPlay = { onPlayMedia(media, filteredList) },
                    onToggleFavorite = { onToggleFavorite(media) },
                    onAddToPlaylist = { onAddToPlaylist(media) },
                    onDelete = { onDeleteMedia(media.id) }
                )
            }
        }
    }
}
