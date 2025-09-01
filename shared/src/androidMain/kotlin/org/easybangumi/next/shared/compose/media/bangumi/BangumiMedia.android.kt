package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.compose.media.AndroidPlayerViewModel
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.MediaPlayerNormal
import org.easybangumi.next.shared.compose.media_radar.MediaRadarBottomPanel
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
internal val logger = logger("BangumiMediaAndroid")

sealed class BangumiMediaSubPage(
    val label: @Composable () -> Unit,
    val content: @Composable (AndroidBangumiMediaViewModel) -> Unit,
) {
    data object Detail: BangumiMediaSubPage (
        label = { Text(stringRes(Res.strings.detailed)) },
        content = {
            BangumiMediaDetailSubPage(it)
        }
    )

    data object Comment: BangumiMediaSubPage (
        label = { Text(stringRes(Res.strings.comment) )},
        content = {
            BangumiMediaCommentSubPage(it)
        }
    )
}

private val bangumiMediaSubPageList = listOf(
    BangumiMediaSubPage.Detail,
    BangumiMediaSubPage.Comment
)


@Composable
actual fun BangumiMedia(mediaParam: MediaParam) {
    val vm = vm(::AndroidBangumiMediaViewModel, mediaParam)

    val scope = rememberCoroutineScope()
    val state = vm.state.collectAsState()
    val sta = state.value

    val pagerState = rememberPagerState {
        bangumiMediaSubPageList.size
    }

    BangumiPopup(vm)

    if (sta.fullscreen) {

    } else {
        Column {
            Box(modifier = Modifier.background(Color.Black).height(with(LocalDensity.current) {
                WindowInsets.statusBars.getTop(LocalDensity.current).toDp()
            }))
            MediaPlayerNormal(
                modifier = Modifier.fillMaxWidth().aspectRatio(AndroidPlayerViewModel.MEDIA_COMPONENT_ASPECT).background(
                    androidx.compose.ui.graphics.Color.Black),
                vm = vm.playerViewModel
            )
            EasyTab(
                modifier = Modifier.fillMaxWidth(),
                scrollable = true,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                size = bangumiMediaSubPageList.size,
                selection = pagerState.currentPage,
                onSelected = {
                    scope.launch {
                        pagerState.animateScrollToPage(it)
                    }
                },
                tabs = { index, selected ->
                    val tab = bangumiMediaSubPageList[index]
                    tab.label.invoke()
                }
            )
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            HorizontalPager(
                pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                val tab = bangumiMediaSubPageList[it]
                Box(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    tab.content(vm)
                }
            }
        }
    }


}

@Composable
fun BangumiPopup(
    vm: AndroidBangumiMediaViewModel,
){
    val state = vm.popupState.collectAsState()
    val popup = state.value
    when (val po = popup) {
        is AndroidBangumiMediaViewModel.Popup.BangumiDetailPanel -> {

        }
        is AndroidBangumiMediaViewModel.Popup.MediaRadarPanel -> {
            logger.info("show media radar panel")
            MediaRadarBottomPanel(
                vm = vm.mediaRadarViewModel,
                onDismissRequest = {
                    vm.dismissPopup()
                },
                onSelection = {
                    vm.onMediaRadarSelect(it)
                    vm.dismissPopup()
                }
            )

        }
        else -> {}
    }
}