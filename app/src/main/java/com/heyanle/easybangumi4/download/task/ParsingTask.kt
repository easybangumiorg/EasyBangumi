package com.heyanle.easybangumi4.download.task

import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easybangumi4.download.DownloadBundle
import com.heyanle.easybangumi4.source.SourceController
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by HeYanLe on 2023/9/3 22:31.
 * https://github.com/heyanLE
 */
class ParsingTask(
    private val sourceController: SourceController,
): DownloadTask {

    // 同时只能有一个 parsing 任务
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    override suspend fun invoke(downloadBundle: DownloadBundle) {
        if(downloadBundle.isParseCompletely){
            return
        }
        val error = suspendCoroutine<Throwable?> {con ->
            scope.launch {
                val play = sourceController.awaitBundle().play(downloadBundle.cartoonSource)
                if (play == null){
                    con.resume(DownloadTask.TaskErrorException(stringRes(com.heyanle.easy_i18n.R.string.source_not_found)))
                    return@launch
                }
                val episodeList = downloadBundle.playLineEpisodeListJson.jsonTo<List<String>>()
                val episodeArrayList = arrayListOf<String>()
                episodeArrayList.addAll(episodeList)
                val playLine = PlayLine(downloadBundle.playLineId, downloadBundle.playLineLabel, episodeArrayList)
                play.getPlayInfo(CartoonSummary(downloadBundle.cartoonId, downloadBundle.cartoonSource, downloadBundle.cartoonUrl), playLine, downloadBundle.episodeIndex.toInt())
                    .complete {
                        // 没问题需要填充数据
                        downloadBundle.playerInfoUri = it.data.uri
                        downloadBundle.playerInfoDecodeType = it.data.decodeType.toLong()
                        downloadBundle.playerInfoHeaderJson = it.data.header?.toJson()?:"{}"
                        downloadBundle.isParseCompletely = true
                        con.resume(null)
                    }.error {
                        con.resume(DownloadTask.TaskErrorException(it.throwable.toString(), it.throwable))
                    }

            }
        }
        if(error != null){
            throw error
        }
    }
}