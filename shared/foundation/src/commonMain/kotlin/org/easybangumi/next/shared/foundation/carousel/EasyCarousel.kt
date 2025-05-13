package org.easybangumi.next.shared.foundation.carousel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.foundation.toPx
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

private val logger = logger("EasyCarousel")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyHorizontalMultiBrowseCarousel(
    easyCarouselState: EasyCarouselState,
    scope: CoroutineScope = rememberCoroutineScope(),
    preferredItemWidth: Dp = 154.dp,
    itemSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    lingBehavior: TargetedFlingBehavior = CarouselDefaults.multiBrowseFlingBehavior(easyCarouselState.carouselState),
    modifier: Modifier,
    showArc: Boolean,
    userScrollEnabled: Boolean = true,
    period: Duration = 5.seconds,
    content: @Composable CarouselItemScope.(itemIndex: Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = modifier.hoverable(interactionSource),
    ) {
        HorizontalMultiBrowseCarousel(
            state = easyCarouselState.carouselState,
            preferredItemWidth = preferredItemWidth,
            flingBehavior = lingBehavior,
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
        if (showArc) {

            AnimatedVisibility(easyCarouselState.canScrollBackward(),
                modifier = Modifier.align(Alignment.CenterStart),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors().copy(
                        containerColor = Color.Black.copy(0.6f),
                        contentColor = Color.White
                    ),
                    onClick = {
                        scope.launch {
                            easyCarouselState.scrollBackward(false)
                        }
                    }) {

                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "")
                }
            }



            AnimatedVisibility(easyCarouselState.canScrollForward(),
                modifier = Modifier.align(Alignment.CenterEnd),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors().copy(
                        containerColor = Color.Black.copy(0.6f),
                        contentColor = Color.White
                    ),
                    onClick = {
                        scope.launch {
                            //easyCarouselState.carouselState.animateScrollBy(width)
                            easyCarouselState.scrollForward(false)
                        }
                    }
                ) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "")
                }
            }
        }
    }

    EasyCarouselEffect(
        enable = !isHovered,
        period = period,
        easyCarouselState = easyCarouselState,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyHorizontalUncontainedCarousel(
    easyCarouselState: EasyCarouselState,
    scope: CoroutineScope = rememberCoroutineScope(),
    itemWidth: Dp,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    flingBehavior: TargetedFlingBehavior = CarouselDefaults.noSnapFlingBehavior(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showArc: Boolean,
    userScrollEnabled: Boolean = true,
    content: @Composable CarouselItemScope.(itemIndex: Int) -> Unit
){
    Box(modifier) {
        HorizontalUncontainedCarousel(
            state = easyCarouselState.carouselState,
            itemWidth = itemWidth,
            flingBehavior = flingBehavior,
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
        if (showArc) {

            AnimatedVisibility(easyCarouselState.canScrollBackward(),
                modifier = Modifier.align(Alignment.CenterStart),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors().copy(
                        containerColor = Color.Black.copy(0.6f),
                        contentColor = Color.White
                    ),
                    onClick = {
                        scope.launch {
                            easyCarouselState.scrollBackward(false)
                        }
                    }) {

                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "")
                }
            }



            AnimatedVisibility(easyCarouselState.canScrollForward(),
                modifier = Modifier.align(Alignment.CenterEnd),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors().copy(
                        containerColor = Color.Black.copy(0.6f),
                        contentColor = Color.White
                    ),
                    onClick = {
                        scope.launch {
                            //easyCarouselState.carouselState.animateScrollBy(width)
                            easyCarouselState.scrollForward(false)
                        }
                    }
                ) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyCarouselEffect(
    enable: Boolean,
    period: Duration = 5.seconds,
    easyCarouselState: EasyCarouselState,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState = lifecycle.currentStateAsState()

    LaunchedEffect(
        enable,
        lifecycleState,
        period,
        easyCarouselState,
    ) {
        snapshotFlow {
            enable && lifecycleState.value.isAtLeast(Lifecycle.State.RESUMED)
        }.collectLatest { active ->
            logger.info("when snapshotFlow collect $active")
            if (active && isActive) {
                delay(period)
            }
            while (active && isActive) {
                if (!easyCarouselState.carouselState.isScrollInProgress) {
                    easyCarouselState.scrollForward(true)
                }
                delay(period)
            }
        }
    }


}