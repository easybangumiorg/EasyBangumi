package com.heyanle.easybangumi4.anime4k

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.media3.common.Effect
import androidx.media3.common.PlaybackException
import androidx.media3.common.VideoFrameProcessingException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.setting.SettingPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class Anime4KPlaybackConfig(
    val enabled: Boolean,
    val mode: Int,
    val quality: String,
    val scale: Int,
) {
    fun normalized(): Anime4KPlaybackConfig {
        if (!enabled) return Disabled
        return copy(
            mode = mode.takeIf { it in A4KChain.MODE_NAMES.indices } ?: A4KChain.DEFAULT_MODE,
            quality = quality.takeIf(A4KChain.QUALITIES::contains) ?: A4KChain.DEFAULT_QUALITY,
            scale = scale.takeIf { it in SupportedScales } ?: 0,
        )
    }

    companion object {
        val SupportedScales = setOf(0, 1, 2, 4)
        val Disabled = Anime4KPlaybackConfig(
            enabled = false,
            mode = A4KChain.DEFAULT_MODE,
            quality = A4KChain.DEFAULT_QUALITY,
            scale = 0,
        )
    }
}

internal sealed interface Anime4KRuntimeState {
    data object Disabled : Anime4KRuntimeState
    data class Applying(val config: Anime4KPlaybackConfig) : Anime4KRuntimeState
    data class Active(val config: Anime4KPlaybackConfig) : Anime4KRuntimeState
    data class Failed(val message: String) : Anime4KRuntimeState
}

/**
 * Owns the Media3 video-effect pipeline for one player instance.
 *
 * Source resolution deliberately does not live here. Rapid preference changes are coalesced before
 * registering a new Media3 input stream, and effect failures are reported to the playback owner so
 * it can recover from the already resolved [com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo].
 */
