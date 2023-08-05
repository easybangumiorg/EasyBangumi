package com.heyanle.easybangumi4.compose.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.res.stringResource
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController

/**
 * Created by HeYanLe on 2023/8/5 23:02.
 * https://github.com/heyanLE
 */
sealed class SettingPage(
    val router: String,
    val title: @Composable () -> Unit,
    val content: @Composable ColumnScope.(NestedScrollConnection) -> Unit,
) {
    object Appearance : SettingPage("appearance", {
        Text(text = stringResource(id = R.string.appearance_setting))
    }, {
        AppearanceSetting(it)
    })

    object Player : SettingPage("player", {
        Text(text = stringResource(id = R.string.player_setting))
    }, {
        PlayerSetting(nestedScrollConnection = it)
    })
}

val settingPages = mapOf(
    SettingPage.Appearance.router to SettingPage.Appearance,
    SettingPage.Player.router to SettingPage.Player,
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