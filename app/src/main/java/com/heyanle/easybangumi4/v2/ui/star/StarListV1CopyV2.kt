package com.heyanle.easybangumi4.v2.ui.star

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.ui.common.CartoonStarCardWithCover
import com.heyanle.easybangumi4.ui.common.EmptyPage
import com.heyanle.easybangumi4.ui.common.FastScrollToTopFab
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** V2-owned copy of the V1 followed-cartoon grid, with V2 colors and card presentation. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun StarListV1CopyV2(
    cartoons: List<CartoonInfo>,
    selection: Set<CartoonInfo>,
    onRefresh: () -> Unit,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: (CartoonInfo) -> Unit,
    nestedScrollConnection: NestedScrollConnection? = null,
) {
    val gridState = rememberLazyGridState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val refreshing = remember { mutableStateOf(false) }
    val refreshState = rememberPullRefreshState(refreshing = refreshing.value, onRefresh = {
        scope.launch {
            refreshing.value = true
            onRefresh()
            delay(500)
            refreshing.value = false
        }
    })
    Box(Modifier.fillMaxSize().pullRefresh(refreshState)) {
        LazyVerticalGrid(
            state = gridState,
            modifier = Modifier.fillMaxSize().let { base ->
                nestedScrollConnection?.let(base::nestedScroll) ?: base
            },
            columns = GridCells.Adaptive(100.dp),
            contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (cartoons.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { EmptyPage(Modifier.height(256.dp)) }
            }
            items(cartoons, key = { "${it.source}:${it.id}" }) { cartoon ->
                CartoonStarCardWithCover(
                    selected = cartoon in selection,
                    cartoon = cartoon,
                    showSourceLabel = true,
                    showWatchProcess = true,
                    showIsUp = true,
                    showIsUpdate = true,
                    onClick = onClick,
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress(it)
                    },
                    v2Presentation = true,
                )
            }
        }
        PullRefreshIndicator(
            refreshing.value,
            refreshState,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = V2Tokens.SurfaceMuted,
            contentColor = V2Theme.colors.accent,
        )
        FastScrollToTopFab(listState = gridState)
    }
}
