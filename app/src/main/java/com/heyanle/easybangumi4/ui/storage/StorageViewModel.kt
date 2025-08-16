package com.heyanle.easybangumi4.ui.storage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.cartoon.repository.db.CartoonDatabase
import com.heyanle.easybangumi4.case.ExtensionCase
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.IExtensionController
import com.heyanle.easybangumi4.storage.BackupController
import com.heyanle.easybangumi4.storage.RestoreController
import com.heyanle.easybangumi4.ui.common.moeDialogAlert
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2024/4/29.
 */
class StorageViewModel : ViewModel() {

    data class State(
        val needBackupCartoonData: Boolean = true,
        val cartoonCount: Int = -1,
        val needBackupPreferenceData: Boolean = false,
        val needBackupExtension: Boolean = false,
        val needBackupRepository: Boolean = false,
        val needExtensionPackageInfo: Set<ExtensionInfo> = setOf(),

        val needBackupSourcePref: Boolean = false,
        val needSourceSet: Set<ExtensionInfo> = setOf(),
        val extensionInfoList: List<ExtensionInfo> = emptyList(),
        val showBackupDialog: Boolean = false,
        val restoreDialogUri: Uri? = null,
        val isBackupDoing: Boolean = false,
        val isRestoreDoing: Boolean = false,
    )


    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val extensionController: ExtensionCase by Inject.injectLazy()

    private val cartoonDatabase: CartoonDatabase by Inject.injectLazy()

    private val restoreController: RestoreController by Inject.injectLazy()
    private val backupController: BackupController by Inject.injectLazy()

    init {
        cartoonDatabase.cartoonInfo.flowAll().map { it.size }
        viewModelScope.launch {
            combine(
                extensionController.flowExtension(),
                cartoonDatabase.cartoonInfo.flowAll().distinctUntilChanged().map { it.count { (it.starTime > 0L || it.lastHistoryTime > 0L) && !it.isLocal } }
            ) { extension, count ->
                extension to count
            }.collectLatest { pair ->
                _state.update {
                    it.copy(
                        extensionInfoList = pair.first.toList(),
                        cartoonCount = pair.second
                    )
                }
            }
        }
    }

    fun setNeedBackupCartoonStar(need: Boolean) {
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

    fun setNeedBackupExtensionRepository(need: Boolean) {
        _state.update {
            it.copy(needBackupRepository = need)
        }
    }

    fun toggleExtensionPackage(extensionInfo: ExtensionInfo) {
        _state.update {
            val set = if (it.needExtensionPackageInfo.contains(extensionInfo)) {
                it.needExtensionPackageInfo - extensionInfo
            } else {
                it.needExtensionPackageInfo + extensionInfo
            }
            it.copy(needExtensionPackageInfo = set)

        }
    }

    fun showBackupDialog() {
        _state.update {
            it.copy(showBackupDialog = true)
        }
    }

    fun showRestoreDialog(uri: Uri?) {
        _state.update {
            it.copy(restoreDialogUri = uri)
        }
    }

    fun dismissBackupDialog() {
        _state.update {
            it.copy(showBackupDialog = false)
        }
    }

    fun onBackup() {
        LauncherBus.current?.createDocument(
            LauncherBus.CreateDocumentReq(
                "application/zip",
                "${System.currentTimeMillis()}_ebg.backup.zip",
                APP.getCachePath()
            )
        ) {
            onBackup(it)
        }
    }


    fun onBackup(uri: Uri?) {
        if (uri == null) {
            stringRes(com.heyanle.easy_i18n.R.string.no_document).moeDialogAlert()
            return
        }
        _state.update {
            it.copy(
                isBackupDoing = true,
                showBackupDialog = false,
            )
        }
        viewModelScope.launch() {
            try {
                val cur = _state.value
                backupController.backup(
                    BackupController.BackupParam(
                        starCartoon = cur.needBackupCartoonData,
                        historyCartoon = cur.needBackupCartoonData,
                        preference = cur.needBackupPreferenceData,
                        extensionRepository = cur.needBackupRepository,
                        sourcePreferencesSource = emptySet(),
                        extensionList = if (cur.needBackupExtension) cur.needExtensionPackageInfo else emptySet(),
                    ),
                    uri
                )
            } catch (e: Exception) {
                e.printStackTrace()
                "${stringRes(R.string.backup_error)} $e".moeDialogAlert()
            }

            _state.update {
                it.copy(
                    isBackupDoing = false,
                )
            }
        }

    }

    fun onRestoreClick() {
        LauncherBus.current?.getBackupZip { uri ->
            if (uri == null) {
                stringRes(com.heyanle.easy_i18n.R.string.no_document).moeDialogAlert()
                return@getBackupZip
            }
            showRestoreDialog(uri)
        }
    }

    fun onRestore(uri: Uri) {
        _state.update {
            it.copy(
                isRestoreDoing = true,
                restoreDialogUri = null,
            )
        }
        viewModelScope.launch() {
            try {
                restoreController.restore(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                "${stringRes(R.string.restore_error)} $e".moeDialogAlert()
            }

            _state.update {
                it.copy(
                    isRestoreDoing = false,
                )
            }
        }
    }

}