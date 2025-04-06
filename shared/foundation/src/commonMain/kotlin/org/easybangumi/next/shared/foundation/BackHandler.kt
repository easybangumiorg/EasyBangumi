package org.easybangumi.next.shared.foundation

import androidx.compose.runtime.Composable


/**
 * Created by HeYanLe on 2025/1/5 22:48.
 * https://github.com/heyanLE
 */
@Composable
expect fun InnerBackHandler(enabled: Boolean = true, onBack: () -> Unit)

