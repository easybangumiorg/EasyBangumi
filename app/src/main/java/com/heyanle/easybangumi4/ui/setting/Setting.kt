package com.heyanle.easybangumi4.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.navigationSetting

/**
 * Created by HeYanLe on 2023/8/5 23:02.
 * https://github.com/heyanLE
 */
sealed class SettingPage(
    val router: String,
    val title: @Composable () -> Unit,
    val content: @Composable ColumnScope.(NestedScrollConnection) -> Unit,
) {

    data object First: SettingPage("first", {
        Text(text = stringResource(id = R.string.setting))
    }, {
        FirstSetting(it)
    })

    data object Appearance : SettingPage("appearance", {
        Text(text = stringResource(id = R.string.appearance_setting))
    }, {
        AppearanceSetting(it)
    })

    data object Player : SettingPage("player", {
        Text(text = stringResource(id = R.string.player_setting))
    }, {
        PlayerSetting(nestedScrollConnection = it)
    })

    data object Download : SettingPage("download", {
        Text(text = stringResource(id = R.string.download_setting))
    }, {
        DownloadSetting(nestedScrollConnection = it)
    })

    data object Extension : SettingPage("extension", {
        Text(text = stringResource(id = R.string.extension_setting))
    }, {
        ExtensionSetting(nestedScrollConnection = it)
    })
}

val settingPages = mapOf(
    SettingPage.Appearance.router to SettingPage.Appearance,
    SettingPage.Player.router to SettingPage.Player,
    SettingPage.Download.router to SettingPage.Download,
    SettingPage.First.router to SettingPage.First,
    SettingPage.Extension.router to SettingPage.Extension,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Setting(
    router: String
) {

    val nav = LocalNavController.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    settingPages[router]?.let { settingPage ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column {
                TopAppBar(
                    title = settingPage.title,
                    navigationIcon = {
                        IconButton(onClick = {
                            nav.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                stringResource(id = R.string.back)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior

                )
                settingPage.content(
                    this,
                    scrollBehavior.nestedScrollConnection
                )

            }
        }
    }
}

@Composable
fun ColumnScope.FirstSetting(
    nestedScrollConnection: NestedScrollConnection
){
    val nav = LocalNavController.current
    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ListItem(
            modifier = Modifier.clickable {
                nav.navigationSetting(SettingPage.Appearance)
            },
            headlineContent = { Text(text = stringResource(id = R.string.appearance_setting)) },
            leadingContent = {
                Icon(
                    Icons.Filled.ColorLens,
                    contentDescription = stringResource(id = R.string.appearance_setting)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                nav.navigationSetting(SettingPage.Player)
            },
            headlineContent = { Text(text = stringResource(id = R.string.player_setting)) },
            leadingContent = {
                Icon(
                    Icons.Filled.PlayCircle,
                    contentDescription = stringResource(id = R.string.player_setting)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                nav.navigationSetting(SettingPage.Download)
            },
            headlineContent = { Text(text = stringResource(id = R.string.download_setting)) },
            leadingContent = {
                Icon(
                    Icons.Filled.Download,
                    contentDescription = stringResource(id = R.string.download_setting)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable {
                nav.navigationSetting(SettingPage.Extension)
            },
            headlineContent = { Text(text = stringResource(id = R.string.extension_setting)) },
            leadingContent = {
                Icon(
                    Icons.Filled.Extension,
                    contentDescription = stringResource(id = R.string.extension_setting)
                )
            }
        )
    }
}