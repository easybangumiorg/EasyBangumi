package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.MediaPlayer
import org.easybangumi.next.shared.compose.media.bangumi.page.BangumiMediaPage
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerVM
import org.easybangumi.next.shared.resources.Res

@Composable
actual fun BangumiMedia(mediaParam: MediaParam) {
    val vm = vm(::DesktopBangumiMediaVM, mediaParam)
    val scope = rememberCoroutineScope()
    val state = vm.commonVM.state.collectAsState()
    val sta = state.value


    val isTableMode = LocalUIMode.current.isTableMode

    BangumiPopup(vm.commonVM)
    if (!sta.isFullscreen) {
        if (isTableMode) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                MediaPlayer(
                    modifier = Modifier.fillMaxHeight().weight(1f).background(Color.Black),
                    playerVm = vm.playerVM
                ) {
                    MediaPlayerFloat(vm)
                }
                BangumiMediaPage(
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
                    playerVm = vm.playerVM
                ){
                    MediaPlayerFloat(vm)
                }
                BangumiMediaPage(
                    vm.commonVM,
                    Modifier.fillMaxWidth().weight(1f)
                )
            }
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
            loadingMsg = stringRes(Res.strings.parsing)
        )
    }

}