//package org.easybangumi.next.shared.foundation.home
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.grid.*
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.clipToBounds
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
//import androidx.compose.ui.input.nestedscroll.nestedScroll
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import app.cash.paging.compose.collectAsLazyPagingItems
//import org.easybangumi.next.shared.foundation.ScrollableHeaderBehavior
//import org.easybangumi.next.shared.foundation.ScrollableHeaderScaffold
//import org.easybangumi.next.shared.foundation.cartoon.CartoonCardWithCover
//import org.easybangumi.next.shared.foundation.elements.LoadScaffold
//import org.easybangumi.next.shared.foundation.lazy.PagingCommon
//import org.easybangumi.next.shared.foundation.lazy.pagingCommon
//import org.easybangumi.next.shared.foundation.view_model.easyVM
//
///**
// * Created by heyanlin on 2025/3/3.
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeComponentContent(
//    content: HomeContent,
//    nestedScrollConnection: NestedScrollConnection? = null,
//    columns: GridCells,
//) {
//    val homeContentViewModel = easyVM<HomeContentViewModel>(content)
//    val uiState = homeContentViewModel.uiState.value
//    LoadScaffold(
//        modifier = Modifier.fillMaxSize(),
//        data = uiState
//    ) {
//        Column {
//            val tabState = it.data.tabState
//            val page = it.data.homePage
//            if (tabState != null) {
//                LazyRow {
//                    itemsIndexed(tabState.first) { index, item ->
//                        val select = tabState.second == index
//                        FilterChip(
//                            selected = select,
//                            onClick = {
//                                homeContentViewModel.select(index)
//                            },
//                            label = { Text(text = item) },
//                        )
//                    }
//                }
//                HorizontalDivider(modifier = Modifier.fillMaxWidth())
//            }
//            Box(
//                modifier = Modifier.fillMaxWidth().weight(1f)
//            ) {
//                homeContentViewModel.child(it.data.homePage) {
//                    HomeComponentPage(
//                        homePage = page,
//                        nestedScrollConnection = if (content is HomeContent.Single) nestedScrollConnection else null,
//                        columns = columns
//                    )
//                }
//            }
//        }
//
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeComponentPage(
//    homePage: HomePage,
//    nestedScrollConnection: NestedScrollConnection? = null,
//    columns: GridCells,
//) {
//    val homePageViewModel = easyVM<HomePageViewModel>(homePage)
//    val uiState = homePageViewModel.uiState.value
//    LoadScaffold(
//        Modifier.fillMaxSize(),
//        data = uiState
//    ) {
//        val tabState = it.data.tabState
//        val headerBehavior = if (tabState == null)
//            null
//        else ScrollableHeaderBehavior.enterAlwaysScrollBehavior(state = homePageViewModel.getScrollableHeaderState(it.data.cartoonPage))
//
//        LaunchedEffect(it.data.cartoonPage) {
//            headerBehavior?.state?.offset = 0f
//        }
//
//        ScrollableHeaderScaffold(
//            modifier = Modifier.fillMaxSize().clipToBounds()
//                .let { if (nestedScrollConnection == null) it else it.nestedScroll(nestedScrollConnection) },
//            behavior = headerBehavior,
//            headerIfBehavior = {
//                if (tabState != null) {
//                    LazyRow(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically,
//                        state = rememberLazyListState()
//                    ) {
//
//                        itemsIndexed(tabState.first) { index, item ->
//                            val selected = index == tabState.second
//                            Surface(
//                                shape = CircleShape,
//                                modifier =
//                                    Modifier
//                                        .padding(2.dp, 8.dp),
//                                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
//                            ) {
//                                Text(
//                                    modifier = Modifier
//                                        .clip(CircleShape)
//                                        .clickable {
//                                            homePageViewModel.select(index)
//                                        }
//                                        .padding(8.dp, 0.dp),
//                                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
//                                    fontWeight = FontWeight.W900,
//                                    text = item,
//                                    fontSize = 12.sp,
//                                )
//                            }
//                        }
//                    }
//                }
//            },
//            content = { contentPadding ->
//                val cartoonPage = it.data.cartoonPage
//                val lazyGridState = homePageViewModel.getLazyGridState(cartoonPage)
//                homePageViewModel.child(cartoonPage) {
//                    HomeCartoonPage(
//                        cartoonPage,
//                        lazyGridState,
//                        contentPadding,
//                        columns
//                    )
//                }
//            }
//        )
//
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeCartoonPage(
//    cartoonPage: CartoonPage,
//    lazyGridState: LazyGridState,
//    contentPaddingValues: PaddingValues,
//    columns: GridCells,
//) {
//
//    val cartoonPageViewModel = easyVM<CartoonPageViewModel>(cartoonPage)
//    val pagingFlow = cartoonPageViewModel.pageState
//    val lazyPageItem = pagingFlow.value.collectAsLazyPagingItems()
//
//    LazyVerticalGrid(
//        state = lazyGridState,
//        columns = columns,
//        contentPadding = contentPaddingValues,
//    ) {
//        items(lazyPageItem.itemCount) {
//            val item = lazyPageItem[it]
//            if (item != null) {
//                CartoonCardWithCover(
//                    cartoonCover = item,
//                    onClick = {
//                        logger.i("HomeComponentUI", "click ${it.name}")
//                    }
//                )
//            }
//
//        }
//        pagingCommon(lazyPageItem)
//    }
//
//    Box(modifier = Modifier.padding(contentPaddingValues)) {
//        PagingCommon(lazyPageItem)
//    }
//
//}
