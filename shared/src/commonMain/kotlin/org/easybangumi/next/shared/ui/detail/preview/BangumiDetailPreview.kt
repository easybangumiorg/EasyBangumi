package org.easybangumi.next.shared.ui.detail.preview

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.shimmer.ShimmerHost
import org.easybangumi.next.shared.foundation.shimmer.drawRectWhenShimmerVisible
import org.easybangumi.next.shared.foundation.view_model.vm
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
    cartoonIndex: CartoonIndex,
    modifier: Modifier,
) {
    val vm = vm(::BangumiDetailPreviewViewModel, cartoonIndex, key = cartoonIndex.toString())
    val state = vm.ui.value
    Row (
        modifier = Modifier.fillMaxWidth().then(modifier),
        verticalAlignment = Alignment.Top
    ) {
        CartoonCoverCard(
            model = vm.coverUrl,
            itemSize = EasyScheme.size.cartoonPreviewWidth,
            itemIsWidth = true,
            coverAspectRatio = EasyScheme.size.cartoonPreviewAspectRatio

        )

        Spacer(modifier = Modifier.size(8.dp))
        LoadScaffold(
            Modifier.fillMaxWidth().weight(1f),
            state,
            isRow = false,
            onLoading = {
                ShimmerHost(Modifier, visible = true) {
                    Column {
                        Text("", modifier = Modifier.fillMaxWidth().drawRectWhenShimmerVisible(), maxLines = 2, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.size(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(3) {
                                Row(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                        .clip(RoundedCornerShape(6.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .drawRectWhenShimmerVisible(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TEST",
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) {
            Column {
                Text(it.data.displayName, maxLines = 2, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.size(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    maxLines = 2
                ) {
                    for (i in 0..3.coerceAtMost(it.data.tags.size)) {
                        val tag = it.data.tags.getOrNull(i) ?: continue
                        Row(modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
//                                    vm.onTagClick(tag.name)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${tag.name ?: ""} ${tag.count ?: ""}",
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                }
            }
//            Row (
//                modifier = Modifier.fillMaxWidth().height(EasyScheme.size.cartoonCoverHeight)
//            ) {
//                CartoonCoverCard(
//                    model = vm.coverUrl
//                )
//            }
        }

    }


}