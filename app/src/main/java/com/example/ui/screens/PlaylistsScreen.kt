package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MediaEntity
import com.example.data.local.PlaylistEntity
import com.example.ui.components.MediaItemCard
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldCyan

@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistEntity>,
    activePlaylistDetail: PlaylistEntity?,
    playlistItems: List<MediaEntity>,
    onOpenCreateDialog: () -> Unit,
    onSelectPlaylist: (PlaylistEntity) -> Unit,
    onClosePlaylistDetail: () -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlayAllInPlaylist: (List<MediaEntity>) -> Unit,
    onPlayMedia: (MediaEntity, List<MediaEntity>) -> Unit,
    onRemoveMediaFromPlaylist: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePlaylistDetail != null) {
        // Playlist Detail View
        PlaylistDetailContent(
            playlist = activePlaylistDetail,
            items = playlistItems,
            onBack = onClosePlaylistDetail,
            onPlayAll = { onPlayAllInPlaylist(playlistItems) },
            onPlayItem = { media -> onPlayMedia(media, playlistItems) },
            onRemoveItem = { mediaId -> onRemoveMediaFromPlaylist(activePlaylistDetail.id, mediaId) },
            modifier = modifier
        )
    } else {
        // Playlists Overview List
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
        ) {
            // Header: Title & Create New Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "আমার প্লেলিস্টসমূহ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "অফলাইন গান ও ভিডিও কালেকশন সাজান",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onOpenCreateDialog,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        modifier = Modifier.testTag("create_playlist_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন প্লেলিস্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Playlist Cards
            items(playlists) { pl ->
                Surface(
                    onClick = { onSelectPlaylist(pl) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_card_${pl.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(pl.colorHex).copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (pl.iconTag) {
                                    "download_done" -> Icons.Default.DownloadDone
                                    "podcasts" -> Icons.Default.Podcasts
                                    else -> Icons.Default.MusicNote
                                },
                                contentDescription = null,
                                tint = Color(pl.colorHex),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pl.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (pl.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = pl.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "প্লেলিস্ট খুলতে ট্যাপ করুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldCyan
                            )
                        }

                        IconButton(onClick = { onDeletePlaylist(pl.id) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Playlist",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistDetailContent(
    playlist: PlaylistEntity,
    items: List<MediaEntity>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onPlayItem: (MediaEntity) -> Unit,
    onRemoveItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${items.size}টি ট্র্যাক • অফলাইন লিসেনিং",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldCyan
                    )
                }
            }
        }

        if (items.isNotEmpty()) {
            item {
                Button(
                    onClick = onPlayAll,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "সবগুলো একসাথে চালান", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (items.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "এই প্লেলিস্টে এখনও কোনো গান বা ভিডিও যুক্ত করা হয়নি।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "অফলাইন ডাউনলোড বা হোম স্ক্রিন থেকে ৩-ডট মেনু ব্যবহার করে এখানে গান যুক্ত করুন।",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(items) { media ->
                MediaItemCard(
                    media = media,
                    onClickPlay = { onPlayItem(media) },
                    onToggleFavorite = {},
                    onAddToPlaylist = {},
                    onDelete = { onRemoveItem(media.id) }
                )
            }
        }
    }
}
