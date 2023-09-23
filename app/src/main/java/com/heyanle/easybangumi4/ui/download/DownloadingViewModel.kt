package com.heyanle.easybangumi4.ui.download

import androidx.lifecycle.ViewModel
import com.heyanle.easybangumi4.base.db.dao.CartoonDownloadDao
import com.heyanle.easybangumi4.download.BaseDownloadController
import com.heyanle.easybangumi4.download.DownloadBus
import com.heyanle.easybangumi4.download.DownloadController
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.injekt.core.Injekt

/**
 * Created by HeYanLe on 2023/8/27 22:05.
 * https://github.com/heyanLE
 */
class DownloadingViewModel: ViewModel() {

    private val downloadDao: CartoonDownloadDao by Injekt.injectLazy()
    private val downloadBus: DownloadBus by Injekt.injectLazy()
    private val downloadController: DownloadController by Injekt.injectLazy()
    private val baseDownloadController: BaseDownloadController by Injekt.injectLazy()

    val flow = baseDownloadController.downloadItem

    fun info(download: DownloadItem): DownloadBus.DownloadingInfo{
        return downloadBus.getInfo(download.uuid)
    }

    fun click(download: DownloadItem){
        downloadController.click(download)
    }

}