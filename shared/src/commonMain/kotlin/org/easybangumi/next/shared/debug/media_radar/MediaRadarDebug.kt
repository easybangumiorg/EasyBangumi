package org.easybangumi.next.shared.debug.media_radar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.easybangumi.next.shared.debug.DebugScope
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.vm
import org.easybangumi.next.shared.compose.media_radar.MediaRadarBottomPanel
import org.easybangumi.next.shared.compose.media_radar.MediaRadarParam
import org.easybangumi.next.shared.compose.media_radar.MediaRadarViewModel
import org.easybangumi.next.shared.data.cartoon.CartoonCover

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
var mediaRadarDebugCover: CartoonCover? = null

@Composable
fun DebugScope.MediaRadarDebug() {
    val cover = mediaRadarDebugCover
    if (cover != null) {
        val param = remember(cover) {
            MediaRadarParam(
                defaultKeyword = cover.name,
                defaultSubKeyword = emptyList(),
                limitWhichKeyword = 0, // 0 means no limit
            )
        }
        val vm = vm(::MediaRadarViewModel, param)
        MediaRadarBottomPanel(
            vm = vm,
            show = true,
            onDismissRequest = {
                onBack()
            },
        )
    }
}