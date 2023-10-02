package com.heyanle.easybangumi4.download

import android.content.Context
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
import com.heyanle.easybangumi4.base.entity.CartoonInfo
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by HeYanLe on 2023/9/17 15:42.
 * https://github.com/heyanLE
 */
class DownloadController(
    private val context: Context,
    private val baseDownloadController: BaseDownloadController,
    private val ariaWrap: AriaWrap,
    private val parseWrap: ParseWrap,
    private val transcodeWrap: TranscodeWrap,
) {

    companion object {
        const val TAG = "DownloadController"
        private const val reservedChars = "|\\?*<\":>+[]/'!"
    }

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val atomLong = AtomicLong(0)


    private val rootFolder = File(context.getFilePath("download"))

    fun init() {
        scope.launch {
            combine(
                baseDownloadController.downloadItem,
                parseWrap.flow,
                transcodeWrap.flow
            ) { list, parses, trancodes ->
                list.forEach {
                    if (it.state == 0 || it.state == 1 && !parses.contains(it)) {
                        "parse ${it}".logi(TAG)
                        parseWrap.parse(it)
                    } else if (it.state == 2) {
                        "aria ${it}".logi(TAG)
                        ariaWrap.tryPush(it)
                    } else if (it.state == 3) {
                        if (it.playerInfo?.decodeType == PlayerInfo.DECODE_TYPE_HLS) {
                            if (!trancodes.contains(it)) {
                                "transcode ${it}".logi(TAG)
                                transcodeWrap.transcode(it)
                            }
                        } else {
                            "completely other ${it}".logi(TAG)
                            baseDownloadController.downloadItemCompletely(it)
                        }

                    } else if (it.state == 4) {
                        "completely ${it}".logi(TAG)
                        baseDownloadController.downloadItemCompletely(it)
                    }
                }
            }.collect()
        }
    }

    init {

    }

    fun newDownload(cartoonInfo: CartoonInfo, download: List<Pair<PlayLine, Int>>) {
        scope.launch {
            baseDownloadController.updateDownloadItem { it ->
                it + download.map {
                    val uuid = "${System.nanoTime()}-${atomLong.getAndIncrement()}"
                    var fileName =
                        "${cartoonInfo.title}-${it.first.label}-${it.first.episode.getOrElse(it.second) { "" }}-${uuid}"
                    fileName = fileName.flatMap {
                        if(reservedChars.contains(it)){
                            emptyList()
                        }else{
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

                        state = 0,

                        filePathWithoutSuffix = File(
                            rootFolder,fileName
                        ).absolutePath
                    )
                }
            }

        }
    }

    fun restart(downloadItem: DownloadItem) {
        scope.launch {
            baseDownloadController.updateDownloadItem {
                it.map {
                    if (it.uuid == downloadItem.uuid) {
                        it.copy(
                            state = 0
                        )
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun click(downloadItem: DownloadItem) {
        when (downloadItem.state) {
            2 -> {
                ariaWrap.click(downloadItem)
            }

            -1 -> {
                restart(downloadItem)
            }
        }
    }

}