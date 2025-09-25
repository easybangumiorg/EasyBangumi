package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.bangumi.page.BangumiMediaPage
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.view_model.vm

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

                BangumiMediaPage(
                    vm.commonVM,
                    Modifier.fillMaxHeight().width(128.dp)
                )
            }
        } else {

        }
    }


}