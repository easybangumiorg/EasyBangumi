package loli.ball.easyplayer2

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.utils.TimeUtils
import loli.ball.easyplayer2.utils.loge
import loli.ball.easyplayer2.utils.pointerInput
import loli.ball.easyplayer2.utils.systemVolume
import loli.ball.easyplayer2.utils.windowBrightness

/**
 * Created by HeYanLe on 2023/3/27 21:47.
 * https://github.com/heyanLE
 */
class GestureControllerScope(
    boxScope: BoxScope,
    val vm: ControlViewModel,
    val showBrightVolumeUi: MutableState<Boolean> = mutableStateOf(false),
    val brightVolumeType: MutableState<DragType> = mutableStateOf(DragType.VOLUME),
    val brightVolumePercent: MutableState<Int> = mutableStateOf(0),
) : BoxScope by boxScope

@Composable
fun SimpleGestureController(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    slideFullTime: Long = 300000,
    longTouchText: String = "2x",
) {
    GestureController(vm, modifier, slideFullTime) {
        BrightVolumeUI()
        SlideUI()
        LongTouchUI(longTouchText)
    }
}

@Composable
fun GestureControllerWithFast(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    slideFullTime: Long = 300000,
    longTouchText: String = "2x",
    fastForwardText: String = "快进",
    fastBackText: String = "快退",
    fastWinDelay: Long = 2000,
    fastWeight: Float = 0.2f
) {
    GestureController(vm, modifier, slideFullTime, supportFast = true, fastWeight = fastWeight) {
        BrightVolumeUI()
        SlideUI()
        LongTouchUI(longTouchText)
        FastUI(
            fastForwardText = fastForwardText,
            fastRewindText = fastBackText,
            fastWeight = fastWeight,
            delayTime = fastWinDelay,)

    }
}

@Composable
fun GestureController(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    slideFullTime: Long = 300000,
    supportFast: Boolean = false,
    fastWeight: Float = 0.2f,
    content: @Composable GestureControllerScope.(ControlViewModel) -> Unit,
) {
    val ctx = LocalContext.current as Activity
    var viewSize by remember { mutableStateOf(IntSize.Zero) }


    val showBrightVolumeUi = remember { mutableStateOf<Boolean>(false) }
    val brightVolumeTYpe = remember {
        mutableStateOf<DragType>(DragType.VOLUME)
    }
    val brightVolumeUiText = remember { mutableStateOf(0) }

    val enableGuest by remember {
        derivedStateOf {
            vm.isFullScreen && vm.controlState != ControlViewModel.ControlState.Locked
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
            .onSizeChanged { viewSize = it }
            .pointerInput("单机双击", true) {
                // 双击
                detectTapGestures(
                    onTap = {
                        "onTap".loge("GestureController")
                        vm.onSingleClick()
                    },
                    onDoubleTap = {
                        "onDoubleTap".loge("GestureController")
                        if (!supportFast) {
                            vm.onPlayPause(!vm.playWhenReady)
                        } else if (enableGuest && it.x < viewSize.width * fastWeight) {
                            vm.fastRewind()
                        } else if (enableGuest && it.x > viewSize.width * (1 - fastWeight)) {
                            vm.fastForward()
                        } else {
                            vm.onPlayPause(!vm.playWhenReady)
                        }

                    }
                )
            }
            .pointerInput("长按倍速", enableGuest) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { vm.onLongPress() },
                    onDragCancel = { vm.onActionUP() },
                    onDragEnd = { vm.onActionUP() },
                    onDrag = { _, _ -> }
                )
            }
            .pointerInput("横向滑动", enableGuest) {
                var horizontalOffset = 0F
                var oldPosition = 0L
                // 横向滑动
                detectHorizontalDragGestures(
                    onDragStart = {
                        "onDragStart".loge("GestureController")
                        oldPosition = vm.position
                        horizontalOffset = 0F
                    },
                    onDragCancel = { vm.onActionUP() },
                    onDragEnd = { vm.onActionUP() },
                    onHorizontalDrag = { _: PointerInputChange, dragAmount: Float ->
                        horizontalOffset += dragAmount
                        val percent = horizontalOffset / viewSize.width
                        vm.onPositionChange(oldPosition + (slideFullTime * percent).toFloat())
                    },
                )
            }
            .brightVolume(enableGuest, showBrightVolumeUi, brightVolumeTYpe) { type -> // 音量、亮度

                brightVolumeUiText.value = (when (type) {
                    DragType.BRIGHTNESS -> ctx.windowBrightness
                    DragType.VOLUME -> with(ctx) { systemVolume }
                } * 100).toInt()
            }
    ) {
        val scope = remember(this, vm) {
            GestureControllerScope(
                this,
                vm,
                showBrightVolumeUi,
                brightVolumeTYpe,
                brightVolumeUiText
            )
        }
        scope.content(vm)
    }
}

