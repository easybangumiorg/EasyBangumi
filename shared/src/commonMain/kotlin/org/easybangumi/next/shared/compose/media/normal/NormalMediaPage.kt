package org.easybangumi.next.shared.compose.media.normal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.media.mediaPlayLineIndex
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.ui.detail.preview.BangumiDetailPreview


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
fun NormalMediaPage(
    commonVM: NormalMediaCommonVM,
    modifier: Modifier,
) {
    val state = commonVM.state.collectAsState()
    val sta = state.value
    val playLineIndexState = commonVM.playIndexState.collectAsState()
    Box(modifier = Modifier.then(modifier)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp, 8.dp),
        ) {
            commonVM.param.cartoonCover?.let {
                normalPreviewCard(it)
                space(Modifier.height(8.dp))
            }
            // 操作按钮
            playerAction()
            space(Modifier.height(8.dp))
            divider()
            // 播放线路和集数选择
            mediaPlayLineIndex(commonVM.playLineIndexVM, commonVM.playLineIndexVM.ui.value, 2)
        }

    }
}

fun LazyListScope.normalPreviewCard(
    cartoonCover: CartoonCover,
) {

    item {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            CartoonCoverCard(
                model = cartoonCover.coverUrl,
                itemSize = EasyScheme.size.cartoonPreviewWidth,
                itemIsWidth = true,
                coverAspectRatio = EasyScheme.size.cartoonPreviewAspectRatio

            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(cartoonCover.name, maxLines = 2, style = MaterialTheme.typography.bodyLarge)
        }
    }

}

fun LazyListScope.playerAction() {
    item {
        NormalMediaActions(
            isStar = false,
            isDownloading = false,
            isDeleting = false,
            isFromRemote = false,
            onStar = {},
            onSearch = {},
            onExtPlayer = {},
            onDownload = {},
            onDelete = {},
            onBindBangumi = {}
        )
    }

}

fun LazyListScope.divider() {
    item{
        HorizontalDivider()
    }
}

fun LazyListScope.space(
    modifier: Modifier = Modifier,
){
    item {
        Spacer(modifier = modifier)
    }
}