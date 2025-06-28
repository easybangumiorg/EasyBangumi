package org.easybangumi.next.shared.foundation.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import coil3.ImageLoader

/**
 * Created by heyanle on 2025/2/27.
 */
val LocalImageLoader = compositionLocalOf<ImageLoader> {
    error("No ImageLoader provided")
}

@Composable
expect fun createImageLoader(): ImageLoader