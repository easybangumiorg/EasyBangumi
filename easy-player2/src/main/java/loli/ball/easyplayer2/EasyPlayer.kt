package loli.ball.easyplayer2

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Created by HeYanLe on 2023/3/9 11:23.
 * https://github.com/heyanLE
 */
@Composable
fun EasyPlayerScaffold(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    EasyPlayerStateSync(vm)
    Column(modifier) {
        EasyPlayer(
            modifier = Modifier.fillMaxWidth(),
            vm = vm,
            control = control,
            isPadMode = false,
            videoFloat = videoFloat
        )
        this@Column.content()
    }

}

// content 是 Box，不是 Column
@Composable
fun EasyPlayerScaffoldBase(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    needSync: Boolean = true,
    isPadMode: Boolean = false,
    contentWeight: Float = 1f,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    if (needSync) {
        EasyPlayerStateSync(vm)
    }
    if (isPadMode) {
        Row(modifier) {
            EasyPlayer(
                modifier = Modifier.weight(1f),
                vm = vm,
                control = control,
                isPadMode = isPadMode,
                videoFloat = videoFloat
            )
            if (!vm.isFullScreen) {
                Box(
                    modifier = Modifier.weight(contentWeight),
                ) {
                    this.content()
                }
            }
        }
    } else {
        Column(modifier) {
            EasyPlayer(
                modifier = Modifier.fillMaxWidth(),
                vm = vm,
                control = control,
                isPadMode = isPadMode,
                videoFloat = videoFloat
            )
            if (!vm.isFullScreen) {
                Box {
                    this.content()
                }
            }
        }
    }
}

@Composable
fun EasyPlayer(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isPadMode: Boolean = false,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
) {
    BackgroundBasedBox(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .let {
                if (vm.isFullScreen) it
                else it.statusBarsPadding()
            }
            .then(modifier),
        background = {
            val surModifier = remember(vm.isFullScreen) {
                if (vm.isFullScreen) {
                    Modifier.fillMaxSize()
                } else if (!isPadMode) {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(ControlViewModel.ratioWidth / ControlViewModel.ratioHeight)
                } else {
                    Modifier.fillMaxSize()
                }
            }
            Box(modifier = surModifier, contentAlignment = Alignment.Center) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        vm.render.getOrCreateView(it).apply {
                            kotlin.runCatching {
                                (parent as? ViewGroup)?.removeView(this)
                                //root.addView(this, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                            }.onFailure {
                                it.printStackTrace()
                            }
                        }
                    }
                )
            }
        },
        foreground = {
            Box(modifier = Modifier.fillMaxSize()) {
                control?.invoke(vm)
                videoFloat?.invoke(vm)
            }
        }
    )
}
