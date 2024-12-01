package com.heyanle.easy_bangumi_cm.base

import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import com.heyanle.easy_bangumi_cm.base.path_provider.pathProviderModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/11/26 23:59.
 * https://github.com/heyanLE
 */
val baseModule: Module
    get() = module {
        includes(pathProviderModule)
    }
