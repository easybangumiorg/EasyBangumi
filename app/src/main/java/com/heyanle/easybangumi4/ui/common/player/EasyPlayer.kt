package com.heyanle.easybangumi4.ui.common.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.heyanle.easybangumi4.ui.common.BackgroundBasedBox
import com.heyanle.easybangumi4.ui.common.player.utils.TimeUtils
import com.heyanle.easybangumi4.ui.common.player.utils.systemVolume
import com.heyanle.easybangumi4.ui.common.player.utils.windowBrightness
import com.heyanle.easybangumi4.utils.OnLifecycleEvent
import com.heyanle.easybangumi4.utils.OnOrientationEvent
import com.heyanle.easybangumi4.utils.loge

/**
 * Created by HeYanLe on 2023/3/9 11:23.
 * https://github.com/heyanLE
 */
@Composable
fun EasyPlayerScaffold(
    modifier: Modifier,
    vm: ControlViewModel,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {

    val ctx = LocalContext.current as Activity
    val ui = rememberSystemUiController()

//    val orientation = LocalConfiguration.current.orientation
//    LaunchedEffect(key1 = orientation){
//        orientation.loge("EasyPlayer")
//    }

    DisposableEffect(key1 = Unit) {
        vm.onLaunch()
        onDispose {
            vm.onDisposed()
            ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(vm.fullScreenState) {
        if (vm.isFullScreen) {
            ctx.requestedOrientation =
                if (vm.isReverse) ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            ui.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            ui.isSystemBarsVisible = false
        } else {
            ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ui.isSystemBarsVisible = true
        }
    }

    // 根据传感器来横竖屏
    OnOrientationEvent(
        SensorManager.SENSOR_DELAY_NORMAL
    ) { listener, orientation ->
        orientation.loge("EasyPlayer")
        vm.onOrientation(orientation, act = ctx)
    }




    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            ui.isSystemBarsVisible = !vm.isFullScreen
        }
    }




    OnLifecycleEvent { _, event ->
        when (event) {
//            Lifecycle.Event.ON_RESUME -> vm.exoPlayer.play()
            Lifecycle.Event.ON_PAUSE -> vm.exoPlayer.pause()
            else -> Unit
        }
    }

    BackHandler(vm.isFullScreen) {
        vm.onFullScreen(false, ctx = ctx)
    }




    Column(
        modifier = modifier
    ) {
        EasyPlayer(
            modifier = Modifier
                .fillMaxWidth(), controlViewModel = vm, control = control, videoFloat = videoFloat
        )
        Box(modifier = Modifier.weight(1f)) {
            this@Column.content()
        }
    }


}

@Composable
fun EasyPlayer(
    modifier: Modifier,
    controlViewModel: ControlViewModel,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
) {

    val ctx = LocalContext.current as Activity

//    BoxWithConstraints {
//        val surModifier = remember(controlViewModel.isFullScreen) {
//            Modifier.run {
//                if (controlViewModel.isFullScreen) {
//                    fillMaxSize()
//                } else {
//                    width(maxWidth)
//                        .height(maxWidth / ControlViewModel.ratioWidth * ControlViewModel.ratioHeight)
//                }
//            }.background(Color.Black)
//
//        }
//        AndroidView(
//            modifier = surModifier,
//            factory = {
//                controlViewModel.surfaceView
//            })
//
//        Box(modifier = surModifier){
//            control?.invoke(controlViewModel)
//            videoFloat?.invoke(controlViewModel)
//        }
//
//    }

    BackgroundBasedBox(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .systemBarsPadding()
            .then(modifier),
        background = {

            val surModifier = remember(controlViewModel.isFullScreen) {
                Modifier
                    .run {
                        if (controlViewModel.isFullScreen) {
                            fillMaxSize()
                        } else {
                            fillMaxWidth().aspectRatio(ControlViewModel.ratioWidth / ControlViewModel.ratioHeight)
                        }
                    }

            }
            Box(
                modifier = surModifier, contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        controlViewModel.surfaceView
                    })
            }
        },
        foreground = {
            Box(modifier = Modifier.fillMaxSize()) {
                control?.invoke(controlViewModel)
                videoFloat?.invoke(controlViewModel)
            }
        },
    )

}

