package com.heyanle.easybangumi4.ui.download

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.download.DownloadBus
import com.heyanle.easybangumi4.download.DownloadController
import com.heyanle.easybangumi4.download.DownloadDispatcher
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.getter.DownloadItemGetter
import org.koin.mp.KoinPlatform.getKoin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * Created by HeYanLe on 2023/8/27 22:05.
 * https://github.com/heyanLE
 */
class DownloadViewModel : ViewModel() {

    private val downloadBus: DownloadBus by getKoin().inject()


    private val downloadItemGetter: DownloadItemGetter by getKoin().inject()
    val downloadingFlow = downloadItemGetter.flowDownloadItem()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val downloadDispatcher: DownloadDispatcher by getKoin().inject()

    val selection = mutableStateMapOf<DownloadItem, Boolean>()

    val removeDownloadItem = mutableStateOf<Collection<DownloadItem>?>(null)

    fun info(download: DownloadItem): DownloadBus.DownloadingInfo {
        return downloadBus.getInfo(download.uuid)
    }

    fun click(download: DownloadItem) {
        downloadDispatcher.clickDownload(downloadItem = download)
    }

    fun remove(selection: Collection<DownloadItem>) {
        selection.forEach {
            downloadDispatcher.removeDownload(it)
        }
    }

}