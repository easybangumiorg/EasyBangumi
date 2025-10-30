package org.easybangumi.next.shared.foundation.cartoon

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
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
    coverAspectRatio: Float? = EasyScheme.size.cartoonCoverAspectRatio,
    textColor: Color = Color.White,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
    onLongPress: ((CartoonCover) -> Unit)? = null,
) {
    CartoonCoverCard(
        modifier,
        markLeftTop = if (star) stringRes(Res.strings.stared_min) else null,
        model = cartoonCover.coverUrl ?: "",
        name = cartoonCover.name,
        cardBackgroundColor = null,
        itemSize = itemSize,
        itemIsWidth = itemIsWidth,
        coverAspectRatio = coverAspectRatio,
        textColor = textColor,
        onClick = {
            onClick(cartoonCover)
        },
        onLongPress = {
            onLongPress?.invoke(cartoonCover)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartoonCardWithCover(
    modifier: Modifier = Modifier,
    star: Boolean = false,
    cartoonInfo: CartoonInfo,
    itemSize: Dp = EasyScheme.size.cartoonCoverWidth,
    itemIsWidth : Boolean = true,
    coverAspectRatio: Float? = EasyScheme.size.cartoonCoverAspectRatio,
    textColor: Color = Color.White,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: ((CartoonInfo) -> Unit)? = null,
) {
    CartoonCoverCard(
        modifier,
        markLeftTop = if (star) stringRes(Res.strings.stared_min) else null,
        model = cartoonInfo.coverUrl ?: "",
        name = cartoonInfo.name,
        cardBackgroundColor = null,
        itemSize = itemSize,
        itemIsWidth = itemIsWidth,
        coverAspectRatio = coverAspectRatio,
        textColor = textColor,
        onClick = {
            onClick(cartoonInfo)
        },
        onLongPress = {
            onLongPress?.invoke(cartoonInfo)
        }
    )
}

@Composable
fun CartoonCoverCardRect(
    modifier: Modifier = Modifier,
    itemSize: Dp = EasyScheme.size.cartoonCoverWidth,
    itemIsWidth : Boolean = true,
    cardBackgroundColor: Color? = null,
    coverAspectRatio: Float? = EasyScheme.size.cartoonCoverAspectRatio,
) {

    val size = remember(
        itemIsWidth, itemSize
    ) {
        if (itemIsWidth) {
            DpSize(itemSize, if (coverAspectRatio == null) Dp.Unspecified else itemSize / coverAspectRatio)
        } else {
            DpSize(if (coverAspectRatio == null) Dp.Unspecified else itemSize * coverAspectRatio, itemSize)
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .run {
                if (cardBackgroundColor != null) {
                    background(cardBackgroundColor)
                } else {
                    this
                }
            }.then(modifier),
    )
}

@Composable
fun CartoonCoverCard(
    modifier: Modifier = Modifier,
    markLeftTop: String? = null,
    markRightTop: String? = null,
    model: String,
    name: String? = null,
    cardBackgroundColor: Color? = null,
    itemSize: Dp = EasyScheme.size.cartoonCoverWidth,
    itemIsWidth : Boolean = true,
    coverAspectRatio: Float? = EasyScheme.size.cartoonCoverAspectRatio,
    textColor: Color = Color.White,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
) {

    val size = remember(
        itemIsWidth, itemSize
    ) {
        if (itemIsWidth) {
            DpSize(itemSize, if (coverAspectRatio == null) Dp.Unspecified else itemSize / coverAspectRatio)
        } else {
            DpSize(if (coverAspectRatio == null) Dp.Unspecified else itemSize * coverAspectRatio, itemSize)
        }
    }

    Box(
        modifier = modifier
            .size(size)
//            .aspectRatio(coverAspectRatio)
            .clip(RoundedCornerShape(16.dp))
            .run {
                if (onLongPress != null) {
                    this.combinedClickable(
                        onClick = {
                            onClick?.invoke()
                        },
                        onLongClick = {
                            onLongPress?.invoke()
                        }
                    )
                } else if (onClick != null) {
                    this.clickable {
                        onClick()
                    }
                } else {
                    this
                }
            }.run {
                if (cardBackgroundColor != null) {
                    background(cardBackgroundColor)
                } else {
                    this
                }
            },
//        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = model,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            error = painterResource(Res.images.empty_soyolin),
        )


        if (name != null) {
            Box(
                Modifier.fillMaxWidth().height(size.height/2f).align(Alignment.BottomCenter).background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        end = Offset.Infinite.copy(0f)
                    )
                ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    text = name,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                )

            }
        }

        if (markLeftTop != null) {
            Text(
                fontSize = 13.sp,
                text = markLeftTop,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(0.dp, 0.dp, 16.dp, 0.dp)
                    )
                    .padding(16.dp, 0.dp)
            )
        }

        if (markRightTop != null) {
            Text(
                fontSize = 13.sp,
                text = markRightTop,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(0.dp, 0.dp, 0.dp, 16.dp)
                    )
                    .padding(16.dp, 0.dp)
            )
        }

    }
}