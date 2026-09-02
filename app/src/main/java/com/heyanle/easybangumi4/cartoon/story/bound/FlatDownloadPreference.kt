package com.heyanle.easybangumi4.cartoon.story.bound

import androidx.core.net.toUri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
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
 * 扁平下载目录偏好：与本地番源的 local_bangumi 并列，存放不绑定元数据的视频文件。
 * SAF 授权沿用用户为本地番源选择的同一个目录树，目录本身即索引（无 NFO、无子目录）。
 */
class FlatDownloadPreference(
    private val cartoonDownloadPreference: CartoonDownloadPreference,
    private val settingPreferences: SettingPreferences,
) {

    companion object {
        const val FLAT_DIR_NAME = "flat_download"
    }

    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.SINGLE)
    private val privateFlatFolder = File(APP.getFilePath(FLAT_DIR_NAME))

    val realFlatDownloadUri = combine(
        settingPreferences.localUsePrivate.stateIn(scope),
        settingPreferences.localUri.stateIn(scope),
    ) { usePrivate, uri ->
        if (usePrivate) {
            privateFlatFolder.toUri()
        } else {
            // 授权失败时仅回退私有目录，不改动本地番源的 usePrivate 设置
            UniFile.fromUri(APP, uri.toUri())?.createDirectory(FLAT_DIR_NAME)?.apply {
                if (cartoonDownloadPreference.localNoMedia.get()) createFile(".nomedia")
            }?.uri ?: privateFlatFolder.toUri().apply {
                "扁平下载目录读取错误，将临时使用私有目录，可进入设置手动重新授权".moeSnackBar()
            }
        }
    }.stateIn(
        scope, SharingStarted.Lazily, if (settingPreferences.localUsePrivate.get()) {
            privateFlatFolder.toUri()
        } else {
            settingPreferences.localUri.get().toUri()
        }
    )

}