@Composable
fun GestureController(
    vm: ControlViewModel,
    modifier: Modifier,
    slideFullTime: Long = 300000,
) {

    val ctx = LocalContext.current as Activity
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    val showBrightVolumeUi = remember { mutableStateOf<DragType?>(null) }
    var brightVolumeUiIcon by remember { mutableStateOf(Icons.Filled.LightMode) }
    var brightVolumeUiText by remember { mutableStateOf(0) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
            .onSizeChanged {
                viewSize = it
            }
            .pointerInput("单机双击") {

                // 双击
                detectTapGestures(
                    onTap = {
                        vm.onSingleClick()
                    },
                    onDoubleTap = {
                        vm.onPlayPause(!vm.playWhenReady)
                    }
                )

            }.run {
                if (vm.isFullScreen && vm.controlState != ControlViewModel.ControlState.Locked) {

                    pointerInput("长按倍速"){

                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                vm.onLongPress()
                            },
                            onDragCancel = {
                                vm.onActionUP()
                            },
                            onDragEnd = {
                                vm.onActionUP()
                            },
                            onDrag = { _, _ -> }
                        )
                    }.
                    pointerInput("横向滑动") {
                        var horizontalOffset = 0F
                        var oldPosition = 0L
                        // 横向滑动
                        detectHorizontalDragGestures(
                            onDragStart = {
                                oldPosition = vm.position
                                horizontalOffset = 0F
                            },
                            onDragCancel = {
                                vm.onActionUP()
                            },
                            onDragEnd = {
                                vm.onActionUP()
                            },
                            onHorizontalDrag = { change: PointerInputChange, dragAmount: Float ->

                                horizontalOffset += dragAmount
                                val percent = horizontalOffset / viewSize.width
                                vm.onPositionChange(oldPosition + (slideFullTime * percent).toLong())
                            },
                        )
                    }.brightVolume(showBrightVolumeUi) { type -> // 音量、亮度
                        brightVolumeUiIcon = when (type) {
                            DragType.BRIGHTNESS -> Icons.Filled.LightMode
                            DragType.VOLUME -> Icons.Filled.VolumeUp
                        }
                        brightVolumeUiText = (when (type) {
                            DragType.BRIGHTNESS -> ctx.windowBrightness
                            DragType.VOLUME -> systemVolume
                        } * 100).toInt()
                    }

                } else {
                    this
                }
            }
    ) {
        // 音量、亮度
        AnimatedVisibility(
            visible = showBrightVolumeUi.value != null,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BrightVolumeUi(
                brightVolumeUiIcon,
                showBrightVolumeUi.value.toString(),
                brightVolumeUiText
            )
        }

        // 横向滑动
        AnimatedVisibility(
            visible = vm.controlState == ControlViewModel.ControlState.HorizontalScroll,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    text = TimeUtils.toString(vm.horizontalScrollPosition) + "/" + TimeUtils.toString(
                        vm.during
                    ), color = Color.White, style = MaterialTheme.typography.titleLarge
                )
            }
        }


        // 长按倍速
        AnimatedVisibility(
            visible = vm.isLongPress,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.FastForward,
                        stringResource(id = com.heyanle.easy_i18n.R.string.long_press_fast_forward),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                        text = stringResource(id = com.heyanle.easy_i18n.R.string.long_press_fast_forward),
                        color = Color.White
                    )

                }
            }
        }


    }
}

@Composable
fun SimpleTopBar(
    vm: ControlViewModel,
    modifier: Modifier,
) {
    if (vm.isFullScreen) {
        val isShow =
            if (vm.controlState == ControlViewModel.ControlState.Normal) vm.isNormalLockedControlShow else {
                vm.controlState != ControlViewModel.ControlState.Locked && vm.controlState != ControlViewModel.ControlState.Ended
            }

        val ctx = LocalContext.current as Activity
        AnimatedVisibility(
            modifier = modifier,
            visible = isShow,
            exit = fadeOut(),
            enter = fadeIn(),
        ) {
            TopControl(

            ) {
                BackBtn {
                    vm.onFullScreen(false, ctx = ctx)
                }
                Text(text = vm.title)
            }
        }
    }

}

@Composable
fun SimpleBottomBar(
    vm: ControlViewModel,
    modifier: Modifier,
    otherAction: (@Composable RowScope.(ControlViewModel) -> Unit)? = null,
) {
    val ctx = LocalContext.current as Activity
    val isShow =
        if (vm.controlState == ControlViewModel.ControlState.Normal) vm.isNormalLockedControlShow else {
            vm.controlState != ControlViewModel.ControlState.Locked && vm.controlState != ControlViewModel.ControlState.Ended
        }

    AnimatedVisibility(
        modifier = modifier,
        visible = isShow,
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        BottomControl {
            PlayPauseBtn(isPlaying = vm.playWhenReady, onClick = {
                vm.onPlayPause(it)
            })
            TimeText(time = vm.position)

            val position =
                if (vm.controlState == ControlViewModel.ControlState.Normal) vm.position else if (vm.controlState == ControlViewModel.ControlState.HorizontalScroll) vm.horizontalScrollPosition else 0


            TimeSlider(
                during = vm.during,
                position = position,
                onValueChange = {
                    vm.onPositionChange(it)
                },
                onValueChangeFinish = {
                    vm.onActionUP()
                }
            )

            TimeText(time = vm.during)

            otherAction?.invoke(this, vm)

            FullScreenBtn(isFullScreen = vm.isFullScreen, onClick = {
                vm.onFullScreen(it, ctx = ctx)
            })
        }
    }

}

@Composable
fun BoxScope.LockBtn(
    vm: ControlViewModel
) {
    val isShow =
        if (vm.controlState == ControlViewModel.ControlState.Normal || vm.controlState == ControlViewModel.ControlState.Locked) vm.isNormalLockedControlShow else {
            vm.controlState != ControlViewModel.ControlState.Locked && vm.controlState != ControlViewModel.ControlState.Ended
        }

    AnimatedVisibility(
        modifier = Modifier
            .align(Alignment.CenterStart),
        visible = vm.isFullScreen && isShow,
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        Box(modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable {
                vm.onLockedChange(vm.controlState != ControlViewModel.ControlState.Locked)
            }
            .padding(8.dp)
        ) {
            val icon =
                if (vm.controlState == ControlViewModel.ControlState.Locked) Icons.Filled.Lock else Icons.Filled.LockOpen

            Icon(
                icon,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
                contentDescription = stringResource(
                    id = com.heyanle.easy_i18n.R.string.locked
                )
            )
        }
    }
}

@Composable
fun BoxScope.ProgressBox(
    vm: ControlViewModel
) {
    if (vm.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}