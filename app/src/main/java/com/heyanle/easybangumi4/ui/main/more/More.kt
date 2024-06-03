package com.heyanle.easybangumi4.ui.main.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.ABOUT
import com.heyanle.easybangumi4.DOWNLOAD
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.SOURCE_MANAGER
import com.heyanle.easybangumi4.STORAGE
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.navigationSetting
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.BooleanPreferenceItem
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.setting.SettingPage
import com.heyanle.injekt.core.Injekt

/**
 * Created by HeYanLe on 2023/3/22 15:29.
 * https://github.com/heyanLE
 */

@Composable
fun More() {

    val nav = LocalNavController.current

    val settingPreferences: SettingPreferences by Injekt.injectLazy()
    val settingMMKVPreferences: SettingMMKVPreferences by Injekt.injectLazy()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {
        EasyBangumiCard()
        Divider()
        BooleanPreferenceItem(
            title = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.in_private))
            },
            subtitle = {
                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.in_private_msg))
            },
            icon = {
                Icon(
                    Icons.Filled.HistoryToggleOff,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.in_private)
                )
            },
            preference = settingPreferences.isInPrivate
        )
        Divider()

        ListItem(
            modifier = Modifier.clickable {
                nav.navigate(SOURCE_MANAGER)
            },
            headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.source_manage)) },
            leadingContent = {
                Icon(
                    Icons.Filled.Extension,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.source_manage)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                nav.navigationCartoonTag()
            },
            headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.tag_manage)) },
            leadingContent = {
                Icon(
                    Icons.Filled.Tag,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.tag_manage)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                nav.navigate(STORAGE)
            },
            headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.backup_and_store)) },
            leadingContent = {
                Icon(
                    Icons.Filled.Storage,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.backup_and_store)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                nav.navigate(DOWNLOAD)
            },
            headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.local_download)) },
            leadingContent = {
                Icon(
                    Icons.Filled.Download,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.local_download)
                )
            }
        )

        Divider()



        //Divider()


        ListItem(
            modifier = Modifier.clickable {
                nav.navigationSetting(SettingPage.First)
            },
            headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.setting)) },
            leadingContent = {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.setting)
                )
            }
        )
        ListItem(
            modifier = Modifier.clickable {
                nav.navigate(ABOUT)
            },
            headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.about)) },
            leadingContent = {
                Icon(
                    Icons.Outlined.Report,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.about)
                )
            }
        )

    }

}

@Composable
fun EasyBangumiCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OkImage(
            modifier = Modifier.size(64.dp),
            image = R.mipmap.logo_new,
            contentDescription = stringResource(com.heyanle.easy_i18n.R.string.app_name)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.app_name))
    }

}

