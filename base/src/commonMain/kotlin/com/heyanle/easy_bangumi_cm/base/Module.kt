package com.heyanle.easy_bangumi_cm.base

import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import com.heyanle.easy_bangumi_cm.base.path_provider.pathProviderModule
import org.koin.core.context.startKoin
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/11/26 23:59.
 * https://github.com/heyanLE
 */
fun initBase() {
    startKoin {
        modules(pathProviderModule)
    }
}