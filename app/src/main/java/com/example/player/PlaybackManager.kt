package com.example.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.data.local.MediaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class AudioEqualizerPreset(val title: String, val desc: String) {
    NORMAL("সাধারণ (Normal)", "স্বাভাবিক ও ব্যালান্সড অডিও"),
    VOCAL_BOOST("ভোকাল ক্লিয়ার (Vocal)", "স্পষ্ট কণ্ঠস্বর ও সংলাপ"),
    BASS_BOOST("বেস বুস্ট (Bass+)", "গভীর ও শক্তিশালী বেস"),
    PODCAST("পডকাস্ট ও ওয়াজ", "কথোপকথন ও বাচনভঙ্গি ফোকাসড")
}

data class PlaybackState(
    val currentMedia: MediaEntity? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isMuted: Boolean = false,
    val isLooping: Boolean = false,
    val isShuffle: Boolean = false,
    val selectedAudioTrackName: String = "বাংলা (Bengali - ডিফল্ট)",
    val sleepTimerMinutesLeft: Int = 0,
    val isFullscreenVideo: Boolean = false,
    val audioPreset: AudioEqualizerPreset = AudioEqualizerPreset.NORMAL
)

class PlaybackManager {

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val playerScope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var playlistQueue: List<MediaEntity> = emptyList()
    private var currentIndex: Int = -1

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val TAG = "PlaybackManager"
        private const val DEFAULT_AUDIO_STREAM = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        private const val DEFAULT_VIDEO_STREAM = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    }

    fun playMedia(media: MediaEntity, queue: List<MediaEntity> = listOf(media)) {
        playlistQueue = queue
        currentIndex = queue.indexOfFirst { it.id == media.id }.takeIf { it >= 0 } ?: 0

        val duration = media.durationSeconds * 1000L
        _playbackState.value = _playbackState.value.copy(
            currentMedia = media,
            isPlaying = true,
            isBuffering = true,
            currentPositionMs = 0L,
            durationMs = if (duration > 0) duration else 180000L,
            selectedAudioTrackName = media.selectedAudioLanguage.ifBlank { "বাংলা (Bengali - ডিফল্ট)" }
        )

        if (media.mediaType == "AUDIO") {
            startAudioPlayback(media)
        } else {
            // For video, release audio media player to prevent dual-audio
            releaseMediaPlayer()
            _playbackState.value = _playbackState.value.copy(isBuffering = false)
            startPositionTicker()
        }
    }

    private fun startAudioPlayback(media: MediaEntity) {
        try {
            releaseMediaPlayer()

            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                // Determine playable source: local file if exists, else streamUrl, else fallback
                val source = getPlayableAudioSource(media)
                setDataSource(source)

                setOnPreparedListener { player ->
                    _playbackState.value = _playbackState.value.copy(
                        isBuffering = false,
                        isPlaying = true,
                        durationMs = player.duration.toLong().coerceAtLeast(media.durationSeconds * 1000L)
                    )
                    applyPlaybackSpeed(_playbackState.value.playbackSpeed)
                    player.start()
                    startPositionTicker()
                }

                setOnCompletionListener {
                    if (_playbackState.value.isLooping) {
                        seekTo(0L)
                        start()
                    } else {
                        playNext()
                    }
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _playbackState.value = _playbackState.value.copy(isBuffering = false)
                    try {
                        reset()
                        setDataSource(DEFAULT_AUDIO_STREAM)
                        prepareAsync()
                    } catch (e: Exception) {
                        startPositionTicker()
                    }
                    true
                }

                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaPlayer for audio: ${e.message}", e)
            _playbackState.value = _playbackState.value.copy(isBuffering = false)
            startPositionTicker()
        }
    }

    private fun getPlayableAudioSource(media: MediaEntity): String {
        if (!media.localFilePath.isNullOrBlank()) {
            val file = File(media.localFilePath)
            if (file.exists() && file.length() > 0) {
                return file.absolutePath
            }
        }
        if (media.streamUrl.isNotBlank() && (media.streamUrl.startsWith("http://") || media.streamUrl.startsWith("https://"))) {
            return media.streamUrl
        }
        return DEFAULT_AUDIO_STREAM
    }

    fun togglePlayPause() {
        val currentState = _playbackState.value
        if (currentState.currentMedia == null) return

        if (currentState.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing player: ${e.message}")
        }
        progressJob?.cancel()
    }

    fun resume() {
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming player: ${e.message}")
        }
        startPositionTicker()
    }

    fun seekTo(positionMs: Long) {
        val boundedPos = positionMs.coerceIn(0L, _playbackState.value.durationMs.coerceAtLeast(1000L))
        _playbackState.value = _playbackState.value.copy(currentPositionMs = boundedPos)
        try {
            mediaPlayer?.seekTo(boundedPos.toInt())
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking player: ${e.message}")
        }
    }

    fun skipForward10s() {
        seekTo(_playbackState.value.currentPositionMs + 10000L)
    }

    fun skipBackward10s() {
        seekTo(_playbackState.value.currentPositionMs - 10000L)
    }

    fun playNext() {
        if (playlistQueue.isEmpty()) return
        if (currentIndex < playlistQueue.size - 1) {
            currentIndex++
            playMedia(playlistQueue[currentIndex], playlistQueue)
        } else if (_playbackState.value.isLooping) {
            currentIndex = 0
            playMedia(playlistQueue[0], playlistQueue)
        }
    }

    fun playPrevious() {
        if (playlistQueue.isEmpty()) return
        if (currentIndex > 0) {
            currentIndex--
            playMedia(playlistQueue[currentIndex], playlistQueue)
        } else {
            seekTo(0L)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
        applyPlaybackSpeed(speed)
    }

    private fun applyPlaybackSpeed(speed: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
                val params = mediaPlayer!!.playbackParams
                params.speed = speed
                mediaPlayer!!.playbackParams = params
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cannot set playback speed: ${e.message}")
        }
    }

    fun switchAudioTrack(trackName: String) {
        _playbackState.value = _playbackState.value.copy(selectedAudioTrackName = trackName)
    }

    fun toggleLoop() {
        val newLoop = !_playbackState.value.isLooping
        _playbackState.value = _playbackState.value.copy(isLooping = newLoop)
        try {
            mediaPlayer?.isLooping = newLoop
        } catch (e: Exception) {
            Log.e(TAG, "Error setting loop: ${e.message}")
        }
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(isShuffle = !_playbackState.value.isShuffle)
    }

    fun setAudioPreset(preset: AudioEqualizerPreset) {
        _playbackState.value = _playbackState.value.copy(audioPreset = preset)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = minutes)
        if (minutes > 0) {
            sleepTimerJob = playerScope.launch {
                var remaining = minutes
                while (remaining > 0 && _playbackState.value.isPlaying) {
                    delay(60000L) // 1 minute
                    remaining--
                    _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = remaining)
                }
                if (remaining <= 0) {
                    pause()
                    _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = 0)
                }
            }
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _playbackState.value = _playbackState.value.copy(isFullscreenVideo = fullscreen)
    }

    fun updateVideoPositionFromView(positionMs: Long, durationMs: Long) {
        _playbackState.value = _playbackState.value.copy(
            currentPositionMs = positionMs,
            durationMs = if (durationMs > 0) durationMs else _playbackState.value.durationMs
        )
    }

    fun closePlayer() {
        pause()
        releaseMediaPlayer()
        _playbackState.value = PlaybackState()
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    private fun startPositionTicker() {
        progressJob?.cancel()
        progressJob = playerScope.launch {
            while (_playbackState.value.isPlaying) {
                delay(500)
                val current = _playbackState.value
                val mp = mediaPlayer
                if (mp != null && current.currentMedia?.mediaType == "AUDIO") {
                    try {
                        if (mp.isPlaying) {
                            val pos = mp.currentPosition.toLong()
                            val dur = mp.duration.toLong().coerceAtLeast(current.durationMs)
                            _playbackState.value = current.copy(
                                currentPositionMs = pos,
                                durationMs = dur
                            )
                        }
                    } catch (e: Exception) {
                        // ignore safe race conditions
                    }
                } else {
                    // For video or ticker fallback
                    val newPos = current.currentPositionMs + (500L * current.playbackSpeed).toLong()
                    if (newPos >= current.durationMs && current.durationMs > 0) {
                        if (current.isLooping) {
                            seekTo(0L)
                        } else {
                            playNext()
                            break
                        }
                    } else {
                        _playbackState.value = current.copy(currentPositionMs = newPos)
                    }
                }
            }
        }
    }
}
