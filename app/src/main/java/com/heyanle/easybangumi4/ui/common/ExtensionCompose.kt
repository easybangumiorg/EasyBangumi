package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.heyanle.easybangumi4.extension.Extension
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.getter.ExtensionGetter
import com.heyanle.injekt.core.Injekt
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Created by HeYanLe on 2023/2/22 19:29.
 * https://github.com/heyanLE
 */

@Composable
fun ExtensionContainer(
    modifier: Modifier = Modifier,
    loadingContainerColor: Color = Color.Transparent,
    content: @Composable (List<Extension>) -> Unit,
) {

    val extension: ExtensionGetter by Injekt.injectLazy()
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
            content((state.appExtensions + state.fileExtension).values.toList())
        }

    }


}