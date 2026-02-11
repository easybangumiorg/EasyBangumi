package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.MediaPlayer
import org.easybangumi.next.shared.compose.media.SpeedTextBtn
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerVM
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.window.LocalEasyWindowState
import java.awt.Cursor

@Composable
actual fun BangumiMedia(mediaParam: MediaParam) {
    val vm = vm(::DesktopBangumiMediaVM, mediaParam)
    val pageParam = remember(vm) {
        BangumiMediaPageParam(vm)
    }
    val scope = rememberCoroutineScope()
    val state = vm.commonVM.state.collectAsState()
    val sta = state.value
    val windowsState = LocalEasyWindowState.current
    LaunchedEffect(Unit) {
        vm.onLaunch(windowsState)
    }
    DisposableEffect(Unit) {
        onDispose {
            vm.onDispose()
        }
    }

    val isTableMode = LocalUIMode.current.isTableMode

    var isExpanded by remember { mutableStateOf(true) }
    var sidePanelWidth by remember { mutableStateOf(vm.sidePanelWidthDp.get().dp) }
    val minPanelWidth = 230.dp
    val maxPanelWidth = 400.dp

    val expandedFactor by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    val spacerWidth = sidePanelWidth * expandedFactor
    val realSidePanelWidth = sidePanelWidth

    val diySpeed = vm.diySpeedFlow.collectAsState()

    BangumiPopup(vm.commonVM)
    if ((vm.playerVM.playconVM.screenMode == BasePlayconViewModel.ScreenMode.FULLSCREEN) || isTableMode) {


        Box(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                MediaPlayer(
                    modifier = Modifier.fillMaxHeight().weight(1f).background(Color.Black),
                    playerVm = vm.playerVM,
                    float = {
                        MediaPlayerFloat(vm)
                        if (vm.playerVM.playconVM.isShowController) (Box(
                            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd).background(
                                    MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp, 0.dp, 0.dp, 8.dp)
                                ).clickable {
                                    isExpanded = !isExpanded
                                }) {
                            Icon(
                                if (isExpanded) Icons.Default.ArrowRight else Icons.Default.ArrowLeft,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        })

                    },
                    secondLineControllerOther = {
                        SpeedTextBtn(
                            diySpeed = diySpeed.value,
                            speedSet = vm.speedList,
                            currentSpeed = vm.currentSpeed.value,
                            onSpeedChange = {
                                vm.onSpeedChange(it)
                            },
                            onEditDiySpeed = {
                                vm.onEditDiySpeed()
                            },
                            onMenuShowDismiss = {
                                if (it) {
                                    vm.playerVM.playconVM.endHideDelayJob()
                                } else {
                                    vm.playerVM.playconVM.restartHideDelayJob()
                                }
                            }
                        )
                    }
                )

                if (spacerWidth > 0.dp) {
                    Spacer(modifier = Modifier.fillMaxHeight().width(spacerWidth))
                }
            }
            Box(
                modifier = Modifier.fillMaxHeight().width(realSidePanelWidth)
                    .offset(realSidePanelWidth - spacerWidth, 0.dp).align(Alignment.TopEnd),
                contentAlignment = Alignment.TopEnd
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (isExpanded) {
                        val density = LocalDensity.current
                        Box(
                            modifier = Modifier.fillMaxHeight().width(3.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))).pointerInput(Unit) {
                                    detectDragGestures(onDragEnd = {
                                        vm.saveSidePanelWidth(sidePanelWidth.value.toInt())
                                    }, onDrag = { change, dragAmount ->
                                        change.consume()
                                        with(density) {
                                            val newWidthPx = (sidePanelWidth.toPx() - dragAmount.x).coerceIn(
                                                    minPanelWidth.toPx(),
                                                    maxPanelWidth.toPx()
                                                )
                                            sidePanelWidth = newWidthPx.toDp()
                                        }
                                    })
                                })
                    }
                    BangumiMediaPage(
                        pageParam, Modifier.fillMaxHeight().weight(1f)
                    )
                }
            }
        }

    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            MediaPlayer(
                modifier = Modifier.fillMaxWidth().aspectRatio(DesktopPlayerVM.MEDIA_COMPONENT_ASPECT)
                .background(Color.Black), playerVm = vm.playerVM, float = {
                MediaPlayerFloat(vm)
            }, secondLineControllerOther = {
                SpeedTextBtn(
                    diySpeed = diySpeed.value,
                    speedSet = vm.speedList,
                    currentSpeed = vm.currentSpeed.value,
                    onSpeedChange = {
                        vm.onSpeedChange(it)
                    },
                    onEditDiySpeed = {
                        vm.onEditDiySpeed()
                    }
                )
            })
            BangumiMediaPage(
                pageParam, Modifier.fillMaxWidth().weight(1f)
            )
        }
    }


}

@Composable
fun BoxScope.MediaPlayerFloat(
    vm: DesktopBangumiMediaVM
) {
    val playIndexState = vm.commonVM.playIndexState.collectAsState().value
    if (playIndexState.playInfo.isLoading()) {
        LoadingElements(
            modifier = Modifier.matchParentSize(),
            isRow = false,
            loadingMsg = stringRes(Res.strings.parsing),
            msgColor = Color.White
        )
    }

}
