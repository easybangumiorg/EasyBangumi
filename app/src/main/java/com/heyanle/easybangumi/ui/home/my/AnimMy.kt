package com.heyanle.easybangumi.ui.home.my

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.heyanle.easybangumi.ui.common.LoadingPage

/**
 * Created by HeYanLe on 2023/1/9 21:27.
 * https://github.com/heyanLE
 */

@Composable
fun AnimMy(){

    LoadingPage(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )
}