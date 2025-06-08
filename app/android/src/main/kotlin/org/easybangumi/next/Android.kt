package org.easybangumi.next

import org.easybangumi.next.platform.AndroidPlatform
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

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
 * 比 shared 中的 Scheduler 执行早，用以初始化平台特化内容
 */
object Android {

    fun onInit(application: EasyApplication) {
        startKoin {
            androidContext(application)

            loadKoinModules(module {
                single {
                    AndroidPlatform
                }.bind(Platform::class)
            })
        }
    }


}