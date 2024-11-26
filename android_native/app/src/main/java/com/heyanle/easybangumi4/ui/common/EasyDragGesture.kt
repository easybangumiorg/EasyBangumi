package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange

/**
 * Created by heyanlin on 2024/7/1.
 */
suspend fun PointerInputScope.detectHorizontalDragGesturesWithDown(
    onDown: (Offset) -> Unit = { },
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onHorizontalDrag: (change: PointerInputChange, dragAmount: Float) -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var overSlop = 0f
        onDown.invoke(down.position)
        val drag = awaitHorizontalTouchSlopOrCancellation(
            down.id,
        ) { change, over ->
            change.consume()
            overSlop = over
        }
        if (drag != null) {
            onDragStart.invoke(drag.position)
            onHorizontalDrag(drag, overSlop)
            if (
                horizontalDrag(drag.id) {
                    onHorizontalDrag(it, it.positionChange().x)
                    it.consume()
                }
            ) {
                onDragEnd()
            } else {
                onDragCancel()
            }
        }
    }
}