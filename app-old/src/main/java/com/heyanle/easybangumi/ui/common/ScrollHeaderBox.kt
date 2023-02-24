package com.heyanle.easybangumi.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

/**
 * Created by HeYanLe on 2023/1/10 13:53.
 * https://github.com/heyanLE
 */

@Composable
fun ScrollHeaderBox(
    modifier: Modifier = Modifier,
    canScroll: (Offset) -> Boolean = { true },
    showForever: MutableState<Boolean> = mutableStateOf(false),
    header: @Composable (Dp) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {

    var headerHeightPx by remember {
        mutableStateOf(0)
    }
    var offsetHeightPx by remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!canScroll(available)) {
                    return Offset.Zero
                }
                if (available.x.absoluteValue > available.y.absoluteValue) {
                    return Offset.Zero
                }
                val delta = available.y

                val min = -headerHeightPx.toFloat()
                val max = 0F

                val old = offsetHeightPx
                val newOffset = offsetHeightPx + delta
                // 设置 Header 的位移范围
                offsetHeightPx = newOffset.coerceIn(-headerHeightPx.toFloat(), 0F)
                if (newOffset > max) {
                    return available.copy(y = +max - old)
                }
                if (newOffset < min) {
                    return available.copy(y = +min - old)
                }
                return available.copy(y = delta)
            }


        }
    }

    Box(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .then(modifier)
    ) {

        content(PaddingValues(top = with(LocalDensity.current) { (headerHeightPx + offsetHeightPx).toDp() }))

        val offsetY =
            if (showForever.value) 0.dp else with(LocalDensity.current) { offsetHeightPx.toDp() }

        Box(
            modifier = Modifier
                .onSizeChanged {
                    headerHeightPx = it.height
                }
                .clipToBounds()
        ) {
            header(offsetY)
        }

    }
}