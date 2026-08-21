package com.example.ui

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MediaEntity
import com.example.data.local.PlaylistEntity
import com.example.data.model.AudioTrackOption
import com.example.data.service.KnownBanglaMediaCatalogue
import com.example.data.model.VideoDetails
import com.example.data.model.VideoStreamOption
import com.example.data.repository.MediaRepository
import com.example.data.service.MediaExtractorService
import com.example.player.PlaybackManager
import com.example.player.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedTab: Int = 0, // 0: Home/Scraper, 1: Downloads, 2: Settings
    val inputUrlText: String = "",
    val isExtracting: Boolean = false,
    val extractedVideoDetails: VideoDetails? = null,
    val showQuickDownloadModal: Boolean = false,
    val selectedDownloadTab: Int = 1, // 0: Video, 1: Audio (Audio tab highlighted with Bangla track default!)
    val selectedVideoOption: VideoStreamOption? = null,
    val selectedAudioOption: AudioTrackOption? = null,
    val searchQuery: String = "",
    val activeCategoryFilter: String = "all",
    val autoClipboardEnabled: Boolean = true,
    val prioritizeBanglaAudio: Boolean = true,
    val lastDetectedClipboardUrl: String = "",
    val showCreatePlaylistDialog: Boolean = false,
    val showAddToPlaylistDialog: Boolean = false,
    val mediaToAddToPlaylist: MediaEntity? = null,
    val activePlaylistDetail: PlaylistEntity? = null,
    val bannerMessage: String? = null
)

