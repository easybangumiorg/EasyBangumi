package com.heyanle.easybangumi4.compose.source_config

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.heyanle.bangumi_source_api.api.component.configuration.ConfigComponent
import com.heyanle.bangumi_source_api.api.component.configuration.SourceConfig
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.compose.common.BooleanPreferenceItem
import com.heyanle.easybangumi4.compose.common.SourceContainerBase
import com.heyanle.easybangumi4.compose.common.StringEditPreferenceItem
import com.heyanle.easybangumi4.compose.common.StringSelectPreferenceItem
import com.heyanle.easybangumi4.source.utils.SourcePreferenceHelper

/**
 * Created by HeYanLe on 2023/8/5 21:36.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceConfig(
    sourceKey: String
) {

    val nav = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    SourceContainerBase(hasSource = {
        it.config(sourceKey) != null
    }) {
        it.source(sourceKey)?.let { source ->
            it.config(sourceKey)?.let { config ->
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(id = com.heyanle.easy_i18n.R.string.source_config) + " - " + source?.label
                            )
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

                    ConfigList(
                        sourcePreferenceHelper = SourcePreferenceHelper.of(APP, source),
                        configComponent = config,
                        nestedScrollConnection = scrollBehavior.nestedScrollConnection
                    )
                }
            }

        }


    }

}

@Composable
fun ConfigList(
    modifier: Modifier = Modifier,
    sourcePreferenceHelper: SourcePreferenceHelper,
    configComponent: ConfigComponent,
    nestedScrollConnection: NestedScrollConnection
) {

    LazyColumn(
        modifier = modifier.nestedScroll(nestedScrollConnection)
    ) {
        items(configComponent.configs()) { config ->


            when (config) {
                is SourceConfig.Switch -> {
                    val value = remember(config) {
                        mutableStateOf(
                            sourcePreferenceHelper.load(config.key, config.def)
                                .toBooleanStrictOrNull() ?: false
                        )
                    }
                    BooleanPreferenceItem(
                        title = {
                            Text(text = config.label)
                        },
                        change = value.value,
                        onChange = {
                            sourcePreferenceHelper.save(config.key, it.toString())
                            value.value = it
                        }
                    )
                }

                is SourceConfig.Edit -> {
                    val value = remember(config) {
                        mutableStateOf(sourcePreferenceHelper.load(config.key, config.def))
                    }
                    StringEditPreferenceItem(
                        title = {
                            Text(text = config.label)
                        },
                        value = value.value,
                        onEdit = {
                            sourcePreferenceHelper.save(config.key, it.toString())
                            value.value = it
                        }
                    )
                }

                is SourceConfig.Selection -> {
                    val value = remember(config) {
                        mutableStateOf(sourcePreferenceHelper.load(config.key, config.def))
                    }

                    StringSelectPreferenceItem(
                        title = { Text(text = config.label) }, textList =
                        config.selections, select = config.selections.indexOf(value.value)
                    ) {
                        if (config.selections.isNotEmpty()) {
                            value.value = config.selections.getOrElse(it) { config.selections[0] }
                            sourcePreferenceHelper.save(
                                config.key,
                                config.selections.getOrElse(it) { config.selections[0] })
                        }

                    }
                }
            }
        }
    }

}