package com.heyanle.easybangumi4.download

import android.content.Context
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanlin on 2023/10/2.
 */
class DownloadController(
    private val context: Context,
    private val localCartoonController: LocalCartoonController,
) {

    private val scope = MainScope()
    private val rootFolder = File(context.getFilePath("download"))
    private val downloadItemJson = File(rootFolder, "item.json")
    private val downloadItemJsonTemp = File(rootFolder, "item.json.bk")

    private val _downloadItem = MutableStateFlow<List<DownloadItem>?>(null)
    val downloadItem = _downloadItem.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            if (!downloadItemJson.exists() && downloadItemJsonTemp.exists()) {
                downloadItemJsonTemp.renameTo(downloadItemJson)
            }
            if (downloadItemJson.exists()) {
                runCatching {
                    val d = downloadItemJson.readText().jsonTo<List<DownloadItem>>()
                    _downloadItem.update {
                        d
                    }
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }

        scope
    }

    fun updateDownloadItem(uuid: String, update: (DownloadItem) -> DownloadItem) {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                val prevValue = _downloadItem.value ?: emptyList()
                val new = prevValue.map {
                    if(it.uuid == uuid){
                        update(it)
                    }else{
                        it
                    }
                }
                if (_downloadItem.compareAndSet(prevValue, new)) {
                    save()
                    break
                }
            }
        }
    }

    fun update(update: ((List<DownloadItem>?) -> List<DownloadItem>)){
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                val prevValue = _downloadItem.value
                val new = update(prevValue)
                if (_downloadItem.compareAndSet(prevValue, new)) {
                    save()
                    break
                }
            }
        }
    }

    fun downloadItemCompletely(downloadItem: DownloadItem){
        localCartoonController.onComplete(downloadItem)
        scope.launch {
            while (isActive) {
                val prevValue = _downloadItem.value ?: emptyList()
                val new = prevValue - downloadItem
                if (_downloadItem.compareAndSet(prevValue, new)) {
                    save()
                    break
                }
            }
        }
    }

    private fun save() {
        _downloadItem.value?.let {
            downloadItemJsonTemp.delete()
            downloadItemJsonTemp.createNewFile()
            downloadItemJsonTemp.writeText(it.toJson())
            downloadItemJsonTemp.renameTo(downloadItemJson)
        }

    }


}