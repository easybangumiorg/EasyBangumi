package com.heyanle.easy_bangumi_cm.component.provider

import com.heyanle.easy_bangumi_cm.component.provider.path.AndroidPathProvider
import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/11/26 23:59.
 * https://github.com/heyanLE
 */

actual val MODULE_COMPONENT_PROVIDER_PLATFORM: Module
    get() = module {
        single {
            AndroidPathProvider(it.get())
        }
    }