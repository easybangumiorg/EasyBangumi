package com.heyanle.extension_load.utils

import android.util.Log

/**
 * 打印info log
 */
fun <T> T.logi(tag: String = ""): T = apply {
    if (toString().length > 4000) {
        for (i in toString().indices step 4000) {
            if (i + 4000 < toString().length) {
                Log.i(tag, toString().substring(i, i + 4000))
            } else {
                Log.i(tag, toString().substring(i, toString().length))
            }
        }
    } else {
        Log.i(tag, toString())
    }
}

/**
 * 打印error log
 */
fun <T> T.loge(tag: String = ""): T = apply {
    if (toString().length > 4000) {
        for (i in toString().indices step 4000) {
            if (i + 4000 < toString().length) {
                Log.e(tag, toString().substring(i, i + 4000))
            } else {
                Log.e(tag, toString().substring(i, toString().length))
            }
        }
    } else {
        Log.e(tag, toString())
    }
}