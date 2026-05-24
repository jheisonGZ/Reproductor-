package com.example.data.model

data class MediaItem(
    val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val thumbnailUrl: String,
    val isVideo: Boolean,
    val durationMs: Long = 0L
)
