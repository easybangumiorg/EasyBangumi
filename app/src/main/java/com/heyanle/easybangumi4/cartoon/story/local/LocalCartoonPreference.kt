package com.heyanle.easybangumi4.cartoon.story.local

import androidx.core.net.toUri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
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
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class LocalCartoonPreference (
    private val androidPreferenceStore: AndroidPreferenceStore
) {

    val dispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val privateFolder = File(APP.getFilePath("local_source"))

    val localUsePrivatePref = androidPreferenceStore.getBoolean("local_use_private", true)
    val localUsePrivate = localUsePrivatePref.stateIn(scope)


    val localUriPref = androidPreferenceStore.getString("local_folder_uri", "")
    val localUri = localUriPref.stateIn(scope)

    val localPathPref = androidPreferenceStore.getString("local_folder_path", "")
    val localPath = localPathPref.stateIn(scope)

    val realLocalUri = combine(
        localUsePrivate,
        localUri,
    ) { usePrivate, uri ->
        if (usePrivate) {
            privateFolder.toUri()
        } else {
            UniFile.fromUri(APP, uri.toUri())?.createDirectory("local_bangumi")?.apply {
                createFile(".nomedia")
            }?.uri ?: privateFolder.toUri()
        }
    }.stateIn(
        scope, SharingStarted.Lazily, if (localUsePrivate.value) {
            privateFolder.toUri()
        } else {
            localUri.value.toUri()
        }
    )

    fun usePrivate(b: Boolean){
        localUsePrivatePref.set(b)
    }


}