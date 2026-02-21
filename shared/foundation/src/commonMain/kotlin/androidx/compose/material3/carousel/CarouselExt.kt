package androidx.compose.material3.carousel

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Suppress("INVISIBLE_REFERENCE", "SYNTHETIC_ACCESSOR", "INVISIBLE_MEMBER",)
@ExperimentalMaterial3Api
@Composable
fun HorizontalMultiBrowseCarouselEasy(
    state: CarouselState,
    preferredItemWidth: Dp,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    flingBehavior: TargetedFlingBehavior =
        CarouselDefaults.singleAdvanceFlingBehavior(state = state),
    minSmallItemWidth: Dp = CarouselDefaults.MinSmallItemSize,
    maxSmallItemWidth: Dp = CarouselDefaults.MaxSmallItemSize,
    userScrollEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable CarouselItemScope.(itemIndex: Int) -> Unit
) {
    val density = LocalDensity.current
    Carousel(
        state = state,
        orientation = Orientation.Horizontal,
        keylineList = { availableSpace, itemSpacingPx ->
            with(density) {
                state.currentItem
                multiBrowseKeylineList(
                    density = this,
                    carouselMainAxisSize = availableSpace,
                    preferredItemSize = preferredItemWidth.toPx(),
                    itemCount = state.pagerState.pageCountState.value.invoke(),
                    itemSpacing = itemSpacingPx,
                    minSmallItemSize = minSmallItemWidth.toPx(),
                    maxSmallItemSize = maxSmallItemWidth.toPx(),
                )
            }
        },
        contentPadding = contentPadding,
        // 2 is the max number of medium and small items that can be present in a multi-browse
        // carousel and should be the upper bounds max non focal visible items.
        maxNonFocalVisibleItemCount = 2,
        modifier = modifier,
        itemSpacing = itemSpacing,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}

@Suppress("INVISIBLE_REFERENCE", "SYNTHETIC_ACCESSOR", "INVISIBLE_MEMBER",)
@ExperimentalMaterial3Api
@Composable
fun HorizontalUncontainedCarousel(
    state: CarouselState,
    itemWidth: Dp,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    flingBehavior: TargetedFlingBehavior = CarouselDefaults.noSnapFlingBehavior(),
    userScrollEnabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable CarouselItemScope.(itemIndex: Int) -> Unit
) {
    val density = LocalDensity.current
    Carousel(
        state = state,
        orientation = Orientation.Horizontal,
        keylineList = { availableSpace, itemSpacingPx ->
            with(density) {
                uncontainedKeylineList(
                    density = this,
                    carouselMainAxisSize = availableSpace,
                    itemSize = itemWidth.toPx(),
                    itemSpacing = itemSpacingPx,
                )
            }
        },
        contentPadding = contentPadding,
        userScrollEnabled = userScrollEnabled,
        // Since uncontained carousels only have one item that masks as it moves in/out of view,
        // there is no need to increase the max non focal count.
        maxNonFocalVisibleItemCount = 0,
        modifier = modifier,
        itemSpacing = itemSpacing,
        flingBehavior = flingBehavior,
        content = content
    )
}


@Suppress("INVISIBLE_REFERENCE", "SYNTHETIC_ACCESSOR", "INVISIBLE_MEMBER",)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Carousel(
    state: CarouselState,
    orientation: Orientation,
    keylineList: (availableSpace: Float, itemSpacing: Float) -> KeylineList,
    contentPadding: PaddingValues,
    maxNonFocalVisibleItemCount: Int,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    flingBehavior: TargetedFlingBehavior =
        CarouselDefaults.singleAdvanceFlingBehavior(state = state),
    userScrollEnabled: Boolean = true,
    content: @Composable CarouselItemScope.(itemIndex: Int) -> Unit
) {
    val beforeContentPadding = contentPadding.calculateBeforeContentPadding(orientation)
    val afterContentPadding = contentPadding.calculateAfterContentPadding(orientation)
    val pageSize =
        remember(keylineList) {
            CarouselPageSize(keylineList, beforeContentPadding, afterContentPadding)
        }

    val snapPosition = KeylineSnapPosition(pageSize)

    if (orientation == Orientation.Horizontal) {
        HorizontalPager(
            state = state.pagerState,
            // Only pass cross axis padding as main axis padding will be handled by the strategy
            contentPadding =
                PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding()
                ),
            pageSize = pageSize,
            verticalAlignment = Alignment.Top,
            pageSpacing = itemSpacing,
            beyondViewportPageCount = maxNonFocalVisibleItemCount,
            snapPosition = snapPosition,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            modifier = modifier
        ) { page ->
            val carouselItemInfo = remember { CarouselItemDrawInfoImpl() }
            val scope = remember { CarouselItemScopeImpl(itemInfo = carouselItemInfo) }
            val clipShape = remember {
                object : Shape {
                    override fun createOutline(
                        size: Size,
                        layoutDirection: LayoutDirection,
                        density: Density
                    ): Outline {
                        return Outline.Rectangle(carouselItemInfo.maskRect)
                    }
                }
            }

            Box(
                modifier =
                    Modifier.carouselItem(
                        index = page,
                        state = state,
                        strategy = { pageSize.strategy },
                        carouselItemDrawInfo = carouselItemInfo,
                        clipShape = clipShape
                    )
            ) {
                scope.content(page)
            }
        }
    } else if (orientation == Orientation.Vertical) {
        VerticalPager(
            state = state.pagerState,
            // Only pass cross axis padding as main axis padding will be handled by the strategy
            contentPadding =
                PaddingValues(
                    start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
                ),
            pageSize = pageSize,
            pageSpacing = itemSpacing,
            beyondViewportPageCount = maxNonFocalVisibleItemCount,
            snapPosition = snapPosition,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            modifier = modifier
        ) { page ->
            val carouselItemInfo = remember { CarouselItemDrawInfoImpl() }
            val scope = remember { CarouselItemScopeImpl(itemInfo = carouselItemInfo) }
            val clipShape = remember {
                object : Shape {
                    override fun createOutline(
                        size: Size,
                        layoutDirection: LayoutDirection,
                        density: Density
                    ): Outline {
                        return Outline.Rectangle(carouselItemInfo.maskRect)
                    }
                }
            }

            Box(
                modifier =
                    Modifier.carouselItem(
                        index = page,
                        state = state,
                        strategy = { pageSize.strategy },
                        carouselItemDrawInfo = carouselItemInfo,
                        clipShape = clipShape
                    )
            ) {
                scope.content(page)
            }
        }
    }
}

@Composable
private fun PaddingValues.calculateAfterContentPadding(orientation: Orientation): Float {
    val dpValue =
        if (orientation == Orientation.Vertical) {
            calculateBottomPadding()
        } else {
            calculateEndPadding(LocalLayoutDirection.current)
        }

    return with(LocalDensity.current) { dpValue.toPx() }
}

@Composable
private fun PaddingValues.calculateBeforeContentPadding(orientation: Orientation): Float {
    val dpValue =
        if (orientation == Orientation.Vertical) {
            calculateTopPadding()
        } else {
            calculateStartPadding(LocalLayoutDirection.current)
        }

    return with(LocalDensity.current) { dpValue.toPx() }
}

