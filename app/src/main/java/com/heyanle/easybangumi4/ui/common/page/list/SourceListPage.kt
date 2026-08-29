package com.heyanle.easybangumi4.ui.common.page.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ripple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.plugin.api.component.page.SourcePage
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.entity.toIdentify
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.CartoonCardWithoutCover
import com.heyanle.easybangumi4.ui.common.PagingCommon
import com.heyanle.easybangumi4.ui.common.commonShow
import com.heyanle.easybangumi4.ui.common.pagingCommon
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.common.page.CartoonPagePresentation
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import io.ktor.http.headersOf

/**
 * Created by heyanlin on 2024/2/9 10:29.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceListPage(
    coverStarVm: CoverStarViewModel,
    pageList: List<SourcePage.SingleCartoonPage>,
    lazyGridState: LazyGridState,
    lazyStaggeredGridState: LazyStaggeredGridState,
    presentation: CartoonPagePresentation = CartoonPagePresentation.Legacy,
) {
    val star = coverStarVm.stateFlow.collectAsState().value.identifySet
    val nav = LocalNavController.current
    val haptic = LocalHapticFeedback.current
    val vm =
        viewModel<SourceGroupListViewModel>(factory = SourceGroupListViewModelFactory(pageList))
    val paging = remember(vm.selected.intValue) {
        val index = vm.selected.intValue
        if (vm.pageList.isNotEmpty() && (index >= vm.pageList.size || index < 0)) {
            vm.selected.intValue = 0
            null
        } else if (vm.pageList.isNotEmpty()) {
            vm.pageList[vm.selected.intValue]
        } else {
            null
        }
    }
    val pagingItems = paging?.second?.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = vm.selected.intValue)

    if (presentation == CartoonPagePresentation.V2) {
        Column(modifier = Modifier.fillMaxSize()) {
            SourceListGroupTab(
                list = pageList,
                curPage = vm.selected.intValue,
                lazyListState = lazyListState,
                presentation = presentation,
                onClick = { vm.selected.intValue = it },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (paging?.first is SourcePage.SingleCartoonPage.WithCover) {
                    LazyVerticalGrid(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyGridState,
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 88.dp),
                    ) {
                        pagingItems?.let { items ->
                            listPageWithCover(
                                pagingItems = items,
                                starSet = star,
                                onClick = { nav.navigationDetailed(it) },
                                onLongPress = {
                                    coverStarVm.dispatchStar(it)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                presentation = presentation,
                            )
                            pagingCommon(items)
                        }
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(150.dp),
                        state = lazyStaggeredGridState,
                        verticalItemSpacing = 4.dp,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        pagingItems?.let { items ->
                            listPageWithoutCover(
                                pagingItems = items,
                                starSet = star,
                                onClick = { nav.navigationDetailed(it) },
                                onLongPress = {
                                    coverStarVm.dispatchStar(it)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            )
                            pagingCommon(items)
                        }
                    }
                }
                pagingItems?.let { PagingCommon(items = it) }
            }
        }
        return
    }

    if (paging?.first is SourcePage.SingleCartoonPage.WithCover) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            state = lazyGridState,
            columns = if (presentation == CartoonPagePresentation.V2) GridCells.Fixed(3) else GridCells.Adaptive(100.dp),
            verticalArrangement = Arrangement.spacedBy(if (presentation == CartoonPagePresentation.V2) 14.dp else 4.dp),
            horizontalArrangement = Arrangement.spacedBy(
                if (presentation == CartoonPagePresentation.V2) 10.dp else 4.dp,
                Alignment.CenterHorizontally,
            ),
            contentPadding = if (presentation == CartoonPagePresentation.V2) {
                PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 88.dp)
            } else {
                PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            },
        ) {
            if (pagingItems?.commonShow() != true) {
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    SourceListGroupTab(
                        list = pageList,
                        curPage = vm.selected.intValue,
                        lazyListState = lazyListState,
                        presentation = presentation,
                        onClick = {
                            vm.selected.intValue = it
                        }
                    )
                }
            }
            pagingItems?.let { pagingItems ->
                listPageWithCover(
                    pagingItems,
                    star,
                    onClick = {
                        nav.navigationDetailed(it)
                    },
                    onLongPress = {
                        coverStarVm.dispatchStar(it)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    presentation = presentation,
                )
                pagingCommon(pagingItems)
            }

        }


    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            state = lazyStaggeredGridState,
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (pagingItems?.commonShow() != true) {
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    SourceListGroupTab(
                        list = pageList,
                        curPage = vm.selected.intValue,
                        lazyListState = lazyListState,
                            presentation = presentation,
                            onClick = {
                            vm.selected.intValue = it
                        }
                    )
                }
            }

            pagingItems?.let {
                listPageWithoutCover(
                    pagingItems,
                    star,
                    onClick = {
                        nav.navigationDetailed(it)
                    },
                    onLongPress = {
                        coverStarVm.dispatchStar(it)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
                pagingCommon(pagingItems)
            }
        }
    }

    pagingItems?.let {
        PagingCommon(items = it){
            Spacer(Modifier.size(4.dp))
            SourceListGroupTab(
                list = pageList,
                curPage = vm.selected.intValue,
                lazyListState = lazyListState,
                onClick = {
                    vm.selected.intValue = it
                },
                presentation = presentation,
            )
        }
    }


}

@Composable
fun SourceListPage(
    coverStarVm: CoverStarViewModel,
    page: SourcePage.SingleCartoonPage,
    lazyGridState: LazyGridState,
    lazyStaggeredGridState: LazyStaggeredGridState,
    vm: SourceListViewModel,
    presentation: CartoonPagePresentation = CartoonPagePresentation.Legacy,
) {
    val star = coverStarVm.stateFlow.collectAsState().value.identifySet
    val nav = LocalNavController.current
    val haptic = LocalHapticFeedback.current
    val pagingItems = vm.curPager.value.collectAsLazyPagingItems()

    if (page is SourcePage.SingleCartoonPage.WithCover) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize(),
            state = lazyGridState,
            columns = if (presentation == CartoonPagePresentation.V2) GridCells.Fixed(3) else GridCells.Adaptive(100.dp),
            verticalArrangement = Arrangement.spacedBy(if (presentation == CartoonPagePresentation.V2) 14.dp else 4.dp),
            horizontalArrangement = Arrangement.spacedBy(
                if (presentation == CartoonPagePresentation.V2) 10.dp else 4.dp,
                Alignment.CenterHorizontally,
            ),
            contentPadding = if (presentation == CartoonPagePresentation.V2) {
                PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 88.dp)
            } else {
                PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
            },
        ) {
            listPageWithCover(
                pagingItems,
                star,
                onClick = {
                    nav.navigationDetailed(it)
                },
                onLongPress = {
                    coverStarVm.dispatchStar(it)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                presentation = presentation,
            )
            pagingCommon(pagingItems)
        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(150.dp),
            state = lazyStaggeredGridState,
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            listPageWithoutCover(
                pagingItems,
                star,
                onClick = {
                    nav.navigationDetailed(it)
                },
                onLongPress = {
                    coverStarVm.dispatchStar(it)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
            pagingCommon(pagingItems)
        }
    }


    PagingCommon(items = pagingItems, extMsg = "\n可尝试点击左上角切换其他源")
}

@Composable
fun SourceListGroupTab(
    list: List<SourcePage.SingleCartoonPage>,
    curPage: Int,
    lazyListState: LazyListState,
    presentation: CartoonPagePresentation = CartoonPagePresentation.Legacy,
    onClick: (Int) -> Unit,
) {
    //val state = rememberLazyListState(initialFirstVisibleItemIndex = curPage)

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (presentation == CartoonPagePresentation.V2) {
            Arrangement.spacedBy(24.dp)
        } else {
            Arrangement.Center
        },
        contentPadding = if (presentation == CartoonPagePresentation.V2) PaddingValues(horizontal = 12.dp) else PaddingValues(0.dp),
        state = lazyListState
    ) {
        itemsIndexed(list) { index, item ->
            val selected = index == curPage
            if (presentation == CartoonPagePresentation.V2) {
                val dotAlpha by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = tween(180),
                    label = "v2-source-secondary-dot",
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) V2Tokens.TextPrimary else V2Tokens.TextSecondary,
                    animationSpec = tween(180),
                    label = "v2-source-secondary-color",
                )
                val interactionSource = remember { MutableInteractionSource() }
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true),
                        ) { onClick(index) }
                        .padding(horizontal = 4.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(6.dp)
                            .background(V2Theme.colors.accent.copy(alpha = dotAlpha), CircleShape),
                    )
                    Text(
                        text = item.label,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                }
                return@itemsIndexed
            }
            Surface(
                shape = CircleShape,
                modifier =
                Modifier
                    .padding(2.dp, 8.dp),
                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            ) {
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onClick(index)
                        }
                        .padding(8.dp, 0.dp),
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.W900,
                    text = item.label,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

fun LazyGridScope.listPageWithCover(
    pagingItems: LazyPagingItems<CartoonCover>,
    starSet: Set<String>,
    onClick: (CartoonCover) -> Unit,
    onLongPress: (CartoonCover) -> Unit,
    presentation: CartoonPagePresentation = CartoonPagePresentation.Legacy,
) {
    items(
        count = pagingItems.itemCount,
        key = { index -> pagingItems.peek(index)?.toIdentify() ?: index },
    ) {
        pagingItems[it]?.let { cover ->
            CartoonCardWithCover(
                modifier = Modifier.fillMaxWidth(),
                star = starSet.contains(cover.toIdentify()),
                cartoonCover = cover,
                onClick = onClick,
                onLongPress = onLongPress,
                v2Presentation = presentation == CartoonPagePresentation.V2,
            )
        }
    }
}

fun LazyStaggeredGridScope.listPageWithoutCover(
    pagingItems: LazyPagingItems<CartoonCover>,
    starSet: Set<String>,
    onClick: (CartoonCover) -> Unit,
    onLongPress: (CartoonCover) -> Unit,
) {
    items(
        count = pagingItems.itemCount,
        key = { index -> pagingItems.peek(index)?.toIdentify() ?: index },
    ) {
        pagingItems[it]?.let { cover ->
            CartoonCardWithoutCover(
                cartoonCover = cover,
                star = starSet.contains(cover.toIdentify()),
                onClick = onClick,
                onLongPress = onLongPress,
            )
        }
    }
}
