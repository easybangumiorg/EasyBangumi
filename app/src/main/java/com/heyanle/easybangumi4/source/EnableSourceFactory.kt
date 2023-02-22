package com.heyanle.easybangumi4.source

import com.heyanle.easybangumi.source.AnimSources
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by HeYanLe on 2023/2/22 20:41.
 * https://github.com/heyanLE
 */
object EnableSourceFactory {

    val scope = MainScope()

    // 把所有源作为一个 flow 对外暴露，全局可用
    private val animSourceFlow = MutableStateFlow(AnimSources(emptyList()))

}