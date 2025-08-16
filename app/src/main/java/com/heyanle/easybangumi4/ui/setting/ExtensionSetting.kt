package com.heyanle.easybangumi4.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationSetting
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.BooleanPreferenceItem
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.inject.core.Inject

/**
 * Created by heyanle on 2024/6/3.
 * https://github.com/heyanLE
 */

@Composable
fun ColumnScope.LocalExtensionSetting(
    nestedScrollConnection: NestedScrollConnection
) {
    val nav = LocalNavController.current

    val scope = rememberCoroutineScope()

    val settingMMKVPreferences: SettingMMKVPreferences by Inject.injectLazy()
    val settingPreferences: SettingPreferences by Inject.injectLazy()

    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BooleanPreferenceItem(
            title = {
                Text(text = stringResource(id = R.string.local_extension_page))
            },
            subtitle = {
                Text(text = stringResource(id = R.string.local_extension_page_msg))
            },
            icon = {
                Icon(
                    Icons.Filled.Download,
                    contentDescription = stringResource(id = R.string.local_extension_page)
                )
            },
            preference = settingMMKVPreferences.localExtensionPage,
            onChange = {
                "重启生效".moeSnackBar()
            }
        )
    }
}

@Composable
fun ColumnScope.ExtensionSetting(
    nestedScrollConnection: NestedScrollConnection
) {

    val nav = LocalNavController.current

    val scope = rememberCoroutineScope()

    val settingMMKVPreferences: SettingMMKVPreferences by Inject.injectLazy()
    val settingPreferences: SettingPreferences by Inject.injectLazy()

    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BooleanPreferenceItem(
            title = {
                Text(text = stringResource(id = R.string.web_view_compatible))
            },
            subtitle = {
                Text(text = stringResource(id = R.string.web_view_compatible_msg))
            },
            icon = {
                Icon(
                    Icons.Filled.Public,
                    contentDescription = stringResource(id = R.string.web_view_compatible)
                )
            },
            preference = settingMMKVPreferences.webViewCompatible
        )

        BooleanPreferenceItem(
            title = {
                Text(text = stringResource(id = R.string.extension_v2))
            },
            subtitle = {
                Text(text = stringResource(id = R.string.extension_v2_msg) + if (settingMMKVPreferences.extensionV2Temp) "开启" else "关闭")
            },
            icon = {
                Icon(
                    Icons.Filled.Extension,
                    contentDescription = stringResource(id = R.string.extension_v2)
                )
            },
            preference = settingMMKVPreferences.extensionV2,
            onChange = {
                "重启生效".moeSnackBar()
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                nav.navigationSetting(SettingPage.LocalExtension)
            },
            headlineContent = { Text(text = stringResource(id = R.string.local_extension_setting)) },
            leadingContent = {
                Icon(
                    Icons.Filled.Extension,
                    contentDescription = stringResource(id = R.string.local_extension_setting)
                )
            }
        )
    }
}
