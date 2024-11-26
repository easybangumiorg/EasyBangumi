package com.heyanle.easybangumi4.ui.source_config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.plugin.source.SourceConfig
import com.heyanle.easybangumi4.plugin.source.SourceInfo
import com.heyanle.easybangumi4.plugin.source.bundle.get
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.preference.SourcePreference
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.ui.common.BooleanPreferenceItem
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.SourceContainerBase
import com.heyanle.easybangumi4.ui.common.StringEditPreferenceItem
import com.heyanle.easybangumi4.ui.common.StringSelectPreferenceItem

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
        it.preference(sourceKey) != null
    }) {
        val sourceInfo = it.sourceInfo(sourceKey)
        when (sourceInfo) {

            is SourceInfo.Loaded -> {
                it.preference(sourceKey)?.let { config ->
                    val preferenceHelper = sourceInfo.componentBundle.get<PreferenceHelper>()
                    Column {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(id = com.heyanle.easy_i18n.R.string.source_config) + " - " + sourceInfo.source.label
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

                        preferenceHelper?.let {
                            ConfigList(
                                sourcePreferenceHelper = it,
                                configComponent = config,
                                nestedScrollConnection = scrollBehavior.nestedScrollConnection
                            )
                        }

                    }
                }
            }

            else -> {

            }
        }


    }

}

@Composable
fun ConfigList(
    modifier: Modifier = Modifier,
    sourcePreferenceHelper: PreferenceHelper,
    configComponent: PreferenceComponent,
    nestedScrollConnection: NestedScrollConnection
) {
    val list = remember(configComponent) {
        configComponent.register()
    }
    LazyColumn(
        modifier = modifier.nestedScroll(nestedScrollConnection)
    ) {

        item {
            Row(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = stringResource(id = R.string.source_config_need_reboot), color = MaterialTheme.colorScheme.onSecondaryContainer)
            }

        }

        items(list) { config ->


            when (config) {
                is SourcePreference.Switch -> {
                    val value = remember(config) {
                        mutableStateOf(
                            sourcePreferenceHelper.get(config.key, config.def)
                                .toBooleanStrictOrNull() ?: false
                        )
                    }
                    BooleanPreferenceItem(
                        title = {
                            Text(text = config.label)
                        },
                        change = value.value,
                        onChange = {
                            sourcePreferenceHelper.put(config.key, it.toString())
                            value.value = it
                        }
                    )
                }

                is SourcePreference.Edit -> {
                    val value = remember(config) {
                        mutableStateOf(sourcePreferenceHelper.get(config.key, config.def))
                    }
                    StringEditPreferenceItem(
                        title = {
                            Text(text = config.label)
                        },
                        value = value.value,
                        defValue = config.def,
                        onEdit = {
                            sourcePreferenceHelper.put(config.key, it.toString())
                            value.value = it
                        }
                    )
                }

                is SourcePreference.Selection -> {
                    val value = remember(config) {
                        mutableStateOf(sourcePreferenceHelper.get(config.key, config.def))
                    }

                    StringSelectPreferenceItem(
                        title = { Text(text = config.label) }, textList =
                        config.selections, select = config.selections.indexOf(value.value)
                    ) {
                        if (config.selections.isNotEmpty()) {
                            value.value = config.selections.getOrElse(it) { config.selections[0] }
                            sourcePreferenceHelper.put(
                                config.key,
                                config.selections.getOrElse(it) { config.selections[0] })
                        }

                    }
                }
            }
        }
    }

}