package com.heyanle.easy_bangumi_cm.common.foundation.plugin.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easy_bangumi_cm.base.service.system.logger
import com.heyanle.easy_bangumi_cm.common.foundation.elements.EmptyElements
import com.heyanle.easy_bangumi_cm.common.foundation.elements.ErrorElements
import com.heyanle.easy_bangumi_cm.common.foundation.elements.LoadingElements
import com.heyanle.easy_bangumi_cm.common.foundation.view_model.easyVM
import com.heyanle.easy_bangumi_cm.common.resources.Res
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeContent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage
import dev.icerock.moko.resources.compose.stringResource

/**
 * Created by heyanlin on 2025/3/3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeComponentContent(
    content: HomeContent,
    scrollBehavior: TopAppBarScrollBehavior,
    lazyGridState: LazyGridState,
    columns: GridCells,
) {
    val homeContentViewModel = easyVM<HomeContentViewModel>(content)
    val uiState = homeContentViewModel.uiState.value
    val page = uiState.homePage
    if (uiState.isLoading) {
        LoadingElements(
            modifier = Modifier.fillMaxSize(),
            isRow = false
        )
    } else if (uiState.isEmpty() || page == null) {
        EmptyElements(
            modifier = Modifier.fillMaxSize(),
            isRow = false
        )
    } else {
        Column {
            // Tab
            val tabState = uiState.tabState
            if (tabState != null) {
                LazyRow {
                    itemsIndexed(tabState.first) { index, item ->
                        val select = tabState.second == index
                        FilterChip(
                            selected = select,
                            onClick = {
                                homeContentViewModel.select(index)
                            },
                            label = { Text(text = item) },
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                homeContentViewModel.child(page) {
                    HomeComponentPage(
                        homePage = page,
                        scrollBehavior = if (content is HomeContent.Single) scrollBehavior else null,
                        lazyGridState = lazyGridState,
                        columns = columns
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeComponentPage(
    homePage: HomePage,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    lazyGridState: LazyGridState,
    columns: GridCells,
) {
    val homePageViewModel = easyVM<HomePageViewModel>(homePage)
    when (
        val uiState = homePageViewModel.uiState.value
    ) {
        HomePageViewModel.UIState.Loading -> {
            LoadingElements(
                modifier = Modifier.fillMaxSize(),
                isRow = false
            )
        }

        HomePageViewModel.UIState.Empty -> {
            EmptyElements(
                modifier = Modifier.fillMaxSize(),
                isRow = false
            )
        }

        is HomePageViewModel.UIState.Error -> {
            ErrorElements(
                modifier = Modifier.fillMaxSize(),
                isRow = false,
                errorMsg = uiState.errorMsg,
                onClick = {
                    homePageViewModel.refresh()
                },
                other = {
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = stringResource(Res.strings.retry),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    )
                }
            )
        }

        is HomePageViewModel.UIState.Success -> {
            val tabState = uiState.tabState
            // Page
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize().let {
                    if (scrollBehavior == null) {
                        it
                    } else {
                        it.nestedScroll(scrollBehavior.nestedScrollConnection)
                    }
                },
                columns = columns,
                state = lazyGridState,
            ) {

                // tab
                if (tabState != null) {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            state = rememberLazyListState()
                        ) {
                            itemsIndexed(tabState.first) { index, item ->
                                val selected = index == tabState.second
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
                                                homePageViewModel.select(index)
                                            }
                                            .padding(8.dp, 0.dp),
                                        color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.W900,
                                        text = item,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