@Composable
fun GestureController(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    slideFullTime: Long = 300000,
    supportFast: Boolean = false,
    horizontalDoubleTapWeight: Float = 0.2f,
    verticalDoubleTapWeight: Float = 0.5f,
    topFastTime: Long,
    content: @Composable GestureControllerScope.(ControlViewModel) -> Unit,
) {
    val ctx = LocalContext.current as Activity
    var viewSize by remember { mutableStateOf(IntSize.Zero) }


    val showBrightVolumeUi = remember { mutableStateOf<Boolean>(false) }
    val brightVolumeTYpe = remember {
        mutableStateOf<DragType>(DragType.VOLUME)
    }
    val brightVolumeUiText = remember { mutableStateOf(0) }

    val enableGuest by remember {
        derivedStateOf {
            vm.isFullScreen && vm.controlState != ControlViewModel.ControlState.Locked
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
            .onSizeChanged { viewSize = it }
            .pointerInput("单机双击", true) {
                // 双击
                detectTapGestures(
                    onTap = {
                        "onTap".loge("GestureController")
                        vm.onSingleClick()
                    },
                    onDoubleTap = {
                        "onDoubleTap".loge("GestureController")
                        if (vm.controlState == ControlViewModel.ControlState.Locked){
                            return@detectTapGestures
                        }
                        if (!supportFast || !vm.isFullScreen) {
                            vm.onPlayPause(!vm.playWhenReady)
                        } else if (it.x < viewSize.width * horizontalDoubleTapWeight) {
                            if (it.y < viewSize.height * verticalDoubleTapWeight) {
                                vm.fastRewindTop(topFastTime)
                            } else {
                                vm.fastRewind()
                            }
                        } else if (it.x > viewSize.width * (1 - horizontalDoubleTapWeight)) {
                            if (it.y < viewSize.height * verticalDoubleTapWeight) {
                                vm.fastForwardTop(topFastTime)
                            } else {
                                vm.fastForward()
                            }
                        } else {
                            vm.onPlayPause(!vm.playWhenReady)
                        }

                    }
                )
            }
            .pointerInput("长按倍速", enableGuest) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { vm.onLongPress() },
                    onDragCancel = { vm.onActionUP() },
                    onDragEnd = { vm.onActionUP() },
                    onDrag = { _, _ -> }
                )
            }
            .pointerInput("横向滑动", enableGuest) {
                var horizontalOffset = 0F
                var oldPosition = 0L
                // 横向滑动
                detectHorizontalDragGestures(
                    onDragStart = {
                        "onDragStart".loge("GestureController")
                        oldPosition = vm.position
                        horizontalOffset = 0F
                    },
                    onDragCancel = { vm.onActionUP() },
                    onDragEnd = { vm.onActionUP() },
                    onHorizontalDrag = { _: PointerInputChange, dragAmount: Float ->
                        horizontalOffset += dragAmount
                        val percent = horizontalOffset / viewSize.width
                        vm.onPositionChange(oldPosition + (slideFullTime * percent).toFloat())
                    },
                )
            }
            .brightVolume(enableGuest, showBrightVolumeUi, brightVolumeTYpe) { type -> // 音量、亮度

                brightVolumeUiText.value = (when (type) {
                    DragType.BRIGHTNESS -> ctx.windowBrightness
                    DragType.VOLUME -> with(ctx) { systemVolume }
                } * 100).toInt()
            }
    ) {
        val scope = remember(this, vm) {
            GestureControllerScope(
                this,
                vm,
                showBrightVolumeUi,
                brightVolumeTYpe,
                brightVolumeUiText
            )
        }
        scope.content(vm)
    }
}


// 音量 亮度
@Composable
fun GestureControllerScope.BrightVolumeUI() {
    val brightVolumeUiIcon = remember(showBrightVolumeUi.value) {
        when (brightVolumeType.value) {
            DragType.BRIGHTNESS -> Icons.Filled.LightMode
            DragType.VOLUME -> Icons.Filled.VolumeUp
        }
    }
    AnimatedVisibility(
        visible = showBrightVolumeUi.value,
        modifier = Modifier.align(Alignment.Center),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        BrightVolumeUi(
            brightVolumeUiIcon,
            this@BrightVolumeUI.showBrightVolumeUi.value.toString(),
            this@BrightVolumeUI.brightVolumePercent.value
        )
    }


}

