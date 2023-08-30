package com.heyanle.easybangumi4.download

import android.content.Context
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easybangumi4.base.entity.CartoonInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Created by HeYanLe on 2023/8/27 20:07.
 * https://github.com/heyanLE
 */
class DownloadController(
    private val downloadBus: DownloadBus
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.IO.limitedParallelism(3)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)


    fun newDownload(
        cartoonInfo: CartoonInfo,
        download: List<Pair<PlayLine, Int>>,
    ) {

        M3U8VodOption().generateIndexFile()
    }


}