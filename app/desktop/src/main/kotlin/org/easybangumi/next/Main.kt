package org.easybangumi.next

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.panic.PanicApp
import org.easybangumi.next.shared.ComposeApp
import org.easybangumi.next.shared.window.EasyWindowController
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

private val logger = logger("")
fun main() = try {
    application {
        LaunchedEffect(Unit) {
            LaunchManager.fireInit()
        }

        when (val state = LaunchManager.state.value) {
            LaunchManager.LaunchState.INITIALIZING -> {
                NormalWindow("正在初始化...")
            }
            LaunchManager.LaunchState.INIT_ERROR -> {
                throw LaunchManager.exception.value ?: LaunchManager.StateException(state)
            }
            LaunchManager.LaunchState.MIGRATING -> {
                NormalWindow("数据迁移中...")
            }
            LaunchManager.LaunchState.MIGRATION_ERROR -> {
                throw LaunchManager.exception.value ?: LaunchManager.StateException(state)
            }
            LaunchManager.LaunchState.READY -> {
                // 正常启动应用
                LaunchedEffect(Unit) {
                    // 预加载
                    LaunchManager.fireLazyInit()
                    EasyWindowController.bindExitApplication(::exitApplication)
                }
                ComposeApp()
//                EasyWindowController.EasyWindowHost()
            }
        }
    }
} catch (e: Throwable) {
    application {
        PanicApp(e)
    }
    logger.error("Application failed to start", e)
}


@Composable
fun ApplicationScope.NormalWindow(
    msg: String,
) {
    Window(onCloseRequest = ::exitApplication, title = "纯纯看番 Next") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(msg)
        }
    }
}

