package com.heyanle.easybangumi4.v2.ui.source

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import com.heyanle.easybangumi4.navigationSetting
import com.heyanle.easybangumi4.navigationSourceConfig
import com.heyanle.easybangumi4.plugin.api.IconSource
import com.heyanle.easybangumi4.plugin.api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.plugin.api.component.page.PageComponent
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.source.ConfigSource
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.plugin.source.SourceInfo
import com.heyanle.easybangumi4.plugin.source.js.source.getIconWithAsyncOrDrawable
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.moeDialogAlert
import com.heyanle.easybangumi4.ui.setting.SettingPage
import com.heyanle.easybangumi4.ui.source_manage.source.SourceViewModel
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.story.StoryEmptyV2
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
internal fun InstalledSourcesV2() {
    val navController = LocalNavController.current
    val viewModel = viewModel<SourceViewModel>()
    val sourceBundleController = LocalSourceBundleController.current
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> viewModel.move(from.index, to.index) },
        onDragEnd = { _, _ -> viewModel.onDragEnd() },
    )

    if (viewModel.configSourceList.isEmpty()) {
        StoryEmptyV2(
            title = "暂无已安装番源",
            subtitle = "可前往番源仓库添加番源",
        )
        return
    }

    LazyColumn(
        state = reorderState.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(reorderState)
            .detectReorderAfterLongPress(reorderState),
        contentPadding = PaddingValues(
            start = 0.dp,
            top = 0.dp,
            end = 0.dp,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(
            items = viewModel.configSourceList,
            key = { it.sourceInfo.source.key },
        ) { configSource ->
            ReorderableItem(
                reorderableState = reorderState,
                key = configSource.sourceInfo.source.key,
            ) { isDragging ->
                val sourceInfo = configSource.sourceInfo
                val canOpenConfig = sourceInfo.source.key == LocalSource.key ||
                    sourceBundleController.preference(sourceInfo.source.key) != null
                InstalledSourceCardV2(
                    configSource = configSource,
                    canOpenConfig = canOpenConfig,
                    isDragging = isDragging,
                    onCheckedChange = { enabled ->
                        if (enabled) viewModel.enable(configSource) else viewModel.disable(configSource)
                    },
                    onClick = {
                        when {
                            sourceInfo.source.key == LocalSource.key -> {
                                navController.navigationSetting(SettingPage.LocalSource)
                            }
                            sourceInfo is SourceInfo.Loaded &&
                                configSource.config.enable &&
                                sourceBundleController.preference(sourceInfo.source.key) != null -> {
                                navController.navigationSourceConfig(sourceInfo.source.key)
                            }
                            sourceInfo is SourceInfo.Error -> sourceInfo.msg.moeDialogAlert()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun InstalledSourceCardV2(
    configSource: ConfigSource,
    canOpenConfig: Boolean,
    isDragging: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val sourceInfo = configSource.sourceInfo
    val source = sourceInfo.source
    val iconSource = source as? IconSource

    Column {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            color = if (isDragging) V2Theme.colors.accentContainer else V2Tokens.WarmBackground,
            contentColor = V2Tokens.TextPrimary,
            tonalElevation = 0.dp,
        ) {
            Row(
            modifier = Modifier.padding(start = V2Tokens.ScreenHorizontalPadding, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                color = V2Theme.colors.accentContainer,
                shape = RoundedCornerShape(11.dp),
            ) {
                OkImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(11.dp)),
                    image = iconSource?.getIconWithAsyncOrDrawable(),
                    contentDescription = source.label,
                    crossFade = false,
                    placeholderColor = null,
                    errorColor = null,
                    placeholderRes = R.drawable.ic_source_default,
                    errorRes = R.drawable.ic_source_default,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = source.label,
                    color = V2Tokens.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = sourceInfo.statusTextV2(),
                    modifier = Modifier.padding(top = 4.dp),
                    color = if (sourceInfo is SourceInfo.Error) V2Tokens.Error else V2Tokens.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            when (sourceInfo) {
                is SourceInfo.Loaded -> {
                    if (canOpenConfig) {
                        IconButton(onClick = onClick) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "配置${source.label}",
                                tint = V2Tokens.TextSecondary,
                            )
                        }
                    }
                    SourceSwitchV2(
                        checked = configSource.config.enable,
                        onCheckedChange = onCheckedChange,
                    )
                }
                is SourceInfo.Disabled -> SourceSwitchV2(
                    checked = false,
                    onCheckedChange = onCheckedChange,
                )
                is SourceInfo.Error -> Unit
            }
        }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = V2Tokens.Divider)
    }
}

@Composable
private fun SourceSwitchV2(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = V2Tokens.Surface,
            checkedTrackColor = V2Theme.colors.accent,
            uncheckedThumbColor = V2Tokens.Surface,
            uncheckedTrackColor = V2Tokens.Divider,
            uncheckedBorderColor = V2Tokens.Divider,
        ),
    )
}

private fun SourceInfo.statusTextV2(): String {
    return when (this) {
        is SourceInfo.Loaded -> {
            val features = buildList {
                if (componentBundle.get(PageComponent::class) is PageComponent) add("首页")
                if (
                    componentBundle.get(PlayComponent::class) is PlayComponent &&
                    componentBundle.get(DetailedComponent::class) is DetailedComponent
                ) add("播放")
                if (componentBundle.get(SearchComponent::class) is SearchComponent) add("搜索")
            }
            listOf(source.version, *features.toTypedArray())
                .filter { it.isNotBlank() }
                .joinToString(" · ")
                .ifBlank { "已启用" }
        }
        is SourceInfo.Disabled -> "已停用"
        is SourceInfo.Error -> msg
    }
}
