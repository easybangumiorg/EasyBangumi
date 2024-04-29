package com.heyanle.easybangumi4.ui.storage.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.cartoon.repository.db.CartoonDatabase
import com.heyanle.easybangumi4.extension.Extension
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.json.JSONObject
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by heyanlin on 2024/4/29.
 */
class BackupViewModel : ViewModel() {

    data class State(
        val needBackupCartoonData: Boolean = true,
        val cartoonCount: Int = -1,
        val needBackupPreferenceData: Boolean = false,
        val needBackupExtension: Boolean = false,
        val extensionList: List<Extension> = emptyList(),
        val needExtensionPackage: Set<Extension> = hashSetOf(),
        val showBackupDialog: Boolean = false,
        val isBackupDoing: Boolean = false,
    )

    private val backupZipRoot = File(APP.getFilePath(), "backup")
    private val cacheRoot = File(APP.getCachePath(), "backup")

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val extensionController: ExtensionController by Injekt.injectLazy()

    private val cartoonDatabase: CartoonDatabase by Injekt.injectLazy()

    private val settingMMKVPreferences: SettingMMKVPreferences by Injekt.injectLazy()
    private val settingPreferences: SettingPreferences by Injekt.injectLazy()
    private val globalHekv: HeKV by Injekt.injectLazy()

    init {
        cartoonDatabase.cartoonInfo.flowAll().map { it.size }
        viewModelScope.launch {
            combine(
                extensionController.state,
                cartoonDatabase.cartoonInfo.flowAll().map { it.size }
            ) { extension, count ->
                extension to count
            }.collectLatest { pair ->
                _state.update {
                    it.copy(
                        extensionList = pair.first.listExtension,
                        cartoonCount = pair.second
                    )
                }
            }
        }
    }

    fun setNeedBackupCartoonData(need: Boolean) {
        _state.update {
            it.copy(needBackupCartoonData = need)
        }
    }

    fun setNeedBackupPreferenceData(need: Boolean) {
        _state.update {
            it.copy(needBackupPreferenceData = need)
        }
    }

    fun setNeedBackupExtension(need: Boolean) {
        _state.update {
            it.copy(needBackupExtension = need)
        }
    }

    fun toggleExtensionPackage(extension: Extension) {
        _state.update {
            val set = if (it.needExtensionPackage.contains(extension)) {
                it.needExtensionPackage - extension
            } else {
                it.needExtensionPackage + extension
            }
            it.copy(needExtensionPackage = set)

        }
    }

    fun showBackupDialog() {
        _state.update {
            it.copy(showBackupDialog = true)
        }
    }

    fun dismissBackupDialog() {
        _state.update {
            it.copy(showBackupDialog = false)
        }
    }

    fun onBackup() {
        _state.update {
            it.copy(
                isBackupDoing = true,
                showBackupDialog = false,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {

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
                        if (state.value.needBackupCartoonData)
                            backupCartoon(File(cacheFolder, "cartoon_info"))
                    },
                    async {
                        if (state.value.needBackupPreferenceData)
                            backupPreference(File(cacheFolder, "preference"))
                    },
                    async {
                        if (state.value.needBackupExtension)
                            backupExtension(File(cacheFolder, "extension"))
                    },
                    async {
                        backupManifest(File(cacheFolder, "manifest.json"))
                    }
                ).forEach {
                    it.await()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                "备份错误 $e".moeDialog()
                _state.update {
                    it.copy(
                        isBackupDoing = false,
                    )
                }
                return@launch
            }


            val targetZip = File(backupZipRoot, fileName)
            val targetZipTemp = File(backupZipRoot, "${fileName}.temp")
            val targetZip4j = ZipFile(targetZipTemp)
            val targetZip4jParameters = ZipParameters()
            targetZip4jParameters.isIncludeRootFolder = false
            targetZip4j.addFolder(cacheFolder, targetZip4jParameters)
            targetZip.delete()
            targetZipTemp.renameTo(targetZip)

            _state.update {
                it.copy(
                    isBackupDoing = false,
                )
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

    private suspend fun backupExtension(folder: File) {
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

    private suspend fun backupManifest(file: File) {
        val manifestO = JSONObject()
        manifestO.put("from", "com.heyanle.easybangumi4")
        manifestO.put("version", BuildConfig.VERSION_CODE)
        manifestO.put("time", System.currentTimeMillis())

        file.delete()
        file.createNewFile()
        file.writeText(manifestO.toString())
    }


}