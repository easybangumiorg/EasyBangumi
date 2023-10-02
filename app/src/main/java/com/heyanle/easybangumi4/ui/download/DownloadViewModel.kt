package com.heyanle.easybangumi4.ui.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.download.DownloadBus
import com.heyanle.easybangumi4.download.DownloadDispatcher
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.getter.DownloadItemGetter
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * Created by HeYanLe on 2023/8/27 22:05.
 * https://github.com/heyanLE
 */
class DownloadViewModel: ViewModel() {

    private val downloadBus: DownloadBus by Injekt.injectLazy()


    private val downloadItemGetter: DownloadItemGetter by Injekt.injectLazy()
    val downloadingFlow = downloadItemGetter.flowDownloadItem().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val downloadDispatcher: DownloadDispatcher by Injekt.injectLazy()

    fun info(download: DownloadItem): DownloadBus.DownloadingInfo{
        return downloadBus.getInfo(download.uuid)
    }

    fun click(download: DownloadItem){
        downloadDispatcher.clickDownload(downloadItem = download)
    }

}