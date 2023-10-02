package com.heyanle.easybangumi4.download.step

import com.heyanle.easybangumi4.download.entity.DownloadItem

/**
 * Created by heyanlin on 2023/10/2.
 */
interface BaseStep {

    fun invoke(downloadItem: DownloadItem)

    fun onClick(downloadItem: DownloadItem): Boolean = false


}

