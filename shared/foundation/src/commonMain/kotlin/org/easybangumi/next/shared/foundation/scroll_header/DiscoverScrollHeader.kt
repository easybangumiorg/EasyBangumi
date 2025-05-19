package org.easybangumi.next.shared.foundation.scroll_header

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.foundation.logger
import kotlin.math.roundToInt

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
 *
 */

private val logger = logger("DiscoverScrollHeader")

class DiscoverScrollHeaderScope(
    val discoverScrollHeaderBehavior: DiscoverScrollHeaderBehavior
) : ScrollHeaderScope {



    internal val headerHeight = mutableStateOf(0f)
    internal val pinHeaderHeight = mutableStateOf(0f)


    override val state: ScrollableHeaderState
        get() = discoverScrollHeaderBehavior.state

    @get:Composable
    override val contentPadding: PaddingValues
        get() = PaddingValues(top = with(LocalDensity.current) { headerHeight.value.toDp() + pinHeaderHeight.value.toDp() })

    @Composable
    override fun Modifier.header(): Modifier {
        return layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            headerHeight.value = placeable.height.toFloat()
            state.offsetLimit = - placeable.height.toFloat()
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(0, state.offset.roundToInt())
            }
        }.scrollable(
            rememberScrollableState {
                -discoverScrollHeaderBehavior.contentScrollableState.dispatchRawDelta(-it)
            },
            orientation = Orientation.Vertical
        )
    }
//
//    @Composable
//    override fun Modifier.content(): Modifier {
//        return layout { measurable, constraints ->
//            val placeable = measurable.measure(constraints)
//            layout(constraints.maxWidth, constraints.maxHeight) {
//                placeable.place(0, (pinHeaderHeight.value + headerHeight.value + state.offset).roundToInt())
//            }
//        }.scrollable(
//            rememberScrollableState {it},
//            orientation = Orientation.Vertical
//        )
//    }


    @Composable
    fun Modifier.pinHeader(): Modifier {
        return layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            pinHeaderHeight.value = placeable.height.toFloat()
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(0, (headerHeight.value + state.offset).roundToInt())
            }
        }.scrollable(
            rememberScrollableState {
                -discoverScrollHeaderBehavior.contentScrollableState.dispatchRawDelta(-it)
            },
            orientation = Orientation.Vertical
        )
    }
}

class DiscoverScrollHeaderBehavior(
    override val state: ScrollableHeaderState,
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val contentScrollableState: ScrollableState,
    val canScroll : () -> Boolean = { true },
) : ScrollableHeaderBehavior<DiscoverScrollHeaderScope> {


    override val scope: DiscoverScrollHeaderScope = DiscoverScrollHeaderScope(this)

    override val isPinned: Boolean = false


    override val nestedScrollConnection: NestedScrollConnection = object: NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            logger.info("onPreScroll: available: $available")
            // Don't intercept if scrolling down.
            if (!canScroll()) return Offset.Zero
            if (available.y > 0) {
                val contentScrollOffsetIfConsumed = state.contentScrollOffset + available.y
                val old = state.offset
                state.offset = contentScrollOffsetIfConsumed
                return Offset(0f, state.offset - old)
            }

            state.offset += available.y
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            logger.info("onPostScroll: available: $available")
            if (!canScroll()) return Offset.Zero
            state.contentScrollOffset += consumed.y
            val old = state.offset
            state.offset += available.y
            return Offset(0f, state.offset - old)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val superConsumed = super.onPostFling(consumed, available)
            return superConsumed +
                    settleHeader(state, available.y, flingAnimationSpec, snapAnimationSpec)
        }
    }
}