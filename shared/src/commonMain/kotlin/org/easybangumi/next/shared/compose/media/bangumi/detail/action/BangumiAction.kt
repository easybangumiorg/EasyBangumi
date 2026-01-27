package org.easybangumi.next.shared.compose.media.bangumi.detail.action

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.easybangumi.next.shared.cartoon.collection.CollectionUIUtils
import org.easybangumi.next.shared.compose.media.bangumi.Action
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMediaPageParam
import org.easybangumi.next.shared.data.bangumi.BangumiConst
import org.easybangumi.next.shared.foundation.stringRes

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
expect fun BangumiAction(
    param: BangumiMediaPageParam
)

@Composable
internal fun RowScope.CollectionAction(
    param: BangumiMediaPageParam
) {

    val vm = param.commonVM
    val state = vm.state.value
    val cartoonInfo = state.cartoonInfo

    val bgmCollectResp = state.collectionState.okOrNull()
    val bgmCollect = bgmCollectResp?.dataOrNull()

    val isBgmLogin = state.hasBgmAccountInfo
    val isLocalCollected = ((cartoonInfo?.starTime?:0L) > 0L)
    val bgmCollectTypeL = bgmCollect?.type
    val bgmCollectType = remember(bgmCollectTypeL) {
        BangumiConst.getTypeDataById(bgmCollectTypeL?.toInt()?: -1)
    }
    val label = CollectionUIUtils.getLabelAction(
        isBgmLogin,
        bgmCollectType,
        isLocalCollected
    )
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

    Action(
        icon = {
            Icon(
                icon.first, contentDescription = null,
                tint = if (icon.second) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            )
        },
        msg = {
            Text(label)
        },
        onClick = {
            vm.onCollectDialogShow()
        }
    )
}