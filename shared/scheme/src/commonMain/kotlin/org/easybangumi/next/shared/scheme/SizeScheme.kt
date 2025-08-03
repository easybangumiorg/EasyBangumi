package org.easybangumi.next.shared.scheme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
val LocalSizeScheme  = compositionLocalOf<SizeScheme> {
    throw IllegalStateException("SizeScheme Not Provide")
}




/**
 * 尺寸方案
 *
 * @param cartoonCoverWidth 番剧封面宽度
 * @param cartoonCoverAspectRatio 番剧封面宽高比
 */
@Immutable
data class SizeScheme (
    val cartoonCoverWidth: Dp = 154.dp,
    val cartoonCoverAspectRatio: Float = 7f/9f,
    val cartoonPreviewAspectRatio: Float = 14f/9f,
    val topAppBarHeight: Dp = 64.dp,
    val tabWidth: Dp = 88.dp,
    val statusBarHeight: Dp,
) {
    val cartoonCoverHeight: Dp by lazy {
        cartoonCoverWidth / cartoonCoverAspectRatio
    }
}
