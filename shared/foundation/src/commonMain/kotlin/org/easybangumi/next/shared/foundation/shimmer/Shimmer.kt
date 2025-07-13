package org.easybangumi.next.shared.foundation.shimmer

import androidx.annotation.FloatRange
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import org.easybangumi.next.shared.foundation.PositionTracker
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

private val LocalShimmerState = staticCompositionLocalOf<ShimmerState> {
    error("Modifier.shimmerItem should be used inside ShimmerHost")
}

data class ShimmerState(
    val isVisible: Boolean,
    val config: ShimmerConfig,
)

@Composable
fun rememberShimmerState(
    visible: Boolean,
    config: ShimmerConfig = ShimmerConfig(),
): ShimmerState {
    return remember(visible, config) {
        ShimmerState(visible, config)
    }
}

enum class ShimmerDirection {
    LeftToRight,
    TopToBottom,
    RightToLeft,
    BottomToTop
}


class ShimmerFloatScope(
    internal val positionTracker: PositionTracker,
    val state: ShimmerState,
    val boxScope: BoxScope,
): BoxScope by boxScope {

    @Composable
    fun Modifier.rectContentKey(
        key: String,
    ) = composed {
        val notDrawModifier = this.then(ShimmerItemModifierWhenVisible(false))
        val position = positionTracker.getRelativePosition(key, "shimmer_host") ?: return@composed notDrawModifier
        val size = positionTracker.getSize(key) ?: return@composed notDrawModifier
        offset {
            IntOffset(position.x.toInt(), position.y.toInt())
        }.size(
            with(LocalDensity.current) { DpSize( size.width.toDp(), size.height.toDp())}
        )
    }


    @Composable
    fun Modifier.onShimmerVisible(
        modifierWhenShimmerVisible: Modifier.() -> Modifier = { this },
    ): Modifier {
//        val state = LocalShimmerState.current
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
//        val state = LocalShimmerState.current
        if (!state.isVisible) {
            // Ensure that the modifier is applied only when shimmer is invisible
            return modifierWhenShimmerInvisible(this)
        }
        return this
    }



}

class ShimmerContentScope(
    internal val positionTracker: PositionTracker,
    val state: ShimmerState,
) {
    @Composable
    fun Modifier.mark(
        key: String,
    ): Modifier = onPlaced {
        positionTracker.setPosition(key, it.positionInRoot())
    }.onSizeChanged {
        positionTracker.setSize(key, it)
    }


    @Composable
    fun Modifier.onShimmerVisible(
        modifierWhenShimmerVisible: Modifier.() -> Modifier = { this },
    ): Modifier {
        return onShimmerVisible(state, modifierWhenShimmerVisible)
    }

    @Composable
    fun Modifier.onShimmerInvisible(
        modifierWhenShimmerInvisible: Modifier.() -> Modifier = { this },
    ): Modifier {
        return onShimmerInvisible(state, modifierWhenShimmerInvisible)
    }

    @Composable
    fun Modifier.drawRectWhenShimmerVisible(
        enable: Boolean = true,
    ): Modifier {
        return drawRectWhenShimmerVisible(state, enable)
    }

    @Composable
    fun Modifier.dismissWhenShimmerVisible(
        enable: Boolean = true,
    ): Modifier {
        return dismissWhenShimmerVisible(state, enable)
    }

    @Composable
    fun Modifier.shimmerItem(
        showWhenShimmerVisible: Boolean = true,
        modifierWhenShimmerVisible: Modifier.() -> Modifier = { this },
        customDraw: (ContentDrawScope.(ShimmerConfig) -> Unit)? = null,
    ): Modifier {
        return shimmerItem(
            state = state,
            showWhenShimmerVisible = showWhenShimmerVisible,
            modifierWhenShimmerVisible = modifierWhenShimmerVisible,
            customDraw = customDraw
        )
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
    floatContent: @Composable ShimmerFloatScope.(ShimmerState) -> Unit = {},
    content: @Composable ShimmerContentScope.(ShimmerState) -> Unit,
) {

    val state = remember(visible, config) {
        ShimmerState(visible, config)
    }
    CompositionLocalProvider(LocalShimmerState provides state){
        Box(
            modifier = modifier.onPlaced {
                positionTracker.setPosition("shimmer_host", it.positionInRoot())
            }.onSizeChanged {
                positionTracker.setSize("shimmer_host", it)
            }
        ) {
            val contentScope = remember(positionTracker) {
                ShimmerContentScope(positionTracker, state)
            }


            Box(Modifier.fillMaxSize().shimmer(state)) {
                contentScope.content(state)
            }
            Box {
                val floatScope = remember(positionTracker) {
                    ShimmerFloatScope(positionTracker, state, this)
                }
                floatScope.floatContent(state)
            }

        }
    }
}

@Composable
fun Modifier.shimmer(
    state: ShimmerState
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
    val angle: Float = 45f,
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



class ShimmerModifier(
    private val state: ShimmerState,
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
    private val config: ShimmerConfig? = null,
) : DrawModifier {
    override fun ContentDrawScope.draw() {
        if (canDraw) {
            if (customDraw == null) {
                drawContent()
            } else if (config != null) {
                customDraw(config)
            }
        } else {
            // Do not draw anything, effectively hiding the content
        }
    }


}


@Composable
fun Modifier.onShimmerVisible(
    state: ShimmerState,
    modifierWhenShimmerVisible: Modifier.() -> Modifier = { this },
): Modifier {
//        val state = LocalShimmerState.current
    if (state.isVisible) {
        // Ensure that the modifier is applied only when shimmer is visible
        return modifierWhenShimmerVisible(this)
    }
    return this
}

@Composable
fun Modifier.onShimmerInvisible(
    state: ShimmerState,
    modifierWhenShimmerInvisible: Modifier.() -> Modifier = { this },
): Modifier {
//        val state = LocalShimmerState.current
    if (!state.isVisible) {
        // Ensure that the modifier is applied only when shimmer is invisible
        return modifierWhenShimmerInvisible(this)
    }
    return this
}

@Composable
fun Modifier.drawRectWhenShimmerVisible(
    state: ShimmerState,
    enable: Boolean = true,
): Modifier {
    if (!enable) {
        return this
    }
    return shimmerItem(state, true, {this}, SHIMMER_ITEM_RECT_ON_DRAW)
}

@Composable
fun Modifier.dismissWhenShimmerVisible(
    state: ShimmerState,
    enable: Boolean = true,
): Modifier {
    if (!enable) {
        return this
    }
    return shimmerItem(state, false, {this}, null)
}

@Composable
fun Modifier.shimmerItem(
    state: ShimmerState,
    showWhenShimmerVisible: Boolean = true,
    modifierWhenShimmerVisible: Modifier.() -> Modifier = { this },
    customDraw: (ContentDrawScope.(ShimmerConfig) -> Unit)? = null,
): Modifier {
    return composed {

//        val shimmerState = LocalShimmerState.current
        val ss = state
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

