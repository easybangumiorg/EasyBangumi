package org.easybangumi.next.shared.foundation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Created by HeYanLe on 2025/1/5 22:48.
 * https://github.com/heyanLE
 */
@Composable
actual fun InnerBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}