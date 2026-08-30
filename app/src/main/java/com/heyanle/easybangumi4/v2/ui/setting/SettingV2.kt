package com.heyanle.easybangumi4.v2.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.MainActivitySwitcher
import com.heyanle.easybangumi4.navigationSetting
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.setting.SettingPage
import com.heyanle.easybangumi4.ui.setting.settingPages
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2ActionRow
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import com.heyanle.easybangumi4.v2.ui.component.V2Section
import com.heyanle.easybangumi4.v2.ui.component.V2Switch
import com.heyanle.easybangumi4.v2.ui.component.V2SectionDivider
import com.heyanle.inject.core.Inject

/** V2 settings route. All registered setting pages use V2 presentation components. */
@Composable
internal fun SettingV2(router: String) {
    val navController = LocalNavController.current
    val settingPage = settingPages[router] ?: SettingPage.First

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground)
            .navigationBarsPadding(),
    ) {
        V2SecondaryHeader(
            title = settingPage.titleText(),
            onBack = navController::popBackStack,
            largeTitle = settingPage == SettingPage.First,
        )
        when (settingPage) {
            SettingPage.First -> FirstSettingV2(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            )
            SettingPage.Appearance -> AppearanceSettingV2(Modifier.weight(1f))
            SettingPage.Player -> PlayerSettingV2(Modifier.weight(1f))
            SettingPage.Download -> DownloadSettingV2(Modifier.weight(1f))
            SettingPage.Developers -> DevelopersSettingV2(Modifier.weight(1f))
            SettingPage.LocalSource -> LocalSourceSettingV2(Modifier.weight(1f))
            SettingPage.DanmakuSource -> DanmakuSourceSettingV2(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SettingPage.titleText(): String = when (this) {
    SettingPage.First -> stringResource(R.string.setting)
    SettingPage.Appearance -> stringResource(R.string.appearance_setting)
    SettingPage.Player -> stringResource(R.string.player_setting)
    SettingPage.Download -> stringResource(R.string.download_setting)
    SettingPage.Developers -> stringResource(R.string.developers_setting)
    SettingPage.LocalSource -> stringResource(R.string.local_extension_setting)
    SettingPage.DanmakuSource -> "弹幕源"
}

@Composable
private fun FirstSettingV2(modifier: Modifier = Modifier) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val settingPreferences: SettingPreferences by Inject.injectLazy()
    val useV2Ui by settingPreferences.useV2Ui.flow().collectAsState(
        initial = settingPreferences.useV2Ui.get(),
    )

    Column(modifier = modifier) {
        V2Section(title = "界面版本") {
            V2ActionRow(
                icon = Icons.AutoMirrored.Filled.ViewQuilt,
                title = "新版界面",
                subtitle = "切换后将返回首页",
                onClick = { MainActivitySwitcher.switch(context, !useV2Ui) },
                trailing = {
                    V2Switch(
                        checked = useV2Ui,
                        onCheckedChange = { MainActivitySwitcher.switch(context, it) },
                    )
                },
            )
        }

        V2Section(title = "体验") {
            V2ActionRow(
                icon = Icons.Filled.ColorLens,
                title = stringResource(R.string.appearance_setting),
                subtitle = "主题、颜色和布局模式",
                onClick = { navController.navigationSetting(SettingPage.Appearance) },
            )
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.PlayCircle,
                title = stringResource(R.string.player_setting),
                subtitle = "播放器、手势和弹幕显示",
                onClick = { navController.navigationSetting(SettingPage.Player) },
            )
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.Download,
                title = stringResource(R.string.download_setting),
                subtitle = "目录、并发和转码选项",
                onClick = { navController.navigationSetting(SettingPage.Download) },
            )
            V2SectionDivider()
            V2ActionRow(
                icon = Icons.Filled.Subtitles,
                title = "弹幕源",
                subtitle = "管理内置弹幕服务",
                onClick = { navController.navigationSetting(SettingPage.DanmakuSource) },
            )
        }

        V2Section(title = "高级") {
            V2ActionRow(
                icon = Icons.Filled.DeveloperMode,
                title = stringResource(R.string.developers_setting),
                subtitle = "调试、网络和实验性功能",
                onClick = { navController.navigationSetting(SettingPage.Developers) },
            )
        }
    }
}
