package org.easybangumi.next

import androidx.compose.ui.window.WindowState

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
class WindowController {

    private val windowStateList: MutableList<WindowState> = mutableListOf()
    private var exitApplication: (()->Unit)? = null

    fun bindExitApplication(
        exitApplication: () -> Unit
    ){
        this.exitApplication = exitApplication
    }

    fun addWindowState(windowState: WindowState) {
        windowStateList.add(windowState)
    }

    fun removeWindowState(windowState: WindowState) {
        windowStateList.remove(windowState)
        if (windowStateList.isEmpty()) {
            exitApplication?.invoke()
        }
    }

    fun getFirstWindowState(): WindowState? {
        return windowStateList.firstOrNull()
    }

}