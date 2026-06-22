package com.heyanle.easybangumi4.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp


/**
 * Created by HeYanLe on 2024/11/3 16:58.
 * https://github.com/heyanLE
 */

@Composable
fun ColumnScope.DevelopersSetting(
    nestedScrollConnection: NestedScrollConnection
) {
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
    }

}
