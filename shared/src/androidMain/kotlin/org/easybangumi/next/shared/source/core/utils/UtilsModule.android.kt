package org.easybangumi.next.shared.source.core.utils

import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.easybangumi.next.webkit.WebViewManager
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

// 局部 utilsModule
actual val utilsModuleExpect: Module
    get() = module {
        single {
            WebViewHelperImpl(get())
        }.binds(arrayOf(WebViewHelper::class))

        // 全局 WebViewManager
        single {
            KoinPlatform.getKoin().get<WebViewManager>()
        }.binds(arrayOf(WebViewManager::class))
    }