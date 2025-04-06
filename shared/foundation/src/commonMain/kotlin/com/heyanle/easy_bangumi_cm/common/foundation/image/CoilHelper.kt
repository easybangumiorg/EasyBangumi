package com.heyanle.easy_bangumi_cm.common.foundation.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import coil3.ImageLoader

/**
 * Created by heyanlin on 2025/2/27.
 */
val LocalImageLoader = compositionLocalOf<ImageLoader> {
    error("No ImageLoader provided")
}

@Composable
expect fun createImageLoader(): ImageLoader