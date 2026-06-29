package org.easybangumi.next.shared.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.easybangumi.next.lib.store.preference.Preference
import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import okio.buffer
import org.easybangumi.next.lib.utils.PathProvider

/**
 * 本地番剧路径配置
 * 支持私有目录和用户自选目录两种模式
 */
class LocalPreference(
    private val preferenceStore: PreferenceStore,
    private val pathProvider: PathProvider,
) {

    private val scope = CoroutineScope(SupervisorJob())

    /**
     * 是否使用私有目录
     */
    val usePrivate: Preference<Boolean> = preferenceStore.getBoolean(
        KEY_USE_PRIVATE, true
    )

    /**
     * 用户自选目录的 UFD（JSON 序列化）
     */
    val userFolderUFD: Preference<String> = preferenceStore.getString(
        KEY_USER_FOLDER_UFD, ""
    )

    /**
     * 是否创建 .nomedia
     */
    val noMedia: Preference<Boolean> = preferenceStore.getBoolean(
        KEY_NO_MEDIA, true
    )

    /**
     * 计算实际的本地根目录 UFD
     */
    val realLocalFolderUFD: StateFlow<UFD> = combine(
        usePrivate.flow(),
        userFolderUFD.flow()
    ) { isPrivate, userUfdJson ->
        if (isPrivate) {
            pathProvider.getFilePath("local_source")
        } else {
            if (userUfdJson.isNotEmpty()) {
                try {
                    Json.decodeFromString<UFD>(userUfdJson)
                } catch (e: Exception) {
                    pathProvider.getFilePath("local_source")
                }
            } else {
                pathProvider.getFilePath("local_source")
            }
        }
    }.stateIn(
        scope,
        SharingStarted.Lazily,
        if (usePrivate.get()) {
            pathProvider.getFilePath("local_source")
        } else {
            val json = userFolderUFD.get()
            if (json.isNotEmpty()) {
                try {
                    Json.decodeFromString<UFD>(json)
                } catch (e: Exception) {
                    pathProvider.getFilePath("local_source")
                }
            } else {
                pathProvider.getFilePath("local_source")
            }
        }
    )

    /**
     * 获取本地番剧根目录 UniFile
     */
    fun getLocalFolder(): org.easybangumi.next.lib.unifile.UniFile? {
        val ufd = realLocalFolderUFD.value
        val root = UniFileFactory.fromUFD(ufd) ?: return null

        val localBangumi = root.child(LOCAL_BANGUMI_DIR)
            ?: root.createDirectory(LOCAL_BANGUMI_DIR)?.let { root.child(LOCAL_BANGUMI_DIR) }

        if (noMedia.get() && localBangumi != null) {
            val nomediaFile = localBangumi.child(".nomedia")
            if (nomediaFile != null && !nomediaFile.exists()) {
                nomediaFile.openSink(false).buffer().use { it.writeUtf8("") }
            }
        }

        return localBangumi
    }

    /**
     * 设置用户自选目录
     */
    fun setUserFolder(ufd: UFD) {
        userFolderUFD.set(Json.encodeToString(ufd))
        usePrivate.set(false)
    }

    /**
     * 切换到私有目录
     */
    fun switchToPrivate() {
        usePrivate.set(true)
    }

    companion object {
        const val KEY_USE_PRIVATE = "local_use_private"
        const val KEY_USER_FOLDER_UFD = "local_user_folder_ufd"
        const val KEY_NO_MEDIA = "local_no_media"
        const val LOCAL_BANGUMI_DIR = "local_bangumi"
    }
}
