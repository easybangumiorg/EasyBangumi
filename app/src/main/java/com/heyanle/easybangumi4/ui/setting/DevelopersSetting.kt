package com.heyanle.easybangumi4.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.plugin.js.JSDebugPreference
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.inject.core.Inject


/**
 * Created by HeYanLe on 2024/11/3 16:58.
 * https://github.com/heyanLE
 */

@Composable
fun ColumnScope.DevelopersSetting(
    nestedScrollConnection: NestedScrollConnection
) {

    val scope = rememberCoroutineScope()

    val settingPreferences: SettingPreferences by Inject.injectLazy()
    val jsDebugPreference: JSDebugPreference by Inject.injectLazy()

    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ListItem(headlineContent = {
            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.js_extension), color = MaterialTheme.colorScheme.primary)
        })

        ListItem(
            modifier = Modifier.clickable {
                jsDebugPreference.encryptJS()
            },
            headlineContent = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.js_extension_encrypt))
            },
            supportingContent = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.js_extension_encrypt_desc))
            },
            trailingContent = {
                IconButton(
                    onClick = {
                        jsDebugPreference.encryptJS()
                    }
                ) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.js_extension_encrypt))
                }
            }
        )



    }

}