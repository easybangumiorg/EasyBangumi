package org.easybangumi.next.shared.foundation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Paint
import androidx.core.graphics.transform

/**
 * Created by HeYanLe on 2025/1/5 22:48.
 * https://github.com/heyanLE
 */
@Composable
actual fun InnerBackHandler(enabled: Boolean, onBack: () -> Unit) {
    Paint().shader?.transform {

    }
    BackHandler(enabled, onBack)
}