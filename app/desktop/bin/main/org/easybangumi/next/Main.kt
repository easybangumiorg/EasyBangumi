package org.easybangumi.next

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.ComposeApp
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
 */

private val logger = logger("")
suspend fun main() {

    try {
        Desktop.onInit()
        Scheduler.onInit()

        val windowController = KoinPlatform.getKoin().get<WindowController>()

        application {
            val windowState = rememberWindowState()
            LaunchedEffect(windowController) {
                windowController.addWindowState(windowState)
            }
            DisposableEffect(windowController) {
                onDispose {
                    windowController.removeWindowState(windowState)
                }
            }
            Window(
                onCloseRequest = ::exitApplication,
                title = "EasyBangumi.next",
//                undecorated = true,
                state = windowState
            ) {
                ComposeApp()
            }
        }
    } catch (e: Throwable) {
        logger.error("Application failed to start", e)
        throw e
    } finally {
    }


}
