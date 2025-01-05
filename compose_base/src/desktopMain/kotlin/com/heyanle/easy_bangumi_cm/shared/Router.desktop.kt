package com.heyanle.easy_bangumi_cm.shared

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry

@Composable
actual fun AnimatedContentScope.NavHook(entity: NavBackStackEntry, content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) {
    content(entity)
}