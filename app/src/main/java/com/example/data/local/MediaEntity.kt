package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_media")
data class MediaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val durationSeconds: Long,
    val durationFormatted: String,
    val thumbnailUrl: String,
    val originalUrl: String,
    val mediaType: String, // "VIDEO" or "AUDIO"
    val selectedQuality: String, // e.g. "1080p Full HD", "320 kbps MP3"
    val selectedAudioLanguage: String, // "বাংলা (Bengali)", "English", etc.
    val streamUrl: String,
    val localFilePath: String = "",
    val downloadStatus: String, // "COMPLETED", "DOWNLOADING", "PAUSED", "ERROR"
    val downloadProgress: Float = 0f, // 0.0 to 1.0
    val totalSizeBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val downloadSpeedFormatted: String = "",
    val isFavorite: Boolean = false,
    val playlistId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedPositionMs: Long = 0L
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String = "",
    val iconTag: String = "music_note",
    val colorHex: Long = 0xFFE50914,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_items", primaryKeys = ["playlistId", "mediaId"])
data class PlaylistItemCrossRef(
    val playlistId: Long,
    val mediaId: String,
    val addedAt: Long = System.currentTimeMillis()
)
