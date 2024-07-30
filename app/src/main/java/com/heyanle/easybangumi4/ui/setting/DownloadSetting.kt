package com.heyanle.easybangumi4.ui.setting

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.cartoon.story.local.LocalCartoonPreference
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.splash.SplashGuildController
import com.heyanle.easybangumi4.ui.common.BooleanPreferenceItem
import com.heyanle.easybangumi4.ui.common.EmumPreferenceItem
import com.heyanle.easybangumi4.ui.common.LongEditPreferenceItem
import com.heyanle.easybangumi4.ui.common.StringSelectPreferenceItem
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/10/2.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ColumnScope.DownloadSetting(
    nestedScrollConnection: NestedScrollConnection
) {
    val settingPreferences: SettingPreferences by Inject.injectLazy()

    val splashGuildController: SplashGuildController by Inject.injectLazy()
    val localController: LocalCartoonPreference by Inject.injectLazy()
    val cartoonDownloadPreferences: CartoonDownloadPreference by Inject.injectLazy()

    val usePrivate = localController.localUsePrivate.collectAsState()
    val path = localController.localPath.collectAsState()


    Column(
        modifier = Modifier
            .weight(1f)
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp, 16.dp)
        ) {

            Text(
                text = stringResource(id = com.heyanle.easy_i18n.R.string.choose_folder_to_bangumi),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = stringResource(id = com.heyanle.easy_i18n.R.string.choose_folder_to_bangumi_msg),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.size(16.dp))

            if (usePrivate.value) {

                Text(
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.private_path_msg),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.size(16.dp))

            }

            Text(
                text = stringResource(id = com.heyanle.easy_i18n.R.string.current_choose_folder,
                    if (usePrivate.value) stringResource(com.heyanle.easy_i18n.R.string.private_folder)
                    else path.value
                ),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.size(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = {
                        localController.usePrivate(true)
                    }
                ) {
                    Text(text = stringResource(id = R.string.private_folder))
                }
                Spacer(modifier = Modifier.size(8.dp))
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = {
                        chooseFolder()
                    }
                ) {
                    Text(text = stringResource(id = R.string.choose_folder))
                }
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            EmumPreferenceItem<CartoonDownloadPreference.DownloadEncode>(
                title = { Text(text = stringResource(id = R.string.download_decode_type)) },
                textList = remember {
                    cartoonDownloadPreferences.downloadEncodeSelection.map { it.second }
                },
                preference = cartoonDownloadPreferences.downloadEncode,
                onChangeListener = {

                }
            )
        }

        if (!usePrivate.value) {
            BooleanPreferenceItem(
                title = { Text(stringResource(id = com.heyanle.easy_i18n.R.string.local_no_media)) },
                subtitle = { Text(stringResource(id = com.heyanle.easy_i18n.R.string.local_no_media_msg)) },
                preference = cartoonDownloadPreferences.localNoMedia,
                onChange = {
                    if (!it) {
                        localController.deleteNoMedia()
                    } else {
                        localController.createNoMedia()
                    }
                }

            )
        }

        LongEditPreferenceItem(
            title = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.downloading_max_count)) },
            preference = cartoonDownloadPreferences.downloadMaxCountPref,
            onChange = {
                stringRes(R.string.should_reboot).moeSnackBar()
            }
        )

        LongEditPreferenceItem(
            title = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.transforming_max_count)) },
            preference = cartoonDownloadPreferences.transformMaxCountPref,
            onChange = {
                stringRes(R.string.should_reboot).moeSnackBar()
            }
        )
    }


}

private fun chooseFolder(){
    val localCartoonPreference: LocalCartoonPreference by Inject.injectLazy()
    val settingPreferences: SettingPreferences by Inject.injectLazy()
    val currUri = settingPreferences.localUri.get()
    LauncherBus.current?.getDocumentTree(Uri.parse(currUri)){ uri ->
        var completely = false
        if(uri != null){
            val path =  UniFile.fromUri(APP, uri)?.filePath
            if (path != null) {
                localCartoonPreference.usePrivate(false)
                settingPreferences.localUri.set(uri.toString())
                settingPreferences.localPath.set(path)
                completely = true
            }
        }
        if (!completely) {
            localCartoonPreference.usePrivate(true)
            stringRes(com.heyanle.easy_i18n.R.string.choose_folder_failed).moeSnackBar()
        } else {
            stringRes(com.heyanle.easy_i18n.R.string.local_folder_change_completely).moeSnackBar()
        }
    }
}