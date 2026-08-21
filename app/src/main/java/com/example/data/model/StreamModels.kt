package com.example.data.model

data class VideoDetails(
    val id: String,
    val title: String,
    val author: String,
    val authorAvatarUrl: String = "",
    val durationSeconds: Long,
    val durationFormatted: String,
    val thumbnailUrl: String,
    val sourcePlatform: PlatformType, // YOUTUBE, SOUNDCLOUD, DIRECT
    val viewCountText: String,
    val uploadDate: String,
    val videoStreams: List<VideoStreamOption>,
    val audioTracks: List<AudioTrackOption>,
    val defaultAudioTrackId: String = "bn_audio_track"
)

enum class PlatformType(val displayName: String, val badgeColorHex: Long) {
    YOUTUBE("YouTube", 0xFFFF0000),
    SOUNDCLOUD("SoundCloud", 0xFFFF5500),
    DIRECT("Direct Stream", 0xFF00E5A3)
}

data class VideoStreamOption(
    val id: String,
    val qualityLabel: String, // e.g. "1080p Full HD", "720p HD", "480p", "360p", "240p"
    val resolutionWidth: Int,
    val resolutionHeight: Int,
    val format: String, // "MP4", "WEBM"
    val sizeEstimatedMb: Double,
    val fps: Int = 30,
    val directStreamUrl: String,
    val isHd: Boolean = false
)

data class AudioTrackOption(
    val id: String,
    val languageCode: String, // "bn", "en", "hi", "orig"
    val languageName: String, // "বাংলা (Bengali)", "English (Original)", "Hindi (ডাবিং)", etc.
    val bitrateKbps: Int, // 320, 256, 128
    val format: String, // "MP3", "M4A/AAC", "OPUS"
    val sizeEstimatedMb: Double,
    val isDefaultSelected: Boolean = false,
    val directAudioUrl: String
)

data class SearchCategory(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val iconName: String
)
