package org.easybangumi.next.shared.cartoon.collection

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.data.bangumi.BangumiConst
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
object CollectionUIUtils {

    @Composable
    fun getLabelAction (
        isBgmLogin: Boolean,
        bgmCollectType: BangumiConst.BangumiCollectType?,
        isLocalCollected: Boolean,
    ): String {
        val isBgmCollected = bgmCollectType != null && bgmCollectType.type != BangumiConst.collectTypeDropped.type
        // 1. bangumi 优先级最高
        if ( isBgmCollected) {
            // 已 xx
            return stringRes(Res.strings.has) + stringRes(bgmCollectType.label)

        }
        // 2. 只有本地收藏的话如果有 bangumi 登录则强调本地收藏
        else if (isLocalCollected) {
            return if (isBgmLogin) {
                // 本地收藏
                stringRes(Res.strings.local_collect)
            } else {
                // 已收藏
                stringRes(Res.strings.collected)
            }

        } else {
            // 3. 本地没收藏，bangumi 抛弃则强调抛弃
            return if (bgmCollectType?.type == BangumiConst.collectTypeDropped.type) {
                // 已抛弃
                stringRes(Res.strings.has) + stringRes(BangumiConst.collectTypeDropped.label)
            } else {
                // 4. 否则未收藏
                stringRes(Res.strings.no_collect)
            }
        }
    }

    fun getLabelOutlineBtn (
        isBgmLogin: Boolean,
        bgmCollectType: BangumiConst.BangumiCollectType?,
        isLocalCollected: Boolean,
    ): ResourceOr {
        val isBgmCollected = bgmCollectType != null && bgmCollectType.type != BangumiConst.collectTypeDropped.type
        // 1. bangumi 优先级最高
        if ( isBgmCollected) {
            // 已 xx
            return bgmCollectType.label

        }
        // 2. 只有本地收藏的话如果有 bangumi 登录则强调本地收藏
        else if (isLocalCollected) {
            return if (isBgmLogin) {
                // 本地收藏
                Res.strings.local_collected
            } else {
                // 已收藏
                Res.strings.collected
            }

        } else {
            // 3. 本地没收藏，bangumi 抛弃则强调抛弃
            return if (bgmCollectType?.type == BangumiConst.collectTypeDropped.type) {
                // 已抛弃
                BangumiConst.collectTypeDropped.label
            } else {
                // 4. 否则未收藏
                Res.strings.no_collect
            }
        }
    }

    fun getIcon (
        isBgmLogin: Boolean,
        bgmCollectType: BangumiConst.BangumiCollectType?,
        isLocalCollected: Boolean,
    ): Pair<ImageVector, Boolean> {
        if (isLocalCollected) {
            return Icons.Filled.Favorite to true
        } else if (bgmCollectType != null && bgmCollectType.type != BangumiConst.collectTypeDropped.type) {
            return Icons.Filled.Favorite to true
        }
        return Icons.Filled.FavoriteBorder to false
    }

}