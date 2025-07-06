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
import androidx.compose.ui.graphics.Brush
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
    textColor: Color = Color.White,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
    onLongPress: ((CartoonCover) -> Unit)? = null,
) {
    CartoonCoverCard(
        modifier,
        mark = if (star) stringRes(Res.strings.stared_min) else null,
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
    coverAspectRatio: Float = EasyScheme.size.cartoonCoverAspectRatio,
    textColor: Color = Color.White,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: ((CartoonInfo) -> Unit)? = null,
) {
    CartoonCoverCard(
        modifier,
        mark = if (star) stringRes(Res.strings.stared_min) else null,
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
fun CartoonCoverCard(
    modifier: Modifier,
    mark: String? = null,
    model: String,
    name: String,
    cardBackgroundColor: Color? = null,
    itemSize: Dp = EasyScheme.size.cartoonCoverWidth,
    itemIsWidth : Boolean = true,
    coverAspectRatio: Float = EasyScheme.size.cartoonCoverAspectRatio,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .run {
                if (itemIsWidth) {
                    width(itemSize)
                        .height(itemSize / coverAspectRatio)
                } else {
                    height(itemSize)
                        .width(itemSize * coverAspectRatio)
                }
            }
//            .aspectRatio(coverAspectRatio)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    onClick()
                },
                onLongClick = {
                    onLongPress?.invoke()
                }
            ).run {
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


        Box(
            modifier.fillMaxSize().background(brush = Brush.linearGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))),
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

        if (mark != null) {
            Text(
                fontSize = 13.sp,
                text = mark,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(0.dp, 0.dp, 16.dp, 0.dp)
                    )
                    .padding(16.dp, 0.dp)
            )
        }

    }
}