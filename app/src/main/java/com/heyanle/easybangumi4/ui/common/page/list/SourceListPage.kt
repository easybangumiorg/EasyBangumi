package com.heyanle.easybangumi4.ui.common.page.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.heyanle.easybangumi4.ui.common.pagingCommon
import com.heyanle.easybangumi4.ui.common.cover_star.CoverStarViewModel
import com.heyanle.easybangumi4.ui.common.page.CartoonPageListTab
import com.heyanle.easybangumi4.ui.common.page.CartoonPagePresentation
import com.heyanle.easybangumi4.ui.common.page.SourcePageTabRole
import com.heyanle.easybangumi4.v2.ui.component.V2ScrollableTabs
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens

/**
 * Created by heyanlin on 2024/2/9 10:29.
 */
@Composable
fun SourceListPage(
    coverStarVm: CoverStarViewModel,
    pageList: List<SourcePage.SingleCartoonPage>,
    lazyGridState: LazyGridState,
    lazyStaggeredGridState: LazyStaggeredGridState,
    presentation: CartoonPagePresentation = CartoonPagePresentation.Legacy,
    tabRole: SourcePageTabRole = SourcePageTabRole.Secondary,
) {
    val star = coverStarVm.stateFlow.collectAsState().value.identifySet
    val nav = LocalNavController.current
    val haptic = LocalHapticFeedback.current
    val vm = viewModel<SourceGroupListViewModel>(factory = SourceGroupListViewModelFactory(pageList))
    val selected = if (vm.pageList.isEmpty()) 0 else vm.selected.intValue.coerceIn(vm.pageList.indices)
    val paging = vm.pageList.getOrNull(selected)
    val pagingItems = paging?.second?.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val isV2 = presentation == CartoonPagePresentation.V2

    Column(modifier = Modifier.fillMaxSize()) {
        // Navigation is outside the grid so its spacing and visibility do not depend on cards
        // or on whether the current page is loading, empty, or failed.
        SourceListGroupTab(
            list = pageList,
            curPage = selected,
            lazyListState = lazyListState,
            presentation = presentation,
            role = tabRole,
            onClick = { vm.selected.intValue = it },
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (paging?.first is SourcePage.SingleCartoonPage.WithCover) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyGridState,
                    columns = GridCells.Adaptive(100.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isV2) 14.dp else 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isV2) 10.dp else 4.dp, Alignment.CenterHorizontally),
                    contentPadding = if (isV2) {
                        PaddingValues(
                            start = 12.dp,
                            top = if (tabRole == SourcePageTabRole.Secondary) 4.dp else 12.dp,
                            end = 12.dp,
                            bottom = 88.dp,
                        )
                    } else PaddingValues(4.dp, 4.dp, 4.dp, 88.dp),
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
            columns = GridCells.Adaptive(100.dp),
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
    role: SourcePageTabRole = SourcePageTabRole.Secondary,
    onClick: (Int) -> Unit,
) {
    if (role == SourcePageTabRole.Hidden || list.size <= 1) return
    val selectedIndex = curPage.coerceIn(list.indices)
    val isV2 = presentation == CartoonPagePresentation.V2
    if (role == SourcePageTabRole.Primary) {
        if (isV2) {
            V2ScrollableTabs(
                labels = list.map { it.label },
                selectedIndex = selectedIndex,
                onSelected = onClick,
            )
        } else {
            CartoonPageListTab(
                cartoonPage = list,
                selectionIndex = selectedIndex,
                onPageClick = onClick,
            )
        }
        HorizontalDivider(color = if (isV2) V2Tokens.Divider else MaterialTheme.colorScheme.outlineVariant)
        return
    }

    LaunchedEffect(selectedIndex, list.size) {
        lazyListState.animateScrollToItem(selectedIndex)
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        state = lazyListState,
    ) {
        itemsIndexed(list) { index, item ->
            if (isV2) {
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { onClick(index) },
                    label = { Text(text = item.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.Transparent,
                        labelColor = V2Tokens.TextSecondary,
                        selectedContainerColor = V2Theme.colors.accentContainer,
                        selectedLabelColor = V2Theme.colors.accent,
                    ),
                )
            } else {
                val selected = index == selectedIndex
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                ) {
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onClick(index) }
                            .padding(horizontal = 8.dp),
                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.W900,
                        text = item.label,
                        fontSize = 12.sp,
                    )
                }
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
