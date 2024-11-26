package com.heyanle.easy_bangumi_cm

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "EasyBangumi",
    ) {
        App()
    }
}