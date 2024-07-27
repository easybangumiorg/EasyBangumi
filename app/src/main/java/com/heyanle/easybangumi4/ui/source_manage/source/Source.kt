package com.heyanle.easybangumi4.ui.source_manage.source

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationSourceConfig
import com.heyanle.easybangumi4.plugin.source.ConfigSource
import com.heyanle.easybangumi4.plugin.source.LocalSourceBundleController
import com.heyanle.easybangumi4.plugin.source.SourceInfo
import com.heyanle.easybangumi4.source_api.IconSource
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.stringRes
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/**
 * Created by HeYanLe on 2023/2/21 23:35.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceTopAppBar(behavior: TopAppBarScrollBehavior) {
    val nav = LocalNavController.current
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                nav.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.close)
                )
            }
        },
        title = { Text(text = stringResource(id = R.string.manage)) },
        scrollBehavior = behavior,
        actions = {
            IconButton(onClick = {
                stringRes(R.string.long_touch_to_drag).moeSnackBar()
            }) {
                Icon(Icons.Filled.Sort, stringResource(id = R.string.long_touch_to_drag))
            }
        }
    )
}

@Composable
fun Source() {
    val nav = LocalNavController.current

    val vm = viewModel<SourceViewModel>()

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        vm.move(from.index, to.index)
    }, onDragEnd = { from, to ->
        vm.onDragEnd()
    })


    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(state)
            .detectReorderAfterLongPress(state)
    ) {
        items(vm.configSourceList, key = { it.sourceInfo.source.key }) { configSource ->
            val sourceInfo = configSource.sourceInfo
            val source = sourceInfo.source
            val config = configSource.config
            val bundle = LocalSourceBundleController.current
            ReorderableItem(reorderableState = state, key = source.key) {
                it.loge("Source")
                Box(
                    modifier = Modifier
                        .run {
                            if (it) {
                                background(
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        3.dp
                                    )
                                )
                            } else {
                                this
                            }
                        },
                ) {
                    SourceItem(
                        configSource,
                        showConfig = bundle.preference(configSource.sourceInfo.source.key) != null,
                        onCheckedChange = { source: ConfigSource, b: Boolean ->
                            if (b) {
                                vm.enable(source)
                            } else {
                                vm.disable(source)
                            }
                        },
                        onClick = {
                            if(it.sourceInfo is SourceInfo.Loaded && it.config.enable && bundle.preference(it.sourceInfo.source.key) != null){
                                nav.navigationSourceConfig(it.sourceInfo.source.key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SourceItem(
    configSource: ConfigSource,
    showConfig: Boolean,
    onCheckedChange: (ConfigSource, Boolean) -> Unit,
    onClick: (ConfigSource) -> Unit,
) {

    val sourceInfo = configSource.sourceInfo
    val config = configSource.config
    val icon = sourceInfo.source as? IconSource

    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.clickable {
            onClick(configSource)
        },
        headlineContent = {
            Text(text = sourceInfo.source.label)
        },
        supportingContent = {
            Text(
                text = sourceInfo.source.version,
            )
        },
        trailingContent = {
            when(sourceInfo){
                is SourceInfo.Loaded -> {
                    Row {
                        if (showConfig){
                            IconButton(
                                onClick = {
                                    onClick(configSource)
                                },
                            ){
                                Icon(Icons.Filled.Settings, contentDescription = sourceInfo.source.label)
                            }
                        }
                        Switch(checked = config.enable, onCheckedChange = {
                            onCheckedChange(configSource, it)
                        })
                    }

                }
                is SourceInfo.Error -> {
                    Text(text = sourceInfo.msg)

                }
            }

        },
        leadingContent = {
            OkImage(
                modifier = Modifier.size(40.dp),
                image = icon?.getIconFactory()?.invoke(),
                contentDescription = sourceInfo.source.label,
                crossFade = false,
                placeholderColor = null,
                errorColor = null,
            )
        }
    )

}