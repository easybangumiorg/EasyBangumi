package com.heyanle.easybangumi.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.lang.reflect.Modifier

/**
 * Created by HeYanLe on 2023/1/12 1:24.
 * https://github.com/heyanLE
 */
@Composable
fun FirstFloatColumn(
    modifier: Modifier,
    first: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    var headerHeightPx by remember {
        mutableStateOf(0)
    }


}