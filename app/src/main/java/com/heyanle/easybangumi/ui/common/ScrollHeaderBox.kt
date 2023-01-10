package com.heyanle.easybangumi.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp

/**
 * Created by HeYanLe on 2023/1/10 13:53.
 * https://github.com/heyanLE
 */

@Composable
fun ScrollHeaderBox(
    modifier: Modifier = Modifier,
    header: @Composable ()->Unit,
    content: @Composable (PaddingValues) ->Unit,
){

    var headerHeightPx by remember {
        mutableStateOf(0)
    }
    var offsetHeightPx by remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = offsetHeightPx + delta
                // 设置 Header 的位移范围
                offsetHeightPx = newOffset.coerceIn(-headerHeightPx.toFloat(), 0F)

                return Offset.Zero
            }

        }
    }

    Box(
        modifier = Modifier.nestedScroll(nestedScrollConnection).then(modifier)
    ) {

        content(PaddingValues(top = with(LocalDensity.current){headerHeightPx.toDp()}))

        Box(
            modifier = Modifier.onSizeChanged {
                        headerHeightPx = it.height
                }.clipToBounds().offset(0.dp, with(LocalDensity.current){offsetHeightPx.toDp()})
        ){
            header()
        }

    }
}