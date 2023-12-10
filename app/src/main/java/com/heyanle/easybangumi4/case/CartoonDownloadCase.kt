package com.heyanle.easybangumi4.case

import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadController
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.cartoon_download.entity.DownloadItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

/**
 * Created by heyanlin on 2023/10/2.
 */
class CartoonDownloadCase(
    private val cartoonDownloadController: CartoonDownloadController,
    private val cartoonDownloadDispatcher: CartoonDownloadDispatcher,
) {
    suspend fun awaitDownloadItem(): List<DownloadItem> {
        return flowDownloadItem()
            .first()
    }

    fun flowDownloadItem(): Flow<List<DownloadItem>> {
        return cartoonDownloadController.downloadItem.filter { it != null }
            .filterIsInstance<List<DownloadItem>>()
    }

    // 下载中则取消下载，没下载则下载
    fun toggle(downloadItem: DownloadItem) {
        cartoonDownloadDispatcher.toggle(downloadItem)
    }

    fun remove(downloadItem: DownloadItem) {
        cartoonDownloadDispatcher.removeDownload(downloadItem)
    }

}