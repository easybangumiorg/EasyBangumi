package com.heyanle.easybangumi4.download.step

import com.heyanle.easybangumi4.download.DownloadBus
import com.heyanle.easybangumi4.download.DownloadController
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by HeYanLe on 2023/10/3 0:05.
 * https://github.com/heyanLE
 */
class CopyStep(
    private val downloadController: DownloadController,
    private val downloadBus: DownloadBus,
) : BaseStep {

    companion object {
        const val NAME = "copy"
    }

    private val scope = MainScope()
    override fun invoke(downloadItem: DownloadItem) {
        scope.launch(Dispatchers.IO) {
            try {
                val source = File(downloadItem.bundle.filePathBeforeCopy)
                val target = File(downloadItem.folder, downloadItem.fileNameWithoutSuffix + ".mp4")
                if (source.absolutePath == target.absolutePath) {
                    downloadController.updateDownloadItem(downloadItem.uuid) {
                        it.copy(
                            state = 2
                        )
                    }
                    return@launch
                }
                scope.launch {
                    downloadBus.getInfo(downloadItem.uuid).apply {
                        status.value = stringRes(com.heyanle.easy_i18n.R.string.copying)
                        this.subStatus.value = ""
                        this.process.value = 0f
                    }
                }
                if (!target.exists()) {
                    downloadController.updateDownloadItem(downloadItem.uuid) {
                        it.copy(
                            state = -1
                        )
                    }
                    return@launch
                }
                target.delete()
                source.copyTo(target)
                source.delete()
                downloadController.updateDownloadItem(downloadItem.uuid) {
                    it.copy(
                        state = 2
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                downloadController.updateDownloadItem(downloadItem.uuid) {
                    it.copy(
                        state = -1,
                        errorMsg = e.message ?: ""
                    )
                }
            }

        }
    }

    override fun onRemove(downloadItem: DownloadItem) {
        downloadController.updateDownloadItem(downloadItem.uuid){
            it.copy(isRemoved = true)
        }
    }


}