package com.heyanle.easybangumi.ui.anim

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.heyanle.easybangumi.ui.LoadingPage

/**
 * Created by HeYanLe on 2023/1/7 21:52.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimHome(
){
    LoadingPage(
        modifier = Modifier.fillMaxSize(),
        loadingMsg = "开发中"
    )

}