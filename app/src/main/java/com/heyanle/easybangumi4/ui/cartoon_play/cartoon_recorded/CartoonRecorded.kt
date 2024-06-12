package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.GifBox
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.utils.logi
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.PlayPauseBtn
import loli.ball.easyplayer2.ViewSeekBar
import loli.ball.easyplayer2.texture.EasyTextureView

/**
 * Created by heyanlin on 2024/6/11.
 */
@Composable
fun CartoonRecorded(
    exoPlayer: ExoPlayer,
    controlViewModel: ControlViewModel,
    currentPosition: Long,
    show: Boolean,
    onDismissRequire: () -> Unit
) {

    if (show) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Box(Modifier.fillMaxSize()) {
                CartoonRecordedContent(
                    exoPlayer,
                    controlViewModel,
                    currentPosition,
                    onDismissRequire
                )
            }
        }
    }


}

@Composable
private fun BoxScope.CartoonRecordedContent(
    exoPlayer: ExoPlayer,
    controlViewModel: ControlViewModel,
    currentPosition: Long,
    onDismissRequire: () -> Unit
) {

    val vm =
        viewModel<CartoonRecordedViewModel>(factory = CartoonRecordedViewModelFactory(exoPlayer))
    val state = vm.stateFlow.collectAsState()
    val sta = state

    LaunchedEffect(Unit) {
        vm.onLaunch()
    }

    DisposableEffect(Unit) {
        // exoPlayer.clearVideoSurface()
        onDispose {
            exoPlayer.seekTo(currentPosition)
            vm.onDispose()
        }
    }
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 左工具栏
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            IconButton(
                onClick = {
                    onDismissRequire()
                }
            ) {
                // 返回按钮
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back"
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {

                RecordedAction(
                    Icons.Default.GifBox,
                    stringResource(com.heyanle.easy_i18n.R.string.record_gif),
                    sta.value.configuration.type == 1
                ) {
                    vm.changeGif()
                }

                RecordedAction(
                    Icons.Default.VideoFile,
                    stringResource(com.heyanle.easy_i18n.R.string.record_video),
                    sta.value.configuration.type == 2
                ) {
                    vm.changeMp4()
                }
            }

        }

        // 播放器区域

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Red)
            ) {
                AndroidView(
                    factory = {
                        vm.layout.apply {
                            (parent as? ViewGroup)?.removeView(this)
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()//.background(Color.Black)
                )


                if (controlViewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                PlayPauseBtn(controlViewModel.playWhenReady) {
                    controlViewModel.onPlayPause(it)
                }
                ViewSeekBar(
                    during = controlViewModel.during.toInt(),
                    position = controlViewModel.position.toInt(),
                    secondary = controlViewModel.bufferPosition.toInt(),
                    onValueChange = {
                        controlViewModel.onPositionChange(it.toFloat())
                    },
                    onValueChangeFinish = {
                        "onValueChangeFinish".logi("ViewComponent")
                        controlViewModel.onActionUPScope()
                    }
                )
            }


        }

        // 右工具栏
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            RecordedAction(
                Icons.Default.PlayCircle,
                stringResource(com.heyanle.easy_i18n.R.string.record_start),
                sta.value.state == 1
            ) {
                vm.changeGif()
            }

            RecordedAction(
                Icons.Default.StopCircle,
                stringResource(com.heyanle.easy_i18n.R.string.record_stop),
                false
            ) {
                vm.changeGif()
            }
        }
    }


}

@Composable
fun ColumnScope.RecordedAction(
    imageVector: ImageVector,
    msg: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.weight(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clip(CircleShape)
                .clickable {
                    onClick()
                }
                .padding(0.dp, 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector,
                msg,
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.size(1.dp))
            Text(
                stringResource(com.heyanle.easy_i18n.R.string.record_gif),
                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}