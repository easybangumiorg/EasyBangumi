package com.heyanle.easybangumi4.ui.download

import androidx.lifecycle.ViewModel
import com.heyanle.easybangumi4.base.db.dao.CartoonDownloadDao
import com.heyanle.easybangumi4.base.entity.CartoonDownload
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.map

/**
 * Created by HeYanLe on 2023/8/27 22:05.
 * https://github.com/heyanLE
 */
class DownloadingViewModel: ViewModel() {

    private val downloadDao: CartoonDownloadDao by Injekt.injectLazy()

    sealed class DownloadingItem {
        data class Header(
            val coverUrl: String,
            val title: String,
            val desc: String
        ) : DownloadingItem()

        data class Downloading(
            val cartoonDownload: CartoonDownload,
            val downloadingProcess: Float,
            val status: String,
        ) : DownloadingItem()
    }

    private val flow = downloadDao.flowAll().map {

    }

}