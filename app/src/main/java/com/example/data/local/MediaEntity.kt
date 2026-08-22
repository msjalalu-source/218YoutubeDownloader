package com.example.data.local

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "downloaded_media",
    indices = [
        Index(value = ["downloadStatus"]),
        Index(value = ["isFavorite"]),
        Index(value = ["createdAt"])
    ]
)
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

@Immutable
@Entity(
    tableName = "playlists",
    indices = [
        Index(value = ["createdAt"])
    ]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String = "",
    val iconTag: String = "music_note",
    val colorHex: Long = 0xFFE50914,
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "mediaId"],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["mediaId"]),
        Index(value = ["addedAt"])
    ]
)
data class PlaylistItemCrossRef(
    val playlistId: Long,
    val mediaId: String,
    val addedAt: Long = System.currentTimeMillis()
)
