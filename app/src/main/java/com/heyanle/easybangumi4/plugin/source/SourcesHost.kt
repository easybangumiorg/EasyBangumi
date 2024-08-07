package com.heyanle.easybangumi4.plugin.source

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.plugin.source.bundle.SourceBundle
import com.heyanle.inject.core.Inject

/**
 * Created by HeYanLe on 2023/2/22 20:41.
 * https://github.com/heyanLE
 */

val LocalSourceBundleController = staticCompositionLocalOf<SourceBundle> {
    error("SourceBundle Not Provide")
}

@Composable
fun SourcesHost(content: @Composable () -> Unit) {
    val sourceStateCase: SourceStateCase by Inject.injectLazy()
    val state = sourceStateCase.flowBundle().collectAsState(
        initial = SourceBundle(
            emptyList()
        )
    )

    CompositionLocalProvider(
        LocalSourceBundleController provides state.value
    ) {
        content()
    }

}