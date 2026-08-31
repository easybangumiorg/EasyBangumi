package com.heyanle.easybangumi4.player.mpv

import android.content.Context
import com.heyanle.easybangumi4.setting.SettingPreferences
import kotlinx.coroutines.flow.StateFlow
import loli.ball.easyplayer2.EasyPlayerController

/**
 * Compile-time boundary between the common playback UI and the optional Anime4K flavor.
 *
 * The normal flavor implements only the factory and returns `null`; it never compiles the native
 * controller, shader store or dev.jdtech.mpv wrapper into its artifact.
 */
interface MpvPlaybackControllerContract : EasyPlayerController {
    val anime4KStatus: StateFlow<MpvAnime4KStatus>

    fun load(
        uri: String,
        headers: Map<String, String>,
        startPositionMs: Long,
        playWhenReady: Boolean,
    )

    fun applyAnime4K()

    fun release()
}

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

fun createMpvPlaybackController(
    context: Context,
    preferences: SettingPreferences,
): MpvPlaybackControllerContract? = createFlavorMpvPlaybackController(context, preferences)
