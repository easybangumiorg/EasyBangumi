package com.heyanle.easybangumi4.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.BooleanPreferenceItem
import com.heyanle.easybangumi4.ui.common.EmumPreferenceItem
import com.heyanle.easybangumi4.ui.common.StringSelectPreferenceItem
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt

/**
 * Created by HeYanLe on 2023/8/5 23:02.
 * https://github.com/heyanLE
 */
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

        BooleanPreferenceItem(
            title = { Text(stringResource(id = com.heyanle.easy_i18n.R.string.player_bottom_nav_padding)) },
            preference = settingPreferences.playerBottomNavigationBarPadding
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


        val sizePre by settingPreferences.cacheSize.flow()
            .collectAsState(settingPreferences.cacheSize.get())
        val size = settingPreferences.cacheSizeSelection
        StringSelectPreferenceItem(
            title = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.max_cache_size)) },
            textList = size.map { it.second },
            select = size.indexOfFirst { it.first == sizePre }.let { if (it == -1) 0 else it }
        ) {
            settingPreferences.cacheSize.set(size[it].first)
            stringRes(com.heyanle.easy_i18n.R.string.should_reboot).moeSnackBar()
        }


    }


}