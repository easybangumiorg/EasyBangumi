package org.easybangumi.next.shared.source.bangumi

import org.easybangumi.next.shared.ktor.ktorModule
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.source.bangumi.business.BangumiBusiness
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
// BangumiSourceModule 装载到沙盒里，从全局 koin 获取
internal val bangumiSourceModule = module {
    single<BangumiConfig> {
        KoinPlatform.getKoin().get()
    }
    single <BangumiAppConfig>{
        KoinPlatform.getKoin().get()
    }
    single <BangumiBusiness> {
        KoinPlatform.getKoin().get()
    }
    single <BangumiApi> {
        get<BangumiBusiness>().api
    }

    includes(ktorModule)
}

val bangumiApiModule = module {
    single {
        val bangumiAppConfig = get<BangumiAppConfig>()
        BangumiConfig(
            appId = bangumiAppConfig.appId,
            appSecret = bangumiAppConfig.appSecret,
            callbackUrl = bangumiAppConfig.callbackUrl,
            handler = bangumiAppConfig.handler
        )
    }
    single {
        val appConfigProvider: BangumiAppConfigProvider = get()
        appConfigProvider.provide()
    }
    single {
        BangumiBusiness(get(), get())
    }
    single {
        get<BangumiBusiness>().api
    }.binds(arrayOf(BangumiApi::class))
}