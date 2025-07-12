package org.easybangumi.next.shared.foundation.scroll_header

import androidx.compose.animation.core.*
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.UIMode
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderState.Companion.Saver
import kotlin.math.abs

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
fun <SCOPE : ScrollHeaderScope> ScrollableHeaderScaffold(
    modifier: Modifier,
    behavior: ScrollableHeaderBehavior<SCOPE>,
    nested: Boolean = true,
    content: @Composable SCOPE.() -> Unit
) {
    Box(modifier.run {
        if (nested) {
            nestedScroll(
                behavior.nestedScrollConnection,
            )
                .clipToBounds()
        } else {
            this
        }
    }) {
        content.invoke(behavior.scope)
    }

}

@Composable
fun rememberScrollableHeaderState(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
): ScrollableHeaderState {
    return rememberSaveable(saver = ScrollableHeaderState.Saver) {
        ScrollableHeaderState(initialHeightOffsetLimit, initialHeightOffset)
    }
}

@Stable
class ScrollableHeaderState(
    initialOffsetLimit: Float = -Float.MAX_VALUE,
    initialOffset: Float = 0f,
) {

    val headerHeight = mutableStateOf(0f)
    val pinHeaderHeight = mutableStateOf(0f)

    var offsetLimit by mutableFloatStateOf(initialOffsetLimit)

    private var _offset = mutableFloatStateOf(initialOffset)
    var offset: Float
        get() = _offset.floatValue
        set(newOffset) {
            _offset.floatValue =
                newOffset.coerceIn(minimumValue = offsetLimit, maximumValue = 0f)
        }


    val collapsedFraction: Float
        get() =
            if (offsetLimit != 0f) {
                offset / offsetLimit
            } else {
                0f
            }

    companion object {
        /** The default [Saver] implementation for [TopAppBarState]. */
        val Saver: Saver<ScrollableHeaderState, *> =
            listSaver(
                save = { listOf(it.offsetLimit, it.offset) },
                restore = {
                    ScrollableHeaderState(
                        initialOffsetLimit = it[0],
                        initialOffset = it[1],
                    )
                }
            )
    }
}

interface ScrollHeaderScope {
    val state: ScrollableHeaderState

    @get:Composable
    val contentPadding: PaddingValues

    @Composable
    fun Modifier.header(
        minHeight: Dp = 0.dp,
    ): Modifier

    @Composable
    fun Modifier.content(): Modifier
}

@Stable
interface ScrollableHeaderBehavior<SCOPE : ScrollHeaderScope> {

    val state: ScrollableHeaderState
    val isPinned: Boolean

    val snapAnimationSpec: AnimationSpec<Float>?
    val flingAnimationSpec: DecayAnimationSpec<Float>?
    val nestedScrollConnection: NestedScrollConnection

    val scope: SCOPE

    companion object {
        @Composable
        fun discoverScrollHeaderBehavior(
            state: ScrollableHeaderState = rememberScrollableHeaderState(),
            snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
            flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
            uiMode: UIMode = LocalUIMode.current
        ): DiscoverScrollHeaderBehavior = remember(state, snapAnimationSpec, flingAnimationSpec) {
            DiscoverScrollHeaderBehavior(
                state = state,
                snapAnimationSpec = snapAnimationSpec,
                flingAnimationSpec = flingAnimationSpec,
                uiMode = uiMode
            )
        }

    }


}

internal suspend fun settleHeader(
    state: ScrollableHeaderState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity.
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = velocity,
        )
            .animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.offset
                state.offset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.offset)
                lastValue = value
                remainingVelocity = this.velocity
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    if (snapAnimationSpec != null) {
        if (state.offset < 0 && state.offset > state.offsetLimit) {
            AnimationState(initialValue = state.offset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.offsetLimit
                },
                animationSpec = snapAnimationSpec
            ) {
                state.offset = value
            }
        }
    }

    return Velocity(0f, remainingVelocity)
}
