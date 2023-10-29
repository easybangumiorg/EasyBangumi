package com.heyanle.easybangumi4.source

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import com.heyanle.easybangumi4.getter.SourceStateGetter
import com.heyanle.easybangumi4.source.bundle.SourceBundle
import org.koin.mp.KoinPlatform.getKoin

/**
 * Created by HeYanLe on 2023/2/22 20:41.
 * https://github.com/heyanLE
 */

val LocalSourceBundleController = staticCompositionLocalOf<SourceBundle> {
    error("SourceBundle Not Provide")
}

@Composable
fun SourcesHost(content: @Composable () -> Unit) {
    val sourceStateGetter: SourceStateGetter by getKoin().inject()
    val state = sourceStateGetter.flowBundle().collectAsState(
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