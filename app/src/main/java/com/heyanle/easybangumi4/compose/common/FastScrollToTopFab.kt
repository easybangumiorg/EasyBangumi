package com.heyanle.easybangumi4.compose.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Created by LoliBall on 2022/2/9 0:05.
 * https://github.com/WhichWho
 */
@Composable
fun FastScrollToTopFab(
    listState: LazyListState,
    after: Int = 10,
    padding: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    EasyFab(
        state = remember { derivedStateOf { listState.firstVisibleItemIndex > after } },
        padding = padding
    ) {
        scope.launch {
            listState.animateScrollToItem(0, 0)
            onClick()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FastScrollToTopFab(
    listState: LazyStaggeredGridState,
    after: Int = 10,
    padding: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    EasyFab(
        state = remember { derivedStateOf { listState.firstVisibleItemIndex > after } },
        padding = padding
    ) {
        scope.launch {
            listState.animateScrollToItem(0, 0)
            onClick()
        }
    }
}

@Composable
fun FastScrollToTopFab(
    listState: LazyGridState,
    after: Int = 10,
    padding: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    EasyFab(
        state = remember { derivedStateOf { listState.firstVisibleItemIndex > after } },
        padding = padding
    ) {
        scope.launch {
            listState.animateScrollToItem(0, 0)
            onClick()
        }
    }
}

@Composable
fun EasyFab(
    state: State<Boolean>,
    padding: PaddingValues = PaddingValues(0.dp),
    icon: @Composable () -> Unit = {
        androidx.compose.material3.Icon(
            Icons.Filled.KeyboardArrowUp,
            contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.click_to_up_top),
            tint = MaterialTheme.colorScheme.onSecondary
        )
    },
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = state.value,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight + 200 },
            animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight + 200 },
            animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
        )
    ) {
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = {
                    onClick()
                },
                modifier = Modifier
                    .padding(20.dp)
                    .padding(padding),
                containerColor = MaterialTheme.colorScheme.secondary,
                elevation = FloatingActionButtonDefaults.elevation(16.dp)
            ) {
                icon()
            }
        }
    }
}