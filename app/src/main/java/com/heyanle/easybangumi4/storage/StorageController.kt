package com.heyanle.easybangumi4.storage

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
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
import com.heyanle.easybangumi4.theme.EasyThemeMode
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.storage.restore.Restore
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.json.JSONObject
import org.koin.dsl.koinApplication
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

    // 备份 =========================================

    private val backupZipRoot = File(APP.getFilePath(), "backup")
    private val cacheRoot = File(APP.getCachePath(), "backup")

    data class BackupParam(
        val needBackupCartoonData: Boolean = true,
        val needBackupPreferenceData: Boolean = false,
        val needBackupExtension: Boolean = false,
        val needBackupExtensionList: Set<Extension> = setOf(),
    )

    suspend fun backup(param: BackupParam) {
        withContext(Dispatchers.IO) {
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
                "${stringRes(com.heyanle.easy_i18n.R.string.backup_completely, targetZip.name)} ".moeDialog()
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

    // 恢复 =========================================

    suspend fun restore(file: File) {
        withContext(Dispatchers.IO) {
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

                val res = migrate(targetFolder, version)
                if (!res) {
                    throw Exception("migrate failed $version -> ${BuildConfig.VERSION_CODE}")
                }

                // 3. 恢复
                listOf(
                    async {
                        restoreCartoon(File(targetFolder, "cartoon_info"))
                    },
                    async {
                        restorePreference(File(targetFolder, "preference"))
                    },
                    async {
                        restoreExtension(File(targetFolder, "extension"))
                    }
                ).forEach {
                    it.await()
                }


                // 4. 删除
                targetFolder.deleteRecursively()
                stringRes(com.heyanle.easy_i18n.R.string.restore_completely).moeSnackBar()
            }.onFailure {
                it.printStackTrace()
                "${stringRes(com.heyanle.easy_i18n.R.string.restore_error)} $it".moeDialog()
            }
        }
    }

    private suspend fun migrate(folder: File, version: Int): Boolean {

        // 在这里迁移
        if (version < 85){
            return false
        }

        return true
    }

    private suspend fun restoreCartoon(folder: File): Boolean {
        val targetDB = File(folder, "easy_bangumi_cartoon.db")

        if (targetDB.exists()) {
            return false
        }

        val database = CartoonDatabase.build(APP, targetDB.absolutePath)

        val cartoonInfo = database.cartoonInfoDao()
        val cartoonTagDao = database.cartoonTagDao()

        val currentCartoonDao = cartoonDatabase.cartoonInfoDao()
        val currentCartoonTagDao = cartoonDatabase.cartoonTagDao()

        val tagIdMap = hashMapOf<Int, Int>()
        cartoonTagDao.getAll().forEach {
            val currTag = currentCartoonTagDao.findByLabel(it.label).firstOrNull()
            if (currTag == null) {
                currentCartoonTagDao.insert(it)
                val realTag = currentCartoonTagDao.findByLabel(it.label).firstOrNull()
                if (realTag != null) {
                    tagIdMap[it.id] = realTag.id
                }
            } else {
                tagIdMap[it.id] = currTag.id
            }
        }

        cartoonInfo.getAll().forEach {
            val tags = it.tags.split(",").map {
                tagIdMap[it.trim().toIntOrNull()?:-1] ?: -1
            }.filter {
                it != -1
            }.joinToString(", ")
            val new = it.copy(tags = tags)
            currentCartoonDao.modify(new)
        }
        return true
    }

    private suspend fun restorePreference(folder: File): Boolean {

        val mmkvFile = File(folder, "mmkv.json")
        val spFile = File(folder, "share_preference.json")
        val hekvFile = File(folder, "global_hekv.json")

        if (mmkvFile.exists()){
            val mmkvO = JSONObject(mmkvFile.readText())
            settingMMKVPreferences.webViewCompatible.let {
                it.set(mmkvO.optBoolean(it.key(), it.get()))
            }
        }
        if (spFile.exists()){
            val spO = JSONObject(spFile.readText())
            settingPreferences.apply {
                isInPrivate.set(spO.optBoolean(isInPrivate.key(), isInPrivate.get()))
                darkMode.set(SettingPreferences.DarkMode.valueOf(spO.optString(darkMode.key(), darkMode.get().name)))
                isThemeDynamic.set(spO.optBoolean(isThemeDynamic.key(), isThemeDynamic.get()))
                themeMode.set(EasyThemeMode.valueOf(spO.optString(themeMode.key(), themeMode.get().name)))
                padMode.set(SettingPreferences.PadMode.valueOf(spO.optString(padMode.key(), padMode.get().name)))
                playerOrientationMode.set(SettingPreferences.PlayerOrientationMode.valueOf(spO.optString(playerOrientationMode.key(), playerOrientationMode.get().name)))
                useExternalVideoPlayer.set(spO.optBoolean(useExternalVideoPlayer.key(), useExternalVideoPlayer.get()))
                playerBottomNavigationBarPadding.set(spO.optBoolean(playerBottomNavigationBarPadding.key(), playerBottomNavigationBarPadding.get()))

                cacheSize.set(spO.optLong(cacheSize.key(), cacheSize.get()))
                cartoonInfoCacheTimeHour.set(spO.optLong(cartoonInfoCacheTimeHour.key(), cartoonInfoCacheTimeHour.get()))
                downloadPath.set(spO.optString(downloadPath.key(), downloadPath.get()))
                customSpeed.set(spO.optDouble(customSpeed.key(), customSpeed.get().toDouble()).toFloat())
                fastWeight.set(spO.optInt(fastWeight.key(), fastWeight.get()))
                fastSecond.set(spO.optInt(fastSecond.key(), fastSecond.get()))
            }
        }

        if (hekvFile.exists()){
            val hekvO = JSONObject(hekvFile.readText())
            hekvO.keys().forEach {
                globalHekv.put(it, hekvO.optString(it))
            }
        }

       return true

    }

    private suspend fun restoreExtension(folder: File): Boolean {
        if (!folder.exists()){
            return false
        }
        folder.listFiles()?.forEach {
            if (it.isFile && it.canRead() && it.name.endsWith(".easybangumi.apk")){
                extensionController.appendExtensionPath(it.absolutePath){
                    it?.printStackTrace()
                }
            }
        }
        return true
    }

    // 导出 =========================================


    suspend fun saveToDownload(file: File) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val downloadFile =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val targetRoot = File(downloadFile, "EasyBangumi/backup")
                    val target = File(targetRoot, file.name)
                    file.copyTo(target, true)
                } else {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/EasyBangumi/backup")
                    }
                    APP.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?.let { uri ->
                            APP.contentResolver.openOutputStream(uri)
                        }?.use {
                            file.inputStream().copyTo(it)
                        }
                }
            }.onFailure {
                it.printStackTrace()
            }

        }
    }

    suspend fun share(file: File) {
        if (file.exists()){
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/zip"
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    APP,
                    "${APP.packageName}.fileProvider",
                    file
                ).let {
                    intent.putExtra(Intent.EXTRA_STREAM, it)
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }else{
                intent.putExtra(Intent.EXTRA_STREAM, file.toURI())
            }

            val realI = Intent.createChooser(intent, stringRes(com.heyanle.easy_i18n.R.string.share_file))
            realI.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            APP.startActivity(realI)
        }
    }
}