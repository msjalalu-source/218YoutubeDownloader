package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
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
import com.example.data.model.AudioTrackOption
import com.example.data.model.PlatformType
import com.example.data.model.VideoDetails
import com.example.data.model.VideoStreamOption
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickDownloadBottomSheet(
    videoDetails: VideoDetails,
    selectedTab: Int, // 0: Video, 1: Audio
    selectedVideoOption: VideoStreamOption?,
    selectedAudioOption: AudioTrackOption?,
    onTabSelected: (Int) -> Unit,
    onVideoOptionSelected: (VideoStreamOption) -> Unit,
    onAudioOptionSelected: (AudioTrackOption) -> Unit,
    onStartDownload: (isAudioOnly: Boolean) -> Unit,
    onPlayDirectStream: (isAudioOnly: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline)
        },
        modifier = Modifier.testTag("quick_download_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Detected Link & Platform
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(EmeraldCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সরাসরি লিঙ্ক স্ক্র্যাপার প্রস্তুত",
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = Color(videoDetails.sourcePlatform.badgeColorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(videoDetails.sourcePlatform.badgeColorHex).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = videoDetails.sourcePlatform.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(videoDetails.sourcePlatform.badgeColorHex),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Media Info Card (Thumbnail + Title + Channel + Duration)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 110.dp, height = 72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = videoDetails.thumbnailUrl,
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Duration badge
                        Surface(
                            color = Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = videoDetails.durationFormatted,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = videoDetails.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = videoDetails.author,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Selector: Video Resolutions vs Audio Tracks
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .height(44.dp),
                indicator = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    modifier = Modifier
                        .background(if (selectedTab == 0) CrimsonRed else Color.Transparent)
                        .testTag("tab_video_resolutions")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ভিডিও অপশন (${videoDetails.videoStreams.size})",
                            color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }

                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    modifier = Modifier
                        .background(if (selectedTab == 1) CrimsonRed else Color.Transparent)
                        .testTag("tab_audio_tracks")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "বাংলা ও অন্যান্য অডিও",
                            color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Options List (Video or Audio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                if (selectedTab == 0) {
                    // Video Resolutions
                    if (videoDetails.videoStreams.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "এই লিংকে শুধুমাত্র সরাসরি অডিও স্ট্রিম উপলভ্য রয়েছে।",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(videoDetails.videoStreams) { option ->
                                val isSelected = selectedVideoOption?.id == option.id
                                Surface(
                                    onClick = { onVideoOptionSelected(option) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CrimsonRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CrimsonRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("video_option_${option.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onVideoOptionSelected(option) },
                                                colors = RadioButtonDefaults.colors(selectedColor = CrimsonRed)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = option.qualityLabel,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (option.resolutionHeight == 480 || option.qualityLabel.contains("480p")) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            color = CrimsonRed.copy(alpha = 0.15f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "ডিফল্ট 480p",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = CrimsonRed,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    if (option.isHd) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            color = AmberGold.copy(alpha = 0.2f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "HD",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = AmberGold,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "${option.format} • ${option.fps}fps",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Text(
                                            text = "~${option.sizeEstimatedMb} MB",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Audio Tracks (Multi-Language with Bangla Priority Default)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(videoDetails.audioTracks) { track ->
                            val isSelected = selectedAudioOption?.id == track.id
                            val isBangla = track.languageCode.startsWith("bn")

                            Surface(
                                onClick = { onAudioOptionSelected(track) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    if (isBangla) EmeraldCyan.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.12f)
                                } else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) {
                                        if (isBangla) EmeraldCyan else CrimsonRed
                                    } else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("audio_track_${track.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onAudioOptionSelected(track) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = if (isBangla) EmeraldCyan else CrimsonRed
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = track.languageName,
                                                    fontWeight = if (isBangla) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isBangla && isSelected) EmeraldCyan else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isBangla) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = CrimsonRed.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "১ম ডিফল্ট বাংলা",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = CrimsonRed,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                } else if (track.languageCode == "hi") {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = AmberGold.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "২য় হিন্দি",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AmberGold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                } else if (track.languageCode == "orig" || track.languageCode == "en") {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "৩য় অরিজিনাল",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${track.format} • ${track.bitrateKbps} kbps",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "~${track.sizeEstimatedMb} MB",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Stream Directly or Download
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play directly button
                OutlinedButton(
                    onClick = { onPlayDirectStream(selectedTab == 1) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EmeraldCyan),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldCyan),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("stream_direct_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "সরাসরি শুনুন", fontWeight = FontWeight.Bold)
                }

                // Download button
                Button(
                    onClick = { onStartDownload(selectedTab == 1) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("download_now_button")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedTab == 0) "ভিডিও ডাউনলোড" else "বাংলা অডিও ডাউনলোড",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
