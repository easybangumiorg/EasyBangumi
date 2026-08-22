package com.heyanle.easybangumi4.v2.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RunCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.ABOUT
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.SOURCE_MANAGER
import com.heyanle.easybangumi4.STORAGE
import com.heyanle.easybangumi4.STORY
import com.heyanle.easybangumi4.TestMain
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.navigationSetting
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.setting.SettingPage
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.ui.component.V2Icon
import com.heyanle.inject.core.Inject

/** V2 information architecture for the More tab. */
@Composable
internal fun MoreV2() {
    val navController = LocalNavController.current
    val settingPreferences: SettingPreferences by Inject.injectLazy()
    val privateMode by settingPreferences.isInPrivate.flow()
        .collectAsState(initial = settingPreferences.isInPrivate.get())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.WarmBackground)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = 28.dp),
    ) {
        MoreHeaderV2(onClick = { navController.navigate(ABOUT) })

        MoreEntryV2(
            icon = Icons.Filled.HistoryToggleOff,
            title = stringResource(R.string.in_private),
            subtitle = null,
            onClick = { settingPreferences.isInPrivate.set(!privateMode) },
            trailing = {
                Switch(
                    checked = privateMode,
                    onCheckedChange = settingPreferences.isInPrivate::set,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = V2Theme.colors.accent,
                        checkedThumbColor = V2Tokens.Surface,
                        checkedBorderColor = V2Theme.colors.accent,
                        uncheckedThumbColor = V2Tokens.IconSecondary,
                        uncheckedTrackColor = V2Tokens.SurfaceMuted,
                        uncheckedBorderColor = V2Tokens.IconSecondary,
                        disabledUncheckedThumbColor = V2Tokens.IconSecondary.copy(alpha = 0.55f),
                        disabledUncheckedTrackColor = V2Tokens.SurfaceMuted.copy(alpha = 0.55f),
                        disabledUncheckedBorderColor = V2Tokens.Divider,
                    ),
                )
            },
        )
        MoreDividerV2()
        MoreEntryV2(Icons.Filled.Extension, stringResource(R.string.source_manage), null, { navController.navigate(SOURCE_MANAGER) })
        MoreDividerV2()
        MoreEntryV2(Icons.Filled.Tag, stringResource(R.string.tag_manage), null, navController::navigationCartoonTag)
        MoreDividerV2()
        MoreEntryV2(Icons.Filled.Storage, stringResource(R.string.backup_and_store), null, { navController.navigate(STORAGE) })
        MoreDividerV2()
        MoreEntryV2(Icons.Filled.Download, stringResource(R.string.local_download), null, { navController.navigate(STORY) })
        MoreDividerV2()
        MoreEntryV2(Icons.Filled.Settings, stringResource(R.string.setting), null, { navController.navigationSetting(SettingPage.First) })
        MoreDividerV2()
        MoreEntryV2(Icons.Outlined.Info, stringResource(R.string.about), null, { navController.navigate(ABOUT) })

        if (BuildConfig.DEBUG) {
            MoreDividerV2()
            MoreEntryV2(Icons.Outlined.RunCircle, "测试按钮", null, TestMain::main)
        }
    }
}

@Composable
private fun MoreHeaderV2(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.more),
        modifier = Modifier.padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 14.dp),
        color = V2Tokens.TextPrimary,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OkImage(
            image = com.heyanle.easybangumi4.R.mipmap.logo_new,
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(56.dp),
        )
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(stringResource(R.string.app_name), color = V2Tokens.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("与动漫相遇的每一天", modifier = Modifier.padding(top = 4.dp), color = V2Tokens.TextSecondary, fontSize = 12.sp)
        }
        Text("›", color = V2Tokens.TextSecondary, fontSize = 26.sp)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = V2Tokens.ScreenHorizontalPadding), color = V2Tokens.Divider)
}

@Composable
private fun MoreEntryV2(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = V2Tokens.ScreenHorizontalPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        V2Icon(
            imageVector = icon,
            contentDescription = null,
            tint = V2Theme.colors.accent,
            modifier = Modifier.size(22.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = title,
                color = V2Tokens.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(text = subtitle, modifier = Modifier.padding(top = 3.dp), color = V2Tokens.TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Text("›", color = V2Tokens.TextSecondary, fontSize = 26.sp)
        }
    }
}

@Composable
private fun MoreDividerV2() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp, end = V2Tokens.ScreenHorizontalPadding),
        color = V2Tokens.Divider,
    )
}
