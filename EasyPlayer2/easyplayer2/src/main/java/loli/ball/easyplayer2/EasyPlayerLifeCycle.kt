package loli.ball.easyplayer2

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import loli.ball.easyplayer2.utils.OnLifecycleEvent
import loli.ball.easyplayer2.utils.OnOrientationEvent
import loli.ball.easyplayer2.utils.loge

/**
 * Created by LoliBall on 2023/4/3 0:34.
 * https://github.com/WhichWho
 */

@Composable
fun EasyPlayerStateSync(vm: ControlViewModel) {

    val ctx = LocalContext.current as Activity
    val ui = rememberSystemUiController()

    DisposableEffect(Unit) {
        vm.onLaunch()
        val old = ctx.requestedOrientation
        onDispose {
            "onDisposed".loge("EasyPlayerStateSync")
            vm.onDisposed()
            ctx.requestedOrientation = old
        }
    }

    LaunchedEffect(vm.fullScreenState) {
        if (vm.isFullScreen) {
//            ctx.requestedOrientation =
//                if (vm.isReverse) ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
//                else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            ui.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            ui.isSystemBarsVisible = false
        } else {
//            ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ui.isSystemBarsVisible = true
        }
    }

    // 根据传感器来横竖屏
    OnOrientationEvent { _, orientation ->
        vm.onOrientation(orientation, act = ctx)
    }


    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> ui.isSystemBarsVisible = !vm.isFullScreen
            Lifecycle.Event.ON_PAUSE -> vm.exoPlayer.pause()
            else -> Unit
        }
    }

    BackHandler(vm.isFullScreen) {
        vm.onFullScreen(false, ctx = ctx)
    }

}