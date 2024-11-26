package com.heyanle.easy_bangumi_cm.component.provider

import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/11/26 23:59.
 * https://github.com/heyanLE
 */

internal expect val MODULE_COMPONENT_PROVIDER_PLATFORM: Module
val MODULE_COMPONENT_PROVIDER: Module
    get() = module {
        includes(MODULE_COMPONENT_PROVIDER_PLATFORM)
    }