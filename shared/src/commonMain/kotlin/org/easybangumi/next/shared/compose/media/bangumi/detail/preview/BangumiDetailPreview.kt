package org.easybangumi.next.shared.compose.media.bangumi.detail.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.cartoon.collection.CollectionUIUtils
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMediaCommonVM
import org.easybangumi.next.shared.data.bangumi.BgmCollectResp
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.shimmer.ShimmerHost
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.scheme.EasyScheme

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
fun BangumiDetailPreview(
    modifier: Modifier = Modifier,
    commonVM: BangumiMediaCommonVM,
) {
    val detailVM = commonVM.bangumiDetailVM
    val bgmSubject = detailVM.ui.value.subjectState

    val collectVM = detailVM.bgmCollectInfoVM
    val collectState = collectVM.ui.value

    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).then(modifier),
        verticalAlignment = Alignment.Top
    ) {
        CartoonCoverCard(
            model = detailVM.coverUrl,
            itemSize = EasyScheme.size.cartoonPreviewWidth,
            itemIsWidth = true,
            coverAspectRatio = EasyScheme.size.cartoonPreviewAspectRatio
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (bgmSubject.isLoading()) {
                ShimmerHost {
                    Text(
                        "",
                        modifier = Modifier.fillMaxWidth().drawRectWhenShimmerVisible(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Text(
                    bgmSubject.okOrNull()?.displayName ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviewCollectBtn(
                    modifier = Modifier,
                    bgmCollectionState = collectState.collectionState,
                    cartoonInfo = collectState.cartoonInfo,
                    onCollectClick = {
                        commonVM.onCollectDialogShow()
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        commonVM.showBangumiDetailPanel()
                    },
                ) {
                    Icon(
                        Icons.Filled.MoreHoriz,
                        contentDescription = ""
                    )
                }
            }

        }
    }
}

@Composable
fun PreviewCollectBtn(
    modifier: Modifier,
    bgmCollectionState: DataState<BgmCollectResp> = DataState.none(),
    cartoonInfo: CartoonInfo? = null,
    onCollectClick: () -> Unit,
) {
    val collectResp = bgmCollectionState.okOrCache()

    OutlinedButton(
        modifier = modifier,
//        shape = RoundedCornerShape(16.dp),
        contentPadding =  PaddingValues(12.dp, 8.dp),
        onClick = {
            onCollectClick()
        }
    ) {
        val collect = collectResp?.dataOrNull()
        val isBgmLogin = collectResp != null
        val bgmCollectType = collect?.bangumiType
        val isLocalCollected = cartoonInfo != null && cartoonInfo.starTime > 0L
        val label = remember(
            isBgmLogin,
            bgmCollectType,
            isLocalCollected
        ) {
            CollectionUIUtils.getLabelOutlineBtn(
                isBgmLogin,
                bgmCollectType,
                isLocalCollected
            )
        }
        val icon = remember(
            isBgmLogin,
            bgmCollectType,
            isLocalCollected
        ) {
            CollectionUIUtils.getIcon(
                isBgmLogin,
                bgmCollectType,
                isLocalCollected
            )
        }

        Icon(
            icon.first, modifier = Modifier.size(16.dp), tint = if (icon.second) MaterialTheme.colorScheme.primary else Color.Unspecified, contentDescription = null
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            stringRes(
                label
            )
        )
    }

}
