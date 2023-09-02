package com.heyanle.easybangumi4.ui.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.db.dao.CartoonDownloadDao
import com.heyanle.easybangumi4.base.entity.CartoonDownload
import com.heyanle.easybangumi4.download.DownloadBus
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * Created by HeYanLe on 2023/8/27 22:05.
 * https://github.com/heyanLE
 */
class DownloadingViewModel: ViewModel() {

    private val downloadDao: CartoonDownloadDao by Injekt.injectLazy()
    private val downloadBus: DownloadBus by Injekt.injectLazy()

    val flow = downloadDao.flowAll().stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    fun info(download: CartoonDownload): DownloadBus.DownloadingInfo{
        return downloadBus.getInfo(download.toIdentify())
    }

}