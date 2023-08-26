package com.heyanle.easybangumi4.compose.main.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.compose.common.OkImage
import com.heyanle.easybangumi4.compose.common.page.CartoonPageListTab
import com.heyanle.easybangumi4.compose.common.page.CartoonPageUI
import com.heyanle.easybangumi4.compose.main.MainViewModel
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/25 15:47.
 * https://github.com/heyanLE
 */
@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun Home() {

    val vm = viewModel<HomeViewModel>()
    val mainVM = viewModel<MainViewModel>()
    val nav = LocalNavController.current

    val state by vm.stateFlow.collectAsState()

    val scope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(key1 = state.selectionKey){
        scrollBehavior.state.contentOffset = 0F
    }

    val showChangeSheet = remember {
        mutableStateOf(false)
    }

    if(showChangeSheet.value){
        ModalBottomSheet(
            scrimColor = Color.Black.copy(alpha = 0.32f),
            onDismissRequest = {
                showChangeSheet.value = false
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                3.dp
            ),
            content = {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface
                ) {
                    Column(
                    ) {
                        Box(
                            Modifier
                                .padding(vertical = 10.dp)
                                .width(32.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .alpha(0.4f)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                                .align(Alignment.CenterHorizontally)
                        )
                        HomeBottomSheet {
                            showChangeSheet.value = false
                        }
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }

                }

            })

    }

    Column {
        HomeTopAppBar(
            scrollBehavior = scrollBehavior,
            title = state.topAppBarTitle,
            onChangeClick = {
                scope.launch {
                    showChangeSheet.value = true
                }
            },
            onSearchClick = { nav.navigationSearch(state.selectionKey) }
        )

        if (state.isShowLabel) {
            CartoonPageListTab(
                state.pages,
                selectionIndex = state.selectionIndex,
                onPageClick = {
                    vm.changeSelectionPage(it)
                }
            )

            Divider()
        }
        
        AnimatedContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .let {
                    if (!state.isShowLabel) {
                        it.nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        it
                    }
                },
            targetState = kotlin.runCatching { state.pages[state.selectionIndex] }.getOrNull(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300, delayMillis = 300)) togetherWith
                        fadeOut(animationSpec = tween(300, delayMillis = 0))
            }, label = ""
        ) {
            it?.let {
                val listVmOwner = vm.getViewModelStoreOwner(it)
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides listVmOwner
                ) {
                    CartoonPageUI(cartoonPage = it)
                }
            }
        }
    }
//    HomeBottomSheet(sheetState = sheetState, defSourceKey = state.selectionKey, onSourceClick = {
//        vm.changeSelectionSource(it)
//    })
}

@Composable
fun HomeBottomSheet(
    onDismissRequest: () -> Unit,
) {
    val animSources = LocalSourceBundleController.current
    val vm = viewModel<HomeViewModel>()

    val state = vm.stateFlow.collectAsState()

    val scope = rememberCoroutineScope()

    ListItem(
        headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.choose_source)) },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
    )
    Divider()
    LazyColumn() {
        items(animSources.pages()) { page ->
            ListItem(
                modifier = Modifier.clickable {
                    vm.changeSelectionSource(page.source.key)
                    scope.launch {
                        onDismissRequest()
                    }
                },
                headlineContent = { Text(text = page.source.label) },
                leadingContent = {
                    val icon = remember {
                        animSources.icon(page.source.key)
                    }
                    OkImage(
                        modifier = Modifier.size(32.dp),
                        image = icon?.getIconFactory()?.invoke(),
                        contentDescription = page.source.label
                    )
                },
                trailingContent = {
                    RadioButton(
                        selected = state.value.selectionKey == page.source.key,
                        onClick = {
                            vm.changeSelectionSource(page.source.key)
                            scope.launch {
                                onDismissRequest()
                            }
                        })
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior?,
    onChangeClick: () -> Unit,
    onSearchClick: () -> Unit,
) {

    TopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = { onChangeClick() }) {
                Icon(
                    Icons.Filled.Sync,
                    stringResource(id = com.heyanle.easy_i18n.R.string.source)
                )
            }
        },
        title = { Text(text = title) },
        actions = {
            IconButton(onClick = { onSearchClick() }) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.search)
                )
            }
        }
    )
}