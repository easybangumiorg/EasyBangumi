package org.easybangumi.next.shared.compose.media.bangumi

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.detail.bangumi.BangumiDetailViewModel
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.PlayLineIndexViewModel
import org.easybangumi.next.shared.compose.media_radar.MediaRadarParam
import org.easybangumi.next.shared.compose.media_radar.MediaRadarViewModel
import org.easybangumi.next.shared.playcon.desktop.DesktopPlayerViewModel
import org.easybangumi.next.shared.playcon.pointer.PointerPlayconViewModel

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
class DesktopBangumiMediaViewModel(
    val param: MediaParam,
): BaseViewModel() {


    val commonVM: BangumiMediaCommonVM by childViewModel {
        BangumiMediaCommonVM(param)
    }


    // == 播放状态 =============================
    val playerViewModel: DesktopPlayerViewModel by childViewModel {
        DesktopPlayerViewModel()
    }



    init {


        // 播放链接变化 -> 播放器
        viewModelScope.launch {
            commonVM.playIndexState.map { it.playInfo }.collectLatest {
                playerViewModel.onPlayInfoChange(it)
            }
        }



    }


}