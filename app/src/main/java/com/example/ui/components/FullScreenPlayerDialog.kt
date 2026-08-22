package com.example.ui.components

import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.example.data.service.KnownBanglaMediaCatalogue
import com.example.data.service.MediaExtractorService
import com.example.player.AudioEqualizerPreset
import com.example.player.PlaybackState
import com.example.recommendation.MLRecommendationEngine
import com.example.recommendation.ScoredMedia
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
    onPlayMediaItem: ((videoId: String, title: String) -> Unit)? = null,
    onToggleSubscribe: ((channelName: String) -> Unit)? = null,
    onToggleLike: ((videoId: String) -> Unit)? = null,
    onRecordSkip: ((videoId: String) -> Unit)? = null,
    onRecordComment: ((videoId: String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val media = playbackState.currentMedia ?: return
    var showTrackSelector by remember { mutableStateOf(false) }
    var showSpeedSelector by remember { mutableStateOf(false) }
    var showPresetSelector by remember { mutableStateOf(false) }
    var showTimerSelector by remember { mutableStateOf(false) }
    var showCommentSheet by remember { mutableStateOf(false) }
    var isVideoBuffering by remember { mutableStateOf(true) }

    val rawVideoId = media.id.removePrefix("stream_")
    val interactionProfile by MLRecommendationEngine.interactionProfile.collectAsState()
    val channels by MLRecommendationEngine.channels.collectAsState()

    val isSubscribed = remember(channels, media.author) {
        channels.find { it.name.equals(media.author, ignoreCase = true) }?.isSubscribed ?: false
    }
    val isLiked = remember(interactionProfile, rawVideoId) {
        interactionProfile.likedVideoIds.contains(rawVideoId)
    }

    // Dynamic suggested videos calculated by ML engine
    val suggestedVideos: List<ScoredMedia> = remember(rawVideoId, interactionProfile, media.author) {
        val foundMeta = KnownBanglaMediaCatalogue.sampleTrending.find { it.videoId == rawVideoId }
        val category = foundMeta?.category ?: "bangla_hits"
        MLRecommendationEngine.getSuggestedVideosForPlayer(
            currentVideoId = rawVideoId,
            currentCategory = category,
            currentAuthor = media.author
        )
    }

    // Double tap feedback state
    var doubleTapSide by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Periodically record watch time in ML Engine
    LaunchedEffect(playbackState.isPlaying, playbackState.currentPositionMs) {
        if (playbackState.isPlaying && playbackState.currentPositionMs > 1000L) {
            val watchedSec = playbackState.currentPositionMs / 1000L
            val totalSec = (playbackState.durationMs / 1000L).coerceAtLeast(1L)
            val foundMeta = KnownBanglaMediaCatalogue.sampleTrending.find { it.videoId == rawVideoId }
            val cat = foundMeta?.category ?: "bangla_hits"
            MLRecommendationEngine.recordWatchProgress(
                videoId = rawVideoId,
                category = cat,
                author = media.author,
                durationSeconds = watchedSec,
                totalDurationSeconds = totalSec
            )
        }
    }

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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_fullscreen_player")) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize",
                            modifier = Modifier.size(30.dp),
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Media Canvas / Video Player Screen with Double-Tap Gesture to Seek
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
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
                                            modifier = Modifier.size(40.dp)
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
                        if (doubleTapSide != null) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .align(if (doubleTapSide == "left") Alignment.CenterStart else Alignment.CenterEnd)
                                    .padding(24.dp)
                                    .size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (doubleTapSide == "left") Icons.Default.Replay10 else Icons.Default.Forward10,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            text = if (doubleTapSide == "left") "-10s" else "+10s",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Video Title & Channel / Subscriptions Bar
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Creator Channel Bar with Subscribe Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(YouTubeRed.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = media.author.firstOrNull()?.toString() ?: "C",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = YouTubeRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = media.author,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "১.৫M সাবস্ক্রাইবার",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // YouTube-style Subscribe / Subscribed Button
                                Button(
                                    onClick = {
                                        onToggleSubscribe?.invoke(media.author)
                                            ?: MLRecommendationEngine.toggleSubscription(media.author)
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSubscribed) MaterialTheme.colorScheme.surfaceVariant else Color.White,
                                        contentColor = if (isSubscribed) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    if (isSubscribed) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = YouTubeRed
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("সাবস্ক্রাইবড", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    } else {
                                        Text("সাবস্ক্রাইব", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. YouTube Action Row: Like, Dislike/Skip, Comments, Audio Track
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like Button (Active state trains ML)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isLiked) YouTubeRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isLiked) BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.5f)) else null,
                            modifier = Modifier.clickable {
                                onToggleLike?.invoke(rawVideoId) ?: MLRecommendationEngine.toggleLike(rawVideoId)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (isLiked) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isLiked) "লাইকড (AI)" else "লাইক",
                                    fontSize = 12.sp,
                                    fontWeight = if (isLiked) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isLiked) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Dislike / Skip Button (Negative signal in ML)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                onRecordSkip?.invoke(rawVideoId) ?: MLRecommendationEngine.recordSkipOrDislike(rawVideoId)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ThumbDown,
                                    contentDescription = "Dislike / Skip",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("স্কিপ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Comment Button (Opens Comment Sheet)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { showCommentSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Comment,
                                    contentDescription = "Comments",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("মন্তব্য", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Audio Track Language Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = YouTubeRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { showTrackSelector = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = playbackState.selectedAudioTrackName.take(8) + "..",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = YouTubeRed
                                )
                            }
                        }
                    }
                }

                // 4. Seek Bar & Timers
                item {
                    Spacer(modifier = Modifier.height(10.dp))
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_seek_slider")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
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
                }

                // 5. Primary Playback Controls
                item {
                    Spacer(modifier = Modifier.height(4.dp))
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
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = onSkipBackward) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Replay 10s",
                                modifier = Modifier.size(26.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Main Play/Pause Button
                        FilledIconButton(
                            onClick = onTogglePlayPause,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = YouTubeRed),
                            modifier = Modifier
                                .size(58.dp)
                                .testTag("fullscreen_play_pause")
                        ) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(onClick = onSkipForward) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                modifier = Modifier.size(26.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = onNext) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                modifier = Modifier.size(30.dp),
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
                }

                // 6. Equalizer, Speed, and Sleep Filter Chips
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = playbackState.playbackSpeed != 1.0f,
                            onClick = { showSpeedSelector = true },
                            label = { Text("${playbackState.playbackSpeed}x", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(13.dp)) }
                        )

                        FilterChip(
                            selected = playbackState.audioPreset != AudioEqualizerPreset.NORMAL,
                            onClick = { showPresetSelector = true },
                            label = { Text(playbackState.audioPreset.title.take(10), fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Equalizer, contentDescription = null, modifier = Modifier.size(13.dp)) }
                        )

                        FilterChip(
                            selected = playbackState.sleepTimerMinutesLeft > 0,
                            onClick = { showTimerSelector = true },
                            label = {
                                Text(
                                    text = if (playbackState.sleepTimerMinutesLeft > 0) "${playbackState.sleepTimerMinutesLeft} মি" else "স্লিপ",
                                    fontSize = 11.sp
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(13.dp)) }
                        )
                    }
                }

                // 7. ML-Based "Suggested Videos" (অ্যালগরিদম মেশিন লার্নিং রিকমেন্ডেশন)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "সাজেস্টেড ভিডিও (ML অ্যালগরিদম)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = YouTubeRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "আপনার জন্য বাছাইকৃত",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = YouTubeRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Suggested Videos List
                items(suggestedVideos, key = { it.meta.videoId }) { suggestedItem ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onPlayMediaItem?.invoke(suggestedItem.meta.videoId, suggestedItem.meta.title)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(width = 110.dp, height = 65.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = suggestedItem.meta.customThumb ?: "https://img.youtube.com/vi/${suggestedItem.meta.videoId}/hqdefault.jpg",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = MediaExtractorService.formatSeconds(suggestedItem.meta.durationSeconds),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = suggestedItem.meta.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${suggestedItem.meta.author} • ${suggestedItem.meta.views}",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                // ML Reason Badge
                                Text(
                                    text = "✨ ${suggestedItem.recommendationReason}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = YouTubeRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Comment Sheet
    if (showCommentSheet) {
        YouTubeCommentSheet(
            videoTitle = media.title,
            onDismiss = { showCommentSheet = false },
            onCommentSubmitted = { text ->
                onRecordComment?.invoke(rawVideoId) ?: MLRecommendationEngine.recordComment(rawVideoId)
            }
        )
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = track,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = YouTubeRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTrackSelector = false }) {
                    Text("বন্ধ করুন", color = YouTubeRed)
                }
            }
        )
    }

    // Speed Selector Dialog
    if (showSpeedSelector) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedSelector = false },
            title = { Text("প্লেব্যাক গতি (Playback Speed)") },
            text = {
                Column {
                    speeds.forEach { speed ->
                        val isSelected = playbackState.playbackSpeed == speed
                        Surface(
                            onClick = {
                                onSpeedChange(speed)
                                showSpeedSelector = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) YouTubeRed.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (speed == 1.0f) "1.0x (স্বাভাবিক)" else "${speed}x",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedSelector = false }) {
                    Text("ঠিক আছে", color = YouTubeRed)
                }
            }
        )
    }

    // Audio Equalizer Preset Selector Dialog
    if (showPresetSelector) {
        AlertDialog(
            onDismissRequest = { showPresetSelector = false },
            title = { Text("বাংলা অডিও সাউন্ড ইকুইলাইজার") },
            text = {
                Column {
                    AudioEqualizerPreset.values().forEach { preset ->
                        val isSelected = playbackState.audioPreset == preset
                        Surface(
                            onClick = {
                                onAudioPresetChange(preset)
                                showPresetSelector = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) YouTubeRed.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = preset.title,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "বেস ও ভয়েস অপ্টিমাইজড",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetSelector = false }) {
                    Text("ঠিক আছে", color = YouTubeRed)
                }
            }
        )
    }

    // Sleep Timer Selector Dialog
    if (showTimerSelector) {
        val timerOptions = listOf(0 to "স্লিপ টাইমার বন্ধ", 15 to "১৫ মিনিট", 30 to "৩০ মিনিট", 45 to "৪৫ মিনিট", 60 to "১ ঘন্টা")
        AlertDialog(
            onDismissRequest = { showTimerSelector = false },
            title = { Text("স্লিপ টাইমার সেট করুন") },
            text = {
                Column {
                    timerOptions.forEach { (mins, label) ->
                        val isSelected = playbackState.sleepTimerMinutesLeft == mins
                        Surface(
                            onClick = {
                                onSleepTimerChange(mins)
                                showTimerSelector = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) YouTubeRed.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) YouTubeRed else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerSelector = false }) {
                    Text("ঠিক আছে", color = YouTubeRed)
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
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var surfaceRef by remember { mutableStateOf<Surface?>(null) }

    val videoUri = remember(media) {
        if (media.localFilePath != null && File(media.localFilePath).exists()) {
            Uri.fromFile(File(media.localFilePath))
        } else if (media.streamUrl.startsWith("http://") || media.streamUrl.startsWith("https://")) {
            Uri.parse(media.streamUrl)
        } else {
            Uri.parse(FALLBACK_VIDEO_URL)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayerRef?.stop()
                mediaPlayerRef?.release()
                mediaPlayerRef = null
                surfaceRef?.release()
                surfaceRef = null
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                        val surface = Surface(st)
                        surfaceRef = surface
                        try {
                            val mp = MediaPlayer().apply {
                                setSurface(surface)
                                setDataSource(ctx, videoUri)
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                                )
                                setOnPreparedListener { player ->
                                    onBufferingChanged(false)
                                    player.isLooping = isLooping
                                    if (isPlaying) {
                                        player.start()
                                    }
                                }
                                setOnErrorListener { _, _, _ ->
                                    onBufferingChanged(false)
                                    true
                                }
                                setOnCompletionListener {
                                    onBufferingChanged(false)
                                    onCompletion()
                                }
                                prepareAsync()
                            }
                            mediaPlayerRef = mp
                        } catch (e: Exception) {
                            onBufferingChanged(false)
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}
                    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                        try {
                            mediaPlayerRef?.stop()
                            mediaPlayerRef?.release()
                            mediaPlayerRef = null
                            surfaceRef?.release()
                            surfaceRef = null
                        } catch (e: Exception) {
                            // ignore
                        }
                        return true
                    }
                    override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                }
            }
        },
        update = {
            try {
                mediaPlayerRef?.let { mp ->
                    if (isPlaying && !mp.isPlaying) {
                        mp.start()
                    } else if (!isPlaying && mp.isPlaying) {
                        mp.pause()
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        },
        modifier = modifier
    )
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
