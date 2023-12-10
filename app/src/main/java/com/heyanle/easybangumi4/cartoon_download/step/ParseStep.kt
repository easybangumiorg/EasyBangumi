package com.heyanle.easybangumi4.cartoon_download.step


import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadBus
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadController
import com.heyanle.easybangumi4.cartoon_download.entity.DownloadItem
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/10/2.
 */
class ParseStep(
    private val sourceStateCase: SourceStateCase,
    private val cartoonDownloadController: CartoonDownloadController,
    private val cartoonDownloadBus: CartoonDownloadBus,
) : BaseStep {

    companion object {
        const val NAME = "parse"
    }

    // 同时只能有一个 parsing 任务
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mainScope = MainScope()

    override fun invoke(downloadItem: DownloadItem) {
        mainScope.launch {
            val info = cartoonDownloadBus.getInfo(downloadItem.uuid)
            info.process.value = -1f
            info.status.value = stringRes(R.string.parsing)
            info.subStatus.value = ""
        }
        scope.launch {
            val play = sourceStateCase.awaitBundle().play(downloadItem.cartoonSource)
            if (play == null) {
                error(downloadItem.uuid, stringRes(R.string.source_not_found))
                return@launch
            }
            play.getPlayInfo(
                CartoonSummary(
                    downloadItem.cartoonId,
                    downloadItem.cartoonSource,
                    downloadItem.cartoonUrl
                ), downloadItem.playLine,
                downloadItem.episode
            )
                .complete {
                    completely(downloadItem, it.data)
                }
                .error {
                    error(downloadItem.uuid, it.throwable.message?:"")
                }

        }
    }

    override fun onRemove(downloadItem: DownloadItem) {
        cartoonDownloadController.updateDownloadItem(downloadItem.uuid){
            it.copy(isRemoved = true)
        }
    }

    private fun error(uuid: String, error: String) {
        cartoonDownloadController.updateDownloadItem(uuid) {
            it.copy(
                state = -1,
                errorMsg = error,
            )
        }
    }

    private fun completely(downloadItem: DownloadItem, playerInfo: PlayerInfo) {
        cartoonDownloadController.updateDownloadItem(downloadItem.uuid) {
            it.copy(
                state = 2,
                stepsChain = it.stepsChain.flatMap {
                    if(playerInfo.decodeType == PlayerInfo.DECODE_TYPE_HLS && it == AriaStep.NAME){
                        listOf(AriaStep.NAME, TranscodeStep.NAME)
                    }else{
                        listOf(it)
                    }
                },
                bundle = it.bundle.apply {
                    this.playerInfo = playerInfo
                    this.downloadFileName =
                        if (playerInfo.decodeType == PlayerInfo.DECODE_TYPE_HLS) it.fileNameWithoutSuffix + ".aria.m3u8" else it.fileNameWithoutSuffix + ".mp4"
                }
            )
        }
    }
}