package com.heyanle.easybangumi4.cartoon_download.step

import com.heyanle.easybangumi4.cartoon_download.entity.DownloadItem

/**
 * Created by heyanlin on 2023/10/2.
 */
interface BaseStep {

    fun init(downloadItem: DownloadItem): DownloadItem? {
        if (downloadItem.state == 1) {
            return downloadItem.copy(state = 0)
        }
        return downloadItem
    }

    fun invoke(downloadItem: DownloadItem)

    fun onRemove(downloadItem: DownloadItem)

    fun onClick(downloadItem: DownloadItem): Boolean = false


}

