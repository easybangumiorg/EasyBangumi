package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi.ui.common.EmptyPage
import com.heyanle.easybangumi.ui.common.LoadingPage
import com.heyanle.extension_load.ExtensionController
import com.heyanle.extension_load.model.Extension

/**
 * Created by HeYanLe on 2023/2/22 19:29.
 * https://github.com/heyanLE
 */

@Composable
fun ExtensionContainer(
    modifier: Modifier = Modifier,
    loadingContainerColor: Color = Color.Transparent,
    content: @Composable (List<Extension>) -> Unit,
){
    val state by ExtensionController.installedExtensionsFlow.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        when(val sta = state){
            is ExtensionController.ExtensionState.None -> {}
            is ExtensionController.ExtensionState.Loading -> {
                LoadingPage(modifier = Modifier.fillMaxSize().background(loadingContainerColor))
            }
            is ExtensionController.ExtensionState.Extensions -> {
                content(sta.extensions)
            }
        }
    }


}