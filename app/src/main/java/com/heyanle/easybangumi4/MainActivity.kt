package com.heyanle.easybangumi4

import androidx.compose.runtime.Composable


/**
 * Created by HeYanLe on 2023/10/29 21:20.
 * https://github.com/heyanLE
 */

class MainActivity : MainActivityHost() {

    @Composable
    override fun NavigationContent() {
        Nav()
    }
}