@androidx.annotation.OptIn(UnstableApi::class)
internal class Anime4KPlaybackController(
    context: Context,
    private val player: ExoPlayer,
    private val preferences: SettingPreferences,
    private val scope: CoroutineScope,
    private val onPipelineResetRequired: (String) -> Unit,
) {
    private val appContext = context.applicationContext
    private val activityManager = appContext.getSystemService(ActivityManager::class.java)
    private val deviceProfile = Anime4KDeviceProfile(
        memoryClassMb = activityManager?.memoryClass ?: 256,
        isLowRamDevice = activityManager?.isLowRamDevice ?: false,
    )
    private val _runtimeState = MutableStateFlow<Anime4KRuntimeState>(Anime4KRuntimeState.Disabled)
    val runtimeState = _runtimeState.asStateFlow()
    private val _scaleCapability = MutableStateFlow(Anime4KScaleCapability())
    val scaleCapability = _scaleCapability.asStateFlow()

    private var collectionJob: Job? = null
    private var lastAppliedConfig: Anime4KPlaybackConfig? = null
    private var inputWidth = 0
    private var inputHeight = 0
    private var maxTextureSize = 0

    fun start() {
        if (collectionJob != null) return
        collectionJob = scope.launch {
            var hasReceivedInitialConfig = false
            combine(
                preferences.anime4kEnabled.flow(),
                preferences.anime4kMode.flow(),
                preferences.anime4kQuality.flow(),
                preferences.anime4kScale.flow(),
            ) { enabled, mode, quality, scale ->
                Anime4KPlaybackConfig(enabled, mode, quality, scale)
            }
                .map(Anime4KPlaybackConfig::normalized)
                .distinctUntilChanged()
                .collectLatest { config ->
                    // Media3 turns every effect change into a new registered video input stream.
                    // Coalesce quick switch/quality/mode taps so only the final config reaches it.
                    if (hasReceivedInitialConfig) delay(CONFIG_SETTLE_MILLIS)
                    hasReceivedInitialConfig = true
                    apply(config)
                }
        }
    }

    private suspend fun apply(config: Anime4KPlaybackConfig) {
        if (config == lastAppliedConfig) return
        _runtimeState.value = Anime4KRuntimeState.Applying(config)
        val effects: List<Effect> = if (config.enabled) {
            val passes = withContext(Dispatchers.Default) {
                Anime4KSource.chainFor(appContext, config.mode, config.quality)
            }
            listOf(
                Anime4KEffect(
                    passes = passes,
                    scaleOverride = config.scale,
                    deviceProfile = deviceProfile,
                    onSafetyEvent = ::onSafetyEvent,
                ),
            )
        } else {
            emptyList()
        }
        runCatching { player.setVideoEffects(effects) }
            .onSuccess {
                lastAppliedConfig = config
                _runtimeState.value = if (config.enabled) {
                    Anime4KRuntimeState.Active(config)
                } else {
                    Anime4KRuntimeState.Disabled
                }
                Log.i(TAG, "effect applied enabled=${config.enabled} mode=${config.mode} quality=${config.quality} scale=${config.scale}")
            }
            .onFailure(::disableAfterFailure)
    }

    /** Clears the failed graph and reflects the fallback in persisted/UI state. */
    fun disableAfterFailure(throwable: Throwable) {
        Log.e(TAG, "effect failed; disable without source re-resolution", throwable)
        runCatching { player.setVideoEffects(emptyList()) }
        lastAppliedConfig = Anime4KPlaybackConfig.Disabled
        _runtimeState.value = Anime4KRuntimeState.Failed(
            throwable.message ?: "Anime4K 渲染失败，已关闭",
        )
        if (preferences.anime4kScale.get() > 0) {
            fallbackToAutomaticScale("检测到超分渲染异常")
        } else if (preferences.anime4kEnabled.get()) {
            preferences.anime4kEnabled.set(false)
            showToast("Anime4K 渲染失败，已自动关闭")
        }
    }

    fun requestScale(scale: Int): Boolean {
        if (scale == 0 || scale in _scaleCapability.value.supportedScales) {
            preferences.anime4kScale.set(scale)
            return true
        }
        val reason = _scaleCapability.value.unsupportedReasons[scale]
            ?: "当前视频或设备不支持 ${scale}× 超分"
        showToast(reason)
        return false
    }

    fun onVideoSizeChanged(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        inputWidth = width
        inputHeight = height
        updateScaleCapability()
    }

    fun onDroppedVideoFrames(count: Int, elapsedMs: Long) {
        if (preferences.anime4kScale.get() <= 0) return
        if (count >= DROPPED_FRAME_LIMIT && elapsedMs <= DROPPED_FRAME_WINDOW_MILLIS) {
            fallbackToAutomaticScale("检测到超分导致持续卡顿")
        }
    }

    private fun onSafetyEvent(event: Anime4KSafetyEvent) {
        scope.launch {
            when (event) {
                is Anime4KSafetyEvent.Capability -> {
                    inputWidth = event.inputWidth
                    inputHeight = event.inputHeight
                    maxTextureSize = event.maxTextureSize
                    updateScaleCapability()
                }
                is Anime4KSafetyEvent.AutomaticFallback -> fallbackToAutomaticScale(event.reason)
                is Anime4KSafetyEvent.RenderFailure -> {
                    runCatching { player.setVideoEffects(emptyList()) }
                    lastAppliedConfig = Anime4KPlaybackConfig.Disabled
                    if (preferences.anime4kScale.get() > 0) {
                        fallbackToAutomaticScale(event.reason)
                    } else if (preferences.anime4kEnabled.get()) {
                        preferences.anime4kEnabled.set(false)
                        showToast("${event.reason}，已关闭 Anime4K")
                    }
                    onPipelineResetRequired(event.reason)
                }
            }
        }
    }

    private fun updateScaleCapability() {
        _scaleCapability.value = Anime4KSafetyPolicy.capability(
            inputWidth = inputWidth,
            inputHeight = inputHeight,
            displayWidth = appContext.resources.displayMetrics.widthPixels,
            maxTextureSize = maxTextureSize,
            deviceProfile = deviceProfile,
        )
        val selectedScale = preferences.anime4kScale.get()
        if (selectedScale > 0 && selectedScale !in _scaleCapability.value.supportedScales) {
            fallbackToAutomaticScale(
                _scaleCapability.value.unsupportedReasons[selectedScale]
                    ?: "当前视频不适合 ${selectedScale}× 超分",
            )
        }
    }

    private fun fallbackToAutomaticScale(reason: String) {
        if (preferences.anime4kScale.get() == 0) return
        preferences.anime4kScale.set(0)
        showToast("$reason，已改为自动倍率")
        Log.w(TAG, "fallback to automatic scale: $reason")
    }

    private fun showToast(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "Anime4KController"
        internal const val CONFIG_SETTLE_MILLIS = 250L
        private const val DROPPED_FRAME_LIMIT = 12
        private const val DROPPED_FRAME_WINDOW_MILLIS = 10_000L

        fun isVideoEffectError(error: PlaybackException): Boolean {
            if (isVideoEffectErrorCode(error.errorCode)) {
                return true
            }
            var cause: Throwable? = error
            while (cause != null) {
                if (cause is VideoFrameProcessingException ||
                    cause::class.java.name.endsWith("VideoSinkException")
                ) {
                    return true
                }
                cause = cause.cause
            }
            return false
        }

        internal fun isVideoEffectErrorCode(errorCode: Int): Boolean =
            errorCode == PlaybackException.ERROR_CODE_VIDEO_FRAME_PROCESSOR_INIT_FAILED ||
                errorCode == PlaybackException.ERROR_CODE_VIDEO_FRAME_PROCESSING_FAILED
    }
}
