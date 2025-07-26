package org.easybangumi.next.shared.debug.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.debug.DebugScope

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
fun DebugScope.ColorDebug () {
    val color = MaterialTheme.colorScheme
    Column (
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState(), enabled = true)
    ){

        colorLine(color.background, "background")
        colorLine(color.onBackground, "onBackground")
        colorLine(color.surface, "surface")
        colorLine(color.onSurface, "onSurface")
        colorLine(color.primary, "primary")
        colorLine(color.onPrimary, "onPrimary")
        colorLine(color.primaryContainer, "primaryContainer")
        colorLine(color.onPrimaryContainer, "onPrimaryContainer")
        colorLine(color.secondary, "secondary")
        colorLine(color.onSecondary, "onSecondary")
        colorLine(color.secondaryContainer, "secondaryContainer")
        colorLine(color.onSecondaryContainer, "onSecondaryContainer")
        colorLine(color.tertiary, "tertiary")
        colorLine(color.onTertiary, "onTertiary")
        colorLine(color.tertiaryContainer, "tertiaryContainer")
        colorLine(color.onTertiaryContainer, "onTertiaryContainer")
        colorLine(color.error, "error")
        colorLine(color.onError, "onError")
        colorLine(color.errorContainer, "errorContainer")
        colorLine(color.onErrorContainer, "onErrorContainer")
        colorLine(color.inversePrimary, "inversePrimary")
        colorLine(color.inverseSurface, "inverseSurface")
        colorLine(color.inverseOnSurface, "inverseOnSurface")
        colorLine(color.surfaceVariant, "surfaceVariant")
        colorLine(color.onSurfaceVariant, "onSurfaceVariant")
        colorLine(color.outline, "outline")
        colorLine(color.outlineVariant, "outlineVariant")
        colorLine(color.scrim, "scrim")
        colorLine(color.surfaceBright, "surfaceBright")
        colorLine(color.surfaceDim, "surfaceDim")
        colorLine(color.surfaceContainerLow, "surfaceContainerLow")
        colorLine(color.surfaceContainer, "surfaceContainer")
        colorLine(color.surfaceContainerHigh, "surfaceContainerHigh")
        colorLine(color.surfaceContainerHighest, "surfaceContainerHighest")
        colorLine(color.surfaceContainerLow, "surfaceContainerLow")
        colorLine(color.surfaceContainerLowest, "surfaceContainerLowest")

    }

}

@Composable
fun ColumnScope.colorLine(
    color: Color,
    name: String,
) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        Box(modifier = Modifier.height(32.dp).width(64.dp).background(color))
        Text(text = name)
    }
}