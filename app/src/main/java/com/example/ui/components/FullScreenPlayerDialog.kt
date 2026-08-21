package com.example.ui.components

import android.net.Uri
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.MediaEntity
import com.example.data.service.MediaExtractorService
import com.example.player.AudioEqualizerPreset
import com.example.player.PlaybackState
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val FALLBACK_VIDEO_URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPlayerDialog(
    playbackState: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onAudioTrackChange: (String) -> Unit,
    onAudioPresetChange: (AudioEqualizerPreset) -> Unit,
    onSleepTimerChange: (Int) -> Unit,
    onToggleLoop: () -> Unit,
    onToggleShuffle: () -> Unit,
    onDismiss: () -> Unit
) {
    val media = playbackState.currentMedia ?: return
    var showTrackSelector by remember { mutableStateOf(false) }
    var showSpeedSelector by remember { mutableStateOf(false) }
    var showPresetSelector by remember { mutableStateOf(false) }
    var showTimerSelector by remember { mutableStateOf(false) }
    var isVideoBuffering by remember { mutableStateOf(true) }

    // Double tap feedback state
    var doubleTapSide by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_fullscreen_player")) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (media.mediaType == "VIDEO") "ভিডিও প্লেয়ার (MP4 HD)" else "অডিও ট্র্যাক প্লেয়ার (MP3)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = media.selectedQuality,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = YouTubeRed
                        )
                    }

                    IconButton(onClick = { showTrackSelector = true }, modifier = Modifier.testTag("audio_track_switch_btn")) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Audio Track",
                            tint = YouTubeRed
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Media Canvas / Video Player Screen with Double-Tap Gesture to Seek
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (media.mediaType == "VIDEO") {
                        // Real Video Player View
                        VideoPlayerView(
                            media = media,
                            isPlaying = playbackState.isPlaying,
                            isLooping = playbackState.isLooping,
                            onBufferingChanged = { isVideoBuffering = it },
                            onCompletion = {
                                if (playbackState.isLooping) {
                                    onSeekTo(0L)
                                } else {
                                    onNext()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isVideoBuffering) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = YouTubeRed,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ভিডিও লোড হচ্ছে...",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        // Audio Artwork & Animated Visualizer
                        AsyncImage(
                            model = media.thumbnailUrl,
                            contentDescription = "Audio Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                ),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (playbackState.isPlaying) {
                                AnimatedAudioWave(modifier = Modifier.padding(bottom = 20.dp))
                            } else {
                                Text(
                                    text = "অডিও প্লে হচ্ছে (${playbackState.audioPreset.title})",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 20.dp)
                                )
                            }
                        }
                    }

                    // Gesture Layer: Double tap left (-10s), Double tap right (+10s), Single tap (Play/Pause)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { offset ->
                                        if (offset.x < size.width / 2) {
                                            onSkipBackward()
                                            doubleTapSide = "left"
                                        } else {
                                            onSkipForward()
                                            doubleTapSide = "right"
                                        }
                                        scope.launch {
                                            delay(750)
                                            doubleTapSide = null
                                        }
                                    },
                                    onTap = {
                                        onTogglePlayPause()
                                    }
                                )
                            }
                    )

                    // Visual Feedback for Double-Tap Rewind/Forward
                    AnimatedVisibility(
                        visible = doubleTapSide != null,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut(),
                        modifier = Modifier.align(if (doubleTapSide == "left") Alignment.CenterStart else Alignment.CenterEnd)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.padding(24.dp).size(68.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (doubleTapSide == "left") Icons.Default.FastRewind else Icons.Default.FastForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = if (doubleTapSide == "left") "-১০ সে" else "+১০ সে",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Media Metadata & Track Language Pill
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = media.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Audio Track Tag
                    Surface(
                        color = YouTubeRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { showTrackSelector = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "অডিও: ${playbackState.selectedAudioTrackName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = YouTubeRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seek Bar & Timers
                Column(modifier = Modifier.fillMaxWidth()) {
                    val currentPos = playbackState.currentPositionMs
                    val totalDuration = playbackState.durationMs.coerceAtLeast(1000L)
                    val sliderValue = (currentPos.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

                    Slider(
                        value = sliderValue,
                        onValueChange = { frac ->
                            onSeekTo((frac * totalDuration).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = YouTubeRed,
                            activeTrackColor = YouTubeRed,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("player_seek_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = MediaExtractorService.formatSeconds(currentPos / 1000L),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = MediaExtractorService.formatSeconds(totalDuration / 1000L),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Primary Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playbackState.isShuffle) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onSkipBackward) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Replay 10s",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Main Play/Pause Button
                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = YouTubeRed),
                        modifier = Modifier
                            .size(62.dp)
                            .testTag("fullscreen_play_pause")
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    IconButton(onClick = onSkipForward) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onToggleLoop) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Loop",
                            tint = if (playbackState.isLooping) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Controls Row: Speed, Equalizer preset, Sleep Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = playbackState.playbackSpeed != 1.0f,
                        onClick = { showSpeedSelector = true },
                        label = { Text("${playbackState.playbackSpeed}x", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )

                    FilterChip(
                        selected = playbackState.audioPreset != AudioEqualizerPreset.NORMAL,
                        onClick = { showPresetSelector = true },
                        label = { Text(playbackState.audioPreset.title.take(10), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Equalizer, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )

                    FilterChip(
                        selected = playbackState.sleepTimerMinutesLeft > 0,
                        onClick = { showTimerSelector = true },
                        label = {
                            Text(
                                text = if (playbackState.sleepTimerMinutesLeft > 0) "${playbackState.sleepTimerMinutesLeft} মি" else "স্লিপ",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    // Audio Track Selector Dialog
    if (showTrackSelector) {
        val tracks = listOf(
            "বাংলা (Bengali - অডিও ট্র্যাক - ডিফল্ট)",
            "হিন্দি (Hindi - ডাবিং অডিও)",
            "অরিজিনাল (Original Track)",
            "বাংলা লাইট (128 kbps দ্রুত ডাউনলোড)"
        )
        AlertDialog(
            onDismissRequest = { showTrackSelector = false },
            title = { Text("অডিও ট্র্যাক ভাষা নির্বাচন") },
            text = {
                Column {
                    tracks.forEach { track ->
                        val isSelected = playbackState.selectedAudioTrackName == track
                        Surface(
                            onClick = {
                                onAudioTrackChange(track)
                                showTrackSelector = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) YouTubeRed.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onAudioTrackChange(track)
                                        showTrackSelector = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = YouTubeRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = track,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTrackSelector = false }) {
                    Text("ঠিক আছে", color = YouTubeRed)
                }
            }
        )
    }

    // Audio Preset / Equalizer Dialog
    if (showPresetSelector) {
        AlertDialog(
            onDismissRequest = { showPresetSelector = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = YouTubeRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("অডিও সাউন্ড ও ইকুয়ালাইজার", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AudioEqualizerPreset.values().forEach { preset ->
                        val isSelected = playbackState.audioPreset == preset
                        Surface(
                            onClick = {
                                onAudioPresetChange(preset)
                                showPresetSelector = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) YouTubeRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) BorderStroke(1.dp, YouTubeRed) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onAudioPresetChange(preset)
                                        showPresetSelector = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = YouTubeRed)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = preset.title,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) YouTubeRed else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = preset.desc,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetSelector = false }) {
                    Text("বন্ধ করুন", color = YouTubeRed)
                }
            }
        )
    }

    // Sleep Timer Dialog
    if (showTimerSelector) {
        val timerOptions = listOf(
            0 to "টাইমার বন্ধ (Off)",
            15 to "১৫ মিনিট পর বন্ধ",
            30 to "৩০ মিনিট পর বন্ধ",
            45 to "৪৫ মিনিট পর বন্ধ",
            60 to "১ ঘন্টা (৬০ মি) পর বন্ধ"
        )
        AlertDialog(
            onDismissRequest = { showTimerSelector = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = YouTubeRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("স্লিপ টাইমার", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    timerOptions.forEach { (mins, label) ->
                        val isSelected = playbackState.sleepTimerMinutesLeft == mins
                        Surface(
                            onClick = {
                                onSleepTimerChange(mins)
                                showTimerSelector = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) YouTubeRed.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onSleepTimerChange(mins)
                                        showTimerSelector = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = YouTubeRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerSelector = false }) {
                    Text("বন্ধ করুন", color = YouTubeRed)
                }
            }
        )
    }

    // Speed Selector Dialog
    if (showSpeedSelector) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedSelector = false },
            title = { Text("প্লেব্যাক গতি নির্বাচন") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    speeds.forEach { sp ->
                        FilterChip(
                            selected = playbackState.playbackSpeed == sp,
                            onClick = {
                                onSpeedChange(sp)
                                showSpeedSelector = false
                            },
                            label = { Text("${sp}x") }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedSelector = false }) {
                    Text("বন্ধ করুন", color = YouTubeRed)
                }
            }
        )
    }
}

@Composable
fun VideoPlayerView(
    media: MediaEntity,
    isPlaying: Boolean,
    isLooping: Boolean,
    onBufferingChanged: (Boolean) -> Unit,
    onCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoUri = remember(media) {
        val local = media.localFilePath
        if (!local.isNullOrBlank() && File(local).exists() && File(local).length() > 0) {
            Uri.fromFile(File(local))
        } else if (media.streamUrl.isNotBlank() && (media.streamUrl.startsWith("http://") || media.streamUrl.startsWith("https://"))) {
            Uri.parse(media.streamUrl)
        } else {
            Uri.parse(FALLBACK_VIDEO_URL)
        }
    }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    DisposableEffect(videoUri) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setVideoURI(videoUri)
                    setOnPreparedListener { mp ->
                        onBufferingChanged(false)
                        mp.isLooping = isLooping
                        if (isPlaying) {
                            start()
                        }
                    }
                    setOnErrorListener { _, _, _ ->
                        onBufferingChanged(false)
                        // Fallback to sample video if main stream had network error
                        try {
                            setVideoURI(Uri.parse(FALLBACK_VIDEO_URL))
                            start()
                        } catch (e: Exception) {
                            // ignore
                        }
                        true
                    }
                    setOnCompletionListener {
                        onBufferingChanged(false)
                        onCompletion()
                    }
                    videoViewRef = this
                }
            },
            update = { view ->
                videoViewRef = view
                try {
                    if (isPlaying) {
                        if (!view.isPlaying) view.start()
                    } else {
                        if (view.isPlaying) view.pause()
                    }
                } catch (e: Exception) {
                    // ignore
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AnimatedAudioWave(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val heights = (0..6).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 8f,
            targetValue = 32f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 400 + (index * 80), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { heightAnim ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(heightAnim.value.dp)
                    .background(YouTubeRed, CircleShape)
            )
        }
    }
}

