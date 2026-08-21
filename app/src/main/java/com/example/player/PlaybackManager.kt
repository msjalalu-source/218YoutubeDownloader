package com.example.player

import com.example.data.local.MediaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaybackState(
    val currentMedia: MediaEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isMuted: Boolean = false,
    val isLooping: Boolean = false,
    val isShuffle: Boolean = false,
    val selectedAudioTrackName: String = "বাংলা (Bengali - ডিফল্ট)",
    val sleepTimerMinutesLeft: Int = 0,
    val isFullscreenVideo: Boolean = false
)

class PlaybackManager {

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val playerScope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var playlistQueue: List<MediaEntity> = emptyList()
    private var currentIndex: Int = -1

    fun playMedia(media: MediaEntity, queue: List<MediaEntity> = listOf(media)) {
        playlistQueue = queue
        currentIndex = queue.indexOfFirst { it.id == media.id }.takeIf { it >= 0 } ?: 0

        val duration = media.durationSeconds * 1000L
        _playbackState.value = _playbackState.value.copy(
            currentMedia = media,
            isPlaying = true,
            currentPositionMs = 0L,
            durationMs = if (duration > 0) duration else 180000L,
            selectedAudioTrackName = media.selectedAudioLanguage.ifBlank { "বাংলা (Bengali - ডিফল্ট)" }
        )

        startPositionTicker()
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
        progressJob?.cancel()
    }

    fun resume() {
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
        startPositionTicker()
    }

    fun seekTo(positionMs: Long) {
        val boundedPos = positionMs.coerceIn(0L, _playbackState.value.durationMs.coerceAtLeast(1000L))
        _playbackState.value = _playbackState.value.copy(currentPositionMs = boundedPos)
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
    }

    fun switchAudioTrack(trackName: String) {
        _playbackState.value = _playbackState.value.copy(selectedAudioTrackName = trackName)
    }

    fun toggleLoop() {
        _playbackState.value = _playbackState.value.copy(isLooping = !_playbackState.value.isLooping)
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(isShuffle = !_playbackState.value.isShuffle)
    }

    fun setSleepTimer(minutes: Int) {
        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesLeft = minutes)
    }

    fun setFullscreen(fullscreen: Boolean) {
        _playbackState.value = _playbackState.value.copy(isFullscreenVideo = fullscreen)
    }

    fun closePlayer() {
        pause()
        _playbackState.value = PlaybackState()
    }

    private fun startPositionTicker() {
        progressJob?.cancel()
        progressJob = playerScope.launch {
            while (_playbackState.value.isPlaying) {
                delay(500)
                val current = _playbackState.value
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
