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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.APPEARANCE_SETTING
import com.heyanle.easybangumi4.HISTORY
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.preferences.InPrivatePreferences
import com.heyanle.easybangumi4.ui.common.BooleanPreferenceItem
import com.heyanle.easybangumi4.ui.common.OkImage

/**
 * Created by HeYanLe on 2023/3/22 15:29.
 * https://github.com/heyanLE
 */

@Composable
fun More() {

    val nav = LocalNavController.current

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
            preference = InPrivatePreferences
        )
        Divider()

        ListItem(
            modifier = Modifier.clickable {
                nav.navigate(HISTORY)
            },
            headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.history)) },
            leadingContent = {
                Icon(
                    Icons.Filled.History,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.history)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                nav.navigate(APPEARANCE_SETTING)
            },
            headlineContent = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.appearance_setting)) },
            leadingContent = {
                Icon(
                    Icons.Filled.ColorLens,
                    contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.appearance_setting)
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

