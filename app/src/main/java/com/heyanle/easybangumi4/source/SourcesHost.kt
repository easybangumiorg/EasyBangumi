package com.heyanle.easybangumi4.source

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.heyanle.injekt.core.Injekt

/**
 * Created by HeYanLe on 2023/2/22 20:41.
 * https://github.com/heyanLE
 */

val LocalSourceBundleController = staticCompositionLocalOf<SourceBundle> {
    error("SourceBundle Not Provide")
}

@Composable
fun SourcesHost(content: @Composable () -> Unit) {
    val sourceController: SourceController by Injekt.injectLazy()
    val state by sourceController.sourceState.collectAsState()

    CompositionLocalProvider(
        LocalSourceBundleController provides (((state as? SourceController.SourceState.Completely)?.sourceBundle)
            ?: SourceBundle(
                emptyList()
            ))
    ) {
        content()
    }

}