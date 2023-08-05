package com.heyanle.easybangumi4.compose.source_manage.source

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.bangumi_source_api.api.IconSource
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.compose.common.OkImage
import com.heyanle.easybangumi4.compose.common.moeSnackBar
import com.heyanle.easybangumi4.navigationSourceConfig
import com.heyanle.easybangumi4.preferences.SourcePreferences
import com.heyanle.easybangumi4.source.SourceMigrationController
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
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

    val migrationController: SourceMigrationController by Injekt.injectLazy()
    val migratingSet = migrationController.migratingSource.collectAsState()

    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(state)
            .detectReorderAfterLongPress(state)
    ) {
        items(vm.sourceLibraryState.value, key = { it }) { sourceKey ->
            ReorderableItem(reorderableState = state, key = sourceKey) {
                it.loge("Source")
                vm.configState.value[sourceKey]?.let { config ->
                    vm.sourceMapState.value[sourceKey]?.let { source ->

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
                                isMigrate = migratingSet.value.contains(source),
                                config = config,
                                source = source,
                                onCheckedChange = { source: Source, b: Boolean ->
                                    if (b) {
                                        vm.enable(source.key)
                                    } else {
                                        vm.disable(source.key)
                                    }
                                },
                                onClick = {
                                    if (migratingSet.value.contains(it) && config.enable) {
                                        nav.navigationSourceConfig(it.key)
                                    }
                                }
                            )
                        }
                    }


                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceItem(
    isMigrate: Boolean,
    config: SourcePreferences.SourceConfig,
    source: Source,
    onCheckedChange: (Source, Boolean) -> Unit,
    onClick: (Source) -> Unit,
) {

    val icon = source as? IconSource

    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier,
        headlineContent = {
            Text(text = source.label)
        },
        supportingContent = {
            Text(
                text = source.version,
            )
        },
        trailingContent = {
            if (isMigrate) {
                Text(text = stringResource(id = R.string.migrating))
            } else {
                Switch(checked = config.enable, onCheckedChange = {
                    onCheckedChange(source, it)
                })
            }

        },
        leadingContent = {
            OkImage(
                modifier = Modifier.size(40.dp),
                image = icon?.getIconFactory()?.invoke(),
                contentDescription = source.label,
                crossFade = false,
                placeholderColor = null,
                errorColor = null,
            )
        }
    )

}