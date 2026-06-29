package org.easybangumi.next.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject

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
@Composable
fun EasyTheme(content: @Composable () -> Unit) {
    val themeController = koinInject<ThemeController>()
    val theme by themeController.themeFlow.collectAsState()
    val isDark = false
//    when (theme.darkMode) {
//        ThemeController.DarkMode.Dark -> true
//        ThemeController.DarkMode.Light -> false
//        else -> isSystemInDarkTheme()
//    }

    val colorScheme = if (theme.isDynamicColor && themeController.isSupportDynamicColor()) {
        themeController.getDynamicColorScheme(isDark) ?: theme.themeMode.getColorScheme(isDark)
    } else {
        theme.themeMode.getColorScheme(isDark)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}