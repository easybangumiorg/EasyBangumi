package com.heyanle.easybangumi4.player.mpv

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import com.heyanle.easybangumi4.setting.SettingPreferences
import dev.jdtech.mpv.MPVLib
import loli.ball.easyplayer2.EasyPlaybackState
import loli.ball.easyplayer2.EasyPlayerController
import loli.ball.easyplayer2.EasyVideoSize
import java.io.File
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MpvAnime4KStatus(
    val enabled: Boolean = false,
    val inputWidth: Int = 0,
    val inputHeight: Int = 0,
    val outputWidth: Int = 0,
    val outputHeight: Int = 0,
) {
    /** Matches Anime4K_Upscale_CNN_x2_S.glsl's //!WHEN expression. */
    val upscaleCnnActive: Boolean? = when {
        !enabled -> false
        inputWidth <= 0 || inputHeight <= 0 || outputWidth <= 0 || outputHeight <= 0 -> null
        else -> outputWidth > inputWidth / 1.2 && outputHeight > inputHeight / 1.2
    }
}

/** libmpv playback core. Anime4K is intentionally owned only by this engine. */
class MpvPlaybackController(
    context: Context,
    private val preferences: SettingPreferences,
) : EasyPlayerController, MPVLib.EventObserver {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArraySet<EasyPlayerController.Listener>()
    private val released = AtomicBoolean(false)
    private val shaderStore = MpvAnime4KShaderStore(appContext)
    private val _anime4KStatus = MutableStateFlow(MpvAnime4KStatus())
    val anime4KStatus: StateFlow<MpvAnime4KStatus> = _anime4KStatus.asStateFlow()

    @Volatile private var fileLoaded = false
    @Volatile private var fileLoading = false
    @Volatile private var paused = true
    @Volatile private var pausedForCache = false
    @Volatile private var eofReached = false
    @Volatile private var positionMs = 0L
    @Volatile private var durationMs = 0L
    @Volatile private var bufferedMs = 0L
    @Volatile private var playbackSpeed = 1f
    @Volatile private var videoWidth = 0
    @Volatile private var videoHeight = 0
    private var pendingLoad: PendingLoad? = null
    private var pendingStartPositionMs = 0L
    private var surfaceAttached = false
    private var attachedSurfaceToken = ""
    private var attachedSurfaceWidth = 0
    private var attachedSurfaceHeight = 0
    private var videoOutputNeedsRestart = false
    private var attachedView: View? = null
    private var surfaceCallback: SurfaceHolder.Callback? = null
    private var textureCallback: TextureView.SurfaceTextureListener? = null
    private var textureSurface: Surface? = null
    private var retainedSurfaceTexture: SurfaceTexture? = null

    init {
        synchronized(SESSION_LOCK) {
            // MPVLib exposes one process-wide native instance. Replacing an existing controller
            // explicitly prevents back-stack ViewModels from observing or destroying each other.
            activeOwner?.releaseForReplacementLocked()
            MPVLib.create(appContext)
            MPVLib.setOptionString("vo", "gpu")
            MPVLib.setOptionString("gpu-context", "android")
            MPVLib.setOptionString("opengl-es", "yes")
            MPVLib.setOptionString("hwdec", "mediacodec-copy")
            MPVLib.setOptionString("hwdec-codecs", "h264,hevc,mpeg4,mpeg2video,vp8,vp9,av1")
            MPVLib.setOptionString("ao", "audiotrack")
            MPVLib.setOptionString("keep-open", "always")
            MPVLib.setOptionString("idle", "yes")
            MPVLib.setOptionString("force-window", "no")
            MPVLib.setOptionString("cache", "yes")
            MPVLib.setOptionString("demuxer-max-bytes", "32MiB")
            MPVLib.setOptionString("demuxer-max-back-bytes", "32MiB")
            MPVLib.init()
            MPVLib.addObserver(this)
            observe("video-params/w", MPVLib.MPV_FORMAT_INT64)
            observe("video-params/h", MPVLib.MPV_FORMAT_INT64)
            observe("time-pos", MPVLib.MPV_FORMAT_DOUBLE)
            observe("duration", MPVLib.MPV_FORMAT_DOUBLE)
            observe("demuxer-cache-time", MPVLib.MPV_FORMAT_DOUBLE)
            observe("speed", MPVLib.MPV_FORMAT_DOUBLE)
            observe("pause", MPVLib.MPV_FORMAT_FLAG)
            observe("paused-for-cache", MPVLib.MPV_FORMAT_FLAG)
            observe("eof-reached", MPVLib.MPV_FORMAT_FLAG)
            applyAnime4K()
            activeOwner = this
        }
    }

    override var playWhenReady: Boolean
        get() = !paused
        set(value) {
            if (value) play() else pause()
        }
    override val isPlaying: Boolean get() = fileLoaded && !paused && !pausedForCache && !eofReached
    override val playbackState: EasyPlaybackState
        get() = when {
            eofReached -> EasyPlaybackState.ENDED
            !fileLoaded -> if (pendingLoad != null || fileLoading) EasyPlaybackState.BUFFERING else EasyPlaybackState.IDLE
            pausedForCache -> EasyPlaybackState.BUFFERING
            else -> EasyPlaybackState.READY
        }
    override val hasMedia: Boolean get() = fileLoaded || fileLoading || pendingLoad != null
    override val currentPosition: Long get() = positionMs
    override val duration: Long get() = durationMs
    override val bufferedPosition: Long get() = bufferedMs
    override val speed: Float get() = playbackSpeed

    fun load(
        uri: String,
        headers: Map<String, String>,
        startPositionMs: Long,
        playWhenReady: Boolean,
    ) {
        if (released.get()) return
        fileLoaded = false
        fileLoading = false
        eofReached = false
        positionMs = startPositionMs.coerceAtLeast(0L)
        durationMs = 0L
        bufferedMs = 0L
        pendingStartPositionMs = positionMs
        applyHeaders(headers)
        applyAnime4K()
        val playableUri = resolvePlayableUri(uri)
        if (playableUri == null) {
            pendingLoad = null
            post { listeners.forEach { it.onPlaybackStateChanged(EasyPlaybackState.IDLE) } }
            return
        }
        val load = PendingLoad(playableUri, playWhenReady)
        pendingLoad = load
        notifyState()
        if (surfaceAttached) executeLoad(load)
    }

    fun applyAnime4K() {
        if (released.get()) return
        val enabled = preferences.mpvAnime4kEnabled.get()
        val paths = if (enabled) {
            shaderStore.resolve(preferences.mpvAnime4kPreset.get())
        } else {
            emptyList()
        }
        MPVLib.setPropertyString("glsl-shaders", paths.joinToString(":"))
        publishAnime4KStatus(enabled)
    }

    override fun play() {
        if (released.get()) return
        MPVLib.setPropertyBoolean("pause", false)
    }

    override fun pause() {
        if (released.get()) return
        MPVLib.setPropertyBoolean("pause", true)
    }

    override fun seekTo(positionMs: Long) {
        if (released.get()) return
        this.positionMs = positionMs.coerceIn(0L, durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE)
        MPVLib.command(arrayOf("seek", (this.positionMs / 1000.0).toString(), "absolute"))
        notifyPosition()
    }

    override fun seekForward() = seekTo(currentPosition + DEFAULT_SEEK_MS)
    override fun seekBack() = seekTo(currentPosition - DEFAULT_SEEK_MS)

    override fun setSpeed(speed: Float) {
        if (released.get()) return
        val normalized = speed.coerceIn(0.25f, 4f)
        playbackSpeed = normalized
        MPVLib.setPropertyDouble("speed", normalized.toDouble())
    }

    override fun stop() {
        if (released.get()) return
        pendingLoad = null
        fileLoaded = false
        fileLoading = false
        MPVLib.command(arrayOf("stop"))
        notifyState()
    }

    override fun attach(view: View) {
        if (attachedView === view) return
        attachedView?.let(::detach)
        when (view) {
            is SurfaceView -> attachSurfaceView(view)
            is TextureView -> attachTextureView(view)
        }
    }

    private fun attachSurfaceView(surfaceView: SurfaceView) {
        val callback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                attachSurface(holder.surface)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // Several Android vendors replace SurfaceView's BufferQueue during rotation while
                // retaining the same View/SurfaceHolder. Rebind here instead of relying only on
                // surfaceCreated, otherwise mpv keeps rendering into an abandoned native window.
                bindSurface(holder.surface, width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) = detachSurface()
        }
        attachedView = surfaceView
        surfaceCallback = callback
        surfaceView.holder.addCallback(callback)
        if (surfaceView.holder.surface?.isValid == true) {
            val frame = surfaceView.holder.surfaceFrame
            bindSurface(surfaceView.holder.surface, frame.width(), frame.height())
        }
    }

    private fun attachTextureView(textureView: TextureView) {
        val callback = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int,
            ) = bindTextureSurface(textureView, surfaceTexture, width, height)

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int,
            ) = bindTextureSurface(textureView, surfaceTexture, width, height)

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                // Keep the producer-side BufferQueue alive across a TextureView reparent/config
                // transition. The controller releases it explicitly when the playback view leaves.
                return retainedSurfaceTexture !== surfaceTexture
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
        }
        attachedView = textureView
        textureCallback = callback
        textureView.surfaceTextureListener = callback
        if (textureView.isAvailable) {
            textureView.surfaceTexture?.let {
                bindTextureSurface(textureView, it, textureView.width, textureView.height)
            }
        }
    }

    override fun detach(view: View) {
        if (attachedView !== view) return
        (view as? SurfaceView)?.let { surfaceView ->
            surfaceCallback?.let(surfaceView.holder::removeCallback)
        }
        (view as? TextureView)?.surfaceTextureListener = null
        surfaceCallback = null
        textureCallback = null
        attachedView = null
        detachSurface()
        textureSurface?.release()
        textureSurface = null
        retainedSurfaceTexture?.release()
        retainedSurfaceTexture = null
    }

    override fun addListener(listener: EasyPlayerController.Listener) {
        listeners += listener
    }

    override fun removeListener(listener: EasyPlayerController.Listener) {
        listeners -= listener
    }

    fun release() {
        synchronized(SESSION_LOCK) {
            if (!released.compareAndSet(false, true)) return
            releaseNativeLocked()
        }
    }

    override fun event(eventId: Int) {
        if (!isActiveOwner()) return
        when (eventId) {
            MPVLib.MPV_EVENT_FILE_LOADED -> {
                fileLoaded = true
                fileLoading = false
                pendingLoad = null
                if (pendingStartPositionMs > 0L) seekTo(pendingStartPositionMs)
                pendingStartPositionMs = 0L
                notifyState()
            }
            MPVLib.MPV_EVENT_END_FILE -> {
                fileLoaded = false
                notifyState()
            }
        }
    }

    override fun eventProperty(property: String) = Unit

    override fun eventProperty(property: String, value: Long) {
        if (!isActiveOwner()) return
        when (property) {
            "video-params/w" -> videoWidth = value.toInt()
            "video-params/h" -> videoHeight = value.toInt()
            else -> return
        }
        if (videoWidth > 0 && videoHeight > 0) post {
            publishAnime4KStatus()
            listeners.forEach { it.onVideoSizeChanged(EasyVideoSize(videoWidth, videoHeight)) }
        }
    }

    override fun eventProperty(property: String, value: Double) {
        if (!isActiveOwner()) return
        when (property) {
            "time-pos" -> {
                positionMs = value.secondsToMs()
                return
            }
            "duration" -> durationMs = value.secondsToMs()
            "demuxer-cache-time" -> bufferedMs = value.secondsToMs()
            "speed" -> playbackSpeed = value.toFloat()
        }
    }

    override fun eventProperty(property: String, value: Boolean) {
        if (!isActiveOwner()) return
        when (property) {
            "pause" -> paused = value
            "paused-for-cache" -> pausedForCache = value
            "eof-reached" -> eofReached = value
            else -> return
        }
        notifyState()
    }

    override fun eventProperty(property: String, value: String) = Unit

    private fun executeLoad(load: PendingLoad) {
        pendingLoad = null
        fileLoading = true
        MPVLib.command(arrayOf("loadfile", load.uri))
        MPVLib.setPropertyBoolean("pause", !load.playWhenReady)
    }

    private fun attachSurface(surface: Surface): Boolean {
        if (released.get() || !surface.isValid) return false
        // SurfaceHolder commonly retains the Java Surface object while replacing its native
        // window. Surface.toString includes that native identity and changes with the BufferQueue.
        val surfaceToken = surface.toString()
        if (surfaceAttached && attachedSurfaceToken == surfaceToken) return false
        if (surfaceAttached) detachSurface()
        MPVLib.attachSurface(surface)
        MPVLib.setPropertyString("force-window", "yes")
        surfaceAttached = true
        attachedSurfaceToken = surfaceToken
        attachedSurfaceWidth = 0
        attachedSurfaceHeight = 0
        Log.i(TAG, "surface attached token=$surfaceToken")
        return true
    }

    private fun bindSurface(surface: Surface, width: Int, height: Int) {
        val newlyAttached = attachSurface(surface)
        if (!surfaceAttached || width <= 0 || height <= 0) return
        val sizeChanged = attachedSurfaceWidth != width || attachedSurfaceHeight != height
        if (sizeChanged) {
            attachedSurfaceWidth = width
            attachedSurfaceHeight = height
            MPVLib.setPropertyString("android-surface-size", "${width}x$height")
            Log.i(TAG, "surface size=${width}x$height")
            publishAnime4KStatus()
        }
        // surfaceCreated attaches first and surfaceChanged supplies the size afterwards, so the
        // restart flag must survive across those two callbacks instead of depending on this call
        // being the one that attached the Surface.
        val restartedVideoOutput = videoOutputNeedsRestart
        if (restartedVideoOutput) {
            // vo=gpu owns an EGL context for the old native window. Merely replacing wid leaves
            // that context rendering into an abandoned BufferQueue, so recreate the VO here.
            MPVLib.setPropertyString("vo", "gpu")
            videoOutputNeedsRestart = false
            // Qualcomm MediaCodec on some vendor builds does not resume producing frames after
            // vo=null -> vo=gpu, even after video-reload/seek. Restart mpv's current playlist
            // entry and let FILE_LOADED restore the position. This rebuilds the decoder/demuxer
            // without asking EasyBangumi to resolve the source again.
            val resumePlayback = !paused
            pendingStartPositionMs = positionMs
            fileLoaded = false
            fileLoading = true
            MPVLib.command(arrayOf("playlist-play-index", "current"))
            MPVLib.setPropertyBoolean("pause", !resumePlayback)
            Log.i(TAG, "video output restarted")
        }
        if (fileLoaded && (newlyAttached || sizeChanged) && !restartedVideoOutput) {
            // Submit the exact BufferQueue size before asking vo=gpu to redraw the retained frame.
            MPVLib.command(arrayOf("seek", "0", "relative", "exact"))
        }
        pendingLoad?.let(::executeLoad)
    }

    private fun bindTextureSurface(
        textureView: TextureView,
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int,
    ) {
        if (released.get()) return
        val retained = retainedSurfaceTexture
        if (retained != null && retained !== surfaceTexture) {
            // TextureView may create a replacement SurfaceTexture during window reparenting even
            // though the Activity and playback ViewModel survived. Put the retained producer back
            // on the view so mpv can keep its EGL/MediaCodec output and network connection.
            val restored = runCatching {
                if (textureView.surfaceTexture !== retained) {
                    textureView.setSurfaceTexture(retained)
                }
            }.onFailure {
                Log.w(TAG, "failed to restore retained TextureView surface; rebinding", it)
            }.isSuccess
            if (restored) {
                textureSurface?.let { bindSurface(it, width, height) }
                Log.i(TAG, "texture surface restored")
                return
            }
        }
        if (retainedSurfaceTexture !== surfaceTexture) {
            textureSurface?.release()
            runCatching { retainedSurfaceTexture?.release() }
            retainedSurfaceTexture = surfaceTexture
            textureSurface = Surface(surfaceTexture)
            Log.i(TAG, "texture surface created")
        }
        textureSurface?.let { bindSurface(it, width, height) }
    }

    private fun detachSurface() {
        if (!surfaceAttached) return
        if (fileLoaded || fileLoading) {
            // Match mpv-android's lifecycle contract: stop the VO before releasing the native
            // window, then recreate it after the next Surface has supplied its exact dimensions.
            MPVLib.setPropertyString("vo", "null")
            videoOutputNeedsRestart = true
        }
        MPVLib.setPropertyString("force-window", "no")
        surfaceAttached = false
        attachedSurfaceToken = ""
        attachedSurfaceWidth = 0
        attachedSurfaceHeight = 0
        publishAnime4KStatus()
        MPVLib.detachSurface()
        Log.i(TAG, "surface detached")
    }

    private fun releaseForReplacementLocked() {
        if (!released.compareAndSet(false, true)) return
        releaseNativeLocked()
    }

    private fun releaseNativeLocked() {
        attachedView?.let(::detach)
        MPVLib.removeObserver(this)
        if (activeOwner === this) activeOwner = null
        MPVLib.destroy()
        listeners.clear()
    }

    private fun isActiveOwner(): Boolean = !released.get() && activeOwner === this

    private fun publishAnime4KStatus(enabled: Boolean = preferences.mpvAnime4kEnabled.get()) {
        _anime4KStatus.value = MpvAnime4KStatus(
            enabled = enabled,
            inputWidth = videoWidth,
            inputHeight = videoHeight,
            outputWidth = attachedSurfaceWidth,
            outputHeight = attachedSurfaceHeight,
        )
    }

    private fun applyHeaders(headers: Map<String, String>) {
        headers.entries.firstOrNull { it.key.equals("User-Agent", true) }
            ?.value?.let { MPVLib.setPropertyString("user-agent", it) }
        headers.entries.firstOrNull { it.key.equals("Referer", true) || it.key.equals("Referrer", true) }
            ?.value?.let { MPVLib.setPropertyString("referrer", it) }
        val custom = headers.filterKeys {
            !it.equals("User-Agent", true) && !it.equals("Referer", true) && !it.equals("Referrer", true)
        }
        MPVLib.setPropertyString(
            "http-header-fields",
            custom.entries.joinToString(",") { "${it.key}: ${it.value}" },
        )
    }

    private fun resolvePlayableUri(uri: String): String? {
        if (!uri.startsWith("content://")) return uri
        return runCatching {
            val descriptor = requireNotNull(
                appContext.contentResolver.openFileDescriptor(Uri.parse(uri), "r"),
            )
            "fdclose://${descriptor.detachFd()}"
        }.getOrNull()
    }

    private fun observe(property: String, format: Int) = MPVLib.observeProperty(property, format)

    private fun notifyState() = post {
        val state = playbackState
        listeners.forEach {
            it.onPlaybackStateChanged(state)
            it.onPlayWhenReadyChanged(playWhenReady)
            it.onIsPlayingChanged(isPlaying)
        }
    }

    private fun notifyPosition() = post {
        listeners.forEach { it.onPositionDiscontinuity(positionMs) }
    }

    private fun post(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }

    private fun Double.secondsToMs(): Long = (this * 1000.0).toLong().coerceAtLeast(0L)

    private data class PendingLoad(val uri: String, val playWhenReady: Boolean)

    private companion object {
        const val TAG = "MpvPlaybackController"
        const val DEFAULT_SEEK_MS = 15_000L
        val SESSION_LOCK = Any()
        @Volatile var activeOwner: MpvPlaybackController? = null
    }
}

