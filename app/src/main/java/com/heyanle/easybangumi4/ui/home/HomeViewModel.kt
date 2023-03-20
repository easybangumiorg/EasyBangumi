package com.heyanle.easybangumi4.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Created by HeYanLe on 2023/3/20 16:22.
 * https://github.com/heyanLE
 */
class HomeViewModel: ViewModel() {

    var customBottomBar by mutableStateOf<(@Composable ()->Unit)?>(null)

}