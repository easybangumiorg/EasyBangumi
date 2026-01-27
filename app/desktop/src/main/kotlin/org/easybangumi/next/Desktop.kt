package org.easybangumi.next

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.easybangumi.next.bangumi.BangumiAppConfigProviderImpl
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.platform.DesktopPlatform
import org.easybangumi.next.libplayer.vlcj.VlcjBridgeManager
import org.easybangumi.next.shared.playcon.desktop.FullscreenStrategy
import org.easybangumi.next.shared.source.bangumi.BangumiAppConfig
import org.easybangumi.next.shared.source.bangumi.BangumiAppConfigProvider
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.binds
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

    val logger = logger("Desktop")

    suspend fun onInit() {
        
        logger.debug("Desktop initialization started")
        startKoin {
            loadKoinModules(module {
                factory {
                    MediaPlayerFactory()
                }.bind()
                single {
                    DesktopPlatform
                }.bind(Platform::class)

                single {
                    logger.debug("Creating VlcjBridgeManager with args: -vvv, --file-caching=2000, --network-caching=2000, --intf=dummy, --video-filter=canvas, --vout=any")
                    VlcjBridgeManager(listOf(
                        "-vvv",
                        "--intf=dummy",
                        "--vout=any"
                    ))
                }

                single {
                    WindowController()
                }

                single {
                    BangumiAppConfigProviderImpl()
                }.binds(arrayOf(BangumiAppConfigProvider::class))


                single {
                    FullscreenStrategy(
                        windowState = { get<WindowController>().getFirstWindowState() ?: throw IllegalStateException() },
                    )
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