private class MpvAnime4KShaderStore(
    private val context: Context,
) {
    private val root by lazy { deploy() }

    fun resolve(preset: SettingPreferences.MpvAnime4KPreset): List<String> {
        val names = when (preset) {
            SettingPreferences.MpvAnime4KPreset.FAST -> listOf(
                "Anime4K_Clamp_Highlights.glsl",
                "Anime4K_Restore_CNN_S.glsl",
                "Anime4K_Upscale_CNN_x2_S.glsl",
            )
            SettingPreferences.MpvAnime4KPreset.QUALITY -> listOf(
                "Anime4K_Clamp_Highlights.glsl",
                "Anime4K_Restore_CNN_M.glsl",
                "Anime4K_Upscale_CNN_x2_M.glsl",
                "Anime4K_AutoDownscalePre_x2.glsl",
                "Anime4K_Upscale_CNN_x2_S.glsl",
            )
            SettingPreferences.MpvAnime4KPreset.STRONG -> listOf(
                "Anime4K_Clamp_Highlights.glsl",
                "Anime4K_Restore_CNN_L.glsl",
                "Anime4K_Upscale_CNN_x2_L.glsl",
                "Anime4K_Restore_CNN_M.glsl",
                "Anime4K_AutoDownscalePre_x2.glsl",
                "Anime4K_Upscale_CNN_x2_M.glsl",
            )
        }
        return names.map { File(root, it).absolutePath }
    }

    private fun deploy(): File {
        val parent = File(context.filesDir, "mpv/anime4k")
        val target = File(parent, VERSION)
        if (target.isDirectory) return target
        parent.deleteRecursively()
        target.mkdirs()
        context.assets.list(ASSET_DIR).orEmpty().forEach { name ->
            context.assets.open("$ASSET_DIR/$name").use { input ->
                File(target, name).outputStream().use { output -> input.copyTo(output) }
            }
        }
        return target
    }

    private companion object {
        const val ASSET_DIR = "anime4k"
        const val VERSION = "v4"
    }
}
