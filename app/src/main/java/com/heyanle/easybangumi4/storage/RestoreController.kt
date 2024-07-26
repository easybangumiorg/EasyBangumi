package com.heyanle.easybangumi4.storage

import android.net.Uri
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.storage.entity.CartoonStorage
import com.heyanle.easybangumi4.theme.EasyThemeMode
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import org.json.JSONObject
import java.io.File
import java.util.zip.GZIPInputStream

/**
 * Created by heyanle on 2024/7/26.
 * https://github.com/heyanLE
 */
class RestoreController(
    private val extensionController: ExtensionController,

    private val cartoonInfoDao: CartoonInfoDao,

    private val settingMMKVPreferences: SettingMMKVPreferences,
    private val settingPreferences: SettingPreferences,
    private val globalHekv: HeKV
) {
    private val cacheRoot = File(APP.getCachePath(), "restore")
    suspend fun restore(uri: Uri) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                // 1. 解压
                val uniFile = UniFile.fromUri(APP, uri) ?: throw Exception("uri error")
                val targetFile = File(cacheRoot, "restore.zip")
                cacheRoot.mkdirs()
                targetFile.deleteRecursively()
                targetFile.createNewFile()
                uniFile.openInputStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val targetFolder = File(cacheRoot, "restore_unzip_temp")
                targetFolder.deleteRecursively()
                targetFolder.mkdirs()
                targetFile.deleteOnExit()
                targetFolder.deleteOnExit()
                ZipFile(targetFile).extractAll(targetFolder.absolutePath)

                val manifestFile = File(targetFolder, BackupController.ManifestFileName)
                if (!manifestFile.exists()) {
                    throw Exception("manifest.json not found")
                }

                // 2. 读取manifest 并 检查
                val manifest = manifestFile.readText()
                val jsonObject = JSONObject(manifest)

                val packageName = jsonObject.optString("from")
                val version = jsonObject.optInt("version")
                val time = jsonObject.optLong("time")

                if (packageName != "com.heyanle.easybangumi4") {
                    throw Exception("package name not match")
                }

                if (time > System.currentTimeMillis()) {
                    throw Exception("time error")
                }

                val res = StorageMigrate.migrate(targetFolder, version)
                if (!res) {
                    "无法恢复旧版备份".moeSnackBar()
                    throw Exception("migrate failed $version -> ${BuildConfig.VERSION_CODE}")
                }

                // 3. 恢复
                listOf(
                    async {
                        restoreCartoonInfo(File(targetFolder, BackupController.CartoonFolderName))
                    },
                    async {
                        restorePreference(File(targetFolder, BackupController.PreferenceFolderName))
                    },
                    async {
                        restoreExtension(File(targetFolder, BackupController.ExtensionFolderName))
                    },
                    async {
                        restoreSourcePreference(File(targetFolder, BackupController.SourcePrefFolderName))
                    }
                ).forEach {
                    it.await()
                }

                // 4. 删除
                targetFile.deleteRecursively()
                targetFolder.deleteRecursively()

                stringRes(R.string.restore_completely).moeSnackBar()
            }.onFailure {
                it.printStackTrace()
                "${stringRes(R.string.restore_error)} $it".moeDialog()
            }
        }
    }
    private suspend fun restoreCartoonInfo(file: File): Boolean {
        val cartoonInfoFolder = File(file, BackupController.CartoonFileName)
        if (!cartoonInfoFolder.exists()) {
            // 文件不存在是否是恢复成功语义，这里有待商榷
            return true
        }

        try {
            // jsonl.gz 文件读取
            GZIPInputStream(cartoonInfoFolder.inputStream()).bufferedReader().use {
                var line: String?
                // 批处理
                val cartoonInfoTemp = mutableListOf<CartoonInfo>()
                do {
                    line = it.readLine() ?: break
                    val cartoon = line.jsonTo<CartoonStorage>() ?: continue
                    cartoonInfoTemp.add(cartoon.toCartoonInfo())
                    if (cartoonInfoTemp.size >= 100) {
                        cartoonInfoDao.modify(cartoonInfoTemp)
                        cartoonInfoTemp.clear()
                    }
                    cartoonInfoDao.modify(cartoon.toCartoonInfo())
                } while (line != null)
                if (cartoonInfoTemp.isNotEmpty()) {
                    cartoonInfoDao.modify(cartoonInfoTemp)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }
    private suspend fun restorePreference(file: File): Boolean {
        try {
            val mmkvFile = File(file, BackupController.MMKVFileName)
            val spFile = File(file, BackupController.SPFileName)
            val hekvFile = File(file, BackupController.HEKVFileName)

            if (mmkvFile.exists() && mmkvFile.canRead()) {
                val mmkvO = JSONObject(mmkvFile.readText())
                settingMMKVPreferences.webViewCompatible.let {
                    it.set(mmkvO.optBoolean(it.key(), it.get()))
                }
            }

            if (hekvFile.exists() && hekvFile.canRead()) {
                val hekvO = JSONObject(hekvFile.readText())
                hekvO.keys().forEach {
                    globalHekv.put(it, hekvO.optString(it))
                }
            }

            if (spFile.exists()) {
                val spO = JSONObject(spFile.readText())
                settingPreferences.apply {
                    isInPrivate.set(spO.optBoolean(isInPrivate.key(), isInPrivate.get()))
                    darkMode.set(
                        SettingPreferences.DarkMode.valueOf(
                            spO.optString(
                                darkMode.key(),
                                darkMode.get().name
                            )
                        )
                    )
                    isThemeDynamic.set(spO.optBoolean(isThemeDynamic.key(), isThemeDynamic.get()))
                    themeMode.set(
                        EasyThemeMode.valueOf(
                            spO.optString(
                                themeMode.key(),
                                themeMode.get().name
                            )
                        )
                    )
                    padMode.set(
                        SettingPreferences.PadMode.valueOf(
                            spO.optString(
                                padMode.key(),
                                padMode.get().name
                            )
                        )
                    )
                    playerOrientationMode.set(
                        SettingPreferences.PlayerOrientationMode.valueOf(
                            spO.optString(
                                playerOrientationMode.key(),
                                playerOrientationMode.get().name
                            )
                        )
                    )
                    useExternalVideoPlayer.set(
                        spO.optBoolean(
                            useExternalVideoPlayer.key(),
                            useExternalVideoPlayer.get()
                        )
                    )
                    playerBottomNavigationBarPadding.set(
                        spO.optBoolean(
                            playerBottomNavigationBarPadding.key(),
                            playerBottomNavigationBarPadding.get()
                        )
                    )
                    cacheSize.set(spO.optLong(cacheSize.key(), cacheSize.get()))
                    customSpeed.set(
                        spO.optDouble(customSpeed.key(), customSpeed.get().toDouble()).toFloat()
                    )
                    fastWeight.set(spO.optInt(fastWeight.key(), fastWeight.get()))
                    fastSecond.set(spO.optInt(fastSecond.key(), fastSecond.get()))
                    fastTopSecond.set(spO.optInt(fastTopSecond.key(), fastTopSecond.get()))
                    detailedScreenEpisodeGridCount.set(
                        spO.optInt(
                            detailedScreenEpisodeGridCount.key(),
                            detailedScreenEpisodeGridCount.get()
                        )
                    )
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }
    private suspend fun restoreExtension(folder: File): Boolean {
        try {
            if (!folder.exists()){
                return false
            }
            folder.listFiles()?.forEach {
                if (it.isFile && it.canRead()){
                    extensionController.appendExtensionPath(it.absolutePath){
                        it?.printStackTrace()
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }
    private suspend fun restoreSourcePreference(folder: File){
        if (!folder.exists()){
            return
        }
        folder.listFiles()?.forEach {
            if (it.isFile && it.canRead() && it.name.endsWith(".json")){
                val sourceKey = it.nameWithoutExtension
                val preferenceHelper by Inject.injectLazy<PreferenceHelper>(sourceKey)
                val sourceData = JSONObject(it.readText())
                sourceData.keys().forEach {
                    preferenceHelper.put(it, sourceData.optString(it))
                }
            }
        }
    }
}