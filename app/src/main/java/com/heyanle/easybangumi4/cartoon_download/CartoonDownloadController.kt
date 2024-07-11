package com.heyanle.easybangumi4.cartoon_download

import android.content.Context
import com.heyanle.easybangumi4.cartoon_download.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon_download.step.BaseStep
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadController(
    private val context: Context,
) {

    private val scope = MainScope()
    private val rootFolder = File(context.getFilePath("download")).apply {
        mkdirs()
    }
    private val downloadItemJson = File(rootFolder, "item.json")
    private val downloadItemJsonTemp = File(rootFolder, "item.json.bk")

    private val _downloadItem = MutableStateFlow<List<CartoonDownloadReq>?>(null)
    val downloadItem = _downloadItem.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            if (!downloadItemJson.exists() && downloadItemJsonTemp.exists()) {
                downloadItemJsonTemp.renameTo(downloadItemJson)
            }
            if (downloadItemJson.exists()) {
                runCatching {
                    val d = downloadItemJson.readText().jsonTo<List<CartoonDownloadReq>>()
                    _downloadItem.update {
                        d ?: emptyList()
                    }
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }

        scope.launch(Dispatchers.IO) {
            downloadItem.collectLatest {
                if (it != null) {
                    downloadItemJsonTemp.delete()
                    downloadItemJsonTemp.createNewFile()
                    downloadItemJson.delete()
                    downloadItemJsonTemp.writeText(it.toJson())
                    downloadItemJsonTemp.renameTo(downloadItemJson)
                }

            }
        }
    }


}