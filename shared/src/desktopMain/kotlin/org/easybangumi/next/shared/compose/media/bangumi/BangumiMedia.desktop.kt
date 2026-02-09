package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.colorResource
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.MediaPlayer
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.press.PressModifier
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.playcon.BasePlayconViewModel
import org.easybangumi.next.shared.playcon.BasePlayconViewModel.ScreenMode
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerVM
import org.easybangumi.next.shared.resources.Res
import org.jetbrains.skia.paragraph.Alignment

@Composable
actual fun BangumiMedia(mediaParam: MediaParam) {
    val vm = vm(::DesktopBangumiMediaVM, mediaParam)
    val pageParam = remember(vm) {
        BangumiMediaPageParam(vm)
    }
    val scope = rememberCoroutineScope()
    val state = vm.commonVM.state.collectAsState()
    val sta = state.value
    LaunchedEffect(Unit) {
        vm.onLaunch()
    }
    DisposableEffect(Unit) {
        onDispose {
            vm.onDispose()
        }
    }



    val isTableMode = LocalUIMode.current.isTableMode

    BangumiPopup(vm.commonVM)
    if ((vm.playerVM.playconVM.screenMode == BasePlayconViewModel.ScreenMode.FULLSCREEN) || isTableMode) {

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            MediaPlayer(
                modifier = Modifier.fillMaxHeight().weight(1f).background(Color.Black),
                playerVm = vm.playerVM
            ) {
                MediaPlayerFloat(vm)
                if(vm.playerVM.playconVM.isShowController) (
                    Box(modifier = Modifier
                        .align(androidx.compose.ui.Alignment.CenterEnd)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp, 0.dp, 0.dp, 8.dp))
                        .clickable {
                            vm.showMediaPage.value = !vm.showMediaPage.value
                        }) {
                        Icon(
                            if (vm.showMediaPage.value)Icons.Default.ArrowRight else Icons.Default.ArrowLeft, contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSecondary)
                    }
                )

            }

            if (vm.showMediaPage.value) {
                BangumiMediaPage(
                    pageParam,
                    Modifier.fillMaxHeight().width(384.dp)
                )
            }

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
                pageParam,
                Modifier.fillMaxWidth().weight(1f)
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