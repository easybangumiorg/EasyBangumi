package com.heyanle.easy_bangumi_cm.shared

import com.heyanle.easy_bangumi_cm.base.service.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.service.system.ILogger
import com.heyanle.easy_bangumi_cm.base.service.system.IPlatformInformation
import com.heyanle.easy_bangumi_cm.base.utils.moshi.MoshiArrayListJsonAdapter
import com.heyanle.lib.inject.api.InjectModule
import com.heyanle.lib.inject.api.InjectScope
import com.heyanle.lib.inject.api.addSingletonFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Created by heyanlin on 2024/12/3.
 */
class SharedModule: InjectModule {

    override fun InjectScope.registerInjectables() {
        addSingletonFactory { // Moshi JSON解析器
            Moshi.Builder()
                .add(MoshiArrayListJsonAdapter.FACTORY)
                .addLast(KotlinJsonAdapterFactory())
                .build()
        }


    }
}