package org.easybangumi.next.shared.source.core.utils

import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

actual val utilsModuleExpect: Module
    get() = module {
        single {
            WebViewHelperImpl()
        }.binds(arrayOf(WebViewHelper::class))
    }