package org.easybangumi.next.shared.foundation.cartoon

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartoonCardWithCover(
    modifier: Modifier = Modifier,
    star: Boolean = false,
    itemSize: Dp = EasyScheme.size.cartoonCoverWidth,
    itemIsWidth : Boolean = true,
    coverAspectRatio: Float = EasyScheme.size.cartoonCoverAspectRatio,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
    onLongPress: ((CartoonCover) -> Unit)? = null,
) {

    Column(
        modifier = modifier
            .width(EasyScheme.size.cartoonCoverWidth)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    onClick(cartoonCover)
                },
                onLongClick = {
                    onLongPress?.invoke(cartoonCover)
                }
            )
            .width(EasyScheme.size.cartoonCoverWidth)
            .padding(4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
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
                .aspectRatio(coverAspectRatio)
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

        Spacer(modifier = Modifier.fillMaxWidth().height(4.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            text = cartoonCover.name,
            maxLines = 4,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartoonCardWithCover(
    modifier: Modifier = Modifier,
    star: Boolean = false,
    cartoonInfo: CartoonInfo,
    itemSize: Dp = EasyScheme.size.cartoonCoverWidth,
    itemIsWidth : Boolean = true,
    coverAspectRatio: Float = EasyScheme.size.cartoonCoverAspectRatio,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: ((CartoonInfo) -> Unit)? = null,
) {

    Column(
        modifier = modifier
            .run {
                if (itemIsWidth) {
                    width(itemSize + 8.dp)
                } else {
                    height(itemSize * EasyScheme.size.cartoonCoverAspectRatio + 8.dp)
                }
            }
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
                .aspectRatio(coverAspectRatio)
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

        Spacer(modifier = Modifier.fillMaxWidth().height(4.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            text = cartoonInfo.name,
            maxLines = 4,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
        )
    }
}