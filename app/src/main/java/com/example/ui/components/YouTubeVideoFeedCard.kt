package com.example.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.service.KnownBanglaMediaCatalogue
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeSurfaceDark
import com.example.ui.theme.YouTubeTextDark
import com.example.ui.theme.YouTubeTextSecondaryDark

@Composable
fun YouTubeVideoFeedCard(
    item: KnownBanglaMediaCatalogue.KnownMeta,
    onCardClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onPlayAudioClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("yt_video_card_${item.videoId}")
    ) {
        // 16:9 YouTube Thumbnail Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF1E1E1E))
        ) {
            AsyncImage(
                model = item.customThumb ?: "https://img.youtube.com/vi/${item.videoId}/maxresdefault.jpg",
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle gradient overlay at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )

            // Duration badge in bottom right (e.g. 04:34)
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = formatDuration(item.durationSeconds),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }

            // Top Left Badges: Bangla Default Audio & Default 480p SD
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = YouTubeRed.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "বাংলা অডিও ১ম",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "480p SD",
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Details Section (Avatar + Title + Channel Info + 3-dots Menu + Quick Download)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Channel Avatar Circle
            val avatarInitial = item.author.firstOrNull()?.toString() ?: "B"
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(YouTubeRed, Color(0xFF8A0000))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarInitial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.author} • ${item.views} • ${item.uploadDate}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Direct 1-Tap Quick Download Button
            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_quick_download_${item.videoId}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "Quick Download",
                    tint = YouTubeRed,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 3-dots Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("ডাউনলোড অপশন (NewPipe পপআপ)") },
                        leadingIcon = {
                            Icon(Icons.Default.Download, contentDescription = null, tint = YouTubeRed)
                        },
                        onClick = {
                            showMenu = false
                            onDownloadClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("সরাসরি ভিডিও চালান") },
                        leadingIcon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onCardClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("বাংলা অডিও শুনুন") },
                        leadingIcon = {
                            Icon(Icons.Default.Headphones, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                            onPlayAudioClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("লিঙ্ক কপি করুন") },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = null)
                        },
                        onClick = {
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
