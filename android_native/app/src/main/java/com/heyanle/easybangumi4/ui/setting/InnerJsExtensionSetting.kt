package com.heyanle.easybangumi4.ui.setting

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.inject.core.Inject


/**
 * Created by HeYanLe on 2024/11/3 17:04.
 * https://github.com/heyanLE
 */

@Composable
fun ColumnScope.InnerJsExtensionSetting(
    nestedScrollConnection: NestedScrollConnection? = null
) {

    val scope = rememberCoroutineScope()

    val settingPreferences: SettingPreferences by Inject.injectLazy()



}