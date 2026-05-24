package com.example.data.repository

import com.example.data.dao.MediaDao
import com.example.data.entity.FavoriteEntity
import com.example.data.entity.RecentlyPlayedEntity
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaRepository(private val mediaDao: MediaDao) {

    // Central hardcoded high-quality catalog of real royalty-free music and videos
    val musicCatalog = listOf(
        MediaItem(
            id = "m1",
            title = "Acoustic Breeze",
            artist = "SoundHelix Band",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            thumbnailUrl = "https://picsum.photos/id/1025/400/400", // Forest/dog
            isVideo = false,
            durationMs = 372000L
        ),
        MediaItem(
            id = "m2",
            title = "Summer Chills",
            artist = "Electro Horizon",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            thumbnailUrl = "https://picsum.photos/id/1043/400/400", // Sunny beach/bridge
            isVideo = false,
            durationMs = 423000L
        ),
        MediaItem(
            id = "m3",
            title = "Power Uplift",
            artist = "Inspirational Groove",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            thumbnailUrl = "https://picsum.photos/id/1053/400/400", // Sunset
            isVideo = false,
            durationMs = 344000L
        ),
        MediaItem(
            id = "m4",
            title = "Tech Horizon",
            artist = "Cyber Beats",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            thumbnailUrl = "https://picsum.photos/id/1062/400/400", // Digital scene
            isVideo = false,
            durationMs = 502000L
        ),
        MediaItem(
            id = "m5",
            title = "Epic Cinematic",
            artist = "Orchestral Dream",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            thumbnailUrl = "https://picsum.photos/id/1069/400/400", // Mountain/adventure
            isVideo = false,
            durationMs = 318000L
        )
    )

    val videoCatalog = listOf(
        MediaItem(
            id = "v1",
            title = "Big Buck Bunny",
            artist = "Blender Foundation",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            thumbnailUrl = "https://picsum.photos/id/1003/600/400", // Deer / nature
            isVideo = true,
            durationMs = 596000L
        ),
        MediaItem(
            id = "v2",
            title = "Elephants Dream",
            artist = "Orange Open Movie Project",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            thumbnailUrl = "https://picsum.photos/id/1011/600/400", // Architecture / fantasy
            isVideo = true,
            durationMs = 653000L
        ),
        MediaItem(
            id = "v4",
            title = "Sintel - CGI Fantasy",
            artist = "Durian Open Movie Project",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            thumbnailUrl = "https://picsum.photos/id/1022/600/400", // Northern lights / mountain
            isVideo = true,
            durationMs = 888000L
        ),
        MediaItem(
            id = "v5",
            title = "Tears of Steel",
            artist = "Mango Open Movie Project",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            thumbnailUrl = "https://picsum.photos/id/1029/600/400", // Futuristic city
            isVideo = true,
            durationMs = 734000L
        )
    )

    val fullCatalog = musicCatalog + videoCatalog

    // Favorites integration
    val favorites: Flow<List<MediaItem>> = mediaDao.getAllFavorites().map { list ->
        list.map { it.toMediaItem() }
    }

    suspend fun addFavorite(item: MediaItem) {
        mediaDao.insertFavorite(FavoriteEntity.fromMediaItem(item))
    }

    suspend fun removeFavorite(id: String) {
        mediaDao.deleteFavoriteById(id)
    }

    fun isFavoriteFlow(id: String): Flow<Boolean> = mediaDao.isFavoriteFlow(id)

    suspend fun isFavorite(id: String): Boolean = mediaDao.isFavorite(id)

    // Recently played integration
    val recentlyPlayed: Flow<List<MediaItem>> = mediaDao.getRecentlyPlayed().map { list ->
        list.map { it.toMediaItem() }
    }

    suspend fun addRecentlyPlayed(item: MediaItem) {
        mediaDao.insertRecentlyPlayed(
            RecentlyPlayedEntity(
                id = item.id,
                title = item.title,
                artist = item.artist,
                url = item.url,
                thumbnailUrl = item.thumbnailUrl,
                isVideo = item.isVideo,
                durationMs = item.durationMs,
                playedAt = System.currentTimeMillis()
            )
        )
    }
}
