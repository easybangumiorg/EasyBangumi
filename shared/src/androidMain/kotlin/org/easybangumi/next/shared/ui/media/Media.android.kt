package org.easybangumi.next.shared.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.libplayer.exoplayer.ExoPlayerCompose
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.playcon.android.AndroidPlaycon
import org.easybangumi.next.shared.playcon.android.AndroidPlayconViewModel
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.ui.media_radar.MediaRadarBottomPanel
import org.easybangumi.next.shared.ui.media_radar.MediaRadarParam

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

internal val logger = logger("Media")
@Composable
actual fun Media(
    cartoonCover: CartoonCover,
    suggestEpisode: Int?,
    mediaRadarParam: MediaRadarParam?
) {

    val vm = vm(::MediaViewModel, cartoonCover, suggestEpisode, mediaRadarParam)

    val radarResult = vm.mediaCommonVM.ui.value.detail.radarResult
    val screenMode = vm.playconVM.screenMode
    when (screenMode) {
        AndroidPlayconViewModel.ScreenMode.NORMAL -> {
            MediaNormal(vm)
        }
        AndroidPlayconViewModel.ScreenMode.FULLSCREEN -> {

        }
    }

    MediaRadarPopup(vm.mediaCommonVM)
    MediaPlayListPopup(vm.mediaCommonVM)
}

sealed class MediaNormalSubPage(
    val label: ResourceOr,
    val content: @Composable (viewModel: MediaViewModel) -> Unit
) {

    class Detail: MediaNormalSubPage(
        label = Res.strings.detailed,
        content = {
            MediaDetailPage(
                modifier = Modifier.fillMaxWidth(),
                vm = it
            )
        }
    )

    class Comment: MediaNormalSubPage(
        label = Res.strings.comment,
        content = {

        }
    )

}

val MediaNormalSubPageList = listOf(
    MediaNormalSubPage.Detail(),
    MediaNormalSubPage.Comment()
)

@Composable
fun MediaNormal(
    viewModel: MediaViewModel
){
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { MediaNormalSubPageList.size }
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Transparent)
    ) {
        Box(Modifier.windowInsetsTopHeight(WindowInsets.Companion.systemBars.only(WindowInsetsSides.Top)).fillMaxWidth().background(Color.Black))
        PlayerNormal(viewModel)
        EasyTab(
            modifier = Modifier.fillMaxWidth(),
            scrollable = true,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            size = MediaNormalSubPageList.size,
            selection = pagerState.currentPage,
            onSelected = {
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }
            },
            tabs = { index, selected ->
                val tab = MediaNormalSubPageList[index]
                Text(
                    text = stringRes(tab.label),
                )
            }
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        HorizontalPager(pagerState) {
            val tab = MediaNormalSubPageList[it]
            Box(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
            ) {
                tab.content(viewModel)
            }
        }

    }
}



@Composable
fun ColumnScope.PlayerNormal(
    viewModel: MediaViewModel
) {
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(MediaViewModel.MEDIA_COMPONENT_ASPECT).background(Color.Black)
    ) {
        ExoPlayerCompose(
            modifier = Modifier.fillMaxWidth().aspectRatio(MediaViewModel.MEDIA_COMPONENT_ASPECT),
            state = viewModel.exoPlayerFrameState
        )
        AndroidPlaycon(
            modifier = Modifier.fillMaxWidth().aspectRatio(MediaViewModel.MEDIA_COMPONENT_ASPECT),
            viewModel = viewModel.playconVM
        )
    }
}




