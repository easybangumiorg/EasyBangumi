package com.heyanle.easybangumi4.compose.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.compose.common.BooleanPreferenceItem
import com.heyanle.easybangumi4.compose.common.EmumPreferenceItem
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.injekt.core.Injekt

/**
 * Created by HeYanLe on 2023/8/5 23:02.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.PlayerSetting(
    nestedScrollConnection: NestedScrollConnection
) {

    val nav = LocalNavController.current

    val scope = rememberCoroutineScope()

    val settingPreferences: SettingPreferences by Injekt.injectLazy()

    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        BooleanPreferenceItem(
            title = { Text(stringResource(id = com.heyanle.easy_i18n.R.string.use_external_player)) },
            preference = settingPreferences.useExternalVideoPlayer
        )

        EmumPreferenceItem(
            title = { Text(stringResource(id = com.heyanle.easy_i18n.R.string.player_orientation_mode)) },
            textList = listOf(
                stringResource(id = com.heyanle.easy_i18n.R.string.auto),
                stringResource(id = com.heyanle.easy_i18n.R.string.always_on),
                stringResource(id = com.heyanle.easy_i18n.R.string.always_off),
            ),
            preference = settingPreferences.playerOrientationMode,
            onChangeListener = {

            }
        )


    }


}