package com.heyanle.easybangumi4.v2.ui.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ActionRow
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2Switch
import com.heyanle.inject.core.Inject

@Composable
internal fun LocalSourceSettingV2(
    modifier: Modifier = Modifier,
) {
    val settingPreferences: SettingMMKVPreferences by Inject.injectLazy()
    val localPageEnabled by settingPreferences.localExtensionPage.flow().collectAsState(
        settingPreferences.localExtensionPage.get(),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        V2Section(title = "本地番源") {
            V2ActionRow(
                icon = Icons.Filled.Folder,
                title = stringResource(R.string.local_extension_page),
                subtitle = stringResource(R.string.local_extension_page_msg),
                onClick = {
                    settingPreferences.localExtensionPage.set(!localPageEnabled)
                    "重启生效".moeSnackBar()
                },
                trailing = {
                    V2Switch(
                        checked = localPageEnabled,
                        onCheckedChange = {
                            settingPreferences.localExtensionPage.set(it)
                            "重启生效".moeSnackBar()
                        },
                    )
                },
            )
        }
        Box(Modifier.height(24.dp))
    }
}
