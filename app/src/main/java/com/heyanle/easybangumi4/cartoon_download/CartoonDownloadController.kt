package com.heyanle.easybangumi4.cartoon_download

import android.content.Context
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson
import com.heyanle.easybangumi4.cartoon_download.entity.DownloadItem
import com.heyanle.easybangumi4.cartoon_download.step.BaseStep
import com.heyanle.easybangumi4.cartoon_download.utils.MediaScanUtils
import com.heyanle.injekt.core.Injekt
import com.heyanle.okkv2.core.okkv
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
class CartoonDownloadController(
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
                        ?.flatMap {
                            val name = it.stepsChain.getOrNull(it.currentSteps)
                            if(name == null){
                                return@flatMap emptyList<DownloadItem>()
                            }else{
                                val step by Injekt.injectLazy<BaseStep>(name)
                                val n = step.init(it)
                                if(n == null){
                                    return@flatMap emptyList<DownloadItem>()
                                }else{
                                    return@flatMap listOf(n)
                                }
                            }
                        }
                    _downloadItem.update {
                        d?: emptyList()
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
            // 刷新图库
            if(downloadItem.bundle.needRefreshMedia){
                val realFile = File(downloadItem.folder, downloadItem.fileNameWithoutSuffix+".mp4")
                if(realFile.exists()){
                    MediaScanUtils.mediaScan(context, realFile.absolutePath)
                }
            }
        }
    }

    private fun save() {
        _downloadItem.value?.let {
            downloadItemJsonTemp.delete()
            downloadItemJsonTemp.createNewFile()
            downloadItemJsonTemp.writeText(it.toJson())
            downloadItemJson.delete()
            downloadItemJsonTemp.renameTo(downloadItemJson)
        }

    }

    var first by okkv("download_first_visible", def = true)

    fun tryShowFirstDownloadDialog(){
        if(!first){
            first = false
            showDownloadHelpDialog()
        }
    }

    fun showDownloadHelpDialog(){
//        MoeDialogData(
//            text = """1、下载后的视频和原来的番剧没有强关联，从普通页面中依然是从网络播放，需要从下载记录中进入才会播放本地文件。
//
//2、下载将经历 解析 下载 转码 复制四个步骤，其中转码步骤会将视频转码成 mp4 。
//
//3、可在下载设置界面中设置下载文件的路径，当选择的是公共目录时会同步刷新到相册。""",
//            title = "下载功能须知",
//            confirmLabel = "下载设置",
//            onConfirm = {
//                navControllerRef?.get()?.navigationSetting(SettingPage.Download)
//            },
//            onDismiss = {
//                it.dismiss()
//            }
//        ).show()
    }


}