@Composable
fun GestureControllerScope.SlideUI() {
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
                textAlign = TextAlign.Center,
                text = TimeUtils.toString(this@GestureControllerScope.vm.horizontalScrollPosition.toLong()) + "/" +
                        TimeUtils.toString(this@GestureControllerScope.vm.during),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun GestureControllerScope.LongTouchUI(text: String = "2x") {
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
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    text = text,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun GestureControllerScope.FastUI(
    fastForwardText: String = "快进",
    fastRewindText: String = "快退",
    fastWeight: Float = 0.2f,

    delayTime: Long = 2000
) {
    LaunchedEffect(key1 = Unit) {
        launch {
            snapshotFlow {
                vm.isFastRewindWinShow
            }.collectLatest {
                if (it) {
                    delay(delayTime)
                    vm.isFastRewindWinShow = false
                }
            }
        }
        launch {
            snapshotFlow {
                vm.isFastForwardWinShow
            }.collectLatest {
                if (it) {
                    delay(delayTime)
                    vm.isFastForwardWinShow = false
                }
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = this@FastUI.vm.isFastRewindWinShow,
            modifier = Modifier
                .weight(maxOf(fastWeight, 0.2f))
                .fillMaxHeight(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .clip(
                        RoundedCornerShape(
                            CornerSize(0),
                            CornerSize(100),
                            CornerSize(100),
                            CornerSize(0)
                        )
                    )
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.FastRewind,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                        text = fastRewindText,
                        color = Color.White
                    )


                }
            }
        }
        Spacer(modifier = Modifier.weight(1f - fastWeight))
        AnimatedVisibility(
            visible = this@FastUI.vm.isFastForwardWinShow,
            modifier = Modifier
                .weight(maxOf(fastWeight, 0.2f))
                .fillMaxHeight(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .clip(
                        RoundedCornerShape(
                            CornerSize(100),
                            CornerSize(0),
                            CornerSize(0),
                            CornerSize(100)
                        )
                    )
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Center,
                        text = fastForwardText,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        Icons.Filled.FastForward,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

}

@Composable
fun GestureControllerScope.FastUI(
    fastForwardText: String = "快进",
    fastRewindText: String = "快退",

    horizontalDoubleTapWeight: Float = 0.2f,
    verticalDoubleTapWeight: Float = 0.5f,
    delayTime: Long = 2000,
) {
    val realHorizontalWeight = horizontalDoubleTapWeight.coerceAtLeast(0.2f)
    LaunchedEffect(key1 = Unit) {
        launch {
            snapshotFlow {
                vm.isFastForwardTopShow || vm.isFastForwardWinShow || vm.isFastRewindWinShow || vm.isFastRewindTopShow
            }.collectLatest {
                if (it) {
                    delay(delayTime)
                    vm.isFastRewindWinShow = false
                    vm.isFastForwardWinShow = false

                    vm.isFastRewindTopShow = false
                    vm.isFastForwardTopShow = false
                }
            }
        }
    }

    AnimatedVisibility(
        visible = vm.isFastForwardTopShow || vm.isFastForwardWinShow || vm.isFastRewindWinShow || vm.isFastRewindTopShow,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row {
            Column {
                Box(modifier = Modifier.weight(verticalDoubleTapWeight).fillMaxWidth()){
                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    CornerSize(0),
                                    CornerSize(100),
                                    CornerSize(100),
                                    CornerSize(0)
                                )
                            )
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.FastRewind,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                text = fastRewindText,
                                color = Color.White
                            )


                        }
                    }
                }
                Box(modifier = Modifier.weight(1 - verticalDoubleTapWeight).fillMaxWidth()){
                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    CornerSize(0),
                                    CornerSize(100),
                                    CornerSize(100),
                                    CornerSize(0)
                                )
                            )
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.FastRewind,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                text = fastRewindText,
                                color = Color.White
                            )


                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f - 2*horizontalDoubleTapWeight))
            Column {
                Box(modifier = Modifier.weight(verticalDoubleTapWeight).fillMaxWidth()){
                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    CornerSize(100),
                                    CornerSize(0),
                                    CornerSize(0),
                                    CornerSize(100)
                                )
                            )
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                text = fastForwardText,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(
                                Icons.Filled.FastForward,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Box(modifier = Modifier.weight(1 - verticalDoubleTapWeight).fillMaxWidth()){
                    Box(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    CornerSize(100),
                                    CornerSize(0),
                                    CornerSize(0),
                                    CornerSize(100)
                                )
                            )
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                text = fastForwardText,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(
                                Icons.Filled.FastForward,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

