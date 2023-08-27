package com.heyanle.easybangumi4.ui.common

/**
 * Created by LoliBall on 2023/2/24 19:22.
 * https://github.com/WhichWho
 */


/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefreshIndicatorTransform
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * The default indicator for Compose pull-to-refresh, based on Android's SwipeRefreshLayout.
 *
 * @sample androidx.compose.material.samples.PullRefreshSample
 *
 * @param refreshing A boolean representing whether a refresh is occurring.
 * @param state The [PullRefreshState] which controls where and how the indicator will be drawn.
 * @param modifier Modifiers for the indicator.
 * @param backgroundColor The color of the indicator's background.
 * @param contentColor The color of the indicator's arc and arrow.
 * @param scale A boolean controlling whether the indicator's size scales with pull progress or not.
 */
@Composable
@ExperimentalMaterialApi
// TODO(b/244423199): Consider whether the state parameter should be replaced with lambdas to
//  enable people to use this indicator with custom pull-to-refresh components.
fun MD3PullRefreshIndicator(
    refreshing: Boolean,
    state: PullRefreshState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
    contentColor: Color = contentColorFor(backgroundColor),
    scale: Boolean = false
) {
//    val showElevation by remember(refreshing, state) {
//        derivedStateOf { refreshing || state.progress > 0.01f }
//    }


    Surface(
        modifier = modifier
            .size(IndicatorSize)
            .pullRefreshIndicatorTransform(state, scale),
        shape = SpinnerShape,
        color = backgroundColor,
//        elevation = if (showElevation) Elevation else 0.dp,
    ) {
        Crossfade(
            targetState = refreshing,
            animationSpec = tween(durationMillis = CrossfadeDurationMs), label = ""
        ) { refreshing ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val spinnerSize = (ArcRadius + StrokeWidth).times(2)

                if (refreshing) {
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = StrokeWidth,
                        modifier = Modifier.size(spinnerSize),
                    )
                } else {
                    CircularArrowIndicator(state, contentColor, Modifier.size(spinnerSize))
                }
            }
        }
    }
}

/**
 * Modifier.size MUST be specified.
 */
@Composable
@ExperimentalMaterialApi
private fun CircularArrowIndicator(
    state: PullRefreshState,
    color: Color,
    modifier: Modifier,
) {
    val path = remember { Path().apply { fillType = PathFillType.EvenOdd } }

    Canvas(modifier.semantics { contentDescription = "Refreshing" }) {
        val values = ArrowValues(state.progress)

        rotate(degrees = values.rotation) {
            val arcRadius = ArcRadius.toPx() + StrokeWidth.toPx() / 2f
            val arcBounds = Rect(
                size.center.x - arcRadius,
                size.center.y - arcRadius,
                size.center.x + arcRadius,
                size.center.y + arcRadius
            )
            drawArc(
                color = color,
                alpha = values.alpha,
                startAngle = values.startAngle,
                sweepAngle = values.endAngle - values.startAngle,
                useCenter = false,
                topLeft = arcBounds.topLeft,
                size = arcBounds.size,
                style = Stroke(
                    width = StrokeWidth.toPx(),
                    cap = StrokeCap.Square
                )
            )
            drawArrow(path, arcBounds, color, values)
        }
    }
}

@Immutable
private class ArrowValues(
    val alpha: Float,
    val rotation: Float,
    val startAngle: Float,
    val endAngle: Float,
    val scale: Float
)

private fun ArrowValues(progress: Float): ArrowValues {
    // Discard first 40% of progress. Scale remaining progress to full range between 0 and 100%.
    val adjustedPercent = max(min(1f, progress) - 0.4f, 0f) * 5 / 3
    // How far beyond the threshold pull has gone, as a percentage of the threshold.
    val overshootPercent = abs(progress) - 1.0f
    // Limit the overshoot to 200%. Linear between 0 and 200.
    val linearTension = overshootPercent.coerceIn(0f, 2f)
    // Non-linear tension. Increases with linearTension, but at a decreasing rate.
    val tensionPercent = linearTension - linearTension.pow(2) / 4

    // Calculations based on SwipeRefreshLayout specification.
    val alpha = progress.coerceIn(0f, 1f)
    val endTrim = adjustedPercent * MaxProgressArc
    val rotation = (-0.25f + 0.4f * adjustedPercent + tensionPercent) * 0.5f
    val startAngle = rotation * 360
    val endAngle = (rotation + endTrim) * 360
    val scale = min(1f, adjustedPercent)

    return ArrowValues(alpha, rotation, startAngle, endAngle, scale)
}

private fun DrawScope.drawArrow(arrow: Path, bounds: Rect, color: Color, values: ArrowValues) {
    arrow.reset()
    arrow.moveTo(0f, 0f) // Move to left corner
    arrow.lineTo(x = ArrowWidth.toPx() * values.scale, y = 0f) // Line to right corner

    // Line to tip of arrow
    arrow.lineTo(
        x = ArrowWidth.toPx() * values.scale / 2,
        y = ArrowHeight.toPx() * values.scale
    )

    val radius = min(bounds.width, bounds.height) / 2f
    val inset = ArrowWidth.toPx() * values.scale / 2f
    arrow.translate(
        Offset(
            x = radius + bounds.center.x - inset,
            y = bounds.center.y + StrokeWidth.toPx() / 2f
        )
    )
    arrow.close()
    rotate(degrees = values.endAngle) {
        drawPath(path = arrow, color = color, alpha = values.alpha)
    }
}

private const val CrossfadeDurationMs = 100
private const val MaxProgressArc = 0.8f

private val IndicatorSize = 50.dp
private val SpinnerShape = RoundedCornerShape(20)
private val ArcRadius = 7.5.dp
private val StrokeWidth = 2.5.dp
private val ArrowWidth = 10.dp
private val ArrowHeight = 5.dp
//private val Elevation = 6.dp
