package com.heyanle.easybangumi4.v2.ui.source

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.ui.common.TabPage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.source_manage.explorePageIndex
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader

/** V2 source manager; existing source and repository ViewModels remain unchanged. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SourceManagerV2(initialPage: Int = -1) {
    val navController = LocalNavController.current
    val resolvedInitialPage = remember(initialPage) {
        (if (initialPage == -1) explorePageIndex else initialPage).coerceIn(0, 1)
    }
    val pagerState = rememberPagerState(initialPage = resolvedInitialPage) { 2 }
    var showRepositoryManageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        explorePageIndex = pagerState.currentPage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        V2SecondaryHeader(
            title = "番源管理",
            onBack = navController::popBackStack,
            largeTitle = true,
            actions = {
                if (pagerState.currentPage == 0) {
                    IconButton(onClick = { stringRes(R.string.long_touch_to_drag).moeSnackBar() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringRes(R.string.long_touch_to_drag),
                            tint = V2Tokens.TextPrimary,
                        )
                    }
                } else {
                    IconButton(onClick = { showRepositoryManageDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "添加仓库",
                            tint = V2Tokens.TextPrimary,
                        )
                    }
                }
            },
        )
        TabPage(
            pagerModifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            tabSize = 2,
            pagerState = pagerState,
            containerColor = V2Tokens.WarmBackground,
            contentColor = V2Tokens.TextPrimary,
            onTabSelect = { explorePageIndex = it },
            tabs = { page, _ ->
                Text(if (page == 0) "已安装" else "番源仓库")
            },
            contents = { page ->
                when (page) {
                    0 -> InstalledSourcesV2()
                    else -> RepositorySourcesV2()
                }
            },
        )
    }

    if (showRepositoryManageDialog) {
        RepositoryManageDialogV2(
            onDismiss = { showRepositoryManageDialog = false },
        )
    }
}
