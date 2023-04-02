package com.heyanle.easybangumi4.source

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/22 20:41.
 * https://github.com/heyanLE
 */

val LocalSourceBundleController = staticCompositionLocalOf<SourceBundle> {
    error("SourceBundle Not Provide")
}

object SourceMaster {

    val scope = MainScope()

    // 把所有源作为一个 flow 对外暴露，全局可用
    private val _animSourceFlow = MutableStateFlow(SourceBundle(emptyList()))
    val animSourceFlow = _animSourceFlow.asStateFlow()


    init {
        scope.launch {
            SourceLibraryMaster.sourceLibraryFlow.collectLatest {
                _animSourceFlow.emit(SourceBundle(it.filter { it.second.enable }
                    .sortedBy { it.second.order }.map { it.first }))
            }
        }
    }


    @Composable
    fun SourceHost(content: @Composable () -> Unit) {
        val sourceBundle = animSourceFlow.collectAsState()
        CompositionLocalProvider(
            LocalSourceBundleController provides sourceBundle.value
        ) {
            content()
        }

    }

}