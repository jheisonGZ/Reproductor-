package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MediaItem

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val thumbnailUrl: String,
    val isVideo: Boolean,
    val durationMs: Long,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toMediaItem(): MediaItem = MediaItem(
        id = id,
        title = title,
        artist = artist,
        url = url,
        thumbnailUrl = thumbnailUrl,
        isVideo = isVideo,
        durationMs = durationMs
    )

    companion object {
        fun fromMediaItem(item: MediaItem): FavoriteEntity = FavoriteEntity(
            id = item.id,
            title = item.title,
            artist = item.artist,
            url = item.url,
            thumbnailUrl = item.thumbnailUrl,
            isVideo = item.isVideo,
            durationMs = item.durationMs
        )
    }
}

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val thumbnailUrl: String,
    val isVideo: Boolean,
    val durationMs: Long,
    val playedAt: Long = System.currentTimeMillis()
) {
    fun toMediaItem(): MediaItem = MediaItem(
        id = id,
        title = title,
        artist = artist,
        url = url,
        thumbnailUrl = thumbnailUrl,
        isVideo = isVideo,
        durationMs = durationMs
    )

    companion object {
        fun fromMediaItem(item: MediaItem): RecentlyPlayedEntity = RecentlyPlayedEntity(
            id = item.id,
            title = item.title,
            artist = item.artist,
            url = item.url,
            thumbnailUrl = item.thumbnailUrl,
            isVideo = item.isVideo,
            durationMs = item.durationMs
        )
    }
}
