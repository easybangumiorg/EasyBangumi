package org.easybangumi.next.shared.foundation.seekbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults.TickSize
import androidx.compose.material3.SliderDefaults.TrackStopIndicatorSize
import androidx.compose.material3.SliderDefaults.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.logger.logger

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
object SeekbarDefault {

    private val logger = logger()

    @Composable
    fun Thumb(
        interactionSource: MutableInteractionSource,
        modifier: Modifier = Modifier,
        colors: SliderColors = colors(),
        enabled: Boolean = true,
        thumbSize: DpSize = DpSize(8.dp, 8.dp)
    ) {
        val interactions = remember { mutableStateListOf<Interaction>() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> interactions.add(interaction)
                    is PressInteraction.Release -> interactions.remove(interaction.press)
                    is PressInteraction.Cancel -> interactions.remove(interaction.press)
                    is DragInteraction.Start -> interactions.add(interaction)
                    is DragInteraction.Stop -> interactions.remove(interaction.start)
                    is DragInteraction.Cancel -> interactions.remove(interaction.start)
                }
            }
        }

        val size =
            if (interactions.isNotEmpty()) {
                thumbSize.copy(width = thumbSize.width / 2)
            } else {
                thumbSize
            }

        logger.info("size: $size, interactions: $interactions")
        Spacer(
            modifier
                .size(size)
                .hoverable(interactionSource = interactionSource)
                .background(
                    if (enabled) colors.thumbColor else colors.disabledThumbColor,
                    CircleShape
                )
        )
    }

    //    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Track(
        seekbarState: SeekbarState,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        colors: SliderColors = colors(),
        drawStopIndicator: (DrawScope.(Offset) -> Unit)? = {
            val color = if (enabled) {
                colors.activeTrackColor
            } else {
                colors.disabledActiveTrackColor
            }
            drawStopIndicator(
                drawScope = this,
                offset = it,
                color = color,
                size = TrackStopIndicatorSize
            )
        },
        drawTick: DrawScope.(Offset, Color) -> Unit = { offset, color ->
            drawStopIndicator(drawScope = this, offset = offset, color = color, size = TickSize)
        },
    ) {
        val inactiveTrackColor = if (enabled) colors.inactiveTrackColor else colors.disabledInactiveTrackColor
        val activeTrackColor = if (enabled) colors.activeTrackColor else colors.disabledActiveTrackColor
        val inactiveTickColor = if (enabled) colors.inactiveTickColor else colors.disabledInactiveTickColor
        val activeTickColor = if (enabled) colors.activeTickColor else colors.disabledActiveTickColor
        Canvas(
            modifier
                .fillMaxWidth()
                .height(4.dp)
        ){
            drawTrack(
                seekbarState.ticksIndex,
                seekbarState.value,
                seekbarState.maxValue,
                inactiveTrackColor,
                activeTrackColor,
                inactiveTickColor,
                activeTickColor,
                seekbarState.trackHeight.toDp(),
                drawStopIndicator,
                drawTick,
            )
        }


    }

    private fun drawStopIndicator(drawScope: DrawScope, offset: Offset, size: Dp, color: Color) {
        with(drawScope) { drawCircle(color = color, center = offset, radius = size.toPx() / 2f) }
    }

    fun DrawScope.drawTrack(
        ticksIndex: LongArray? = null,
        currentValue: Long,
        maxValue: Long,
        inactiveTrackColor: Color,
        activeTrackColor: Color,
        inactiveTickColor: Color,
        activeTickColor: Color,
        height: Dp,
        drawStopIndicator: (DrawScope.(Offset) -> Unit)?,
        drawTick: DrawScope.(Offset, Color) -> Unit,
    ) {
        val sliderStart = Offset(0f, center.y)
        val sliderEnd = Offset(size.width, center.y)
        val trackStrokeWidth = height.toPx()
        val sliderValue =
            Offset(sliderStart.x + (((sliderEnd.x - sliderStart.x) * currentValue) / maxValue.toFloat()), center.y)
        val cornerSize = trackStrokeWidth / 2
        // inactive track
        if (sliderValue.x < sliderEnd.x - cornerSize) {
            val start = sliderValue.x
            val end = sliderEnd.x
            drawTrackPath(
                Offset(start, 0f),
                Size(end - start, trackStrokeWidth),
                inactiveTrackColor,
                cornerSize,
                cornerSize
            )
            drawStopIndicator?.invoke(this, Offset(end - cornerSize, center.y))
        }

        // active track
        val activeTrackEnd = sliderValue.x
        if (activeTrackEnd > cornerSize) {
            drawTrackPath(
                Offset(0f, 0f),
                Size(activeTrackEnd, trackStrokeWidth),
                activeTrackColor,
                cornerSize,
                cornerSize
            )
        }

        val start = Offset(sliderStart.x + cornerSize, sliderStart.y)
        val end = Offset(sliderEnd.x - cornerSize, sliderEnd.y)
        ticksIndex?.forEachIndexed { index, tick ->
            // skip ticks that fall on the stop indicator
            if (drawStopIndicator != null) {
                if (index == ticksIndex.size - 1) {
                    return@forEachIndexed
                }
            }
            val outsideFraction = tick > currentValue || tick < 0
            val center = Offset(start.x + ((end.x - start.x) * tick) / maxValue.toFloat(), center.y)
            drawTick(
                this,
                center, // offset
                if (outsideFraction) inactiveTickColor else activeTickColor // color
            )
        }
    }

    private val trackPath = Path()
    private fun DrawScope.drawTrackPath(
        offset: Offset,
        size: Size,
        color: Color,
        startCornerRadius: Float,
        endCornerRadius: Float
    ) {
        val startCorner = CornerRadius(startCornerRadius, startCornerRadius)
        val endCorner = CornerRadius(endCornerRadius, endCornerRadius)
        val track =
            RoundRect(
                rect = Rect(Offset(offset.x, 0f), size = Size(size.width, size.height)),
                topLeft = startCorner,
                topRight = endCorner,
                bottomRight = endCorner,
                bottomLeft = startCorner
            )
        trackPath.addRoundRect(track)
        drawPath(trackPath, color)
        trackPath.rewind()
    }


}