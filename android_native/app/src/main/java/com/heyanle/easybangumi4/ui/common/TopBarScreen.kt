package com.heyanle.easybangumi4.ui.common

import androidx.compose.runtime.Composable

/**
 * Created by HeYanLe on 2023/2/21 23:27.
 * https://github.com/heyanLE
 */
class HomePage(
    val tabLabel: @Composable (() -> Unit),
    val topAppBar: @Composable (()->Unit),
    val content: @Composable (() -> Unit),
)