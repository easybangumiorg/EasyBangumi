package com.heyanle.easybangumi4.storage

import android.net.Uri
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.storage.entity.CartoonStorage
import com.heyanle.easybangumi4.ui.common.moeDialogAlert
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toJson
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.json.JSONObject
import java.io.File
import java.util.zip.GZIPOutputStream

/**
 * Created by heyanle on 2024/7/26.
 * https://github.com/heyanLE
 */
class BackupController(
    private val cartoonInfoDao: CartoonInfoDao,
    private val settingMMKVPreferences: SettingMMKVPreferences,
    private val settingPreferences: SettingPreferences,
    private val jsonFileProvider: JsonFileProvider,
    private val globalHekv: HeKV
) {

    companion object {
        const val CartoonFolderName = "cartoon_info"
        const val CartoonFileName = "cartoon_info_list.jsonl.gz"

        const val PreferenceFolderName = "preference"
        const val MMKVFileName = "mmkv.json"
        const val SPFileName = "share_preference.json"
        const val HEKVFileName = "global_hekv.json"
        const val ManifestFileName = "manifest.json"

        const val ExtensionFolderName = "extension"

        const val SourcePrefFolderName = "source_pref"

        const val ExtensionRepositoryFileName = "extension_repository.jsonl"
    }

    private val cacheRoot = File(APP.getCachePath(), "backup")

    data class BackupParam(
        // 备份追番数据
        val starCartoon: Boolean = true,

        // 备份历史记录
        val historyCartoon: Boolean = false,

        // app 设置
        val preference: Boolean = true,

        val extensionRepository: Boolean = true,

        // 拓展
        val extensionList: Set<ExtensionInfo> = setOf(),

        // 源设置
        val sourcePreferencesSource: Set<Source> = setOf(),
    )


    suspend fun backup(param: BackupParam, uri: Uri) {
        withContext(Dispatchers.IO) {
            val fileName = "backup.zip"

            cacheRoot.mkdirs()

            val cacheFolder = File(cacheRoot, "temp_backup")
            cacheFolder.mkdirs()

            val uniFile = UniFile.fromUri(APP, uri)
            if (uniFile == null || !uniFile.canWrite()) {
                stringRes(R.string.create_document_err).moeDialogAlert()
                return@withContext
            }

            try {
                listOf(
                    async {
                        backupCartoon(File(cacheFolder, CartoonFolderName), param.starCartoon, param.historyCartoon)
                    },
                    async {
                        if (param.preference)
                            backupPreference(File(cacheFolder, PreferenceFolderName))
                    },
                    async {
                        if (param.extensionRepository)
                            backupExtensionRepository(cacheFolder)
                    },
                    async {
                        if (param.extensionList.isNotEmpty())
                            backupExtension(File(cacheFolder, ExtensionFolderName), param.extensionList)
                    },
                    async {
                        backupManifest(File(cacheFolder, ManifestFileName))
                    },
                    async {
                        if (param.sourcePreferencesSource.isNotEmpty())
                            backupSourcePreferences(File(cacheFolder, SourcePrefFolderName), param.sourcePreferencesSource)
                    }
                ).forEach {
                    it.await()
                }

                val targetZip = File(cacheRoot, fileName)
                val targetZip4j = ZipFile(targetZip)
                val targetZip4jParameters = ZipParameters()
                targetZip4jParameters.isIncludeRootFolder = false
                targetZip4j.addFolder(cacheFolder, targetZip4jParameters)
                uniFile.openOutputStream(false).use {
                    targetZip.inputStream().copyTo(it)
                }
                "${stringRes(R.string.backup_completely)} ".moeDialogAlert()
            } catch (e: Exception) {
                e.printStackTrace()
                "${stringRes(R.string.backup_error)} $e".moeDialogAlert()
            }
        }
    }

    private suspend fun backupCartoon(
        folder: File,
        star: Boolean,
        history: Boolean
    ) {

        if (!star && !history) return
        withContext(Dispatchers.IO){
            folder.deleteRecursively()
            folder.mkdirs()
            val file = File(folder, CartoonFileName)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            val cartoonList = cartoonInfoDao.flowAll().first()
                .filter {
                    (star && it.starTime > 0) || (history && it.lastHistoryTime > 0) && !it.isLocal
                }.map {
                    CartoonStorage.fromCartoonInfo(it, star, history)
                }
            GZIPOutputStream(file.outputStream()).bufferedWriter().use { writer ->
                cartoonList.forEach {
                    writer.write(it.toJson())
                    writer.newLine()
                }
                writer.flush()
            }
        }
    }
    private suspend fun backupPreference(folder: File) = withContext(Dispatchers.IO) {
        val mmkvO = JSONObject()
        val spO = JSONObject()
        val hekvO = JSONObject()

        folder.deleteRecursively()
        folder.mkdirs()

        settingMMKVPreferences.webViewCompatible.let {
            mmkvO.put(it.key(), it.get())
        }

        settingPreferences.apply {
            spO.put(isInPrivate.key(), isInPrivate.get())
            spO.put(darkMode.key(), darkMode.get().name)
            spO.put(isThemeDynamic.key(), isThemeDynamic.get())
            spO.put(themeMode.key(), themeMode.get().name)
            spO.put(padMode.key(), padMode.get().name)
            spO.put(playerOrientationMode.key(), playerOrientationMode.get().name)
            spO.put(useExternalVideoPlayer.key(), useExternalVideoPlayer.get())
            spO.put(playerBottomNavigationBarPadding.key(), playerBottomNavigationBarPadding.get())
            spO.put(cacheSize.key(), cacheSize.get())
            spO.put(customSpeed.key(), customSpeed.get())
            spO.put(fastWeight.key(), fastWeight.get())
            spO.put(fastSecond.key(), fastSecond.get())
            spO.put(fastTopSecond.key(), fastTopSecond.get())
            spO.put(detailedScreenEpisodeGridCount.key(), detailedScreenEpisodeGridCount.get())
        }

        globalHekv.map().forEach() {
            hekvO.put(it.key, it.value)
        }

        val mmkvFile = File(folder, MMKVFileName)
        val spFile = File(folder, SPFileName)
        val hekvFile = File(folder, HEKVFileName)

        mmkvFile.delete()
        spFile.delete()
        hekvFile.delete()

        mmkvFile.createNewFile()
        spFile.createNewFile()
        hekvFile.createNewFile()

        mmkvFile.writeText(mmkvO.toString())
        spFile.writeText(spO.toString())
        hekvFile.writeText(hekvO.toString())

    }

    private suspend fun backupExtensionRepository(folder: File) = withContext(Dispatchers.IO) {
        val list = jsonFileProvider.extensionRepository.getOrNull() ?: emptyList()
        val file = File(folder, ExtensionRepositoryFileName)
        file.delete()
        file.createNewFile()
        file.bufferedWriter().use { writer ->
            list.forEach {
                writer.write(it.toJson())
                writer.newLine()
            }
            writer.flush()
        }
    }
    private suspend fun backupExtension(folder: File, list: Set<ExtensionInfo>) = withContext(Dispatchers.IO) {
        folder.deleteRecursively()
        folder.mkdirs()
        var count = 0
        list.forEach {
            val source = File(it.sourcePath)
            if (source.exists() && source.isFile && source.canRead()) {
                val targetName =
                    "${it.pkgName}-${count++}.${it.suffix()}"
                val target = File(folder, targetName)
                source.copyTo(target)
            }
        }
    }
    private suspend fun backupSourcePreferences(folder: File, list: Set<Source>) = withContext(Dispatchers.IO) {
        folder.deleteRecursively()
        folder.mkdirs()
        for (source in list) {
            val preferenceHelper by Inject.injectLazy<PreferenceHelper>(source.key)
            val sourceData = preferenceHelper.map()

            if (sourceData.isEmpty()){
                continue
            }

            val sourceJson = File(folder, "${source.key}.json")
            sourceJson.delete()
            sourceJson.createNewFile()
            val sourceJsonO = JSONObject()
            sourceData.forEach {
                sourceJsonO.put(it.key, it.value)
            }

            sourceJson.writeText(sourceJsonO.toString())
        }
    }
    private suspend fun backupManifest(file: File) = withContext(Dispatchers.IO) {
        val manifestO = JSONObject()
        manifestO.put("from", "com.heyanle.easybangumi4")
        manifestO.put("version", BuildConfig.VERSION_CODE)
        manifestO.put("time", System.currentTimeMillis())
        file.delete()
        file.createNewFile()
        file.writeText(manifestO.toString())
    }
}