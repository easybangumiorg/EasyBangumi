package com.heyanle.easybangumi4.cartoon.story.local

import androidx.core.net.toUri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.cartoon.story.download_v1.CartoonDownloadPreference
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.moeSnackBar
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
    private val androidPreferenceStore: AndroidPreferenceStore,
    private val cartoonDownloadPreference: CartoonDownloadPreference,
    private val settingPreferences: SettingPreferences,
) {

    val dispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val privateBangumiFolder = File(APP.getFilePath("local_source"))

    val localUsePrivate = settingPreferences.localUsePrivate.stateIn(scope)

    val localUri = settingPreferences.localUri.stateIn(scope)
    val localPath = settingPreferences.localPath.stateIn(scope)

    val realBangumiLocalUri = combine(
        localUsePrivate,
        localUri,
    ) { usePrivate, uri ->
        if (usePrivate) {
            privateBangumiFolder.toUri()
        } else {
            UniFile.fromUri(APP, uri.toUri())?.createDirectory("local_bangumi")?.apply {
                if (cartoonDownloadPreference.localNoMedia.get())
                    createFile(".nomedia")
            }?.uri ?: privateBangumiFolder.toUri().apply {
                "本地文件夹读取错误，将自动切换到私有目录，可进入设置手动重新授权".moeSnackBar()
                usePrivate(true)
            }
        }
    }.stateIn(
        scope, SharingStarted.Lazily, if (localUsePrivate.value) {
            privateBangumiFolder.toUri()
        } else {
            localUri.value.toUri()
        }
    )

    fun deleteNoMedia(){
        realBangumiLocalUri.value?.let {
            UniFile.fromUri(APP, it)?.listFiles()?.forEach {
                if(it.name == ".nomedia"){
                    it.delete()
                }
            }
        }
    }

    fun createNoMedia(){
        realBangumiLocalUri.value?.let {
            UniFile.fromUri(APP, it)?.createFile(".nomedia")
        }
    }

    fun usePrivate(b: Boolean){
        settingPreferences.localUsePrivate.set(b)
    }


}