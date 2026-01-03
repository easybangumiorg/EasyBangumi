package org.easybangumi.next.shared.compose.detail.bangumi

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.bangumi.BgmCollectResp
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.shimmer.ShimmerHost
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.data.bangumi.BgmRating
import org.easybangumi.next.shared.data.bangumi.BgmSubject
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
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

@Composable
fun BangumiDetailHeader(
    modifier: Modifier,
    coverUrl: String,
    contentPaddingTop: Dp,
    isHeaderPin: Boolean = false,
    subjectState: DataState<BgmSubject>,
    bgmCollectionState: DataState<BgmCollectResp> = DataState.none(),
    cartoonInfo: CartoonInfo? = null,
    onCollectClick: () -> Unit,
){
    val surfaceLowestColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    val backgroundPainter = remember(backgroundColor) {
        BrushPainter(SolidColor(backgroundColor))
    }
    val data = subjectState.cacheData
    Box(modifier.height(IntrinsicSize.Min)) {
        Crossfade(data) {
            if (it != null && !isHeaderPin) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize().blur(32.dp),
                    model = coverUrl,
                    contentScale = ContentScale.FillWidth,
                    contentDescription = "cartoon cover background",
                    fallback = backgroundPainter,
                    placeholder = backgroundPainter,
                )
            }

            Box(
                modifier = Modifier.fillMaxSize()
                    .run {
                        if (isHeaderPin) {
                            background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        } else {
                            background(
                                remember(
                                    surfaceLowestColor, backgroundColor,
                                ) {
                                    if (surfaceLowestColor.luminance() < 0.5f) {
                                        Brush.verticalGradient(
                                            0f to backgroundColor.copy(alpha = 0xA2.toFloat() / 0xFF),
                                            0.4f to backgroundColor.copy(alpha = 0xA2.toFloat() / 0xFF),
                                            1.00f to surfaceLowestColor,
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            0f to Color(0xA2FAFAFA),
                                            0.4f to Color(0xA2FAFAFA),
                                            1.00f to surfaceLowestColor,
                                        )
                                    }
                                }
                            )
                        }
                    },
            )
        }



        Column(
            modifier = Modifier.padding(16.dp, 0.dp)
        ) {
            Spacer(modifier = Modifier.size(contentPaddingTop))

            Row(
                modifier = Modifier.fillMaxWidth().height(EasyScheme.size.cartoonCoverHeight)
            ) {

                CartoonCoverCard(
                    model = coverUrl,
                )

                Spacer(modifier = Modifier.size(16.dp))

                AnimatedContent(
                    subjectState,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 90)))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    },
                ) {
                    val subjectState = it
                    LoadScaffold(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        data = subjectState,
                        onCacheOrData = { state, cache ->
                            HeaderContent(cache,
                                bgmCollectionState = bgmCollectionState,
                                cartoonInfo = cartoonInfo,
                                onCollectClick = onCollectClick,
                            )
                        },
                        onLoading = {
                            ShimmerHost(
                                visible = true
                            ) {
                                Column {
                                    Text(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).drawRectWhenShimmerVisible(),
                                        text = "",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).drawRectWhenShimmerVisible(),
                                        text = "",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).drawRectWhenShimmerVisible(),
                                        text = "",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }

                        },
                    ) {
                        HeaderContent(it.data,bgmCollectionState = bgmCollectionState,
                            cartoonInfo = cartoonInfo,
                            onCollectClick = onCollectClick,)
                    }
                }


            }

//            Row {
//                HeaderCollectBtn(
//                    modifier = Modifier.weight(1f),
//                    bgmCollectionState = bgmCollectionState,
//                    cartoonInfo = cartoonInfo,
//                    onCollectClick = onCollectClick,
//                )
//                // TODO 播放按钮
//                Button(
//                    modifier = Modifier.weight(1f),
//                    onClick = {  }
//                ) {}
//            }
        }
    }

}

