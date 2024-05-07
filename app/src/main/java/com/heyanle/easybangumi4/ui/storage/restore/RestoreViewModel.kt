package com.heyanle.easybangumi4.ui.storage.restore

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.storage.StorageController
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import org.json.JSONObject
import java.io.File

/**
 * Created by heyanlin on 2024/4/30.
 */
class RestoreViewModel : ViewModel() {

    companion object {
        const val TAG = "RestoreViewModel"
    }

    private val backupZipRoot = File(APP.getFilePath(), "backup")
    private val cacheRoot = File(APP.getCachePath(), "restore")

    private val storageController: StorageController by Injekt.injectLazy()

    data class RestoreState(
        val backupFileList: List<File> = emptyList(),
        val isRestoreDoing: Boolean = false,

        val restoreDialogFile: File? = null,
        val deleteDialogFile: File? = null,
    )

    private val fileObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ExtensionFolderObserverQ(backupZipRoot.absolutePath)
    } else ExtensionFolderObserver(backupZipRoot.absolutePath)


    private val _state = MutableStateFlow(RestoreState())
    val state = _state.asStateFlow()

    fun refresh() {
        (backupZipRoot.listFiles()?.toList()?.filterIsInstance<File>()?.sortedByDescending { it.name }
            ?: emptyList<File>()).let { list ->
            _state.update {
                it.copy(backupFileList = list)
            }
        }
    }

    fun onLaunch() {
        fileObserver.startWatching()
        refresh()
    }

    fun onDisposed() {
        fileObserver.stopWatching()
    }

    override fun onCleared() {
        onDisposed()
        super.onCleared()
    }

    fun share(file: File) {
        viewModelScope.launch {
            storageController.share(file)
        }
    }

    fun saveToDownload(file: File) {
        viewModelScope.launch() {
            storageController.saveToDownload(file)
        }
    }

    fun showDeleteDialog(file: File) {
        _state.update {
            it.copy(deleteDialogFile = file)
        }
    }

    fun dismissDeleteDialog() {
        _state.update {
            it.copy(deleteDialogFile = null)
        }
    }

    fun showRestoreDialog(file: File) {
        _state.update {
            it.copy(restoreDialogFile = file)
        }
    }

    fun dismissRestoreDialog() {
        _state.update {
            it.copy(restoreDialogFile = null)
        }
    }

    fun delete(file: File) {
        _state.update {
            it.copy(restoreDialogFile = null)
        }
        kotlin.runCatching {
            file.delete()
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun restore(file: File) {
        _state.update {
            it.copy(restoreDialogFile = null, isRestoreDoing = true)
        }
        viewModelScope.launch() {
            kotlin.runCatching {
                storageController.restore(file)
            }.onFailure {
                it.printStackTrace()
            }
            _state.update {
                it.copy(isRestoreDoing = false)
            }
        }
    }

    // 观察文件夹
    @RequiresApi(Build.VERSION_CODES.Q)
    inner class ExtensionFolderObserverQ(private val extensionFolder: String) :
        FileObserver(
            File(extensionFolder),
            DELETE_SELF or DELETE or CREATE or MOVED_FROM or MOVED_TO
        ) {

        override fun onEvent(event: Int, path: String?) {
            "${event} ${path} onEvent".logi(TAG)
            if (event and DELETE == DELETE || event and DELETE_SELF == DELETE_SELF || path != null) {
                refresh()
            }
        }
    }

    inner class ExtensionFolderObserver(private val extensionFolder: String) :
        FileObserver(
            extensionFolder,
            DELETE_SELF or DELETE or CREATE or MOVED_FROM or MOVED_TO
        ) {
        override fun onEvent(event: Int, path: String?) {
            "${event} ${path} onEvent".logi(TAG)
            if (event and DELETE == DELETE || event and DELETE_SELF == DELETE_SELF || path != null) {
                refresh()
            }
        }

    }

}