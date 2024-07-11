package loli.ball.easyplayer2

import android.app.Activity
import android.content.Context
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.utils.loge
import loli.ball.easyplayer2.utils.rememberBatteryReceiver

/**
 * Created by LoliBall on 2023/4/3 0:32.
 * https://github.com/WhichWho
 */

@Composable
fun SimpleTopBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isShowOnNormalScreen: Boolean = false,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay() && (vm.isFullScreen || isShowOnNormalScreen),
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        TopControl {
            val ctx = LocalContext.current as Activity
            BackBtn {
                vm.onFullScreen(false, ctx = ctx)
            }
            Text(text = vm.title, color = Color.White)
        }
    }
}

@Composable
fun ElectricityTopBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isShowOnNormalScreen: Boolean = false,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay() && (vm.isFullScreen || isShowOnNormalScreen),
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        TopControl {
            val ctx = LocalContext.current as Activity
            BackBtn {
                vm.onFullScreen(false, ctx = ctx)
            }
            Text(text = vm.title, color = Color.White)

            Spacer(modifier = Modifier.weight(1f))

            val br = rememberBatteryReceiver()


            val ic = if (br.isCharge.value) {
                Icons.Filled.BatteryChargingFull
            } else {
                if (br.electricity.value <= 10) {
                    Icons.Filled.Battery0Bar
                } else if (br.electricity.value <= 20) {
                    Icons.Filled.Battery2Bar
                } else if (br.electricity.value <= 40) {
                    Icons.Filled.Battery3Bar
                } else if (br.electricity.value <= 60) {
                    Icons.Filled.Battery4Bar
                } else if (br.electricity.value <= 70) {
                    Icons.Filled.Battery5Bar
                } else if (br.electricity.value <= 90) {
                    Icons.Filled.Battery6Bar
                } else {
                    Icons.Filled.BatteryFull
                }
            }
            Icon(ic, "el", modifier = Modifier.rotate(90F), tint = Color.White)
            Text(text = "${br.electricity.value}%", color = Color.White)
            Spacer(modifier = Modifier.size(16.dp))


        }
    }
}


@Composable
fun SimpleBottomBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    otherAction: (@Composable RowScope.(ControlViewModel) -> Unit)? = null,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay(),
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        BottomControl(
            paddingValues
        ) {
            PlayPauseBtn(isPlaying = vm.playWhenReady, onClick = {
                vm.onPlayPause(it)
            })
            TimeText(time = vm.position, Color.White)



            val position by remember {
                derivedStateOf {
                    when (vm.controlState) {
                        ControlViewModel.ControlState.HorizontalScroll -> {
                            vm.horizontalScrollPosition
                        }
                        ControlViewModel.ControlState.Normal -> {
                            vm.position.toFloat()
                        }
                        else -> {
                            0f
                        }
                    }
                }
            }

            TimeSlider(
                during = vm.during,
                position = position,
                onValueChange = {
                    "onValueChange $it".loge("EasyPlayerExtends")
                    vm.onPositionChange(it)
                },
                onValueChangeFinish = {
                    "onValueChangeFinish".loge("EasyPlayerExtends")
                    vm.onActionUP()
                }
            )

            TimeText(time = vm.during, Color.White)

            otherAction?.invoke(this, vm)

            val ctx = LocalContext.current as Activity
            FullScreenBtn(isFullScreen = vm.isFullScreen, onClick = {
                vm.onFullScreen(it, ctx = ctx)
            })
        }
    }

}

@Composable
fun SimpleBottomBarWithSeekBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    otherAction: (@Composable RowScope.(ControlViewModel) -> Unit)? = null,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay(),
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        BottomControl(
            paddingValues
        ) {
            PlayPauseBtn(isPlaying = vm.playWhenReady, onClick = {
                vm.onPlayPause(it)
            })
            TimeText(time = vm.position, Color.White)



            val position by remember {
                derivedStateOf {
                    when (vm.controlState) {
                        ControlViewModel.ControlState.HorizontalScroll -> {
                            vm.horizontalScrollPosition
                        }
                        ControlViewModel.ControlState.Normal -> {
                            vm.position.toFloat()
                        }
                        else -> {
                            0f
                        }
                    }
                }
            }

            ViewSeekBar(
                during = vm.during.toInt(),
                position = position.toInt(),
                secondary = vm.bufferPosition.toInt(),
                onValueChange = {
                    vm.onPositionChange(it.toFloat())
                },
                onValueChangeFinish = {
                    vm.onActionUPScope()
                }
            )

            TimeText(time = vm.during, Color.White)

            otherAction?.invoke(this, vm)

            val ctx = LocalContext.current as Activity
            FullScreenBtn(isFullScreen = vm.isFullScreen, onClick = {
                vm.onFullScreen(it, ctx = ctx)
            })
        }
    }

}

@Composable
fun BoxScope.LockBtn(vm: ControlViewModel) {

    val isShowLock = when (vm.controlState) {
        ControlViewModel.ControlState.Normal -> vm.isNormalLockedControlShow
        ControlViewModel.ControlState.Locked -> vm.isNormalLockedControlShow
        ControlViewModel.ControlState.Ended -> false
        else -> true
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.CenterStart),
        visible = vm.isFullScreen && isShowLock,
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
            Icon(
                if (vm.controlState == ControlViewModel.ControlState.Locked) Icons.Filled.Lock
                else Icons.Filled.LockOpen,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
                contentDescription = null
            )
        }
    }
}

@Composable
fun BoxScope.ProgressBox(vm: ControlViewModel) {
    if (vm.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
