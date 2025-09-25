package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.playcon.android.AndroidPlayerViewModel
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.MediaPlayer
import org.easybangumi.next.shared.compose.media.bangumi.page.BangumiMediaCommentSubPage
import org.easybangumi.next.shared.compose.media.bangumi.page.BangumiMediaDetailSubPage
import org.easybangumi.next.shared.compose.media.bangumi.page.BangumiMediaPage
import org.easybangumi.next.shared.playcon.android.MediaPlayerSync
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



@Composable
actual fun BangumiMedia(mediaParam: MediaParam) {
    val vm = vm(::AndroidBangumiMediaViewModel, mediaParam)

    val scope = rememberCoroutineScope()
    val state = vm.commonVM.state.collectAsState()
    val sta = state.value



    BangumiPopup(vm.commonVM)

    Column {
        if (!sta.isFullscreen) {
            Box(modifier = Modifier.background(Color.Black).fillMaxWidth().height(with(LocalDensity.current) {
                WindowInsets.statusBars.getTop(LocalDensity.current).toDp()
            }).background(Color.Black))
        }
        val playerModifier = remember(sta.isFullscreen) {
            if (sta.isFullscreen) {
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(AndroidPlayerViewModel.MEDIA_COMPONENT_ASPECT)
                    .background(Color.Black)
            }
        }
        MediaPlayerSync(vm.playerViewModel)
        MediaPlayer(
            modifier = playerModifier,
            playerVm = vm.playerViewModel
        )
        if (!sta.isFullscreen) {
            BangumiMediaPage(
                vm.commonVM,
                Modifier.fillMaxWidth().weight(1f)
            )
        }
    }


}

