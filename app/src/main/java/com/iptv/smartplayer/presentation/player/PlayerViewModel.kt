package com.iptv.smartplayer.presentation.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.iptv.smartplayer.core.player.MpvController
import com.iptv.smartplayer.core.player.PlayerEngineType
import com.iptv.smartplayer.core.util.Resource
import com.iptv.smartplayer.data.local.datastore.PlaybackPreferences
import com.iptv.smartplayer.data.remote.xtream.XtreamUrlBuilder
import com.iptv.smartplayer.domain.repository.LibraryRepository
import com.iptv.smartplayer.domain.repository.MovieRepository
import com.iptv.smartplayer.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlaybackContentType { LIVE, VOD }

/** المحرك المُستخدَم فعلياً الآن للعرض في PlayerScreen (يختلف عن التفضيل المخزَّن في وضع AUTO) */
enum class ActiveEngine { EXOPLAYER, LIBMPV }

data class PlayerUiState(
    val isControlsVisible: Boolean = true,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val contentType: PlaybackContentType = PlaybackContentType.VOD,
    val activeEngine: ActiveEngine = ActiveEngine.EXOPLAYER,
    val title: String = "",
    val positionMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val errorMessage: String? = null,
    /** true فقط أثناء الثانية التي يُبدَّل فيها المحرك تلقائياً — تُستخدم لعرض رسالة عابرة للمستخدم */
    val didFallbackToMpv: Boolean = false,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val exoPlayer: ExoPlayer,
    val mpvController: MpvController,
    private val playbackPreferences: PlaybackPreferences,
    private val libraryRepository: LibraryRepository,
    private val movieRepository: MovieRepository,
    private val urlBuilder: XtreamUrlBuilder,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val contentType: String = checkNotNull(savedStateHandle[Screen.Player.ARG_CONTENT_TYPE])
    private val contentId: String = checkNotNull(savedStateHandle[Screen.Player.ARG_CONTENT_ID])
    private val nextArg: String? = savedStateHandle[Screen.Player.ARG_NEXT]

    private var currentUrl: String = ""
    private var preferredEngine: PlayerEngineType = PlayerEngineType.AUTO

    private val _uiState = MutableStateFlow(
        PlayerUiState(contentType = if (contentType == "live") PlaybackContentType.LIVE else PlaybackContentType.VOD),
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var autoHideJob: kotlinx.coroutines.Job? = null

    // New: progress/save/prefetch/autoplay state
    private val SAVE_PROGRESS_INTERVAL_MS = 10_000L
    private var nextUrl: String? = null
    private var lastSavedAtMillis: Long = 0L
    private var autoplayEnabled: Boolean = true
    private var prefetchEnabled: Boolean = true

    init {
        viewModelScope.launch {
            preferredEngine = playbackPreferences.playerEngine.firstOrAuto()
        }

        // Read autoplay/prefetch preferences
        viewModelScope.launch {
            playbackPreferences.autoplayNext.collect { autoplayEnabled = it }
        }
        viewModelScope.launch {
            playbackPreferences.prefetchNext.collect { prefetchEnabled = it }
        }

        setupExoPlayerListeners()
        setupMpvListener()
        scheduleAutoHide()
        trackExoProgress()
        resolveAndPlay()
    }

    /**
     * يحلّ الرابط الفعلي تلقائياً من contentType/contentId القادمين عبر Navigation، بدل انتظار
     * استدعاء خارجي لـplayUrl(). هذا يغلق الفجوة التي كانت موجودة سابقاً حيث لم تكن أي شاشة
     * تستدعي playUrl() فعلياً بأي رابط حقيقي.
     */
    private fun resolveAndPlay() {
        viewModelScope.launch {
            when (contentType) {
                "live" -> {
                    val streamId = contentId.toIntOrNull() ?: return@launch
                    playUrl(urlBuilder.buildLiveUrl(streamId), "بث مباشر", nextArg)
                }
                "series" -> {
                    // contentId هنا هو معرّف الحلقة كما يُرجعه Xtream (قد لا يكون رقماً صرفاً)
                    playUrl(urlBuilder.buildEpisodeUrl(contentId, extension = "mp4"), "حلقة", nextArg)
                }
                else -> { // "vod"
                    val streamId = contentId.toIntOrNull() ?: return@launch
                    movieRepository.getMovieDetails(streamId).collect { resource ->
                        if (resource is Resource.Success) {
                            val movie = resource.data
                            playUrl(urlBuilder.buildMovieUrl(streamId, movie.containerExtension), movie.title, nextArg)
                        }
                    }
                }
            }
        }
    }

    /**
     * يُستدعى داخلياً من resolveAndPlay()، ويبقى public لدعم إعادة التشغيل اليدوي مستقبلاً
     * next: رابط العنصر التالي (اختياري) ليُستخدم للـ prefetch/autoplay
     */
    fun playUrl(url: String, title: String, next: String? = null) {
        currentUrl = url
        nextUrl = next
        _uiState.value = _uiState.value.copy(title = title, errorMessage = null)

        when (preferredEngine) {
            PlayerEngineType.LIBMPV -> startWithMpv(url)
            PlayerEngineType.EXOPLAYER -> startWithExoPlayer(url)
            PlayerEngineType.AUTO -> startWithExoPlayer(url) // المحاولة الأولى دائماً عبر ExoPlayer في وضع AUTO
        }
    }

    private fun startWithExoPlayer(url: String) {
        _uiState.value = _uiState.value.copy(activeEngine = ActiveEngine.EXOPLAYER)

        // Use playlist approach: clear existing items and add current, optionally add next to prefetch
        exoPlayer.clearMediaItems()
        exoPlayer.addMediaItem(MediaItem.fromUri(url))

        if (prefetchEnabled && !nextUrl.isNullOrBlank()) {
            val n = nextUrl!!
            if (exoPlayer.mediaItems.none { it.localConfiguration?.uri.toString() == n }) {
                exoPlayer.addMediaItem(MediaItem.fromUri(n))
            }
        }

        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun startWithMpv(url: String) {
        _uiState.value = _uiState.value.copy(activeEngine = ActiveEngine.LIBMPV)
        exoPlayer.stop()
        mpvController.playUrl(url)
    }

    /** يُستدعى تلقائياً عند فشل ExoPlayer في وضع AUTO فقط — التبديل الصامت الأساسي في هذه الميزة */
    private fun fallbackToMpvIfNeeded() {
        if (preferredEngine != PlayerEngineType.AUTO || _uiState.value.activeEngine == ActiveEngine.LIBMPV) return
        _uiState.value = _uiState.value.copy(didFallbackToMpv = true, errorMessage = null, isBuffering = true)
        startWithMpv(currentUrl)
    }

    private fun setupExoPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (_uiState.value.activeEngine == ActiveEngine.EXOPLAYER) {
                    _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (_uiState.value.activeEngine == ActiveEngine.EXOPLAYER) {
                    _uiState.value = _uiState.value.copy(isBuffering = playbackState == Player.STATE_BUFFERING)

                    // When ended -> autoplay next if enabled
                    if (playbackState == Player.STATE_ENDED) {
                        viewModelScope.launch {
                            if (autoplayEnabled && !nextUrl.isNullOrBlank()) {
                                val toPlay = nextUrl!!
                                nextUrl = null
                                startWithExoPlayer(toPlay)
                            } else {
                                _uiState.value = _uiState.value.copy(isControlsVisible = true)
                            }
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                // بدل عرض خطأ فوري للمستخدم، نجرّب libmpv أولاً (وضع AUTO) — هذا هو جوهر الميزة
                fallbackToMpvIfNeeded()
                if (preferredEngine != PlayerEngineType.AUTO) {
                    _uiState.value = _uiState.value.copy(errorMessage = "تعذّر تشغيل هذا المحتوى، يرجى إعادة المحاولة")
                }
            }
        })
    }

    private fun setupMpvListener() {
        viewModelScope.launch {
            mpvController.state.collect { mpvState ->
                if (_uiState.value.activeEngine == ActiveEngine.LIBMPV) {
                    _uiState.value = _uiState.value.copy(
                        isPlaying = mpvState.isPlaying,
                        isBuffering = mpvState.isBuffering,
                        positionMillis = mpvState.positionMillis,
                        durationMillis = mpvState.durationMillis,
                        errorMessage = mpvState.errorMessage,
                    )
                }
            }
        }
    }

    fun onUserInteraction() {
        _uiState.value = _uiState.value.copy(isControlsVisible = true, didFallbackToMpv = false)
        scheduleAutoHide()
    }

    fun togglePlayPause() {
        when (_uiState.value.activeEngine) {
            ActiveEngine.EXOPLAYER -> if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
            ActiveEngine.LIBMPV -> mpvController.togglePlayPause()
        }
        onUserInteraction()
    }

    fun seekBy(deltaMillis: Long) {
        when (_uiState.value.activeEngine) {
            ActiveEngine.EXOPLAYER -> exoPlayer.seekTo((exoPlayer.currentPosition + deltaMillis).coerceAtLeast(0))
            ActiveEngine.LIBMPV -> mpvController.seekBy((deltaMillis / 1000).toInt())
        }
        onUserInteraction()
    }

    fun retry() {
        when (_uiState.value.activeEngine) {
            ActiveEngine.EXOPLAYER -> exoPlayer.prepare()
            ActiveEngine.LIBMPV -> mpvController.retry()
        }
    }

    /** يسمح للمستخدم بفرض التبديل يدوياً من الإعدادات السريعة داخل المشغل (مستقبلاً) */
    fun switchEngineManually(engine: ActiveEngine) {
        if (engine == _uiState.value.activeEngine) return
        if (engine == ActiveEngine.LIBMPV) startWithMpv(currentUrl) else startWithExoPlayer(currentUrl)
    }

    private fun scheduleAutoHide() {
        autoHideJob?.cancel()
        autoHideJob = viewModelScope.launch {
            delay(4000)
            _uiState.value = _uiState.value.copy(isControlsVisible = false)
        }
    }

    private fun trackExoProgress() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.activeEngine == ActiveEngine.EXOPLAYER) {
                    val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    _uiState.value = _uiState.value.copy(positionMillis = pos, durationMillis = dur)

                    // Save periodically for VOD
                    if (_uiState.value.contentType == PlaybackContentType.VOD && pos > 0 && dur > 0) {
                        val now = System.currentTimeMillis()
                        if (now - lastSavedAtMillis >= SAVE_PROGRESS_INTERVAL_MS) {
                            lastSavedAtMillis = now
                            viewModelScope.launch {
                                libraryRepository.saveWatchProgress(
                                    contentId = contentId,
                                    contentType = com.iptv.smartplayer.data.local.entity.ContentType.MOVIE,
                                    title = _uiState.value.title,
                                    posterUrl = null,
                                    positionMillis = pos,
                                    durationMillis = dur,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            if (_uiState.value.contentType == PlaybackContentType.VOD) {
                libraryRepository.saveWatchProgress(
                    contentId = contentId,
                    contentType = com.iptv.smartplayer.data.local.entity.ContentType.MOVIE,
                    title = _uiState.value.title,
                    posterUrl = null,
                    positionMillis = _uiState.value.positionMillis,
                    durationMillis = _uiState.value.durationMillis,
                )
            }
        }
        exoPlayer.release()
        mpvController.release()
        super.onCleared()
    }
}

// Helper صغير لقراءة أول قيمة من Flow التفضيلات دون كسر تسلسل init{} أعلاه
private suspend fun kotlinx.coroutines.flow.Flow<PlayerEngineType>.firstOrAuto(): PlayerEngineType =
    try {
        kotlinx.coroutines.flow.first(this)
    } catch (e: Exception) {
        PlayerEngineType.AUTO
    }
