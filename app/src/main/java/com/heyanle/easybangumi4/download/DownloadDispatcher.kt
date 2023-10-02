package com.heyanle.easybangumi4.download

import android.app.Application
import android.util.Log
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.download.entity.DownloadBundle
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.download.step.AriaStep
import com.heyanle.easybangumi4.download.step.BaseStep
import com.heyanle.easybangumi4.download.step.CopyStep
import com.heyanle.easybangumi4.download.step.ParseStep
import com.heyanle.easybangumi4.getter.DownloadItemGetter
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by heyanlin on 2023/10/2.
 */
class DownloadDispatcher(
    private val application: Application,
    private val downloadController: DownloadController,
    private val downloadItemGetter: DownloadItemGetter,
    private val settingPreferences: SettingPreferences,
) {

    companion object {
        const val TAG = "DownloadController"
        private const val reservedChars = "|\\?*<\":>+[]/'!"
    }

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val atomLong = AtomicLong(0)

    init {
        scope.launch {
            downloadItemGetter.flowDownloadItem().collect {
                Log.i(TAG, "${it.size}")
                it.find { it.needDispatcher() }?.let {
                    dispatch(it)
                }
            }
        }
    }

    fun clickDownload(downloadItem: DownloadItem): Boolean {
        val name = downloadItem.stepsChain.getOrNull(downloadItem.currentSteps)
        if (name != null) {
            val step = getStep(name = name)
            return step.onClick(downloadItem)
        }
        return false
    }

    fun newDownload(cartoonInfo: CartoonInfo, download: List<Pair<PlayLine, Int>>) {
        scope.launch {
            val new = download.map {
                val uuid = "${System.nanoTime()}-${atomLong.getAndIncrement()}"
                var fileName =
                    "${cartoonInfo.title}-${it.first.label}-${it.first.episode.getOrElse(it.second) { "" }}-${uuid}"
                fileName = fileName.flatMap {
                    if (reservedChars.contains(it) || it == '\n' || it == ' ' || it == '\t' || it == '\r') {
                        emptyList()
                    } else {
                        listOf(it)
                    }
                }.joinToString("")
                val file = settingPreferences.downloadPath.get()
                val dd = settingPreferences.downloadPathSelection.find {
                    it.first == file
                }?.second?:""

                val downloadTarget =
                    if(dd == stringRes(com.heyanle.easy_i18n.R.string.public_movie_path) || dd == stringRes(
                            com.heyanle.easy_i18n.R.string.public_dcim_path)){
                        application.getFilePath("download")
                    }else{
                        file
                    }
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
                    episodeLabel = it.first.episode.getOrElse(it.second) { "" },
                    episodeIndex = it.second,
                    state = 0,
                    currentSteps = 0,
                    stepsChain = if(downloadTarget == file)listOf(ParseStep.NAME, AriaStep.NAME)else listOf(ParseStep.NAME, AriaStep.NAME, CopyStep.NAME) ,
                    folder = file,
                    fileNameWithoutSuffix = fileName,
                    bundle = DownloadBundle(
                        downloadFolder = downloadTarget,
                        filePathBeforeCopy = File(downloadTarget, "$fileName.mp4").absolutePath
                    )
                )
            }
            downloadController.update {
                (it ?: emptyList()) + new
            }
        }
    }

    private fun dispatch(downloadItem: DownloadItem) {
        if (!downloadItem.needDispatcher()) {
            return
        }
        val nextIndex =
            if (downloadItem.state == 2) downloadItem.currentSteps + 1 else downloadItem.currentSteps
        if (nextIndex >= downloadItem.stepsChain.size) {
            downloadController.downloadItemCompletely(downloadItem = downloadItem)
            return
        }
        val name = downloadItem.stepsChain.getOrNull(nextIndex)
        if (name == null) {
            downloadController.updateDownloadItem(downloadItem.uuid) {
                it.copy(
                    state = -1
                )
            }
            return
        }
        val step = getStep(name)
        downloadController.updateDownloadItem(downloadItem.uuid) {
            it.copy(
                state = 1,
                currentSteps = nextIndex
            )
        }
        try {
            step.invoke(downloadItem)
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

    private fun getStep(name: String) = Injekt.get<BaseStep>(name)


}