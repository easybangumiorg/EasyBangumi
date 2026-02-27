package org.easybangumi.next.shared.compose.detail.bangumi

import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.cartoon.collection.CollectionUIUtils
import org.easybangumi.next.shared.data.bangumi.BgmCollectResp
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.elements.LoadScaffold
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.scheme.EasyScheme
import org.easybangumi.next.shared.data.bangumi.BgmRating
import org.easybangumi.next.shared.data.bangumi.BgmSubject
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.shimmer.drawRectWhenShimmerVisible
import org.easybangumi.next.shared.foundation.shimmer.rememberShimmerState
import org.easybangumi.next.shared.foundation.shimmer.shimmer
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
    onPlayClick: () -> Unit,
){
    val surfaceLowestColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
    val backgroundPainter = remember(backgroundColor) {
        BrushPainter(SolidColor(backgroundColor))
    }
    val data = subjectState.cacheData
    val shimmerState = rememberShimmerState(subjectState.isLoading())
    Layout (
        modifier = modifier.fillMaxWidth().clipToBounds().background(surfaceLowestColor),
        measurePolicy = {measurables, constraints ->
            if (measurables.isEmpty()) {
                return@Layout layout(0, 0) {}
            }
            // 测量所有 children
            val placeables = measurables.map { it.measure(constraints) }

            val wrapHeight: Placeable? = placeables.getOrNull(1)
            logger.info("wrapHeight: $wrapHeight size: ${wrapHeight?.height}")

            layout(constraints.maxWidth, wrapHeight?.height ?: constraints.maxHeight) {
                placeables.forEach {
                    it.place(0, 0)
                }
            }
        },
        content = {
            Crossfade(data, modifier = Modifier) {
                if (it != null && !isHeaderPin && !shimmerState.isVisible) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize().blur(32.dp),
                        model = coverUrl,
                        alignment = Alignment.TopCenter,
                        contentScale = ContentScale.FillWidth,
                        contentDescription = "cartoon cover background",
                        fallback = backgroundPainter,
                        placeholder = backgroundPainter,
                    )
                }


            }



            if (subjectState.isLoading()) {

                Column(
                    modifier = Modifier.shimmer(shimmerState).padding(16.dp, 0.dp)
                ){
                    Spacer(modifier = Modifier.size(contentPaddingTop))
                    Text(
                        text = data?.displayName ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).drawRectWhenShimmerVisible(shimmerState)
                    )
                    Spacer(modifier = Modifier.size(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().height(EasyScheme.size.cartoonCoverHeight)
                    ) {

                        Box(modifier.width(EasyScheme.size.cartoonCoverWidth).height(EasyScheme.size.cartoonCoverHeight).clip(RoundedCornerShape(12.dp)).drawRectWhenShimmerVisible(shimmerState))
                        Spacer(modifier = Modifier.size(16.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).drawRectWhenShimmerVisible(shimmerState),
                                text = "",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).drawRectWhenShimmerVisible(shimmerState),
                                text = "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).drawRectWhenShimmerVisible(shimmerState),
                                text = "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
            } else {
                Box(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    Crossfade(data, modifier = Modifier.fillMaxWidth().fillMaxSize()){
                        if (it != null && !isHeaderPin) {
                            Box(
                                modifier = Modifier.fillMaxWidth().fillMaxSize()
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
                    }


                    Column(
                        modifier = Modifier.padding(16.dp, 0.dp)
                    ) {
                        Spacer(modifier = Modifier.size(contentPaddingTop))

                        Text(
                            text = data?.displayName ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().height(EasyScheme.size.cartoonCoverHeight)
                        ) {

                            CartoonCoverCard(
                                model = coverUrl,
                            )

                            Spacer(modifier = Modifier.size(16.dp))

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

                                },
                            ) {
                                HeaderContent(it.data,bgmCollectionState = bgmCollectionState,
                                    cartoonInfo = cartoonInfo,
                                    onCollectClick = onCollectClick,)
                            }


                        }

                        Spacer(modifier = Modifier.size(4.dp))
//
                        Row {
                            PlayBtn(Modifier.weight(1f), cartoonInfo = cartoonInfo) {
                                onPlayClick()
                            }
                        }




                    }
                }

            }


        },
    )




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
            Spacer(modifier = Modifier.size(8.dp))
            HeaderScore(Modifier, it)
        }
        Spacer(modifier = Modifier.weight(1f))
        Row {
            Spacer(modifier = Modifier.weight(1f))
            HeaderCollectBtn(
                modifier = Modifier,
                bgmCollectionState = bgmCollectionState,
                cartoonInfo = cartoonInfo,
                onCollectClick = onCollectClick,
            )
        }




    }
}

@Composable
fun PlayBtn(
    modifier: Modifier,
    cartoonInfo: CartoonInfo? = null,
    onClick: () -> Unit,
) {
    val hasHistory = cartoonInfo != null && cartoonInfo.lastHistoryTime > 0
    val label = if (hasHistory) {
        stringRes(Res.strings.last_episode_title, cartoonInfo!!.lastEpisodeLabel)
    } else {
        stringRes(Res.strings.play_now)
    }
    Button(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Icon(Icons.Filled.PlayArrow, modifier = Modifier.size(16.dp), contentDescription = null)
        Spacer(modifier = Modifier.size(4.dp))
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

    OutlinedButton(
        modifier = modifier,
//        shape = RoundedCornerShape(16.dp),
//        contentPadding = PaddingValues(0.dp),
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
            icon.first, modifier = Modifier.size(16.dp), tint = if (icon.second) MaterialTheme.colorScheme.primary else Color.Unspecified  , contentDescription = null
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            stringRes(
                label
            )
        )
    }

}

@Composable
fun HeaderScore(
    modifier: Modifier,
    rating: BgmRating,
    color: Color = MaterialTheme.colorScheme.primary,
){

    Column(modifier) {
        Text(
            "${rating.total} 人评丨${ rating.score?.toString()?:""}",
            Modifier,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            softWrap = false,
            color = color
        )
        FiveRatingStars(score = rating.score?.toInt()?:0, color = color)
    }


}

@Composable
fun HeaderRanking(
    modifier: Modifier,
    rating: BgmRating,
    color: Color = MaterialTheme.colorScheme.primary,
){

    Text(
        "Rank: #${rating.rank}",
        modifier,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        softWrap = false,
        color = color
    )


}


@Composable
fun FiveRatingStars(
    score: Int, // range 0..10
    starSize: Dp = 21.dp,
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


