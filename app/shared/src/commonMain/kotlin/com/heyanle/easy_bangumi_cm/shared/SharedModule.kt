package com.heyanle.easy_bangumi_cm.shared

import com.heyanle.easy_bangumi_cm.utils.moshi.MoshiArrayListJsonAdapter
import com.heyanle.easy_bangumi_cm.shared.model.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.shared.model.system.ILogger
import com.heyanle.easy_bangumi_cm.shared.model.system.IPlatformInformation
import com.heyanle.easy_bangumi_cm.shared.platform.PlatformInformation
import com.heyanle.easy_bangumi_cm.shared.platform.PlatformLogger
import com.heyanle.easy_bangumi_cm.shared.platform.PlatformPath
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory
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

        addSingletonFactory<IPlatformInformation> { // 平台信息
            PlatformInformation()
        }

        addSingletonFactory<ILogger> { // 日志
            PlatformLogger()
        }

        addSingletonFactory<IPathProvider> { // 路径
            PlatformPath()
        }
    }
}