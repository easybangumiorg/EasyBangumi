package org.easybangumi.next.shared

import org.easybangumi.next.lib.store.storeModule
import org.easybangumi.next.shared.data.dataModule
import org.easybangumi.next.shared.plugin.pluginModule
import org.easybangumi.next.shared.preference.preferenceModule
import org.easybangumi.next.shared.theme.ThemeController
import org.easybangumi.next.shared.theme.themeModule
import org.koin.compose.getKoin
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

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

object Scheduler {

    private var isInit = false


    // 业务要么保证只执行一次，要么保证只在同一个线程饱和执行多次
    fun onInit() {
        if (!isInit) {
            isInit = true
            loadKoinModules(listOf(
                pluginModule,
                storeModule,
                dataModule,
                themeModule,
                preferenceModule,
            ))
        }

    }

    fun onSplashPageLaunch() {

    }

    fun onHomePageLaunch() {

    }

}