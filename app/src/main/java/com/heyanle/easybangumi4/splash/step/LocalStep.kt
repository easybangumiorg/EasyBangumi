package com.heyanle.easybangumi4.splash.step

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults.filledTonalButtonColors
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.cartoon.local.LocalCartoonPreference
import com.heyanle.easybangumi4.splash.SplashGuildController
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile


/**
 * Created by heyanlin on 2024/7/4.
 */
class LocalStep: BaseStep {

    private val splashGuildController: SplashGuildController by Inject.injectLazy()
    private val localController: LocalCartoonPreference by Inject.injectLazy()

    override val name: String
        get() = "Local"
    override val version: Int
        get() = 0

    @Composable
    override fun Compose() {

        val usePrivate = localController.localUsePrivate.collectAsState()
        val path = localController.localPath.collectAsState()

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
                    colors = filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = {
                        localController.usePrivate(true)
                    }
                ) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.private_folder))
                }
                Spacer(modifier = Modifier.size(8.dp))
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    colors = filledTonalButtonColors(
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

    }

    private fun chooseFolder(){
        val currUri = localController.localUriPref.get()
        LauncherBus.current?.getDocumentTree(Uri.parse(currUri)){ uri ->
            var completely = false
            if(uri != null){
                val path =  UniFile.fromUri(APP, uri)?.filePath
                if (path != null) {
                    localController.usePrivate(false)
                    localController.localUriPref.set(uri.toString())
                    localController.localPathPref.set(path)
                    completely = true
                }
            }
            if (!completely) {
                localController.usePrivate(true)
                stringRes(com.heyanle.easy_i18n.R.string.choose_folder_failed).moeSnackBar()

            }
        }
    }
}