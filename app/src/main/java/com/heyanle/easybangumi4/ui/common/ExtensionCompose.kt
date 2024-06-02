package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.heyanle.easybangumi4.extension.ExtensionInfo
import com.heyanle.easybangumi4.case.ExtensionCase
import com.heyanle.injekt.core.Injekt

/**
 * Created by HeYanLe on 2023/2/22 19:29.
 * https://github.com/heyanLE
 */

@Composable
fun ExtensionContainer(
    modifier: Modifier = Modifier,
    loadingContainerColor: Color = Color.Transparent,
    content: @Composable (List<ExtensionInfo>) -> Unit,
) {

    val extension: ExtensionCase by Injekt.injectLazy()
    val state by extension.flowExtensionState().collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        if (state.isLoading) {
            LoadingPage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(loadingContainerColor)
            )
        } else {
            content((state.appExtensions + state.fileExtensionInfo).values.toList())
        }

    }


}