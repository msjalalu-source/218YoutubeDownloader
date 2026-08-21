package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM downloaded_media ORDER BY createdAt DESC")
    fun getAllMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM downloaded_media WHERE downloadStatus = 'COMPLETED' ORDER BY createdAt DESC")
    fun getCompletedDownloads(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM downloaded_media WHERE downloadStatus = 'DOWNLOADING' OR downloadStatus = 'PAUSED' ORDER BY createdAt DESC")
    fun getActiveDownloads(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM downloaded_media WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM downloaded_media WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: String): MediaEntity?

    @Query("SELECT * FROM downloaded_media WHERE id = :id LIMIT 1")
    fun observeMediaById(id: String): Flow<MediaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Update
    suspend fun updateMedia(media: MediaEntity)

    @Query("UPDATE downloaded_media SET downloadProgress = :progress, downloadedBytes = :downloadedBytes, downloadSpeedFormatted = :speed, downloadStatus = :status WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, progress: Float, downloadedBytes: Long, speed: String, status: String)

    @Query("UPDATE downloaded_media SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: String, isFav: Boolean)

    @Query("UPDATE downloaded_media SET lastPlayedPositionMs = :positionMs WHERE id = :id")
    suspend fun updatePlaybackPosition(id: String, positionMs: Long)

    @Query("DELETE FROM downloaded_media WHERE id = :id")
    suspend fun deleteMediaById(id: String)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItemToPlaylist(crossRef: PlaylistItemCrossRef)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND mediaId = :mediaId")
    suspend fun removeItemFromPlaylist(playlistId: Long, mediaId: String)

    @Query("""
        SELECT m.* FROM downloaded_media m
        INNER JOIN playlist_items p ON m.id = p.mediaId
        WHERE p.playlistId = :playlistId
        ORDER BY p.addedAt DESC
    """)
    fun getMediaForPlaylist(playlistId: Long): Flow<List<MediaEntity>>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    fun getPlaylistItemCount(playlistId: Long): Flow<Int>
}
