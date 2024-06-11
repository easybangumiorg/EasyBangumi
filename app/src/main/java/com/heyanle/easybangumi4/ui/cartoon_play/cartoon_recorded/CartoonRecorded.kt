package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer

/**
 * Created by heyanlin on 2024/6/11.
 */
@Composable
fun CartoonRecorded(
    exoPlayer: ExoPlayer,
    currentPosition: Long,
    show: Boolean,
    onDismissRequire: () -> Unit
) {

    if (show){
        Box(Modifier.fillMaxSize()){
            CartoonRecordedContent(exoPlayer, currentPosition, onDismissRequire)
        }
    }


}

@Composable
private fun BoxScope.CartoonRecordedContent(
    exoPlayer: ExoPlayer,
    currentPosition: Long,
    onDismissRequire: () -> Unit
) {

    DisposableEffect(Unit) {
        exoPlayer.clearVideoSurface()
        onDispose {
            exoPlayer.seekTo(currentPosition)
        }
    }

    val vm = viewModel<CartoonRecordedViewModel>(factory = CartoonRecordedViewModelFactory(exoPlayer))





}