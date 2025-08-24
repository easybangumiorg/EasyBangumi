package org.easybangumi.next.shared.foundation.systembar

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.easybangumi.next.lib.logger.logger

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
val logger = logger("EasySystemBar")
val LocalEasySystemBar = staticCompositionLocalOf<EasySystemBarContext> {
    error("EasySystemBarContext Not Provide")
}

class EasySystemBarContext(
    private val activity: Activity
) {

    data class SystemBarConfig(
        val statusBarColor: Color = Color.Transparent,
        val navBarColor: Color = Color.Transparent,
        val isStatusBarAppearanceLight: Boolean = true,
        val isNavBarAppearanceLight: Boolean = true,
    ) {

    }
    val configArray = mutableStateOf<List<SystemBarConfig>>(emptyList())
//    val configArray = mutableListOf<SystemBarConfig>()

    fun pushConfig(
        config: SystemBarConfig
    ) {
        logger.info("pushConfig: $config")
        configArray.value += config
    }

    fun removeConfig(
        config: SystemBarConfig
    ) {
        logger.info("removeConfig: $config")
        configArray.value -= config
    }

    @Composable
    fun Effect() {
        val config = configArray.value.lastOrNull()
        LaunchedEffect(config) {
            logger.info("Effect: $config")
            if (config != null) {
                apply(config)
            }
        }
    }

    fun apply(config: SystemBarConfig) {
        SystemBarUtils.setStatusBarColor(activity, config.statusBarColor.toArgb())
        SystemBarUtils.setNavBarColor(activity, config.navBarColor.toArgb())
        SystemBarUtils.setIsAppearanceLightStatusBars(activity, config.isStatusBarAppearanceLight)
        SystemBarUtils.setIsAppearanceLightNavBars(activity, config.isNavBarAppearanceLight)
    }

}