class MainViewModel(
    private val repository: MediaRepository,
    val playbackManager: PlaybackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val completedDownloads: StateFlow<List<MediaEntity>> = repository.completedDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDownloads: StateFlow<List<MediaEntity>> = repository.activeDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedia: StateFlow<List<MediaEntity>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState

    init {
        viewModelScope.launch {
            repository.initializeDefaultPlaylistsIfEmpty()
        }
    }

    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun onUrlInputChanged(newUrl: String) {
        _uiState.value = _uiState.value.copy(inputUrlText = newUrl)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setCategoryFilter(category: String) {
        _uiState.value = _uiState.value.copy(activeCategoryFilter = category)
    }

    fun checkClipboardForMediaLink(context: Context) {
        if (!_uiState.value.autoClipboardEnabled) return

        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val item = clipData.getItemAt(0)
                    val text = item.text?.toString()?.trim() ?: ""
                    
                    if (text.isNotBlank() && MediaExtractorService.isSupportedUrl(text)) {
                        if (text != _uiState.value.lastDetectedClipboardUrl) {
                            _uiState.value = _uiState.value.copy(
                                lastDetectedClipboardUrl = text,
                                inputUrlText = text
                            )
                            // Automatically extract and open download modal
                            extractAndShowDownloadDialog(text, isAutoDetected = true)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore clipboard permission nuances safely
        }
    }

    fun extractAndShowDownloadDialog(urlToExtract: String = _uiState.value.inputUrlText, isAutoDetected: Boolean = false) {
        val targetUrl = urlToExtract.trim()
        if (targetUrl.isBlank()) return

        if (MediaExtractorService.isAdultOrRestrictedContent(targetUrl)) {
            _uiState.value = _uiState.value.copy(
                isExtracting = false,
                bannerMessage = "⚠️ ১৮+ বা প্রাপ্তবয়স্ক কন্টেন্ট সম্পূর্ণ নিষিদ্ধ ও রেস্ট্রিক্টেড করা হয়েছে।"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExtracting = true,
                bannerMessage = if (isAutoDetected) "ক্লিপবোর্ড থেকে লিংক শনাক্ত করা হয়েছে! স্ক্র্যাপ করা হচ্ছে..." else "মিডিয়া ও অডিও ট্র্যাক এক্সট্রাক্ট করা হচ্ছে..."
            )

            try {
                val details = MediaExtractorService.extractMediaDetails(targetUrl)
                
                // Bangla audio track is first priority & default selected (followed by Hindi, then Original)
                val defaultAudio = details.audioTracks.find { it.isDefaultSelected }
                    ?: details.audioTracks.find { it.languageCode.startsWith("bn") }
                    ?: details.audioTracks.firstOrNull()

                // Default Video Resolution is 480p
                val defaultVideo = details.videoStreams.find { it.qualityLabel.contains("480p") || it.resolutionHeight == 480 }
                    ?: details.videoStreams.find { it.qualityLabel.contains("720p") || it.resolutionHeight == 720 }
                    ?: details.videoStreams.find { it.qualityLabel.contains("1080p") || it.resolutionHeight == 1080 }
                    ?: details.videoStreams.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    extractedVideoDetails = details,
                    selectedAudioOption = defaultAudio,
                    selectedVideoOption = defaultVideo,
                    showQuickDownloadModal = true,
                    bannerMessage = "সরাসরি স্ট্রিম ও বাংলা অডিও ট্র্যাক প্রস্তুত!"
                )
            } catch (se: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    bannerMessage = se.message ?: "⚠️ ১৮+ কন্টেন্ট রেস্ট্রিক্টেড করা হয়েছে।"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    bannerMessage = "লিংক স্ক্র্যাপ করতে সমস্যা হয়েছে। অনুগ্রহ করে লিংকটি চেক করুন।"
                )
            }
        }
    }

    fun setSelectedDownloadTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedDownloadTab = tabIndex)
    }

    fun selectVideoOption(option: VideoStreamOption) {
        _uiState.value = _uiState.value.copy(selectedVideoOption = option)
    }

    fun selectAudioOption(option: AudioTrackOption) {
        _uiState.value = _uiState.value.copy(selectedAudioOption = option)
    }

    fun dismissDownloadModal() {
        _uiState.value = _uiState.value.copy(showQuickDownloadModal = false)
    }

    fun startDownloadFromModal(isAudioOnly: Boolean, customTitle: String? = null, threads: Int = 3) {
        val details = _uiState.value.extractedVideoDetails ?: return
        val videoOpt = _uiState.value.selectedVideoOption
        val audioOpt = _uiState.value.selectedAudioOption ?: return

        val effectiveDetails = if (!customTitle.isNullOrBlank()) {
            details.copy(title = customTitle)
        } else {
            details
        }

        viewModelScope.launch {
            repository.startDownload(
                videoDetails = effectiveDetails,
                selectedVideo = videoOpt,
                selectedAudio = audioOpt,
                isAudioOnly = isAudioOnly
            )
            _uiState.value = _uiState.value.copy(
                showQuickDownloadModal = false,
                selectedTab = 1, // Switch to downloads tab
                bannerMessage = "${effectiveDetails.title.take(22)}... (${threads} Threads) ডাউনলোড শুরু হয়েছে!"
            )
        }
    }

    fun playStreamDirectly(isAudioOnly: Boolean) {
        val details = _uiState.value.extractedVideoDetails ?: return
        val videoOpt = _uiState.value.selectedVideoOption
        val audioOpt = _uiState.value.selectedAudioOption

        val mediaEntity = MediaEntity(
            id = "stream_" + details.id,
            title = details.title,
            author = details.author,
            durationSeconds = details.durationSeconds,
            durationFormatted = details.durationFormatted,
            thumbnailUrl = details.thumbnailUrl,
            originalUrl = details.id,
            mediaType = if (isAudioOnly) "AUDIO" else "VIDEO",
            selectedQuality = if (isAudioOnly) (audioOpt?.format ?: "MP3") else (videoOpt?.qualityLabel ?: "720p HD"),
            selectedAudioLanguage = audioOpt?.languageName ?: "বাংলা (Bengali)",
            streamUrl = if (isAudioOnly) (audioOpt?.directAudioUrl ?: "") else (videoOpt?.directStreamUrl ?: audioOpt?.directAudioUrl ?: ""),
            downloadStatus = "STREAMING"
        )

        _uiState.value = _uiState.value.copy(showQuickDownloadModal = false)
        playbackManager.playMedia(mediaEntity)
    }

    fun playOfflineMedia(media: MediaEntity, playlist: List<MediaEntity>? = null) {
        val queue = playlist ?: completedDownloads.value.ifEmpty { listOf(media) }
        playbackManager.playMedia(media, queue)
    }

    fun deleteMedia(id: String) {
        viewModelScope.launch {
            repository.deleteMedia(id)
        }
    }

    fun toggleFavorite(media: MediaEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(media)
        }
    }

    fun pauseDownload(id: String) {
        viewModelScope.launch {
            repository.pauseDownload(id)
        }
    }

    fun resumeDownload(media: MediaEntity) {
        viewModelScope.launch {
            repository.resumeDownload(media)
        }
    }

    // Playlist dialogs & actions
    fun openCreatePlaylistDialog() {
        _uiState.value = _uiState.value.copy(showCreatePlaylistDialog = true)
    }

    fun dismissCreatePlaylistDialog() {
        _uiState.value = _uiState.value.copy(showCreatePlaylistDialog = false)
    }

    fun createPlaylist(name: String, description: String = "", colorHex: Long = 0xFFFF334B) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name.trim(), description.trim(), colorHex)
            _uiState.value = _uiState.value.copy(
                showCreatePlaylistDialog = false,
                bannerMessage = "নতুন প্লেলিস্ট তৈরি হয়েছে!"
            )
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
            if (_uiState.value.activePlaylistDetail?.id == id) {
                _uiState.value = _uiState.value.copy(activePlaylistDetail = null)
            }
        }
    }

    fun openAddToPlaylistDialog(media: MediaEntity) {
        _uiState.value = _uiState.value.copy(
            showAddToPlaylistDialog = true,
            mediaToAddToPlaylist = media
        )
    }

    fun dismissAddToPlaylistDialog() {
        _uiState.value = _uiState.value.copy(
            showAddToPlaylistDialog = false,
            mediaToAddToPlaylist = null
        )
    }

    fun addMediaToPlaylist(playlistId: Long, mediaId: String) {
        viewModelScope.launch {
            repository.addMediaToPlaylist(playlistId, mediaId)
            _uiState.value = _uiState.value.copy(
                showAddToPlaylistDialog = false,
                mediaToAddToPlaylist = null,
                bannerMessage = "প্লেলিস্টে যুক্ত করা হয়েছে!"
            )
        }
    }

    fun viewPlaylistDetail(playlist: PlaylistEntity) {
        _uiState.value = _uiState.value.copy(activePlaylistDetail = playlist)
    }

    fun closePlaylistDetail() {
        _uiState.value = _uiState.value.copy(activePlaylistDetail = null)
    }

    fun toggleAutoClipboard(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoClipboardEnabled = enabled)
    }

    fun togglePrioritizeBanglaAudio(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(prioritizeBanglaAudio = enabled)
    }

    fun clearBanner() {
        _uiState.value = _uiState.value.copy(bannerMessage = null)
    }
}
