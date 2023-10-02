package com.heyanle.easybangumi4.getter

import com.heyanle.easybangumi4.download.DownloadController
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.download.entity.LocalCartoon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

/**
 * Created by heyanlin on 2023/10/2.
 */
class DownloadItemGetter(
    private val downloadController: DownloadController
) {
    suspend fun awaitDownloadItem(): List<DownloadItem> {
        return flowDownloadItem()
            .first()
    }

    fun flowDownloadItem(): Flow<List<DownloadItem>> {
        return downloadController.downloadItem.filter { it != null }
            .filterIsInstance<List<DownloadItem>>()
    }

}