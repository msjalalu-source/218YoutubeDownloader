package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

private val HeaderRed = Color(0xFFB71C1C) // NewPipe Crimson Red Header
private val DialogDarkBackground = Color(0xFF2B2B2B)
private val DialogTextSecondary = Color(0xFFB0B0B0)

data class CaptionOption(
    val id: String,
    val languageName: String,
    val format: String = "VTT / SRT",
    val sizeEstimatedMb: Double = 0.08
)

val sampleCaptions = listOf(
    CaptionOption("bn_caption", "বাংলা সাবটাইটেল (Bengali)", "SRT"),
    CaptionOption("en_caption", "English Subtitles", "VTT"),
    CaptionOption("ar_caption", "Arabic (العربية)", "SRT")
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
    var showStreamSelectorMenu by remember { mutableStateOf(false) }
    var selectedCaption by remember { mutableStateOf(sampleCaptions.first()) }

    // Ensure initial default selections
    val currentVideo = selectedVideoOption
        ?: videoDetails.videoStreams.find { it.qualityLabel.contains("480p") || it.resolutionHeight == 480 }
        ?: videoDetails.videoStreams.find { it.qualityLabel.contains("720p") || it.resolutionHeight == 720 }
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
                .padding(horizontal = 24.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DialogDarkBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.testTag("download_dialog_back_button")
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
                                    fontSize = 20.sp,
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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Content Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 18.dp)
                    ) {
                        // File name label
                        Text(
                            text = "File name",
                            color = DialogTextSecondary,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // File name input field (underlined style matching screenshot)
                        val customTextSelectionColors = TextSelectionColors(
                            handleColor = HeaderRed,
                            backgroundColor = HeaderRed.copy(alpha = 0.4f)
                        )
                        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                            TextField(
                                value = fileName,
                                onValueChange = { fileName = it },
                                singleLine = false,
                                maxLines = 3,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = HeaderRed,
                                    unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.5f),
                                    cursorColor = HeaderRed
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("download_file_name_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Radio Button Group: Video | Audio | Captions
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
                                        unselectedColor = Color.LightGray
                                    )
                                )
                                Text(
                                    text = "Video",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedMediaType == 0) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

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
                                        unselectedColor = Color.LightGray
                                    )
                                )
                                Text(
                                    text = "Audio",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedMediaType == 1) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

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
                                        unselectedColor = Color.LightGray
                                    )
                                )
                                Text(
                                    text = "Captions",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedMediaType == 2) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stream / Quality Selection Dropdown Trigger
                        Surface(
                            onClick = { showStreamSelectorMenu = true },
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stream_quality_dropdown_trigger")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left details (Format and Quality / Resolution / Language)
                                Column(modifier = Modifier.weight(1f)) {
                                    when (selectedMediaType) {
                                        0 -> {
                                            Text(
                                                text = currentVideo?.format ?: "MPEG-4",
                                                color = DialogTextSecondary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = currentVideo?.qualityLabel ?: "480p SD",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                        1 -> {
                                            Text(
                                                text = currentAudio?.format ?: "M4A / AAC",
                                                color = DialogTextSecondary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = currentAudio?.languageName ?: "বাংলা (Bengali)",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = selectedCaption.format,
                                                color = DialogTextSecondary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = selectedCaption.languageName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                }

                                // Right details (File size and Dropdown arrow)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val sizeText = when (selectedMediaType) {
                                        0 -> "${currentVideo?.sizeEstimatedMb ?: 14.8} MB"
                                        1 -> "${currentAudio?.sizeEstimatedMb ?: 5.6} MB"
                                        else -> "${selectedCaption.sizeEstimatedMb} MB"
                                    }
                                    Text(
                                        text = sizeText,
                                        color = DialogTextSecondary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Quality",
                                        tint = DialogTextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Threads Slider
                        Text(
                            text = "Threads",
                            color = DialogTextSecondary,
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${threadCount.roundToInt()}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )
                            Slider(
                                value = threadCount,
                                onValueChange = { threadCount = it },
                                valueRange = 1f..8f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.LightGray,
                                    inactiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("download_threads_slider")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Centered Disclaimer / Helper note
                        Text(
                            text = "Streams which are not yet supported by the downloader are not shown",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal / Dialog for Stream Selection
    if (showStreamSelectorMenu) {
        AlertDialog(
            onDismissRequest = { showStreamSelectorMenu = false },
            containerColor = Color(0xFF222222),
            title = {
                Text(
                    text = when (selectedMediaType) {
                        0 -> "Select Video Resolution"
                        1 -> "Select Audio Track (বাংলা / হিন্দি / অরিজিনাল)"
                        else -> "Select Captions"
                    },
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedMediaType) {
                        0 -> {
                            videoDetails.videoStreams.forEach { option ->
                                val isSelected = currentVideo?.id == option.id
                                Surface(
                                    onClick = {
                                        onVideoOptionSelected(option)
                                        showStreamSelectorMenu = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) HeaderRed.copy(alpha = 0.25f) else Color(0xFF2E2E2E),
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = option.qualityLabel,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                if (option.resolutionHeight == 480 || option.qualityLabel.contains("480p")) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "(Default)",
                                                        color = HeaderRed,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
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
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            videoDetails.audioTracks.forEach { track ->
                                val isSelected = currentAudio?.id == track.id
                                Surface(
                                    onClick = {
                                        onAudioOptionSelected(track)
                                        showStreamSelectorMenu = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) HeaderRed.copy(alpha = 0.25f) else Color(0xFF2E2E2E),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = track.languageName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "${track.format} • ${track.bitrateKbps} kbps",
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
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            sampleCaptions.forEach { caption ->
                                val isSelected = selectedCaption.id == caption.id
                                Surface(
                                    onClick = {
                                        selectedCaption = caption
                                        showStreamSelectorMenu = false
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) HeaderRed.copy(alpha = 0.25f) else Color(0xFF2E2E2E),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = caption.languageName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStreamSelectorMenu = false }) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
