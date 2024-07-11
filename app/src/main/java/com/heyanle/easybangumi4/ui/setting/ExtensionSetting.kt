package com.heyanle.easybangumi4.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
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
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.BooleanPreferenceItem
import com.heyanle.inject.core.Inject

/**
 * Created by heyanle on 2024/6/3.
 * https://github.com/heyanLE
 */
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
    }
}
