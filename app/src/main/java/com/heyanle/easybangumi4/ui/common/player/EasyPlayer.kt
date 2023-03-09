package com.heyanle.easybangumi4.ui.common.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.heyanle.easybangumi4.ui.common.BackgroundBasedBox
import com.heyanle.easybangumi4.ui.common.player.utils.isKeepScreenOn
import com.heyanle.easybangumi4.utils.OnLifecycleEvent

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
    content: @Composable (PaddingValues) -> Unit,
) {

    val density = LocalDensity.current
    var videoHeight by remember {
        mutableStateOf(320.dp)
    }

    val ctx = LocalContext.current as Activity
    val ui = rememberSystemUiController()
    LaunchedEffect(vm.isFullScreen) {
        if (vm.isFullScreen) {
            ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            ui.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            ui.isSystemBarsVisible = false
        } else {
            ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ui.isSystemBarsVisible = true
        }
    }
    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            if (vm.isFullScreen) {
                ui.isSystemBarsVisible = false
            }
        }
    }
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> vm.exoPlayer.play()
            Lifecycle.Event.ON_PAUSE -> vm.exoPlayer.pause()
            else -> Unit
        }
    }
    DisposableEffect(Unit) {
        ctx.isKeepScreenOn = true
        onDispose { ctx.isKeepScreenOn = false }
    }
    BackHandler(vm.isFullScreen) {
        vm.onFullScreen(false)
    }

    Box(modifier = modifier) {
        EasyPlayer(
            modifier = Modifier.onSizeChanged {
                videoHeight = with(density) { it.height.toDp() }
            }, controlViewModel = vm, control = control, videoFloat = videoFloat
        )
        if (!vm.isFullScreen) {
            content(PaddingValues(0.dp, videoHeight, 0.dp, 0.dp))
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
    val surfaceModifier = remember(controlViewModel.isFullScreen) {
        Modifier.apply {
            if (controlViewModel.isFullScreen) {
                fillMaxSize()
            } else {
                fillMaxWidth().aspectRatio(ControlViewModel.ratioWidth / ControlViewModel.ratioHeight)
            }
        }

    }

    BackgroundBasedBox(modifier = modifier, background = {

        DisposableEffect(key1 = Unit) {
            controlViewModel.onLaunch()
            onDispose {
                controlViewModel.onDisposed()
            }
        }

        Box(
            modifier = surfaceModifier, contentAlignment = Alignment.Center
        ) {
            AndroidView(factory = {
                controlViewModel.surfaceView
            })

        }
    }, foreground = {
        Box(modifier = Modifier.fillMaxSize()){
            control?.invoke(controlViewModel)
            videoFloat?.invoke(controlViewModel)
        }

    })
}

@Composable
fun BoxScope.SimpleTopBar(
    vm: ControlViewModel
) {
    if (vm.isFullScreen) {
        val isShow =
            ((vm.controlState == ControlViewModel.ControlState.Normal) && vm.isNormalLockedControlShow) || vm.controlState != ControlViewModel.ControlState.Ended || vm.controlState != ControlViewModel.ControlState.Normal || vm.controlState != ControlViewModel.ControlState.Locked

        AnimatedVisibility(visible = isShow) {
            TopControl(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                BackBtn {
                    vm.onFullScreen(false)
                }
                Text(text = vm.title)
            }
        }
    }

}

@Composable
fun BoxScope.SimpleBottomBar(
    vm: ControlViewModel,
    otherAction: (@Composable RowScope.(ControlViewModel) -> Unit)? = null,
) {
    val isShow =
        ((vm.controlState == ControlViewModel.ControlState.Normal) && vm.isNormalLockedControlShow) || vm.controlState != ControlViewModel.ControlState.Ended || vm.controlState != ControlViewModel.ControlState.Normal || vm.controlState != ControlViewModel.ControlState.Locked

    AnimatedVisibility(visible = isShow) {
        BottomControl(
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
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
                vm.onFullScreen(it)
            })
        }
    }

}

@Composable
fun BoxScope.LockBtn(
    vm: ControlViewModel
) {
    val isShow =
        ((vm.controlState == ControlViewModel.ControlState.Normal || vm.controlState == ControlViewModel.ControlState.Locked) && vm.isNormalLockedControlShow) || vm.controlState != ControlViewModel.ControlState.Ended || vm.controlState != ControlViewModel.ControlState.Normal
    AnimatedVisibility(visible = isShow) {
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.5f))
                .padding(2.dp),
            onClick = {
                vm.onLockedChange(vm.controlState != ControlViewModel.ControlState.Locked)
            }) {
            val icon =
                if (vm.controlState == ControlViewModel.ControlState.Locked) Icons.Filled.Lock else Icons.Filled.LockOpen

            Icon(
                icon, contentDescription = stringResource(
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