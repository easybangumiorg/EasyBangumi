package com.heyanle.easybangumi4.cartoon_download

import android.app.Application
import android.util.Log
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon_download.entity.DownloadBundle
import com.heyanle.easybangumi4.cartoon_download.entity.DownloadItem
import com.heyanle.easybangumi4.cartoon_download.step.AriaStep
import com.heyanle.easybangumi4.cartoon_download.step.BaseStep
import com.heyanle.easybangumi4.cartoon_download.step.CopyStep
import com.heyanle.easybangumi4.cartoon_download.step.ParseStep
import com.heyanle.easybangumi4.getter.DownloadItemGetter
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by heyanlin on 2023/10/2.
 */
class CartoonDownloadDispatcher(
    private val application: Application,
    private val cartoonDownloadController: CartoonDownloadController,
    private val downloadItemGetter: DownloadItemGetter,
    private val settingPreferences: SettingPreferences,
) {

    companion object {
        const val TAG = "DownloadController"
        private const val reservedChars = "|\\?*<\":>+[]/'!"
    }

    private val cacheRoot = application.getCachePath("download")
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val atomLong = AtomicLong(0)

    init {
        scope.launch {
            // 先清理垃圾
            removeDirty()

            downloadItemGetter.flowDownloadItem().collect {
                Log.i(TAG, "refresh ${it.size}")
                if(it.count {
                    it.state == 0 || it.state == 1 || it.state == 2
                    } > 0){
                    CartoonDownloadService.tryStart()
                }
                it.find { it.needDispatcher() }?.let {
                    dispatch(it)
                }
            }
        }
    }

    fun removeDownload(downloadItem: DownloadItem) {
        val name = downloadItem.stepsChain.getOrElse(downloadItem.currentSteps){""}
        if(name.isNotEmpty()){
            getStep(name).onRemove(downloadItem)
        }
    }

    fun clickDownload(downloadItem: DownloadItem): Boolean {
        if(downloadItem.state == -1){
            // 错误的任务点击重下
            val uuid = "${System.nanoTime()}-${atomLong.getAndIncrement()}"
            var fileName =
                "${downloadItem.cartoonTitle}-${downloadItem.playLine.label}-${downloadItem.episode.label}-${uuid}"
            fileName = fileName.flatMap {
                if (reservedChars.contains(it) || it == '\n' || it == ' ' || it == '\t' || it == '\r') {
                    emptyList()
                } else {
                    listOf(it)
                }
            }.joinToString("")
            val realTarget = settingPreferences.downloadPath.get()
            val downloadRoot = File(cacheRoot, uuid)
            downloadRoot.mkdirs()
            val new = downloadItem.copy(
                uuid = uuid,
                folder = realTarget,
                fileNameWithoutSuffix = fileName,
                state = 0,
                currentSteps = 0,
                bundle = DownloadBundle(
                    downloadFolder = downloadRoot.absolutePath,
                    filePathBeforeCopy = File(downloadRoot, "$fileName.mp4").absolutePath,
                    needRefreshMedia = settingPreferences.needRefreshMedia.contains(realTarget)
                )
            )
            newDownload(listOf(new))
            return true
        }
        val name = downloadItem.stepsChain.getOrNull(downloadItem.currentSteps)
        if (name != null) {
            val step = getStep(name = name)
            return step.onClick(downloadItem)
        }
        return false
    }

    fun newDownload(cartoonInfo: CartoonInfo, download: List<Pair<PlayLine, Episode>>) {
        scope.launch {
            "newDownload ${cartoonInfo.title} ${download.size}".logi(TAG)
            val new = download.map {
                val uuid = "${System.nanoTime()}-${atomLong.getAndIncrement()}"
                var fileName =
                    "${cartoonInfo.title}-${it.first.label}-${it.second.label}-${uuid}"
                fileName = fileName.flatMap {
                    if (reservedChars.contains(it) || it == '\n' || it == ' ' || it == '\t' || it == '\r') {
                        emptyList()
                    } else {
                        listOf(it)
                    }
                }.joinToString("")
                val realTarget = settingPreferences.downloadPath.get()
                val downloadRoot = File(cacheRoot, uuid)
                downloadRoot.mkdirs()


                DownloadItem(
                    uuid = uuid,
                    cartoonId = cartoonInfo.id,
                    cartoonUrl = cartoonInfo.url,
                    cartoonSource = cartoonInfo.source,
                    cartoonTitle = cartoonInfo.title,
                    cartoonCover = cartoonInfo.coverUrl,
                    cartoonDescription = cartoonInfo.description,
                    cartoonGenre = cartoonInfo.genre,
                    playLine = it.first,
                    episode = it.second,
                    state = 0,
                    currentSteps = 0,
                    stepsChain =  listOf(ParseStep.NAME, AriaStep.NAME, CopyStep.NAME) ,
                    folder = realTarget,
                    fileNameWithoutSuffix = fileName,
                    sourceLabel = cartoonInfo.sourceName,
                    bundle = DownloadBundle(
                        downloadFolder = downloadRoot.absolutePath,
                        filePathBeforeCopy = File(downloadRoot, "$fileName.mp4").absolutePath,
                        needRefreshMedia = settingPreferences.needRefreshMedia.contains(realTarget)
                    )
                )
            }
            newDownload(new)
        }
    }

    private fun newDownload(downloadItems: List<DownloadItem>){
        cartoonDownloadController.update {
            (it ?: emptyList()) + downloadItems
        }
    }

    private fun dispatch(downloadItem: DownloadItem) {
        "dispatch ${downloadItem}".logi(TAG)
        if (!downloadItem.needDispatcher()) {
            return
        }
        if(downloadItem.state == -1 || downloadItem.isRemoved){
            if(downloadItem.bundle.downloadFolder.isNotEmpty()){
                File(downloadItem.bundle.downloadFolder).delete()
            }
            if(downloadItem.isRemoved){
                cartoonDownloadController.update {
                    it?.minus(downloadItem)?: emptyList()
                }
            }
            return
        }
        val nextIndex =
            if (downloadItem.state == 2) downloadItem.currentSteps + 1 else downloadItem.currentSteps
        if (nextIndex >= downloadItem.stepsChain.size) {
            cartoonDownloadController.downloadItemCompletely(downloadItem = downloadItem)
            return
        }
        val name = downloadItem.stepsChain.getOrNull(nextIndex)
        if (name == null) {
            cartoonDownloadController.updateDownloadItem(downloadItem.uuid) {
                it.copy(
                    state = -1
                )
            }
            return
        }
        val step = getStep(name)
        cartoonDownloadController.updateDownloadItem(downloadItem.uuid) {
            it.copy(
                state = 1,
                currentSteps = nextIndex
            )
        }
        try {
            step.invoke(downloadItem)
        } catch (e: Exception) {
            e.printStackTrace()
            cartoonDownloadController.updateDownloadItem(downloadItem.uuid) {
                it.copy(
                    state = -1,
                    errorMsg = e.message ?: ""
                )
            }
        }
        scope.launch {
            // 每次调度后清理垃圾
            removeDirty()
        }
    }

    private suspend fun removeDirty(){
        val ignoreUUID = hashSetOf<String>()
        downloadItemGetter.awaitDownloadItem().forEach {
            if(it.state != -1 && it.state != 3 && !it.isRemoved){
                ignoreUUID.add(it.uuid)
            }
        }
        File(cacheRoot).listFiles()?.forEach {
            if(it != null && it.exists() && !ignoreUUID.contains(it.name)){
                it.deleteRecursively()
            }
        }
    }

    private fun getStep(name: String) = Injekt.get<BaseStep>(name)


}