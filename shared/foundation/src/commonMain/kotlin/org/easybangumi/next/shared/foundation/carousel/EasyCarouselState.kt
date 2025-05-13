package org.easybangumi.next.shared.foundation.carousel

import androidx.annotation.FloatRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import org.easybangumi.next.lib.logger.logger

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

    suspend fun scrollForward(
        loop: Boolean = false
    ){
        if (carouselState.isScrollInProgress) {
            return
        }
        logger.info("scrollNext ${pagerState.canScrollBackward} ${carouselState.pagerState.currentPage} ${carouselState.pagerState.pageCount}")

        if (pagerState.canScrollForward) {
            pagerState.animateScrollToPage(
                (carouselState.pagerState.currentPage + 1) % (carouselState.pagerState.pageCount),
            )
        } else if (loop) {
            pagerState.animateScrollToPage(0)
        }

    }

    suspend fun scrollBackward(
        loop: Boolean = false
    ){
        if (carouselState.isScrollInProgress) {
            return
        }
        if (!pagerState.canScrollBackward && !loop) {
            return
        }
        var index = carouselState.pagerState.currentPage - 1
        if (index < 0) {
            index = pagerState.pageCount - 1
        }
        if (index < 0) {
            if (! loop) {
                return
            }
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