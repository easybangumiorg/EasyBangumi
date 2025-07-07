package org.easybangumi.next.shared.foundation.shimmer

import androidx.annotation.FloatRange
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.rotateRad
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.Constraints
import kotlin.math.PI
import kotlin.math.tan

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

private val LocalShimmerState = staticCompositionLocalOf<ShimmerLocalState> {
    error("Modifier.shimmerItem should be used inside ShimmerHost")
}

data class ShimmerLocalState(
    val isVisible: Boolean,
    val config: ShimmerConfig,
)

enum class ShimmerDirection {
    LeftToRight,
    TopToBottom,
    RightToLeft,
    BottomToTop
}

class ShimmerFloatScope(
    internal val positionTracker: PositionTracker,
) {

    @Composable
    fun Modifier.floatWithContentChildren(
        key: String,
    ) {
        val hostPosition = positionTracker.getPosition("shimmer_host")
        val position = positionTracker.getPosition(key)
    }


}

class ShimmerContentScope(
    internal val positionTracker: PositionTracker,
) {
    @Composable
    fun Modifier.markPosition(
        key: String,
    ): Modifier = onPlaced {
        positionTracker.setPosition(key, it.positionInRoot())
    }
}

@Composable
fun ShimmerHost(
    modifier: Modifier = Modifier,
    visible: Boolean,
    positionTracker: PositionTracker = remember {
        PositionTracker()
    },
    config: ShimmerConfig = ShimmerConfig(),
    floatContent: @Composable ShimmerFloatScope.() -> Unit = {},
    content: @Composable ShimmerContentScope.() -> Unit,
) {

    val state = remember(visible, config) {
        ShimmerLocalState(visible, config)
    }
    CompositionLocalProvider(LocalShimmerState provides state){
        Box(
            modifier = modifier.onPlaced {
                positionTracker.setPosition("root", it.positionInRoot())
            }
        ) {
            val contentScope = remember(positionTracker) {
                ShimmerContentScope(positionTracker)
            }

            val floatScope = remember(positionTracker) {
                ShimmerFloatScope(positionTracker)
            }
            Box(Modifier.shimmer(state)) {
                contentScope.content()
            }
            Box {
                floatScope.floatContent()
            }

        }
    }
}

@Composable
private fun Modifier.shimmer(
    state: ShimmerLocalState
): Modifier = composed {
    // make sure we are using the LocalShimmerState
    var progress: Float by remember { mutableStateOf(0f) }
    if (state.isVisible) {
        val infiniteTransition = rememberInfiniteTransition()
        val config = state.config
        progress = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(
                    durationMillis = config.duration.toInt(),
                    delayMillis = config.delay.toInt(),
                    easing = LinearEasing
                ),
                repeatMode = config.repeatMode
            ),
        ).value
    }
    ShimmerModifier(state= state, progress = progress)
}

data class ShimmerConfig(
    // 未高亮部分颜色
    val contentColor: Color = Color.LightGray.copy(alpha = 0.3f),
    // 高亮部分颜色
    val higLightColor: Color = Color.LightGray.copy(alpha = 0.9f),
    // 渐变部分宽度
    @FloatRange(from = 0.0, to = 1.0)
    val dropOff: Float = 0.5f,
    // 高亮部分宽度
    @FloatRange(from = 0.0, to = 1.0)
    val intensity: Float = 0.2f,
    //骨架屏动画方向
    val direction: ShimmerDirection = ShimmerDirection.LeftToRight,
    //动画旋转角度
    val angle: Float = 20f,
    //动画时长
    val duration: Float = 1000f,
    //两次动画间隔
    val delay: Float = 200f,
    val repeatMode: RepeatMode = RepeatMode.Restart
)

val SHIMMER_ITEM_RECT_ON_DRAW: (ContentDrawScope.(ShimmerConfig) -> Unit) = { config: ShimmerConfig ->
    drawRect(
        color = config.contentColor,
        size = size
    )
}

@Composable
fun Modifier.onShimmerVisible(
    modifierWhenShimmerVisible: Modifier.() -> Modifier = { this },
): Modifier {
    val state = LocalShimmerState.current
    if (state.isVisible) {
        // Ensure that the modifier is applied only when shimmer is visible
        return modifierWhenShimmerVisible(this)
    }
    return this
}

@Composable
fun Modifier.onShimmerInvisible(
    modifierWhenShimmerInvisible: Modifier.() -> Modifier = { this },
): Modifier {
    val state = LocalShimmerState.current
    if (!state.isVisible) {
        // Ensure that the modifier is applied only when shimmer is invisible
        return modifierWhenShimmerInvisible(this)
    }
    return this
}

