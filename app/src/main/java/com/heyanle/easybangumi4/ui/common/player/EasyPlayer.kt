package com.heyanle.easybangumi4.ui.common.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.heyanle.easybangumi4.ui.common.BackgroundBasedBox
import com.heyanle.easybangumi4.ui.common.player.surface.EasySurfaceView
import com.heyanle.easybangumi4.ui.common.player.utils.isKeepScreenOn
import com.heyanle.easybangumi4.utils.OnLifecycleEvent
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

    Column(
        modifier = modifier
    ) {
        EasyPlayer(
            modifier = Modifier
                .fillMaxWidth()
            , controlViewModel = vm, control = control, videoFloat = videoFloat
        )
        Box(modifier = Modifier.weight(1f)){
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

    val androidView = remember<@Composable ()->Unit> {
        {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    EasySurfaceView(it)
                }){
                controlViewModel.onSurfaceView(it)
            }
        }
    }

    BackgroundBasedBox(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        background = {
            if(controlViewModel.isFullScreen){
                "test1".loge()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {

                    androidView()

                }
            }else{
                "test2".loge()
                BoxWithConstraints {
                    Box(
                        modifier = Modifier
                            .width(maxWidth)
                            .height(maxWidth / ControlViewModel.ratioWidth * ControlViewModel.ratioHeight)
                            .background(Color.Black)
                    ) {

                        androidView()

                    }
                }

            }
        },
        foreground = {
            Box(modifier = Modifier.fillMaxSize()){
                control?.invoke(controlViewModel)
                videoFloat?.invoke(controlViewModel)
            }
        },
    )

}

@Composable
fun SimpleTopBar(
    vm: ControlViewModel,
    modifier: Modifier,
) {
    if (vm.isFullScreen) {
        val isShow = if(vm.controlState == ControlViewModel.ControlState.Normal) vm.isNormalLockedControlShow else {vm.controlState  != ControlViewModel.ControlState.Locked && vm.controlState != ControlViewModel.ControlState.Ended }

        AnimatedVisibility(
            modifier = modifier,
            visible = isShow,
            exit = fadeOut(),
            enter = fadeIn(),
        ) {
            TopControl(

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
fun SimpleBottomBar(
    vm: ControlViewModel,
    modifier: Modifier,
    otherAction: (@Composable RowScope.(ControlViewModel) -> Unit)? = null,
) {
    val isShow = if(vm.controlState == ControlViewModel.ControlState.Normal) vm.isNormalLockedControlShow else {vm.controlState  != ControlViewModel.ControlState.Locked && vm.controlState != ControlViewModel.ControlState.Ended }

    AnimatedVisibility(
        modifier = modifier,
        visible = isShow,
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        BottomControl{
            PlayPauseBtn(isPlaying = vm.playWhenReady, onClick = {
                vm.onPlayPause(it)
            })
            TimeText(time = vm.position)

            val position =
                if (vm.controlState == ControlViewModel.ControlState.Normal) vm.position else if (vm.controlState == ControlViewModel.ControlState.HorizontalScroll) vm.horizontalScrollPosition else 0


            TimeSlider(
                during = vm.during.coerceAtLeast(0),
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
    val isShow = if(vm.controlState == ControlViewModel.ControlState.Normal || vm.controlState == ControlViewModel.ControlState.Locked) vm.isNormalLockedControlShow else {vm.controlState  != ControlViewModel.ControlState.Locked && vm.controlState != ControlViewModel.ControlState.Ended }

    AnimatedVisibility(
        modifier = Modifier
            .align(Alignment.CenterStart),
        visible = isShow,
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
        ){
            val icon =
                if (vm.controlState == ControlViewModel.ControlState.Locked) Icons.Filled.Lock else Icons.Filled.LockOpen

            Icon(
                icon,
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