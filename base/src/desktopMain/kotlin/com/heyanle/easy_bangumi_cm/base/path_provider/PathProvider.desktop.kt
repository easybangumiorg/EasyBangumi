package com.heyanle.easy_bangumi_cm.base.path_provider

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 路径提供者
 * Created by HeYanLe on 2024/11/27 0:00.
 * https://github.com/heyanLE
 */
internal actual val pathProviderModule: Module
    get() = module {
        single {
            DesktopPathProvider()
        }
    }