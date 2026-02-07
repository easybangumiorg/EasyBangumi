package org.easybangumi.next.shared.compose.bangumi.account_card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.LocalNavController
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.compose.bangumi.bangumiContainer
import org.easybangumi.next.shared.compose.bangumi.onBangumiContainer
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.view_model.vm
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
@Composable
fun BangumiAccountCard(
    modifier: Modifier,
) {
    val vm = vm(::BangumiAccountCardVM)
    val sta = vm.ui.value
    val icon = remember(sta) {
        sta.cacheData?.avatar?.getCommonUrlFirst() ?: Res.images.bangumi
    }
    val label = remember(sta) {
        sta.cacheData?.nickname ?: "未绑定 Bangumi"
    }
    val desc = remember(sta) {
        sta.cacheData?.id?.let {
            "#$it"
        } ?: "点击绑定"
    }
    val trailing = remember(sta) {
        (sta.mapError { it.errorMsg }?.let { it to true }) ?: if (sta.cacheData?.id == null) null else "点击重新绑定" to false
    }
    val nav = LocalNavController.current
    ListItem(
        modifier = modifier.padding(8.dp, 0.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                nav.navigate(RouterPage.BangumiLogin)
            }
            .background(MaterialTheme.colorScheme.bangumiContainer, RoundedCornerShape(8.dp)),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = MaterialTheme.colorScheme.onBangumiContainer,
            supportingColor = MaterialTheme.colorScheme.onBangumiContainer,
        ),
        headlineContent = {
            Text(
                text = label,
                maxLines = 1,
            )
        },
        supportingContent = {
            Text(
                text = desc,
                maxLines = 1,
            )
        },
        leadingContent = {
            AsyncImage(
                model = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = trailing?.let {
            {
                Text(
                    text = it.first,
                    color = if (it.second) MaterialTheme.colorScheme.error else Color.Unspecified,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    )



    when (sta) {
        is DataState.None -> {

        }
        is DataState.Loading -> {

        }
        is DataState.Error -> {

        }
        is DataState.Ok -> {

        }
    }
}