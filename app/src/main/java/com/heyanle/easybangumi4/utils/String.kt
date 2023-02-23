package com.heyanle.easybangumi4.utils

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.ui.common.moeSnackBar

/**
 * Created by HeYanLe on 2022/12/23 17:53.
 * https://github.com/heyanLE
 */

fun stringRes(resId: Int, vararg formatArgs: Any): String {
    return APP.getString(resId, formatArgs)
}

fun String.openUrl() = runCatching {
    APP.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(this)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}.onFailure { "${it.loge().javaClass.simpleName}(${it.localizedMessage})".moeSnackBar() }

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