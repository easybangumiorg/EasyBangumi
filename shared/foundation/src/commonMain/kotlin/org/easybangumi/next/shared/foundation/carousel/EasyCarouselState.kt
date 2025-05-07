package org.easybangumi.next.shared.foundation.carousel

import androidx.annotation.FloatRange
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.logger.logger
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
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("INVISIBLE_REFERENCE")
class EasyCarouselState(
    private val currentItem: Int = 0,
    @FloatRange(from = -0.5, to = 0.5) currentItemOffsetFraction: Float = 0f,
    itemCount: () -> Int,
    
) {

    val logger = logger()

    val carouselState = CarouselState(currentItem, currentItemOffsetFraction, itemCount)
    private val pagerState = carouselState.pagerState

    fun canScrollForward() = pagerState.canScrollForward
    fun canScrollBackward() = pagerState.canScrollBackward

    suspend fun scrollNext(){
        if (carouselState.isScrollInProgress) {
            return
        }
        logger.info("scrollNext ${pagerState.canScrollBackward} ${carouselState.pagerState.currentPage} ${carouselState.pagerState.pageCount}")

        if (pagerState.canScrollForward) {
            pagerState.animateScrollToPage(
                (carouselState.pagerState.currentPage + 1) % (carouselState.pagerState.pageCount),
            )
        } else {
            pagerState.animateScrollToPage(0)
        }

    }

    suspend fun scrollLast(){
        if (carouselState.isScrollInProgress) {
            return
        }
        var index = carouselState.pagerState.currentPage - 1
        if (index < 0) {
            index = pagerState.pageCount - 1
        }
        if (index < 0) {
            index = currentItem
        }
        pagerState.animateScrollToPage(
            index,
        )
    }

    companion object {
        /** To keep current item and item offset saved */
        val Saver: Saver<EasyCarouselState, *> =
            listSaver(
                save = {
                    listOf(
                        it.pagerState.currentPage,
                        it.pagerState.currentPageOffsetFraction,
                        it.pagerState.pageCount,
                    )
                },
                restore = {
                    EasyCarouselState(
                        currentItem = it[0] as Int,
                        currentItemOffsetFraction = it[1] as Float,
                        itemCount = { it[2] as Int },
                    )
                }
            )
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberEasyCarouselState(
    initialItem: Int = 0,
    itemCount: () -> Int,
): EasyCarouselState {
    return rememberSaveable(saver = EasyCarouselState.Saver) {
        EasyCarouselState(
            currentItem = initialItem,
            currentItemOffsetFraction = 0F,
            itemCount = itemCount
        )
    }
        .apply { carouselState.itemCountState.value = itemCount }
}