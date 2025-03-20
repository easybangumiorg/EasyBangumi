package com.heyanle.easy_bangumi_cm.common.foundation.o

import androidx.compose.animation.core.*
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.isSpecified
import com.heyanle.easy_bangumi_cm.base.service.system.logger
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 参考 TopAppBarScrollBehavior 实现的可滚动的 Header
 * Created by heyanlin on 2025/3/20.
 */

@Composable
fun ScrollableHeaderScaffold(
    modifier: Modifier,
    behavior: ScrollableHeaderBehavior,
    header: @Composable (state: ScrollableHeaderState) -> Unit,
    // padding 为 LazyColumn 的 contentPadding 不能作为整个 content 的 Padding
    content: @Composable (scrollContentPadding: PaddingValues) -> Unit
) {
    val offset = behavior.state.offset
    Box(modifier.nestedScroll(behavior.nestedScrollConnection)) {
        // content
        Box(Modifier.fillMaxSize()) {
            content(behavior.getContentPadding())
        }

        // header
        Box(Modifier.layout(behavior.onHeaderLayout)) {
            header(behavior.state)
        }
    }

}

// ============= state =============
@Stable
class ScrollableHeaderState (
    initialOffsetLimit: Float,
    initialOffset: Float,
    initialContentScrollOffset: Float,
    initialHeaderHeight: Float,
) {

    var headerHeight = mutableFloatStateOf(initialHeaderHeight)
    var offsetLimit by mutableFloatStateOf(initialOffsetLimit)

    private var _offset = mutableFloatStateOf(initialOffset)
    var offset: Float
        get() = _offset.floatValue
        set(newOffset) {
            _offset.floatValue =
                newOffset.coerceIn(minimumValue = offsetLimit, maximumValue = 0f)
        }


    var contentScrollOffset by mutableFloatStateOf(initialContentScrollOffset)

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
                save = { listOf(it.offsetLimit, it.offset, it.contentScrollOffset, it.headerHeight.value) },
                restore = {
                    ScrollableHeaderState(
                        initialOffsetLimit = it[0],
                        initialOffset = it[1],
                        initialContentScrollOffset = it[2],
                        initialHeaderHeight = it[3]
                    )
                }
            )
    }
}

@Composable
fun rememberScrollableHeaderState(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
    initialContentOffset: Float = 0f,
    initialHeaderHeightDp: Dp = Dp.Unspecified
): ScrollableHeaderState {

    val initialHeightHeight = if (initialHeaderHeightDp.isSpecified) with(LocalDensity.current) {initialHeaderHeightDp.toPx()} else -1f

    return rememberSaveable(saver = ScrollableHeaderState.Saver) {
        ScrollableHeaderState(initialHeightOffsetLimit, initialHeightOffset, initialContentOffset, initialHeightHeight)
    }
}

// ============= behavior =============

@Stable
interface ScrollableHeaderBehavior {

    val state: ScrollableHeaderState
    val isPinned: Boolean

    val snapAnimationSpec: AnimationSpec<Float>?
    val flingAnimationSpec: DecayAnimationSpec<Float>?
    val nestedScrollConnection: NestedScrollConnection

    val onHeaderLayout: MeasureScope.(Measurable, Constraints) -> MeasureResult
    val onContentLayout: MeasureScope.(Measurable, Constraints) -> MeasureResult

    @Composable fun getContentPadding(): PaddingValues


    companion object {
        @Composable
        fun enterAlwaysScrollBehavior(
            state: ScrollableHeaderState = rememberScrollableHeaderState(),
            canScroll: () -> Boolean = { true },
            snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
            flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
        ): ScrollableHeaderBehavior =
            EnterAlwaysScrollBehavior(
                state = state,
                snapAnimationSpec = snapAnimationSpec,
                flingAnimationSpec = flingAnimationSpec,
                canScroll = canScroll
            )

        @Composable
        fun exitUntilCollapsedScrollBehavior(
            state: ScrollableHeaderState = rememberScrollableHeaderState(),
            canScroll: () -> Boolean = { true },
            snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
            flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
        ): ScrollableHeaderBehavior =
            ExitUntilCollapsedScrollBehavior(
                state = state,
                snapAnimationSpec = snapAnimationSpec,
                flingAnimationSpec = flingAnimationSpec,
                canScroll = canScroll
            )
    }
}

