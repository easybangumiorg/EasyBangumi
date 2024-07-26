package com.heyanle.easybangumi4.storage

import androidx.core.net.toUri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.getFilePath
import com.hippo.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.io.File

/**
 * Created by heyanle on 2024/7/26.
 * https://github.com/heyanLE
 */
class StoragePreference(
    private val settingPreferences: SettingPreferences,
) {


    val dispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val privateBackupFolder = File(APP.getFilePath("backup"))

    val localUsePrivate = settingPreferences.localUsePrivate.stateIn(scope)
    val localUri = settingPreferences.localUri.stateIn(scope)

    val backupLocalUriOrNull = combine(
        localUsePrivate,
        localUri,
    ) { usePrivate, uri ->
        if (usePrivate) {
            null
        } else {
            UniFile.fromUri(APP, uri.toUri())?.createDirectory("local_bangumi")?.uri
        }
    }.stateIn(
        scope, SharingStarted.Lazily, if (localUsePrivate.value) {
            null
        } else {
            localUri.value.toUri()
        }
    )
}