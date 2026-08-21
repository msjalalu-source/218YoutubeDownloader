package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AudioTrackOption
import com.example.data.model.VideoDetails
import com.example.data.model.VideoStreamOption
import kotlin.math.roundToInt

private val HeaderRed = Color(0xFFC4302B) // NewPipe Crimson Red Header
private val DialogDarkBackground = Color(0xFF383838) // Authentic NewPipe Dark Gray
private val DialogTextSecondary = Color(0xFFA8A8A8)
private val DropdownButtonBg = Color(0xFF4A4A4A)

data class CaptionOption(
    val id: String,
    val languageName: String,
    val languageCode: String = "en.1",
    val format: String = "VTT / SRT",
    val sizeEstimatedMb: Double = 0.08
)

val sampleCaptions = listOf(
    CaptionOption("bn_caption", "বাংলা সাবটাইটেল (Bengali)", "bn.1", "SRT", 0.05),
    CaptionOption("en_caption", "English Subtitles", "en.2", "VTT", 0.08),
    CaptionOption("ar_caption", "Arabic (العربية)", "ar.3", "SRT", 0.06)
)

@Composable
fun QuickDownloadBottomSheet(
    videoDetails: VideoDetails,
    selectedTab: Int, // 0: Video, 1: Audio, 2: Captions
    selectedVideoOption: VideoStreamOption?,
    selectedAudioOption: AudioTrackOption?,
    onTabSelected: (Int) -> Unit,
    onVideoOptionSelected: (VideoStreamOption) -> Unit,
    onAudioOptionSelected: (AudioTrackOption) -> Unit,
    onStartDownload: (isAudioOnly: Boolean, customTitle: String, threads: Int) -> Unit,
    onPlayDirectStream: (isAudioOnly: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember(videoDetails.title) { mutableStateOf(videoDetails.title) }
    var selectedMediaType by remember(selectedTab) { mutableIntStateOf(selectedTab) } // 0: Video, 1: Audio, 2: Captions
    var threadCount by remember { mutableFloatStateOf(3f) }
    var showVideoResolutionMenu by remember { mutableStateOf(false) }
    var showAudioTrackMenu by remember { mutableStateOf(false) }
    var selectedCaption by remember { mutableStateOf(sampleCaptions.first()) }

    // Ensure initial default selections
    val currentVideo = selectedVideoOption
        ?: videoDetails.videoStreams.find { it.qualityLabel.contains("720p") || it.resolutionHeight == 720 }
        ?: videoDetails.videoStreams.find { it.qualityLabel.contains("480p") || it.resolutionHeight == 480 }
        ?: videoDetails.videoStreams.firstOrNull()

    val currentAudio = selectedAudioOption
        ?: videoDetails.audioTracks.find { it.isDefaultSelected }
        ?: videoDetails.audioTracks.find { it.languageCode.startsWith("bn") }
        ?: videoDetails.audioTracks.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = DialogDarkBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("newpipe_download_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Bar (Red background with Arrow Back, "Download", "OKAY")
                    Surface(
                        color = HeaderRed,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("download_dialog_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Download",
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            TextButton(
                                onClick = {
                                    val isAudio = selectedMediaType == 1
                                    onStartDownload(isAudio, fileName, threadCount.roundToInt())
                                },
                                modifier = Modifier.testTag("download_dialog_okay_button")
                            ) {
                                Text(
                                    text = "OKAY",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Content Area (Exact NewPipe layout)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        // 1. File name label
                        Text(
                            text = "File name",
                            color = DialogTextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. File name input field with underline
                        val customTextSelectionColors = TextSelectionColors(
                            handleColor = Color.White,
                            backgroundColor = Color.White.copy(alpha = 0.3f)
                        )
                        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                            TextField(
                                value = fileName,
                                onValueChange = { fileName = it },
                                singleLine = false,
                                maxLines = 3,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.White,
                                    unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.6f),
                                    cursorColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("download_file_name_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Radio Button Group: Video | Audio | Captions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Video Radio
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        selectedMediaType = 0
                                        onTabSelected(0)
                                    }
                                    .testTag("radio_media_video")
                            ) {
                                RadioButton(
                                    selected = selectedMediaType == 0,
                                    onClick = {
                                        selectedMediaType = 0
                                        onTabSelected(0)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.White,
                                        unselectedColor = DialogTextSecondary
                                    )
                                )
                                Text(
                                    text = "Video",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Audio Radio
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        selectedMediaType = 1
                                        onTabSelected(1)
                                    }
                                    .testTag("radio_media_audio")
                            ) {
                                RadioButton(
                                    selected = selectedMediaType == 1,
                                    onClick = {
                                        selectedMediaType = 1
                                        onTabSelected(1)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.White,
                                        unselectedColor = DialogTextSecondary
                                    )
                                )
                                Text(
                                    text = "Audio",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Captions Radio
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        selectedMediaType = 2
                                        onTabSelected(2)
                                    }
                                    .testTag("radio_media_captions")
                            ) {
                                RadioButton(
                                    selected = selectedMediaType == 2,
                                    onClick = {
                                        selectedMediaType = 2
                                        onTabSelected(2)
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.White,
                                        unselectedColor = DialogTextSecondary
                                    )
                                )
                                Text(
                                    text = "Captions",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Primary Stream Selection Row (Video Resolution or Audio Format or Captions)
                        Surface(
                            onClick = {
                                when (selectedMediaType) {
                                    0 -> showVideoResolutionMenu = true
                                    1 -> showAudioTrackMenu = true
                                    else -> showVideoResolutionMenu = true
                                }
                            },
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stream_quality_dropdown_trigger")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    when (selectedMediaType) {
                                        0 -> {
                                            Text(
                                                text = currentVideo?.format ?: "MPEG-4",
                                                color = DialogTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = currentVideo?.qualityLabel ?: "720p",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        1 -> {
                                            Text(
                                                text = currentAudio?.format ?: "M4A / AAC",
                                                color = DialogTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "${currentAudio?.bitrateKbps ?: 128} kbps",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = selectedCaption.format,
                                                color = DialogTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = selectedCaption.languageName,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val sizeText = when (selectedMediaType) {
                                        0 -> "${currentVideo?.sizeEstimatedMb ?: 125.23} MB"
                                        1 -> "${currentAudio?.sizeEstimatedMb ?: 5.6} MB"
                                        else -> "${selectedCaption.sizeEstimatedMb} MB"
                                    }
                                    Text(
                                        text = sizeText,
                                        color = DialogTextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    // Circular dropdown arrow pill like in screenshot
                                    Surface(
                                        shape = CircleShape,
                                        color = DropdownButtonBg,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Select Option",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Secondary Stream Row (Audio Track selection for Video, e.g. en.4 English original or bn.1 Bangla)
                        if (selectedMediaType == 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                onClick = { showAudioTrackMenu = true },
                                color = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("audio_track_dropdown_trigger")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentAudio?.languageCode ?: "en.4",
                                            color = DialogTextSecondary,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = currentAudio?.languageName ?: "English original",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Audio Track",
                                        tint = DialogTextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 6. Threads Slider Section
                        Text(
                            text = "Threads",
                            color = DialogTextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${threadCount.roundToInt()}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.width(24.dp)
                            )
                            Slider(
                                value = threadCount,
                                onValueChange = { threadCount = it },
                                valueRange = 1f..8f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color(0xFF666666)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("download_threads_slider")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 7. Footer disclaimer
                        Text(
                            text = "Streams which are not yet supported by the downloader are not shown",
                            color = DialogTextSecondary,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal / Dialog for Video Resolution Selection
    if (showVideoResolutionMenu) {
        AlertDialog(
            onDismissRequest = { showVideoResolutionMenu = false },
            containerColor = Color(0xFF2E2E2E),
            title = {
                Text(
                    text = "Select Video Resolution",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    videoDetails.videoStreams.forEach { option ->
                        val isSelected = currentVideo?.id == option.id
                        Surface(
                            onClick = {
                                onVideoOptionSelected(option)
                                showVideoResolutionMenu = false
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) HeaderRed.copy(alpha = 0.35f) else Color(0xFF383838),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = option.qualityLabel,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${option.format} • ${option.fps}fps",
                                        color = DialogTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${option.sizeEstimatedMb} MB",
                                        color = DialogTextSecondary,
                                        fontSize = 12.sp
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVideoResolutionMenu = false }) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    // Modal / Dialog for Audio Track Language Selection
    if (showAudioTrackMenu) {
        AlertDialog(
            onDismissRequest = { showAudioTrackMenu = false },
            containerColor = Color(0xFF2E2E2E),
            title = {
                Text(
                    text = "Select Audio Track Language",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    videoDetails.audioTracks.forEach { track ->
                        val isSelected = currentAudio?.id == track.id
                        Surface(
                            onClick = {
                                onAudioOptionSelected(track)
                                showAudioTrackMenu = false
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) HeaderRed.copy(alpha = 0.35f) else Color(0xFF383838),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = track.languageName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${track.languageCode} • ${track.bitrateKbps} kbps • ${track.format}",
                                        color = DialogTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${track.sizeEstimatedMb} MB",
                                        color = DialogTextSecondary,
                                        fontSize = 12.sp
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioTrackMenu = false }) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}
