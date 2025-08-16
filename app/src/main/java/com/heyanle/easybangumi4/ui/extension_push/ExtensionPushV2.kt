package com.heyanle.easybangumi4.ui.extension_push

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.TabIndicator
import com.heyanle.easybangumi4.ui.source_manage.ExplorePageItems
import com.heyanle.easybangumi4.ui.source_manage.explorePageIndex
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2025/8/16
 * https://github.com/heyanLE
 */

sealed class PushPage(
    val tabLabel: @Composable (() -> Unit),
    val content: @Composable (() -> Unit),
) {

    data object Repository : PushPage(
        tabLabel = {
            Text("仓库管理")
        },
        content = {
            ExtensionRepository()
        }
    )

    data object PushV1 : PushPage(
        tabLabel = {
            Text("手动添加")
        },
        content = {
            ExtensionPush(false)
        }
    )

}

val PushPageItems = listOf(
    PushPage.Repository,
    PushPage.PushV1,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExtensionPushV2() {
    val pagerState =
        rememberPagerState(initialPage = 0) {
            PushPageItems.size
        }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()

    val nav = LocalNavController.current


    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        TopAppBar(
            title = {
                Text(stringResource(R.string.extension_repository_manager))
            },
            navigationIcon = {
                IconButton(onClick = {
                    nav.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        stringResource(id = R.string.back)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            indicator = {
                TabIndicator(currentTabPosition = it[pagerState.currentPage])
            },
        ) {
            PushPageItems.forEachIndexed { index, explorePage ->
                Tab(selected = index == pagerState.currentPage,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                            explorePageIndex = index
                        }
                    },
                    text = {
                        explorePage.tabLabel()
                    })
            }
        }

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
        ) {
            val page = PushPageItems[it]
            page.content()
        }

    }
}