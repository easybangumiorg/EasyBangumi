package com.heyanle.easybangumi4.ui.common.player

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.google.android.exoplayer2.ExoPlayer

/**
 * Created by HeYanLe on 2023/3/9 11:23.
 * https://github.com/heyanLE
 */
@Composable
fun EasyPlayerScaffold(
    withControl: Boolean,
    exoPlayer: ExoPlayer,
    bottomAction: (@Composable RowScope.(ControlViewModel) -> Unit)? = null,
    control: (@Composable (ExoPlayer) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {


}