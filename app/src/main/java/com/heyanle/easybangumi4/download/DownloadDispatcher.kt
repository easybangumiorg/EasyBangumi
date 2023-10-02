package com.heyanle.easybangumi4.download

import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.download.step.AriaStep
import com.heyanle.easybangumi4.download.step.BaseStep
import com.heyanle.easybangumi4.download.step.ParseStep
import com.heyanle.easybangumi4.getter.DownloadItemGetter
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by heyanlin on 2023/10/2.
 */
class DownloadDispatcher(
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
            downloadController.downloadItem.collectLatest {
                it?.find { it.needDispatcher() }?.let {
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
                    if (reservedChars.contains(it)) {
                        emptyList()
                    } else {
                        listOf(it)
                    }
                }.joinToString("")
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
                    state = 1,
                    currentSteps = -1,
                    stepsChain = listOf(ParseStep.NAME, AriaStep.NAME),
                    filePathWithoutSuffix = File(
                        settingPreferences.downloadPath.get(), fileName
                    ).absolutePath
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
        val nextIndex = downloadItem.currentSteps + 1
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
        step.invoke(downloadItem)
        downloadController.updateDownloadItem(downloadItem.uuid) {
            it.copy(
                currentSteps = nextIndex,
                state = 0
            )
        }
    }

    private fun getStep(name: String) = Injekt.get<BaseStep>(name)


}