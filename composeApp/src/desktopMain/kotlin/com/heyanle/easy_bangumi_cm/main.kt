package com.heyanle.easy_bangumi_cm

import androidx.compose.material.Text
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    Global.onInit()
    println(System.getProperty("compose.application.resources.dir"))
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "EasyBangumi",
        ) {
            App()
            Text(System.getProperty("compose.application.resources.dir"))
        }
    }
}