package com.heyanle.easybangumi4.cartoon.story.download

import com.heyanle.easybangumi4.bus.DownloadingBus

/**
 * Created by HeYanLe on 2023/9/17 15:39.
 * https://github.com/heyanLE
 */
class CartoonDownloadBus (
    private val downloadBus: DownloadingBus,
){


    fun getInfo(key: String): DownloadingBus.DownloadingInfo {
        return downloadBus.getInfo(DownloadingBus.DownloadScene.CARTOON, key)
    }

    fun remove(key: String) {
        downloadBus.remove(DownloadingBus.DownloadScene.CARTOON, key = key)
    }
}