package com.heyanle.easybangumi4.player.mpv

/**
 * Host play/pause requests are authoritative even while the native engine or Surface is catching
 * up. A queued native "pause=false" event must never undo a newer sleep-timer pause request.
 */
internal class MpvPlaybackIntent {
    data class Load(val uri: String, val playWhenReady: Boolean)

    @Volatile
    var playWhenReady: Boolean = false
        private set

    @Volatile
    private var nativePaused = true

    @Volatile
    private var pendingUri: String? = null

    val hasPendingLoad: Boolean get() = pendingUri != null
    val isPlayingAllowed: Boolean get() = playWhenReady && !nativePaused

    @Synchronized
    fun queueLoad(uri: String, playWhenReady: Boolean) {
        pendingUri = uri
        this.playWhenReady = playWhenReady
    }

    @Synchronized
    fun requestPlayback(playWhenReady: Boolean) {
        this.playWhenReady = playWhenReady
    }

    @Synchronized
    fun takePendingLoad(): Load? {
        val uri = pendingUri ?: return null
        pendingUri = null
        return Load(uri, playWhenReady)
    }

    @Synchronized
    fun cancelPendingLoad() {
        pendingUri = null
    }

    fun onNativePauseChanged(paused: Boolean) {
        nativePaused = paused
    }
}
