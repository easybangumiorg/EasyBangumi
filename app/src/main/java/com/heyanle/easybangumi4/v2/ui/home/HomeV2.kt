package com.heyanle.easybangumi4.v2.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.navigationSourceManager
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.plugin.source.js.source.getIconWithAsyncOrDrawable
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.page.CartoonPageUI
import com.heyanle.easybangumi4.ui.main.home.HomeViewModel
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ScrollableTabs
import com.heyanle.easybangumi4.ui.common.page.CartoonPagePresentation

/** First fully owned V2 page. Business state and page ViewModels remain unchanged. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeV2() {
    val viewModel = viewModel<HomeViewModel>()
    val state by viewModel.stateFlow.collectAsState()
    val navController = LocalNavController.current
    var showSourceSelector by remember { mutableStateOf(false) }

    if (showSourceSelector) {
        HomeSourceSelectorV2(
            selectedSourceKey = state.selectionKey,
            onSourceSelected = { key ->
                viewModel.changeSelectionSource(key)
                showSourceSelector = false
            },
            onManageSources = {
                showSourceSelector = false
                navController.navigationSourceManager(1)
            },
            onDismissRequest = { showSourceSelector = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground),
    ) {
        HomeHeaderV2(
            sourceLabel = state.topAppBarTitle,
            onSourceClick = { showSourceSelector = true },
            onSearchClick = { navController.navigationSearch(state.selectionKey) },
        )

        when {
            state.isLoading -> LoadingPage(
                modifier = Modifier.fillMaxSize(),
                loadingMsg = "正在加载番源",
            )

            !state.hasPageComponent -> HomeEmptyStateV2(
                message = "暂无可用番源",
                actionLabel = "选择番源",
                onAction = { navController.navigationSourceManager(1) },
            )

            state.pages.isEmpty() -> HomeEmptyStateV2(
                message = "这里还没有内容",
                actionLabel = "重新选择番源",
                onAction = { showSourceSelector = true },
            )

            else -> {
                if (state.isShowLabel) {
                    V2ScrollableTabs(
                        labels = state.pages.map { it.label },
                        selectedIndex = state.selectionIndex,
                        onSelected = viewModel::changeSelectionPage,
                    )
                    HorizontalDivider(color = V2Tokens.Divider)
                }

                AnimatedContent(
                    targetState = state.pages.getOrNull(state.selectionIndex),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    transitionSpec = {
                        fadeIn(tween(durationMillis = 220)) togetherWith
                            fadeOut(tween(durationMillis = 120))
                    },
                    label = "v2-home-page",
                ) { page ->
                    page?.let {
                        CompositionLocalProvider(
                            LocalViewModelStoreOwner provides viewModel.getViewModelStoreOwner(it),
                        ) {
                            CartoonPageUI(
                                cartoonPage = it,
                                presentation = CartoonPagePresentation.V2,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeaderV2(
    sourceLabel: String,
    onSourceClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = V2Tokens.ScreenHorizontalPadding,
                top = 18.dp,
                end = 8.dp,
                bottom = 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onSourceClick)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.SyncAlt,
                contentDescription = "切换番源",
                tint = V2Tokens.TextPrimary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(14.dp))
            Text(
                text = sourceLabel.ifBlank { "选择番源" },
                color = V2Tokens.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "搜索",
                tint = V2Tokens.TextPrimary,
            )
        }
    }
}

@Composable
private fun HomeEmptyStateV2(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = V2Tokens.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "可在番源管理中添加或启用内容来源",
            color = V2Tokens.TextSecondary,
            fontSize = 14.sp,
        )
        Spacer(Modifier.size(20.dp))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = V2Theme.colors.accent,
                contentColor = V2Theme.colors.onAccent,
            ),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(actionLabel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSourceSelectorV2(
    selectedSourceKey: String,
    onSourceSelected: (String) -> Unit,
    onManageSources: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sources = LocalSourceBundleController.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = V2Tokens.Surface,
        contentColor = V2Tokens.TextPrimary,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Text(
            text = "选择番源",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            color = V2Tokens.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(color = V2Tokens.Divider)
        LazyColumn {
            items(
                HomeViewModel.prioritizeHomeSources(sources.pages()),
                key = { it.source.key },
            ) { page ->
                ListItem(
                    modifier = Modifier.clickable {
                        onSourceSelected(page.source.key)
                    },
                    headlineContent = { Text(page.source.label) },
                    leadingContent = {
                        val icon = remember(page.source.key) {
                            sources.icon(page.source.key)
                        }
                        OkImage(
                            modifier = Modifier.size(32.dp),
                            image = icon?.getIconWithAsyncOrDrawable(),
                            contentDescription = page.source.label,
                            placeholderRes = R.drawable.ic_source_default,
                            errorRes = R.drawable.ic_source_default,
                        )
                    },
                    trailingContent = {
                        RadioButton(
                            selected = selectedSourceKey == page.source.key,
                            onClick = { onSourceSelected(page.source.key) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = V2Theme.colors.accent,
                            ),
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = V2Tokens.Surface),
                )
            }
            item {
                HorizontalDivider(color = V2Tokens.Divider)
                ListItem(
                    modifier = Modifier.clickable(onClick = onManageSources),
                    headlineContent = { Text("管理番源") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Extension,
                            contentDescription = null,
                            tint = V2Theme.colors.accent,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = V2Tokens.Surface),
                )
            }
        }
    }
}
