package com.heyanle.easybangumi4.getter

import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadController
import com.heyanle.easybangumi4.cartoon_download.entity.DownloadItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

/**
 * Created by heyanlin on 2023/10/2.
 */
class DownloadItemGetter(
    private val cartoonDownloadController: CartoonDownloadController
) {
    suspend fun awaitDownloadItem(): List<DownloadItem> {
        return flowDownloadItem()
            .first()
    }

    fun flowDownloadItem(): Flow<List<DownloadItem>> {
        return cartoonDownloadController.downloadItem.filter { it != null }
            .filterIsInstance<List<DownloadItem>>()
    }

}