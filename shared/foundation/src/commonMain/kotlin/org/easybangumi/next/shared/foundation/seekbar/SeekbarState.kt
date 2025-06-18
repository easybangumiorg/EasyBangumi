package org.easybangumi.next.shared.foundation.seekbar

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.coroutineScope
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

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
class SeekbarState(
    initValue: Long,
    // only support { 0 - maxValue }
    val maxValue: Long,
    val ticksIndex: LongArray? = null,
): DraggableState {

    // 用户设置的虚拟数轴
    private var valueState by mutableLongStateOf(initValue)
    var value: Long
        set(newVal) {
            val coercedValue = newVal.coerceIn(0, maxValue)
            val ticksIndex = ticksIndex
            valueState = if (ticksIndex == null || ticksIndex.isEmpty()) {
                coercedValue
            } else {
                ticksIndex.minBy {
                    it - coercedValue
                }
            }
        }
        get() = valueState

    var onValueChange: ((Long) -> Unit)? = null
    var onValueChangeFinished: (() -> Unit)? = null

    val onGestureEnd = {
        if (!isDragging) {
            onValueChangeFinished?.invoke()
        }
    }

    val coercedValueAsFraction
        get() = if (maxValue == 0L) 0F else value.coerceIn(0, maxValue) / maxValue.toFloat()

    // 实际数轴
    private var rawOffset by mutableFloatStateOf(0f)
    private var pressOffset by mutableFloatStateOf(0f)
    private var totalWidth by mutableIntStateOf(0)
    internal var trackHeight by mutableFloatStateOf(0f)
    internal var thumbWidth by mutableFloatStateOf(0f)

    fun updateDimensions(newTrackHeight: Float, newTotalWidth: Int) {
        trackHeight = newTrackHeight
        totalWidth = newTotalWidth
    }

    private val dragScope: DragScope =
        object : DragScope {
            override fun dragBy(pixels: Float): Unit = dispatchRawDelta(pixels)
        }
    var isDragging by mutableStateOf(false)
        private set
    private val scrollMutex = MutatorMutex()


    fun onPress(pos: Offset) {
        pressOffset = pos.x - rawOffset
    }

    override fun dispatchRawDelta(delta: Float) {
        val maxPx = max(totalWidth - thumbWidth / 2, 0f)
        val minPx = min(thumbWidth / 2, maxPx)
        rawOffset = (rawOffset + delta + pressOffset)
        pressOffset = 0f
        val rawValue = (maxValue * (rawOffset - minPx) / (maxPx - minPx)).roundToLong()
        val ticketValue = ticksIndex?.minByOrNull {
            it - rawValue
        } ?: rawValue
        if (ticketValue != value) {
            value = ticketValue
            onValueChange?.invoke(ticketValue)
        }
    }

    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit
    ) = coroutineScope {
        isDragging = true
        scrollMutex.mutateWith(dragScope, dragPriority, block)
        isDragging = false
    }
}
