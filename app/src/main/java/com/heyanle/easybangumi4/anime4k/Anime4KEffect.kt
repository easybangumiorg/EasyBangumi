package com.heyanle.easybangumi4.anime4k

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram

/**
 * Anime4K 高清渲染效果。
 *
 * 挂到 ExoPlayer#setVideoEffects 上即可：管线在解码帧（NATIVE 尺寸）上运行
 * 整条 shader 链，输出放大后的帧，由 Media3 呈现。
 */
@OptIn(UnstableApi::class)
internal class Anime4KEffect(
    private val passes: List<A4KPass>,
    private val scaleOverride: Int = 0,
    private val deviceProfile: Anime4KDeviceProfile,
    private val onSafetyEvent: (Anime4KSafetyEvent) -> Unit = {},
) : GlEffect {

    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram {
        // 实时读屏宽：旋转（竖屏/横屏）后倍率能随显示宽度更新
        return Anime4KRenderer(
            useHighPrecision = useHdr,
            passes = passes,
            displayWidthProvider = { context.resources.displayMetrics.widthPixels },
            scaleOverride = scaleOverride,
            deviceProfile = deviceProfile,
            onSafetyEvent = onSafetyEvent,
        )
    }

    override fun isNoOp(inputWidth: Int, inputHeight: Int): Boolean {
        return passes.isEmpty()
    }
}
