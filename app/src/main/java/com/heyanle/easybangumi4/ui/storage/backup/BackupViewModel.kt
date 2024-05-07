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
import com.heyanle.easybangumi4.storage.StorageController
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

    private val storageController: StorageController by Injekt.injectLazy()

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
        viewModelScope.launch() {

            val time = System.currentTimeMillis()
            val date: Date = Date(time)
            val df: DateFormat =
                SimpleDateFormat("yyyy-MM-dd:HH-mm-ss", Locale.getDefault())
            val name = "${df.format(date)}:${time}"
            val fileName = "${name}.easybangumi.backup.zip"


            val cacheFolder = File(cacheRoot, name)
            cacheFolder.mkdirs()


            try {
                val cur = _state.value
                storageController.backup(
                    StorageController.BackupParam(
                        needBackupCartoonData = cur.needBackupCartoonData,
                        needBackupPreferenceData = cur.needBackupPreferenceData,
                        needBackupExtension = cur.needBackupExtension,
                        needBackupExtensionList = cur.needExtensionPackage
                    )
                )

            } catch (e: Exception) {
                e.printStackTrace()
                "备份错误 $e".moeDialog()
            }

            _state.update {
                it.copy(
                    isBackupDoing = false,
                )
            }
        }

    }



}