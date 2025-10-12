package org.easybangumi.next.shared.foundation.seekbar

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import org.easybangumi.next.shared.foundation.scroll_header.logger
import kotlin.math.max
import kotlin.math.roundToInt

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

private enum class SeekComponents {
    THUMB,
    TRACK
}

@Composable
fun Seekbar(
    state: SeekbarState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    thumbSize: DpSize = DpSize(8.dp, 8.dp),
    interactionSource: MutableInteractionSource  = remember { MutableInteractionSource() },
    thumb: @Composable (SeekbarState, DpSize) -> Unit = { state, thumbSize -> SeekbarDefault.Thumb(interactionSource, thumbSize = thumbSize) },
    track: @Composable (SeekbarState) -> Unit = { SeekbarDefault.Track(state) },
) {
    val press = Modifier.sliderTapModifier(
        state = state,
        interactionSource = interactionSource,
        enabled = enabled
    )
    val drag = Modifier.draggable(
        orientation = Orientation.Horizontal,
        state = state,
        interactionSource = interactionSource,
        enabled = enabled,
        onDragStopped = { state.onGestureEnd() },
        startDragImmediately = state.isDragging,
    )
    val density = LocalDensity.current

    LaunchedEffect(thumbSize, state) {
        state.thumbWidth =  with(density){ thumbSize.width.toPx() }
    }

    Layout(
        content = {
            Box(
                modifier =
                    Modifier.layoutId(SeekComponents.THUMB).size(thumbSize)
            ) {
                thumb(state, thumbSize)
            }
            Box(modifier = Modifier.layoutId(SeekComponents.TRACK).fillMaxWidth()) { track(state) }
        },
        modifier = modifier
//            .minimumInteractiveComponentSize()
//            .requiredSizeIn(minWidth = 4.dp, minHeight = 4.dp)
            // 无障碍后续在优化
//            .sliderSemantics(state, enabled)
            .focusable(enabled, interactionSource)
            .then(press)
            .then(drag),
        measurePolicy = { measurables, constraints ->
            val thumbPlaceable =
                measurables.fastFirst { it.layoutId == SeekComponents.THUMB }.measure(constraints)
            val trackPlaceable =
                measurables
                    .fastFirst { it.layoutId == SeekComponents.TRACK }
                    .measure(constraints)
            val sliderWidth = trackPlaceable.width

            val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)
//            if (constraints.maxHeight != Constraints.Infinity) {
//                sliderHeight = max(sliderHeight, constraints.maxHeight)
//            }
            state.updateDimensions(trackPlaceable.height.toFloat(), sliderWidth)



            val trackOffsetX = with(density) { thumbSize.width.toPx() / 2f}.roundToInt()
            val thumbOffsetX = ((trackPlaceable.width) * state.coercedValueAsFraction).roundToInt()
            val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
            val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2


            layout(sliderWidth, sliderHeight) {
                trackPlaceable.placeRelative(trackOffsetX, trackOffsetY)
                thumbPlaceable.placeRelative(thumbOffsetX, thumbOffsetY)
            }
        }

    )
}

@Stable
private fun Modifier.sliderTapModifier(
    state: SeekbarState,
    interactionSource: MutableInteractionSource,
    enabled: Boolean
) =
    if (enabled) {
        pointerInput(state, interactionSource) {
            detectTapGestures(
                onPress = { state.onPress(it) },
                onTap = {
                    state.dispatchRawDelta(0f)
                    state.onGestureEnd()
                }
            )
        }
    } else {
        this
    }

