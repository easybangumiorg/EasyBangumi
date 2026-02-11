package org.easybangumi.next.shared.compose.media.normal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.MediaPlayer
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerVM
import org.easybangumi.next.shared.resources.Res

@Composable
actual fun NormalMedia(param: MediaParam) {
    val vm = vm(::DesktopNormalMediaVM, param)
    val scope = rememberCoroutineScope()
    val state = vm.commonVM.state.collectAsState()
    val sta = state.value


    val isTableMode = LocalUIMode.current.isTableMode

//    BangumiPopup(vm.commonVM)
    if (!sta.isFullscreen) {
        if (isTableMode) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                MediaPlayer(
                    modifier = Modifier.fillMaxHeight().weight(1f).background(Color.Black),
                    playerVm = vm.playerVM,
                    float = {
                        MediaPlayerFloat(vm)
                    }
                )
                NormalMediaPage(
                    vm.commonVM,
                    Modifier.fillMaxHeight().width(384.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                MediaPlayer(
                    modifier = Modifier.fillMaxWidth().aspectRatio(DesktopPlayerVM.MEDIA_COMPONENT_ASPECT)
                        .background(Color.Black),
                    playerVm = vm.playerVM,
                    float = {
                        MediaPlayerFloat(vm)
                    }
                )
                NormalMediaPage(
                    vm.commonVM,
                    Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
    }

}

@Composable
fun BoxScope.MediaPlayerFloat(
    vm: DesktopNormalMediaVM
) {
    val playIndexState = vm.commonVM.playIndexState.collectAsState().value
    if (playIndexState.playInfo.isLoading()) {
        LoadingElements(
            modifier = Modifier.matchParentSize(),
            isRow = false,
            loadingMsg = stringRes(Res.strings.parsing)
        )
    }

}