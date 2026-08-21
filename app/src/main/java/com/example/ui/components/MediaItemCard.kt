package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MediaEntity
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldCyan
import com.example.ui.theme.YouTubeRed
import com.example.utils.FileUtils
import java.io.File

@Composable
fun MediaItemCard(
    media: MediaEntity,
    onClickPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
    onPauseDownload: () -> Unit = {},
    onResumeDownload: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showFileInfoDialog by remember { mutableStateOf(false) }

    val isDownloading = media.downloadStatus == "DOWNLOADING"
    val isPaused = media.downloadStatus == "PAUSED"
    val isCompleted = media.downloadStatus == "COMPLETED"

    Surface(
        onClick = {
            if (!isDownloading && !isPaused) onClickPlay()
        },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("media_card_${media.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media Thumbnail with Duration
                Box(
                    modifier = Modifier
                        .size(width = 96.dp, height = 64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = media.thumbnailUrl,
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Duration Badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = media.durationFormatted,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    // Format Badge
                    Surface(
                        color = if (media.mediaType == "AUDIO") EmeraldCyan else CrimsonRed,
                        shape = RoundedCornerShape(bottomEnd = 6.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = if (media.mediaType == "AUDIO") "MP3" else "MP4",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Subtitle + Bangla Track
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = media.author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = EmeraldCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = media.selectedAudioLanguage.take(18),
                                fontSize = 9.sp,
                                color = EmeraldCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = media.selectedQuality,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Options Menu / Actions
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("সরাসরি প্লে করুন") },
                            leadingIcon = {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = YouTubeRed)
                            },
                            onClick = {
                                showMenu = false
                                onClickPlay()
                            }
                        )

                        if (isCompleted && !media.localFilePath.isNullOrBlank()) {
                            DropdownMenuItem(
                                text = { Text("ডিভাইস প্লেয়ার / ফাইলে খুলুন") },
                                leadingIcon = {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = EmeraldCyan)
                                },
                                onClick = {
                                    showMenu = false
                                    FileUtils.openWithExternalApp(
                                        context = context,
                                        filePath = media.localFilePath,
                                        isVideo = media.mediaType == "VIDEO"
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("ফাইল লোকেশন ও তথ্য") },
                                leadingIcon = {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    showFileInfoDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("ফাইল শেয়ার করুন") },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    FileUtils.shareFile(
                                        context = context,
                                        filePath = media.localFilePath,
                                        title = media.title,
                                        isVideo = media.mediaType == "VIDEO"
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text(if (media.isFavorite) "প্রিয় তালিকা থেকে মুছুন" else "প্রিয় তালিকায় যোগ করুন") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (media.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = CrimsonRed
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleFavorite()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("প্লেলিস্টে যুক্ত করুন") },
                            leadingIcon = {
                                Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onAddToPlaylist()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("মুছে ফেলুন", color = CrimsonRed) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonRed)
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Downloading Progress Bar & Speed Indicator
            if (isDownloading || isPaused) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { media.downloadProgress },
                        color = CrimsonRed,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPaused) "ডাউনলোড বিরতিতে রয়েছে" else "ডাউনলোড হচ্ছে: ${(media.downloadProgress * 100).toInt()}% (${media.downloadSpeedFormatted})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPaused) MaterialTheme.colorScheme.onSurfaceVariant else EmeraldCyan,
                            fontWeight = FontWeight.Medium
                        )

                        Row {
                            if (isDownloading) {
                                TextButton(onClick = onPauseDownload) {
                                    Text("বিরতি", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            } else {
                                TextButton(onClick = onResumeDownload) {
                                    Text("পুনরায় চালু", fontSize = 11.sp, color = EmeraldCyan)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // File Info Dialog
    if (showFileInfoDialog) {
        val path = media.localFilePath ?: ""
        val file = File(path)
        val exists = file.exists()
        val fileSize = if (exists) FileUtils.formatBytes(file.length()) else FileUtils.formatBytes(media.totalSizeBytes)

        AlertDialog(
            onDismissRequest = { showFileInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "ফাইল তথ্য ও পাথ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = media.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ফরম্যাট:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (media.mediaType == "AUDIO") "MP3 অডিও" else "MP4 ভিডিও", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ফাইলের সাইজ:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(fileSize, fontWeight = FontWeight.Bold, color = EmeraldCyan, style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("অডিও ট্র্যাক:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(media.selectedAudioLanguage, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("স্টোরেজ ফাইল পাথ:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = path,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("File Path", path))
                                        Toast.makeText(context, "পাথ কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("পাথ কপি করুন", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFileInfoDialog = false
                        FileUtils.openWithExternalApp(
                            context = context,
                            filePath = path,
                            isVideo = media.mediaType == "VIDEO"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ডিভাইস প্লেয়ারে খুলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFileInfoDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
