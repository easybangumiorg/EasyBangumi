package loli.ball.easyplayer2.utils

import android.hardware.SensorManager
import android.view.OrientationEventListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Created by LoliBall on 2023/3/25 19:43.
 * https://github.com/WhichWho
 */
class CDAction(private val cd: Int = 300) {
    private var lastTime = -1L
    fun action(block: () -> Unit) {
        val now = System.currentTimeMillis()
        if (lastTime < 0 || now - lastTime >= cd) {
            lastTime = now
            block()
        }
    }
}

@Composable
fun OnOrientationEvent(
    rate: Int = SensorManager.SENSOR_DELAY_NORMAL,
    changeCD: Int = 300,
    onEvent: (listener: OrientationEventListener, orientation: Int) -> Unit
) {
    val ctx = LocalContext.current
    val cdAction = remember(changeCD) {
        CDAction(changeCD)
    }
    val listener = remember(ctx) {
        object : OrientationEventListener(ctx, rate) {
            override fun onOrientationChanged(orientation: Int) {
                cdAction.action {
                    onEvent(this, orientation)
                }
            }
        }
    }
    DisposableEffect(key1 = Unit) {
        listener.enable()
        onDispose {
            listener.disable()
        }
    }

}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

fun Modifier.pointerInput(
    key1: Any?,
    enable: Boolean,
    block: suspend PointerInputScope.() -> Unit
): Modifier = composed {
    if (enable) pointerInput(key1, block)
    else this
}