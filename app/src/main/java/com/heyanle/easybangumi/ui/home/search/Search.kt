package com.heyanle.easybangumi.ui.home.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.heyanle.easybangumi.ui.common.LoadingPage

/**
 * Created by HeYanLe on 2023/1/10 16:34.
 * https://github.com/heyanLE
 */

@Composable
fun Search(){
    LoadingPage(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        loadingMsg = "开发中"
    )
}