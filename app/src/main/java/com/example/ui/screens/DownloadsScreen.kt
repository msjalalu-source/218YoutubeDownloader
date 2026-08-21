package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MediaEntity
import com.example.ui.components.MediaItemCard
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldCyan
import com.example.ui.theme.YouTubeRed
import com.example.utils.FileUtils
import java.io.File

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
    val context = LocalContext.current
    var selectedFilterIndex by remember { mutableStateOf(0) }
    val filters = listOf("সবগুলো", "ভিডিও (MP4)", "অডিও গান (MP3)", "📁 ফাইল ম্যানেজার", "পছন্দনীয়")

    val totalStorageBytes = remember(completedDownloads) {
        completedDownloads.sumOf { media ->
            val file = media.localFilePath?.let { File(it) }
            if (file != null && file.exists()) file.length().coerceAtLeast(media.totalSizeBytes)
            else media.totalSizeBytes
        }
    }

    val folderDir = remember { FileUtils.getDownloadFolder(context).absolutePath }

    val filteredList = when (selectedFilterIndex) {
        1 -> completedDownloads.filter { it.mediaType == "VIDEO" }
        2 -> completedDownloads.filter { it.mediaType == "AUDIO" }
        3 -> completedDownloads // Handled specially in UI for File Manager view
        4 -> completedDownloads.filter { it.isFavorite }
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
        // Storage & Library Header Card
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
                                    .size(38.dp)
                                    .background(YouTubeRed.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ডাউনলোড ও ফাইল স্টোরেজ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "মোট সাইজ: ${FileUtils.formatBytes(totalStorageBytes)} • ${completedDownloads.size} টি মিডিয়া ফাইল",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = YouTubeRed.copy(alpha = 0.15f),
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Folder Path", folderDir))
                                Toast.makeText(context, "ফোল্ডার পাথ কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পাথ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = YouTubeRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = EmeraldCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = folderDir,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
        } else if (selectedFilterIndex == 3) {
            // File Manager View: Shows physical files on disk with direct play, open in external app, share, and delete
            item {
                Text(
                    text = "ডিভাইসের সংরক্ষিত ফাইলসমূহ (${completedDownloads.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            items(completedDownloads) { media ->
                val filePath = media.localFilePath ?: ""
                val file = File(filePath)
                val isVideo = media.mediaType == "VIDEO"
                val displaySize = if (file.exists() && file.length() > 0) {
                    FileUtils.formatBytes(file.length())
                } else {
                    FileUtils.formatBytes(media.totalSizeBytes)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().clickable {
                        onPlayMedia(media, completedDownloads)
                    }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(if (isVideo) YouTubeRed.copy(alpha = 0.15f) else EmeraldCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isVideo) Icons.Default.VideoFile else Icons.Default.AudioFile,
                                    contentDescription = null,
                                    tint = if (isVideo) YouTubeRed else EmeraldCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = media.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (isVideo) YouTubeRed.copy(alpha = 0.2f) else EmeraldCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (isVideo) "MP4 • ${media.selectedQuality}" else "MP3 • ${media.selectedAudioLanguage.take(12)}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isVideo) YouTubeRed else EmeraldCyan,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = displaySize,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onPlayMedia(media, completedDownloads) }
                            ) {
                                Icon(Icons.Default.PlayCircleFilled, contentDescription = "Play", tint = YouTubeRed, modifier = Modifier.size(34.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Path row
                        Text(
                            text = filePath,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Path", filePath))
                                    Toast.makeText(context, "পাথ কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পাথ কপি", fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            OutlinedButton(
                                onClick = {
                                    FileUtils.shareFile(context, filePath, media.title, isVideo)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("শেয়ার", fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = {
                                    FileUtils.openWithExternalApp(context, filePath, isVideo)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isVideo) YouTubeRed else EmeraldCyan),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ওপেন ফাইল", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
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
