package com.heyanle.easybangumi.ui.home.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.heyanle.easybangumi.ui.common.LoadingPage

/**
 * Created by HeYanLe on 2023/1/9 21:51.
 * https://github.com/heyanLE
 */
@Composable
fun AnimHistory() {
    LoadingPage(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        loadingMsg = "开发中"
    )
}