@Composable
fun Modifier.drawRectWhenShimmerVisible(
    enable: Boolean = true,
): Modifier {
    if (!enable) {
        return this
    }
    return shimmerItem(true, {this}, SHIMMER_ITEM_RECT_ON_DRAW)
}

@Composable
fun Modifier.dismissWhenShimmerVisible(
    enable: Boolean = true,
): Modifier {
    if (!enable) {
        return this
    }
    return shimmerItem(false, {this}, null)
}

@Composable
fun Modifier.shimmerItem(
    showWhenShimmerVisible: Boolean = true,
    modifierWhenShimmerVisible: Modifier.() -> Modifier = { this },
    customDraw: (ContentDrawScope.(ShimmerConfig) -> Unit)? = null,
): Modifier {
    return composed {

        val shimmerState = LocalShimmerState.current
        val ss = shimmerState
        if (ss == null || !ss.isVisible) {
            return@composed this
        }
        modifierWhenShimmerVisible(this).then(
            ShimmerItemModifierWhenVisible(
                canDraw = ss.isVisible && showWhenShimmerVisible,
                customDraw = customDraw,
                config = ss.config
            )
        )
    }

}


class ShimmerModifier(
    private val state: ShimmerLocalState,
    private val progress: Float,
) : DrawModifier, LayoutModifier {

    private val visible: Boolean = state.isVisible
    private val config: ShimmerConfig = state.config

    private val cleanPaint = Paint()
    private val paint = Paint().apply {
        isAntiAlias = true
        style = PaintingStyle.Fill
        blendMode = BlendMode.SrcIn
    }

    private val angleTan = tan(config.angle.toDouble() * (PI / 180.0)).toFloat()
    private var translateHeight = 0f
    private var translateWidth = 0f
    private val intensity = config.intensity
    private val dropOff = config.dropOff
    private val colors = listOf(
        config.contentColor,
        config.higLightColor,
        config.higLightColor,
        config.contentColor
    )
    private val colorStops: List<Float> = listOf(
        ((1f - intensity - dropOff) / 2f).coerceIn(0f, 1f),
        ((1f - intensity - 0.001f) / 2f).coerceIn(0f, 1f),
        ((1f + intensity + 0.001f) / 2f).coerceIn(0f, 1f),
        ((1f + intensity + dropOff) / 2f).coerceIn(0f, 1f)
    )

    override fun ContentDrawScope.draw() {
        drawIntoCanvas {
            it.withSaveLayer(Rect(0f, 0f, size.width, size.height), paint = cleanPaint) {
                drawContent()
                if (visible) {
                    val (dx, dy) = when (config.direction) {
                        ShimmerDirection.LeftToRight -> Pair(
                            offset(-translateWidth, translateWidth, progress),
                            0f
                        )

                        ShimmerDirection.RightToLeft -> Pair(
                            offset(translateWidth, -translateWidth, progress),
                            0f
                        )

                        ShimmerDirection.TopToBottom -> Pair(
                            0f,
                            offset(-translateHeight, translateHeight, progress)
                        )


                        ShimmerDirection.BottomToTop -> Pair(
                            0f,
                            offset(translateHeight, -translateHeight, progress)
                        )

                    }
                    it.save()
//                    it.rotateRad(-config.angle, size.width / 2f, size.height / 2f)
                    it.translate(dx, dy)
                    it.drawRect(Rect(0f, 0f, size.width, size.height), paint = paint)
                    it.restore()
                }
            }
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        val size = Size(width = placeable.width.toFloat(), height = placeable.height.toFloat())
        updateSize(size)
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }

    private fun updateSize(size: Size) {
        translateWidth = size.width + angleTan * size.height
        translateHeight = size.height + angleTan * size.width
        val toOffset = when (config.direction) {
            ShimmerDirection.RightToLeft, ShimmerDirection.LeftToRight -> Offset(size.width, 0f)
            else -> Offset(0f, size.height)
        }
        paint.shader = LinearGradientShader(
            Offset(0f, 0f),
            toOffset,
            colors,
            colorStops
        )
    }

    //计算位置渐变
    private fun offset(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }
}

class ShimmerItemModifierWhenVisible(
    private val canDraw: Boolean,
    private val customDraw: (ContentDrawScope.(ShimmerConfig) -> Unit)? = null,
    private val config: ShimmerConfig,
) : DrawModifier {
    override fun ContentDrawScope.draw() {
        if (canDraw) {
            if (customDraw == null) {
                drawContent()
            } else {
                customDraw(config)
            }
        } else {
            // Do not draw anything, effectively hiding the content
        }
    }


}
