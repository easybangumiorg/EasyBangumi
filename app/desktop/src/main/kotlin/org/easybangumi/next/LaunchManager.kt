package org.easybangumi.next

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import org.easybangumi.next.jcef.JcefManager
import org.easybangumi.next.lib.utils.PathProviderImpl
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.libplayer.vlcj.VlcBridgeManagerProvider
import org.easybangumi.next.libplayer.vlcj.VlcjBridgeManager
import org.easybangumi.next.shared.Scheduler
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
 *
 *  1. 初始化 -> 2. 数据迁移 -> 3. 就绪
 *
 */
object LaunchManager {

    enum class LaunchState {
        INITIALIZING,
        INIT_ERROR,
        MIGRATING,
        MIGRATION_ERROR,
        READY,
    }

    class StateException(state: LaunchState, cause: Throwable? = null) : Exception("State error ${LaunchManager.state.value.name}", cause)

    val state = mutableStateOf<LaunchState>(LaunchState.INITIALIZING)
    val exception = mutableStateOf<StateException?>(null)

    fun fireInit() {

        coroutineProvider.globalScope().launch(coroutineProvider.io()) {
            try {
                Desktop.onInit()
                Scheduler.onInit()
                if ( Migrate.needMigrate() ) {
                    state.value = LaunchState.MIGRATING
                    fireMigration()
                } else {
                    state.value = LaunchState.READY
                }
            } catch ( e: Throwable ) {
                e.printStackTrace()
                state.value = LaunchState.INIT_ERROR
                exception.value = StateException(LaunchState.INIT_ERROR, e)
                return@launch
            }
        }
    }

    private fun fireMigration() {
        coroutineProvider.globalScope().launch(coroutineProvider.io()) {
            try {
                Migrate.migrate()
                state.value = LaunchState.READY
            } catch ( e: Throwable ) {
                e.printStackTrace()
                exception.value = StateException(LaunchState.MIGRATION_ERROR, e)
                state.value = LaunchState.MIGRATION_ERROR
                return@launch
            }
        }
    }

    fun fireLazyInit() {
        // 预加载 vlc
        KoinPlatform.getKoin().get<VlcBridgeManagerProvider>().tryInit()

        // 预加载 jcef
        JcefManager.tryPreload()
    }

}