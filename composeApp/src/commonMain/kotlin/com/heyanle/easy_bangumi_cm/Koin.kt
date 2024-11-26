package com.heyanle.easy_bangumi_cm

import com.heyanle.easy_bangumi_cm.business.media.MODULE_BUSINESS_MEDIA
import com.heyanle.easy_bangumi_cm.component.provider.MODULE_COMPONENT_PROVIDER
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/11/27 0:29.
 * https://github.com/heyanLE
 */
fun initKoin(){
    startKoin {
        modules(MODULE_COMPONENT_PROVIDER)
        modules(MODULE_BUSINESS_MEDIA)
    }
}