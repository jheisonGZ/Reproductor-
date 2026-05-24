package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MediaRepository(db.mediaDao())

    // UI Tab / Navigation State
    val tabs = listOf("Inicio", "Buscar", "Tu Biblioteca")
    private val _currentTab = MutableStateFlow("Inicio")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Player Bottom Sheet state (collapsed or expanded)
    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    // Catalogs & DB Flows
    val musicCatalog = repository.musicCatalog
    val videoCatalog = repository.videoCatalog
    val fullCatalog = repository.fullCatalog

    val favorites: StateFlow<List<MediaItem>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<MediaItem>> = repository.recentlyPlayed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<MediaItem>> = _searchQuery
        .combine(flowOf(fullCatalog)) { query, catalog ->
            if (query.isBlank()) {
                catalog
            } else {
                catalog.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), fullCatalog)

    // Player Engine (Audio MediaPlayer)
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    // Playlist Queue support
    private val _playbackQueue = MutableStateFlow<List<MediaItem>>(emptyList())
    val playbackQueue: StateFlow<List<MediaItem>> = _playbackQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isRepeatEnabled = MutableStateFlow(false)
    val isRepeatEnabled: StateFlow<Boolean> = _isRepeatEnabled.asStateFlow()

    // Loading / buffering state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Track active video view details
    private val _activeVideoItem = MutableStateFlow<MediaItem?>(null)
    val activeVideoItem: StateFlow<MediaItem?> = _activeVideoItem.asStateFlow()

    // Track Favorite status dynamic flow for currently playing
    val isCurrentItemFavorite: StateFlow<Boolean> = _currentMediaItem
        .flatMapLatest { item ->
            if (item != null) repository.isFavoriteFlow(item.id) else flowOf(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        setupMediaPlayer()
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener {
                _isLoading.value = false
                _durationMs.value = it.duration.toLong()
                _isPlaying.value = true
                it.start()
                startProgressTracker()
            }
            setOnCompletionListener {
                if (_isRepeatEnabled.value) {
                    it.seekTo(0)
                    it.start()
                } else {
                    playNext()
                }
            }
            setOnErrorListener { _, what, extra ->
                Log.e("SoundPlayer", "MediaPlayer Error: what=$what, extra=$extra")
                _isLoading.value = false
                _isPlaying.value = false
                true
            }
        }
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Playback commands
    fun playMedia(item: MediaItem, customQueue: List<MediaItem> = emptyList()) {
        if (item.isVideo) {
            // Pause music player
            pauseMedia()
            // Launch full screen or screen-docked Video Player!
            _activeVideoItem.value = item
            viewModelScope.launch {
                repository.addRecentlyPlayed(item)
            }
            return
        }

        // It is an Audio item
        _activeVideoItem.value = null // dismiss active video if any
        val queue = if (customQueue.isNotEmpty()) customQueue else listOf(item)
        _playbackQueue.value = queue
        val idx = queue.indexOfFirst { it.id == item.id }
        _currentIndex.value = if (idx != -1) idx else 0

        loadAndPlay(item)
    }

    private fun loadAndPlay(item: MediaItem) {
        _currentMediaItem.value = item
        _isLoading.value = true
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        _durationMs.value = item.durationMs

        stopProgressTracker()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                mediaPlayer?.let { player ->
                    player.reset()
                    player.setDataSource(item.url)
                    player.prepareAsync()
                }
                repository.addRecentlyPlayed(item)
            } catch (e: Exception) {
                Log.e("SoundPlayer", "Error preparing DataSource", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
        } else {
            // Check if there's an item, if not load first catalog item
            if (_currentMediaItem.value == null && musicCatalog.isNotEmpty()) {
                playMedia(musicCatalog.first(), musicCatalog)
            } else if (_currentMediaItem.value != null) {
                player.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        }
    }

    fun pauseMedia() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
        }
    }

    fun playNext() {
        val queue = _playbackQueue.value
        if (queue.isEmpty()) return

        var nextIdx = _currentIndex.value + 1
        if (_isShuffleEnabled.value) {
            nextIdx = (0 until queue.size).random()
        } else if (nextIdx >= queue.size) {
            nextIdx = 0 // loop around
        }

        _currentIndex.value = nextIdx
        val nextItem = queue[nextIdx]
        loadAndPlay(nextItem)
    }

    fun playPrevious() {
        val queue = _playbackQueue.value
        if (queue.isEmpty()) return

        var prevIdx = _currentIndex.value - 1
        if (prevIdx < 0) {
            prevIdx = queue.size - 1 // loop to end
        }

        _currentIndex.value = prevIdx
        val prevItem = queue[prevIdx]
        loadAndPlay(prevItem)
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            _currentPositionMs.value = positionMs
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun toggleRepeat() {
        _isRepeatEnabled.value = !_isRepeatEnabled.value
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            if (repository.isFavorite(item.id)) {
                repository.removeFavorite(item.id)
            } else {
                repository.addFavorite(item)
            }
        }
    }

    fun closeVideoPlayer() {
        _activeVideoItem.value = null
    }

    private fun startProgressTracker() {
        progressJob = viewModelScope.launch {
            while (true) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition.toLong()
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressTracker()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