class ExitUntilCollapsedScrollBehavior (
    override val state: ScrollableHeaderState,
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true }
) : ScrollableHeaderBehavior {

    private var contentOffset = 0f

    override val isPinned: Boolean = false
    override val nestedScrollConnection: NestedScrollConnection =
        object: NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Don't intercept if scrolling down.
                if (!canScroll() || available.y > 0f) return Offset.Zero

                val prevHeightOffset = state.offset
                state.offset = state.offset + available.y
                return if (prevHeightOffset != state.offset) {
                    // We're in the middle of top app bar collapse or expand.
                    // Consume only the scroll on the Y axis.
                    available.copy(x = 0f)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!canScroll()) return Offset.Zero
                state.contentScrollOffset += consumed.y

                if (available.y < 0f || consumed.y < 0f) {
                    // When scrolling up, just update the state's height offset.
                    val oldHeightOffset = state.offset
                    state.offset = state.offset + consumed.y
                    return Offset(0f, state.offset - oldHeightOffset)
                }

                if (consumed.y == 0f && available.y > 0) {
                    // Reset the total content offset to zero when scrolling all the way down. This
                    // will eliminate some float precision inaccuracies.
                    state.contentScrollOffset = 0f
                }

                if (available.y > 0f) {
                    // Adjust the height offset in case the consumed delta Y is less than what was
                    // recorded as available delta Y in the pre-scroll.
                    val oldHeightOffset = state.offset
                    state.offset = state.offset + available.y
                    return Offset(0f, state.offset - oldHeightOffset)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val superConsumed = super.onPostFling(consumed, available)
                return superConsumed +
                        settleHeader(state, available.y, flingAnimationSpec, snapAnimationSpec)
            }


        }
    override val onHeaderLayout: MeasureScope.(Measurable, Constraints) -> MeasureResult = { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        state.headerHeight.value = placeable.height.toFloat()
        state.offsetLimit = - state.headerHeight.value
        contentOffset = placeable.height + state.offset
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(0, state.offset.roundToInt())
        }
    }
    override val onContentLayout: MeasureScope.(Measurable, Constraints) -> MeasureResult = { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(0, contentOffset.roundToInt())
        }
    }

    @Composable
    override fun getContentPadding(): PaddingValues {
        return PaddingValues()
    }
}

class EnterAlwaysScrollBehavior(
    override val state: ScrollableHeaderState,
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true }
): ScrollableHeaderBehavior {

    override val isPinned: Boolean = false
    override val nestedScrollConnection: NestedScrollConnection =
        object: NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                logger.i("ScrollableHeaderBehavior", "onPreScroll: available: $available")
                // Don't intercept if scrolling down.
                if (!canScroll()) return Offset.Zero
                state.offset += available.y
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                logger.i("ScrollableHeaderBehavior", "onPostScroll: consumed: $consumed, available: $available")
                if (!canScroll()) return Offset.Zero
                state.contentScrollOffset += consumed.y

                if (available.y != 0f) {
                    // When scrolling up, just update the state's height offset.
                    val oldHeightOffset = state.offset
                    state.offset += consumed.y
                    return Offset(0f, state.offset - oldHeightOffset)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val superConsumed = super.onPostFling(consumed, available)
                return superConsumed +
                        settleHeader(state, available.y, flingAnimationSpec, snapAnimationSpec)
            }
        }

    override val onHeaderLayout: MeasureScope.(Measurable, Constraints) -> MeasureResult = { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        state.headerHeight.value = placeable.height.toFloat()
        state.offsetLimit = - state.headerHeight.value
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(0, state.offset.roundToInt())
        }
    }

    override val onContentLayout: MeasureScope.(Measurable, Constraints) -> MeasureResult = { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(0, 0)
        }
    }

    @Composable
    override fun getContentPadding(): PaddingValues {
        val headerHeight = state.headerHeight.value
        val contentPaddingTopDp = with(LocalDensity.current) {headerHeight.coerceAtLeast(0f).toDp()}
        return PaddingValues(top = contentPaddingTopDp)
    }

}

private suspend fun settleHeader(
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
                // avoid rounding errors and stop if anything is unconsumed
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    // Snap if animation specs were provided.
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
