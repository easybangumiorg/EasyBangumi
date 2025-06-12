package org.easybangumi.next

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.platform.DesktopPlatform
import org.easybangumi.next.libplayer.vlcj.VlcjBridgeManager
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import uk.co.caprica.vlcj.factory.MediaPlayerFactory


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
 *
 * 比 shared 中的 Scheduler 执行早，用以初始化平台特化内容
 */
object Desktop {

    suspend fun onInit() {
        startKoin {
            loadKoinModules(module {
                factory {
                    MediaPlayerFactory()
                }.bind()
                single {
                    DesktopPlatform
                }.bind(Platform::class)

                single {
                    VlcjBridgeManager()
                }
            })
        }

        coroutineScope() {
            async(coroutineProvider.io()) {
                // 预加载 vlcj
                KoinPlatform.getKoin().get<VlcjBridgeManager>().preloadVlc()
            }
        }

    }

}