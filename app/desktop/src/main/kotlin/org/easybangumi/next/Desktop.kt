package org.easybangumi.next

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.easybangumi.next.bangumi.BangumiAppConfigProviderImpl
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.PathProvider
import org.easybangumi.next.lib.utils.PathProviderImpl
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.libplayer.vlcj.VlcBridgeManagerProvider
import org.easybangumi.next.libplayer.vlcj.VlcjBridgeManager
import org.easybangumi.next.platform.DesktopPlatform
import org.easybangumi.next.shared.playcon.desktop.FullscreenStrategy
import org.easybangumi.next.shared.source.bangumi.BangumiAppConfigProvider
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
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
                single {
                    logger.debug("Initializing PathProviderImpl for Desktop")
                    PathProviderImpl(
                        when(get<Platform>().hostOs) {
                            DesktopHostOs.Linux -> PathProviderImpl.PLATFORM_LINUX
                            DesktopHostOs.MacOS -> PathProviderImpl.PLATFORM_MACOS
                            DesktopHostOs.Windows -> PathProviderImpl.PLATFORM_WINDOWS
                            else -> throw IllegalStateException("Unsupported OS: ${platformInformation.hostOs}")
                        }
                    )
                }.bind(PathProvider::class)

                factory {
                    MediaPlayerFactory()
                }.bind()
                single {
                    DesktopPlatform
                }.bind(Platform::class)

                single {
                    // 可能会阻塞
                    runBlocking {
                        logger.debug("Creating VlcjBridgeManager with args: -vvv, --file-caching=2000, --network-caching=2000, --intf=dummy, --video-filter=canvas, --vout=any")
                        get<VlcBridgeManagerProvider>()
                            .getManager()
                    }
                }
                single {
                    VlcBridgeManagerProvider(
                        ioScope = CoroutineScope(SupervisorJob() + coroutineProvider.io() ),
                        libvlcArgs = listOf(
                            "-vvv",
                            "--file-caching=10000",
                            "--network-caching=10000",
                            "--intf=dummy",
                            "--video-filter=canvas",
                            "--vout=any"
                        )
                    )
                }

                single {
                    WindowController()
                }

                single {
                    BangumiAppConfigProviderImpl()
                }.binds(arrayOf(BangumiAppConfigProvider::class))


//                single {
//                    FullscreenStrategy(
//                        windowState = { get<WindowController>().getFirstWindowState() ?: throw IllegalStateException() },
//                    )
//                }
            })
        }
    }

}