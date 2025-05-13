package org.easybangumi.next.shared.foundation.cartoon

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.icerock.moko.resources.compose.painterResource
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.image.AsyncImage
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartoonCardWithCover(
    modifier: Modifier = Modifier,
    star: Boolean = false,
    itemSize: Dp,
    itemIsWidth : Boolean = true,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
    onLongPress: ((CartoonCover) -> Unit)? = null,
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    onClick(cartoonCover)
                },
                onLongClick = {
                    onLongPress?.invoke(cartoonCover)
                }
            )
            .padding(4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .run {
                    if (itemIsWidth) {
                        width(itemSize)
                    } else {
                        height(itemSize)
                    }
                }
                .aspectRatio(7 / 9F)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = cartoonCover.coverUrl ?: "",
                contentDescription = cartoonCover.name,
                contentScale = ContentScale.FillBounds,
                // TODO placeholder
                error = painterResource(Res.images.empty_soyolin),
            )
            if (star) {
                Text(
                    fontSize = 13.sp,
                    text = stringRes(Res.strings.stared_min),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(0.dp, 0.dp, 16.dp, 0.dp)
                        )
                        .padding(4.dp, 0.dp)
                )
            }
        }

        Spacer(modifier = Modifier.size(4.dp))
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = cartoonCover.name,
            maxLines = 4,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.size(4.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartoonCardWithCover(
    modifier: Modifier = Modifier,
    star: Boolean = false,
    cartoonInfo: CartoonInfo,
    itemSize: Dp,
    itemIsWidth : Boolean = true,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: ((CartoonInfo) -> Unit)? = null,
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    onClick(cartoonInfo)
                },
                onLongClick = {
                    onLongPress?.invoke(cartoonInfo)
                }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .run {
                    if (itemIsWidth) {
                        width(itemSize)
                    } else {
                        height(itemSize)
                    }
                }
                .aspectRatio(7 / 9F)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = cartoonInfo.coverUrl ?: "",
                contentDescription = cartoonInfo.name,
                // TODO placeholder
                error = painterResource(Res.images.empty_soyolin),
            )
            if (star) {
                Text(
                    fontSize = 13.sp,
                    text = stringRes(Res.strings.stared_min),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(0.dp, 0.dp, 16.dp, 0.dp)
                        )
                        .padding(4.dp, 0.dp)
                )
            }
        }

        Spacer(modifier = Modifier.size(4.dp))
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = cartoonInfo.name,
            maxLines = 4,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.size(4.dp))
    }
}