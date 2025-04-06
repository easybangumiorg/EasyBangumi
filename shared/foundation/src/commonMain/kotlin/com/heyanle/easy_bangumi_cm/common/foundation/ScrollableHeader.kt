package com.heyanle.easy_bangumi_cm.common.foundation

import androidx.compose.animation.core.*
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import com.heyanle.easy_bangumi_cm.base.service.system.logger
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Created by heyanlin on 2025/3/20.
 */

@Composable
fun ScrollableHeaderScaffold(
    modifier: Modifier,
    behavior: ScrollableHeaderBehavior?,
    headerIfBehavior: @Composable (state: ScrollableHeaderState) -> Unit,
    // padding 为 LazyColumn 的 contentPadding 不能作为整个 content 的 Padding
    content: @Composable (scrollContentPadding: PaddingValues) -> Unit
) {
    if (behavior == null) {
        Box(modifier) {
            content(PaddingValues())
        }
    } else {
        Box(modifier.nestedScroll(behavior.nestedScrollConnection)) {
            // content
            behavior.contentHost(content)

            // header
            behavior.headerHost(headerIfBehavior)
        }
    }
}

// ============= state =============

@Composable
fun rememberScrollableHeaderState(
    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
    initialHeightOffset: Float = 0f,
    initialContentOffset: Float = 0f,
): ScrollableHeaderState {


    return rememberSaveable(saver = ScrollableHeaderState.Saver) {
        ScrollableHeaderState(initialHeightOffsetLimit, initialHeightOffset, initialContentOffset)
    }
}

@Stable
class ScrollableHeaderState (
    initialOffsetLimit: Float,
    initialOffset: Float,
    initialContentScrollOffset: Float,
) {

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
                save = { listOf(it.offsetLimit, it.offset, it.contentScrollOffset) },
                restore = {
                    ScrollableHeaderState(
                        initialOffsetLimit = it[0],
                        initialOffset = it[1],
                        initialContentScrollOffset = it[2],
                    )
                }
            )
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

    @Composable
    fun headerHost(header: @Composable (state: ScrollableHeaderState) -> Unit)

    @Composable
    fun contentHost(content: @Composable (contentPadding: PaddingValues) -> Unit)


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
    }
}

class EnterAlwaysScrollBehavior(
    override val state: ScrollableHeaderState,
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true }
): ScrollableHeaderBehavior {

    private var headerHeight = mutableStateOf(0f)

    override val isPinned = false
    override val nestedScrollConnection: NestedScrollConnection =
        object : NestedScrollConnection {
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


    @Composable
    override fun headerHost(header: @Composable (state: ScrollableHeaderState) -> Unit) {
        Box(Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            headerHeight.value = placeable.height.toFloat()
            state.offsetLimit = - placeable.height.toFloat()
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(0, state.offset.roundToInt())
            }
        }) {
            header(state)
        }
    }

    @Composable
    override fun contentHost(content: @Composable (contentPadding: PaddingValues) -> Unit) {
        Box(Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.place(0, 0)
            }
        }) {
            content(PaddingValues(top = with(LocalDensity.current) { headerHeight.value.toDp() }))
        }
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
