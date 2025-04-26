package org.easybangumi.next

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.easybangumi.next.shared.ComposeApp
import org.easybangumi.next.shared.Scheduler

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

fun main() {

    Scheduler.onInit()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "EasyBangumi.next",
        ) {
            ComposeApp()
        }
    }
}