package com.heyanle.easybangumi4.storage

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.cartoon.repository.db.CartoonDatabase
import com.heyanle.easybangumi4.extension.Extension
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source.SourceController
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.ui.storage.restore.Restore
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.json.JSONObject
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by heyanlin on 2024/5/7.
 */
class StorageController(
    private val extensionController: ExtensionController,
    private val cartoonDatabase: CartoonDatabase,
    private val sourceController: SourceController,

    private val settingMMKVPreferences: SettingMMKVPreferences,
    private val settingPreferences: SettingPreferences,
    private val globalHekv: HeKV
) {

    private val backupZipRoot = File(APP.getFilePath(), "backup")
    private val cacheRoot = File(APP.getCachePath(), "backup")

    data class BackupParam(
        val needBackupCartoonData: Boolean = true,
        val needBackupPreferenceData: Boolean = false,
        val needBackupExtension: Boolean = false,
        val needBackupExtensionList: List<Extension> = emptyList(),
    )

    suspend fun backup(param: BackupParam){
        withContext(Dispatchers.IO){
            val time = System.currentTimeMillis()
            val date: Date = Date(time)
            val df: DateFormat =
                SimpleDateFormat("yyyy-MM-dd:HH-mm-ss", Locale.getDefault())
            val name = "${df.format(date)}:${time}"
            val fileName = "${name}.easybangumi.backup.zip"


            val cacheFolder = File(cacheRoot, name)
            cacheFolder.mkdirs()

            try {
                listOf(
                    async {
                        if (param.needBackupCartoonData)
                            backupCartoon(File(cacheFolder, "cartoon_info"))
                    },
                    async {
                        if (param.needBackupPreferenceData)
                            backupPreference(File(cacheFolder, "preference"))
                    },
                    async {
                        if (param.needBackupExtension)
                            backupExtension(File(cacheFolder, "extension"), param)
                    },
                    async {
                        backupManifest(File(cacheFolder, "manifest.json"))
                    }
                ).forEach {
                    it.await()
                }

                val targetZip = File(backupZipRoot, fileName)
                val targetZipTemp = File(backupZipRoot, "${fileName}.temp")
                val targetZip4j = ZipFile(targetZipTemp)
                val targetZip4jParameters = ZipParameters()
                targetZip4jParameters.isIncludeRootFolder = false
                targetZip4j.addFolder(cacheFolder, targetZip4jParameters)
                targetZip.delete()
                targetZipTemp.renameTo(targetZip)
                "${stringRes(com.heyanle.easy_i18n.R.string.backup_completely)} ${targetZip.name}".moeDialog()
            } catch (e: Exception) {
                e.printStackTrace()
                "${stringRes(com.heyanle.easy_i18n.R.string.backup_error)} $e".moeDialog()
            }
        }
    }

    private suspend fun backupCartoon(folder: File) {
        folder.deleteRecursively()
        folder.mkdirs()
        val cartoonInfoDB = APP.getDatabasePath("easy_bangumi_cartoon.db") ?: return
        val cartoonInfoDBShm = APP.getDatabasePath("easy_bangumi_cartoon.db-shm") ?: return
        val cartoonInfoDBWal = APP.getDatabasePath("easy_bangumi_cartoon.db-wal") ?: return
        val cacheTargetDB = File(folder, "easy_bangumi_cartoon.db")
        val cacheTargetDBShm = File(folder, "easy_bangumi_cartoon.db-shm")
        val cacheTargetDBWal = File(folder, "easy_bangumi_cartoon.db-wal")

        kotlin.runCatching {
            cacheTargetDB.delete()
            cartoonInfoDB.copyTo(cacheTargetDB, true)
        }
        kotlin.runCatching {
            cacheTargetDBShm.delete()
            cartoonInfoDBShm.copyTo(cacheTargetDBShm, true)
        }
        kotlin.runCatching {
            cacheTargetDBWal.delete()
            cartoonInfoDBWal.copyTo(cacheTargetDBWal, true)
        }

    }

    private suspend fun backupPreference(folder: File) {
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
            spO.put(cartoonInfoCacheTimeHour.key(), cartoonInfoCacheTimeHour.get())
            spO.put(downloadPath.key(), downloadPath.get())
            spO.put(customSpeed.key(), customSpeed.get())
            spO.put(fastWeight.key(), fastWeight.get())
            spO.put(fastSecond.key(), fastSecond.get())
        }

        globalHekv.map().forEach() {
            hekvO.put(it.key, it.value)
        }

        val mmkvFile = File(folder, "mmkv.json")
        val spFile = File(folder, "share_preference.json")
        val hekvFile = File(folder, "global_hekv.json")

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

    private suspend fun backupExtension(folder: File, param: BackupParam) {
        folder.deleteRecursively()
        folder.mkdirs()
        var count = 0
        param.needBackupExtensionList.forEach {
            val source = File(it.sourcePath)
            if (source.exists() && source.isFile && source.canRead()) {
                val targetName =
                    "${it.pkgName}-${System.currentTimeMillis()}-${count++}.easybangumi.apk"
                val target = File(folder, targetName)
                source.copyTo(target)
            }
        }
    }

    private suspend fun backupManifest(file: File) {
        val manifestO = JSONObject()
        manifestO.put("from", "com.heyanle.easybangumi4")
        manifestO.put("version", BuildConfig.VERSION_CODE)
        manifestO.put("time", System.currentTimeMillis())

        file.delete()
        file.createNewFile()
        file.writeText(manifestO.toString())
    }


    suspend fun restore(file: File){
        withContext(Dispatchers.IO){
            kotlin.runCatching {
                // 1. 解压
                val targetFolder = File(cacheRoot, file.nameWithoutExtension)
                targetFolder.deleteRecursively()
                targetFolder.mkdirs()
                ZipFile(file).extractAll(targetFolder.absolutePath)

                val manifestFile = File(targetFolder, "manifest.json")
                if (!manifestFile.exists()) {
                    throw Exception("manifest.json not found")
                }

                // 2. 读取manifest
                val manifest = manifestFile.readText()
                val jsonObject = JSONObject(manifest)

                val packageName = jsonObject.optString("from")
                val version = jsonObject.optInt("version")
                val time = jsonObject.optLong("time")

                val res = migrate(targetFolder, version)
                if (!res){
                    throw Exception("migrate failed $version -> ${BuildConfig.VERSION_CODE}")
                }

                // 3. 恢复
                val cartoonFolder = File(targetFolder, "cartoon_info")
                if (cartoonFolder.exists()) {
                    restoreCartoon(cartoonFolder)
                }

                val preferenceFolder = File(targetFolder, "preference")
                if (preferenceFolder.exists()) {
                    restorePreference(preferenceFolder)
                }

                val extensionFolder = File(targetFolder, "extension")
                if (extensionFolder.exists()) {
                    restoreExtension(extensionFolder)
                }

                // 4. 删除
                targetFolder.deleteRecursively()
                "${stringRes(com.heyanle.easy_i18n.R.string.restore_completely)}".moeDialog()
            }.onFailure {
                it.printStackTrace()
                "${stringRes(com.heyanle.easy_i18n.R.string.restore_error)} $it".moeDialog()
            }
        }
    }

    private suspend fun migrate(folder: File, version: Int): Boolean {

        // 在这里迁移

        return true
    }

    private suspend fun restoreCartoon(folder: File): Boolean {
        val targetDB = File(folder, "easy_bangumi_cartoon.db")

        if (targetDB.exists()){
            return false
        }

        val database = CartoonDatabase.build(APP, targetDB.absolutePath)

        val cartoonInfo = database.cartoonInfoDao()
        val cartoonTagDao = database.cartoonTagDao()

        val currentCartoonDao = cartoonDatabase.cartoonInfoDao()
        val currentCartoonTagDao = cartoonDatabase.cartoonTagDao()

        cartoonInfo.getAll().forEach {

        }

    }

    private suspend fun restorePreference(folder: File) {
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
            spO.put(cartoonInfoCacheTimeHour.key(), cartoonInfoCacheTimeHour.get())
            spO.put(downloadPath.key(), downloadPath.get())
            spO.put(customSpeed.key(), customSpeed.get())
            spO.put(fastWeight.key(), fastWeight.get())
            spO.put(fastSecond.key(), fastSecond.get())
        }

        globalHekv.map().forEach() {
            hekvO.put(it.key, it.value)
        }

        val mmkvFile = File(folder, "mmkv.json")
        val spFile = File(folder, "share_preference.json")
        val hekvFile = File(folder, "global_hekv.json")

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

    private suspend fun restoreExtension(folder: File) {
        val cur = state.value
        folder.deleteRecursively()
        folder.mkdirs()
        var count = 0
        cur.needExtensionPackage.forEach {
            val source = File(it.sourcePath)
            if (source.exists() && source.isFile && source.canRead()) {
                val targetName =
                    "${it.pkgName}-${System.currentTimeMillis()}-${count++}.easybangumi.apk"
                val target = File(folder, targetName)
                source.copyTo(target)
            }
        }
    }

}