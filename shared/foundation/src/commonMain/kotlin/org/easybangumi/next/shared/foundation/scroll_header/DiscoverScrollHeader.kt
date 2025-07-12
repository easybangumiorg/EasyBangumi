package org.easybangumi.next.shared.foundation.scroll_header

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.foundation.InputMode
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.UIMode
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

internal val logger = logger("DiscoverScrollHeader")

class DiscoverScrollHeaderScope(
    val discoverScrollHeaderBehavior: DiscoverScrollHeaderBehavior
) : ScrollHeaderScope {


    internal val headerHeight = mutableStateOf(0f)
    internal val pinHeaderHeight = mutableStateOf(0f)


    override val state: ScrollableHeaderState
        get() = discoverScrollHeaderBehavior.state

    @get:Composable
    override val contentPadding: PaddingValues
        get() = PaddingValues(top = with(LocalDensity.current) {
//            logger.info("headerHeight: ${headerHeight.value}, pinHeaderHeight: ${pinHeaderHeight.value}, offset: ${state.offset}")
            (headerHeight.value + pinHeaderHeight.value + state.offset).coerceAtLeast(0f).toDp()
        })

    @Composable
    override fun Modifier.header(
        minHeight: Dp,
    ): Modifier {
        val minHeightPx = with(LocalDensity.current) { minHeight.roundToPx() }
        return layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            headerHeight.value = placeable.height.toFloat()
            state.headerHeight.value = placeable.height.toFloat()
            state.offsetLimit = -placeable.height.toFloat() + minHeightPx
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(0, state.offset.roundToInt())
            }
        }.scrollable(rememberScrollableState { 0f }, Orientation.Vertical)
    }

    @Composable
    fun Modifier.pinHeader(): Modifier {
        return layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            pinHeaderHeight.value = placeable.height.toFloat()
            state.pinHeaderHeight.value = placeable.height.toFloat()
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(0, (headerHeight.value + state.offset).roundToInt())
            }
        }.scrollable(rememberScrollableState { 0f }, Orientation.Vertical)
    }

    @Composable
    override fun Modifier.content(): Modifier {
        return layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(0, (headerHeight.value + pinHeaderHeight.value + state.offset).roundToInt())
            }
        }
    }

    @Composable
    fun Modifier.contentPointerScrollOpt(enabled: Boolean): Modifier {
        return contentPointerScrollOpt(enabled, this)
    }
}

@Composable
internal expect fun DiscoverScrollHeaderScope.contentPointerScrollOpt(
    enabled: Boolean,
    modifier: Modifier
): Modifier

class DiscoverScrollHeaderBehavior(
    override val state: ScrollableHeaderState,
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val uiMode: UIMode,
) : ScrollableHeaderBehavior<DiscoverScrollHeaderScope> {


    override val scope: DiscoverScrollHeaderScope = DiscoverScrollHeaderScope(this)

    override val isPinned: Boolean = false

    override val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
//            logger.info("onPreScroll: available: $available")
            if (available.y < 0) {
                val old = state.offset
                state.offset += available.y
                return Offset(0f, state.offset - old)
            } else {
                return Offset.Zero
            }

        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
//            logger.info("onPostScroll: consumed: $consumed, available: $available")
            val old = state.offset
            state.offset += available.y
            return Offset(0f, state.offset - old)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
//            logger.info("onPostFling: consumed: $consumed, available: $available")
            val superConsumed = super.onPostFling(consumed, available)
            if (uiMode.inputMode == InputMode.TOUCH) {
                return superConsumed +
                        settleHeader(state, available.y, flingAnimationSpec, snapAnimationSpec)
            } else {
                // 鼠标模式不用自动吸附
                return superConsumed
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
//            logger.info("onPreFling: available: $available")
            return super.onPreFling(available)
        }
    }
}

@Composable
fun rememberDiscoverHeaderTabState(
    key: String,
    firstVisibleItemIndex: Int = 0,
    firstVisibleItemScrollOffset: Int = 0,
    initialScrollOffset: Float = 0f,
): DiscoverScrollHeaderTabState {
    return rememberSaveable(
        key = key,
        saver = DiscoverScrollHeaderTabState.Saver
    ) {
        DiscoverScrollHeaderTabState(
            firstVisibleItemIndex,
            firstVisibleItemScrollOffset,
            initialScrollOffset
        )
    }
}

@Stable
class DiscoverScrollHeaderTabState(
    firstVisibleItemIndex: Int = 0,
    firstVisibleItemScrollOffset: Int = 0,
    initialScrollOffset: Float = 0f,
) {

    val lazyGridState = LazyGridState(
        firstVisibleItemIndex = firstVisibleItemIndex,
        firstVisibleItemScrollOffset = firstVisibleItemScrollOffset
    )
    var contentOffset by mutableFloatStateOf(initialScrollOffset)

    companion object {
        /** The default [Saver] implementation for [TopAppBarState]. */
        val Saver: Saver<DiscoverScrollHeaderTabState, *> =
            listSaver(
                save = {
                    listOf(
                        it.lazyGridState.firstVisibleItemIndex,
                        it.lazyGridState.firstVisibleItemScrollOffset,
                        it.contentOffset
                    )
                },
                restore = {
                    DiscoverScrollHeaderTabState(
                        it[0].toInt(),
                        it[1].toInt(),
                        it[2].toFloat()
                    )
                }
            )
    }

}
