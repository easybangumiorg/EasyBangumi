package com.heyanle.easy_bangumi_cm.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.heyanle.easy_bangumi_cm.common.foundation.image.LocalImageLoader
import com.heyanle.easy_bangumi_cm.common.foundation.image.createImageLoader
import com.heyanle.easy_bangumi_cm.common.theme.EasyTheme

/**
 * Created by heyanlin on 2025/2/27.
 */
@Composable
fun App() {
    CompositionLocalProvider(LocalImageLoader provides createImageLoader()) {
        EasyTheme {
            Nav()
        }
    }
}