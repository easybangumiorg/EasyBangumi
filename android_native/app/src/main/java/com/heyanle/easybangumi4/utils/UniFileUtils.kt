package com.heyanle.easybangumi4.utils

import com.hippo.unifile.UniFile

/**
 * Created by heyanle on 2024/7/9.
 * https://github.com/heyanLE
 */
fun UniFile.deleteRecursively() {
    if (!exists()) {
        return
    }
    if(isDirectory){
        listFiles()?.forEach {
            it.deleteRecursively()
        }
    }
    delete()
}