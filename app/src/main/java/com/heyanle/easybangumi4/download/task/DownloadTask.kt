package com.heyanle.easybangumi4.download.task

import com.heyanle.easybangumi4.download.DownloadBundle

/**
 * Created by HeYanLe on 2023/9/3 22:32.
 * https://github.com/heyanLE
 */
interface DownloadTask {

    suspend fun invoke(downloadBundle: DownloadBundle)

    class TaskErrorException(val errorMsg: String, val exception: Throwable? = null): Exception()

}