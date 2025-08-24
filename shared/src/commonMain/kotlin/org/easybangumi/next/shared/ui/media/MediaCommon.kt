package org.easybangumi.next.shared.ui.media

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.DialogHost
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.ui.detail.DetailPreview
import org.easybangumi.next.shared.ui.media_radar.MediaRadar
import org.easybangumi.next.shared.ui.media_radar.MediaRadarBottomPanel
import org.easybangumi.next.shared.ui.media_radar.MediaRadarState
import org.easybangumi.next.shared.ui.media_radar.MediaRadarViewModel

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
@Composable
fun Media (
    cartoonCover: CartoonCover,
    suggestEpisode: Episode? = null,
) {

}

@Composable
fun MediaDetailPreview(
    modifier: Modifier,
    vm: MediaCommonViewModel,
) {
    Box(modifier) {
        val showPlayDetail = vm.ui.value.detail.showDetailFromPlay
        if (showPlayDetail) {
            MediaPlayPreview(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                vm = vm,
                introMaxLine = 3,
            )
        } else {
            DetailPreview(
                modifier = Modifier.fillMaxWidth(),
                cartoonIndex = remember(vm.cartoonCover) {
                    vm.cartoonCover.toCartoonIndex()
                },
            )
        }
    }

}

@Composable
fun MediaPlayPreview(
    modifier: Modifier,
    vm: MediaCommonViewModel,
    introMaxLine: Int,
){
    val playCover = vm.ui.value.detail.radarResult?.playCover
    if (playCover != null) {
        Row (
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).then(modifier),
            verticalAlignment = Alignment.Top
        ) {
            CartoonCoverCard(
                model = playCover.coverUrl,
                coverAspectRatio = EasyScheme.size.cartoonPreviewAspectRatio,
                itemSize = EasyScheme.size.cartoonPreviewWidth,
                itemIsWidth = true,

            )
            Spacer(modifier = Modifier.size(4.dp))
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(playCover.name, maxLines = 2, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                Text(playCover.intro, maxLines = introMaxLine, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

}
