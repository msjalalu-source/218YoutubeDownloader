package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MediaEntity
import com.example.ui.components.MediaItemCard
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldCyan

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
    val filters = listOf("সব অফলাইন", "অডিও গান", "ভিডিও", "প্রিয় গান")

    val filteredList = when (selectedFilterIndex) {
        1 -> completedDownloads.filter { it.mediaType == "AUDIO" }
        2 -> completedDownloads.filter { it.mediaType == "VIDEO" }
        3 -> completedDownloads.filter { it.isFavorite }
        else -> completedDownloads
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // Active Downloads Section
        if (activeDownloads.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ডাউনলোড প্রক্রিয়াধীন (${activeDownloads.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldCyan
                    )
                }
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

        // Offline Library Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সংরক্ষিত অফলাইন মিডিয়া",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${completedDownloads.size}টি আইটেম",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Filter Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(filters.indices.toList()) { index ->
                    val isSelected = selectedFilterIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterIndex = index },
                        label = { Text(filters[index], fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonRed,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("download_filter_$index")
                    )
                }
            }
        }

        // Empty state or Completed items list
        if (completedDownloads.isEmpty() && activeDownloads.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো অফলাইন মিডিয়া ডাউনলোড নেই",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ইউটিউব বা সাউন্ডক্লাউড লিংক কপি করে অ্যাপে আসলেই স্বয়ংক্রিয় ডাউনলোডের অপশন আসবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToHome,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                        ) {
                            Text("হোম পেজে যান", color = Color.White, fontWeight = FontWeight.Bold)
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
                        text = "এই ফিল্টারে কোনো সংরক্ষিত মিডিয়া পাওয়া যায়নি",
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
