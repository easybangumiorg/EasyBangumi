package com.heyanle.easybangumi4.download

import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.source.SourceController
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Created by HeYanLe on 2023/9/17 16:17.
 * https://github.com/heyanLE
 */
class ParseWrap(
    private val sourceController: SourceController,
    private val baseDownloadController: BaseDownloadController,
    private val downloadBus: DownloadBus,
) {
    // 同时只能有一个 parsing 任务
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mainScope = MainScope()

    private val _flow = MutableStateFlow<Set<DownloadItem>>(emptySet())
    val flow = _flow.asStateFlow()

    fun parse(downloadItem: DownloadItem){
        scope.launch {
            mainScope.launch {
                val info = downloadBus.getInfo(downloadItem.uuid)
                info.process.value = -1f
                info.status.value = stringRes(R.string.parsing)
                info.subStatus.value = ""
            }
            val play = sourceController.awaitBundle().play(downloadItem.cartoonSource)
            if (play == null){
                error(downloadItem, stringRes(R.string.source_not_found))
                return@launch
            }
            play.getPlayInfo(CartoonSummary(downloadItem.cartoonId, downloadItem.cartoonSource, downloadItem.cartoonUrl), downloadItem.playLine,
                downloadItem.episodeIndex
            )
                .complete {
                    completely(downloadItem, it.data)
                }
                .error {
                    error(downloadItem)
                }

        }

    }

    private fun completely(downloadItem: DownloadItem, playerInfo: PlayerInfo){
        baseDownloadController.updateDownloadItem {
            it.map {
                if (it != downloadItem) {
                    it
                } else {
                    it.copy(
                        state = 2,
                        playerInfo = playerInfo,
                    )
                }
            }
        }
        _flow.update {
            it - downloadItem
        }
    }

    private fun error(downloadItem: DownloadItem, error: String){
        baseDownloadController.updateDownloadItem {
            it.map {
                if (it != downloadItem) {
                    it
                } else {
                    it.copy(
                        state = -1,
                        errorMsg = error,
                    )
                }
            }
        }
        _flow.update {
            it - downloadItem
        }
    }


}