@Composable
fun HeaderContent(
    it: BgmSubject,
    bgmCollectionState: DataState<BgmCollectResp> = DataState.none(),
    cartoonInfo: CartoonInfo? = null,
    onCollectClick: () -> Unit,
) {
    Column {
        Text(
            text = it.displayName ?: "",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            modifier = Modifier,
            text = it.date ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            modifier = Modifier,
            text = it.displayEpisode ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.size(8.dp))
//        Spacer(modifier = Modifier.weight(1f))
        it.rating?.let {
            HeaderRanking(Modifier, it)
        }
        Spacer(modifier = Modifier.weight(1f))
        HeaderCollectBtn(
            modifier = Modifier,
            bgmCollectionState = bgmCollectionState,
            cartoonInfo = cartoonInfo,
            onCollectClick = onCollectClick,
        )


    }
}

@Composable
fun HeaderCollectBtn(
    modifier: Modifier,
    bgmCollectionState: DataState<BgmCollectResp> = DataState.none(),
    cartoonInfo: CartoonInfo? = null,
    onCollectClick: () -> Unit,
) {
    val collectResp = bgmCollectionState.okOrCache()
    Button(
        modifier = modifier,
        onClick = {
        onCollectClick()
        }
    ) {

        if (collectResp != null) {
            val collectResp = collectResp.dataOrNull()
            if (collectResp == null) {
                // bgm 未收藏 - 本地未收藏 -> 点击收藏
                if (cartoonInfo == null || cartoonInfo.starTime == 0L) {
                    Icon(Icons.Filled.FavoriteBorder, modifier = Modifier.size(16.dp),contentDescription = null)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(stringRes(Res.strings.no_collect))
                }
                // bgm 未收藏 - 本地已收藏 -> 已本地收藏
                else {
                    Icon(Icons.Filled.Favorite, modifier = Modifier.size(16.dp), contentDescription = null)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(stringRes(Res.strings.local_collected))
                }
            } else {
                // bgm 已 - 本地未收藏 -> [再看|想看 ……]
                if (cartoonInfo == null || cartoonInfo.starTime == 0L) {
                    Text(stringRes(collectResp.bangumiType?.label?:""))
                } else {
                    Text(stringRes(collectResp.bangumiType?.label?:""))
                    Spacer(modifier = Modifier.size(4.dp))
                    Box(modifier.width(1.dp).height(16.dp).background(LocalContentColor.current))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(stringRes(Res.strings.collected))
                }
            }
        } else {
            // 未登录 - 本地未收藏 -> 点击收藏
            if (cartoonInfo == null || cartoonInfo.starTime == 0L) {
                Icon(Icons.Filled.FavoriteBorder, modifier = Modifier.size(16.dp), contentDescription = null)
                Spacer(modifier = Modifier.size(4.dp))
                Text(stringRes(Res.strings.no_collect))
            }
            // 未登录 - 本地已收藏 -> 已收藏
            else {
                Icon(Icons.Filled.Favorite, modifier = Modifier.size(16.dp), contentDescription = null)
                Spacer(modifier = Modifier.size(4.dp))
                Text(stringRes(Res.strings.collected))
            }
        }
    }

}

@Composable
fun HeaderRanking(
    modifier: Modifier,
    rating: BgmRating,
    color: Color = MaterialTheme.colorScheme.primary,
){

    Row (
        verticalAlignment = Alignment.CenterVertically,

        ){

        Column(modifier) {
            Text(
                "${rating.total} 人评丨#${rating.rank}",
                Modifier.padding(end = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                softWrap = false,
                color = color
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    rating.score?.toString()?:"",
                    color = color,
                    style = MaterialTheme.typography.bodyMedium
                )
                FiveRatingStars(score = rating.score?.toInt()?:0, color = color)

            }


        }

    }



}

@Composable
fun FiveRatingStars(
    score: Int, // range 0..10
    starSize: Dp = 22.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy((-1).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {

            Icon(
                when {
                    score >= 2 -> Icons.Rounded.Star
                    score == 1 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = color,

                )
            Icon(
                when {
                    score >= 4 -> Icons.Rounded.Star
                    score == 3 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = color,
            )
            Icon(
                when {
                    score >= 6 -> Icons.Rounded.Star
                    score == 5 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = color,
            )
            Icon(
                when {
                    score >= 8 -> Icons.Rounded.Star
                    score == 7 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = color,
            )
            Icon(
                when {
                    score >= 10 -> Icons.Rounded.Star
                    score == 9 -> Icons.AutoMirrored.Rounded.StarHalf
                    else -> Icons.Rounded.StarOutline
                },
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = color,
            )
        }
    }
}


