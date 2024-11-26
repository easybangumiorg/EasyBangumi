package com.heyanle.easybangumi4.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE

/**
 * Created by heyanlin on 2023/12/25.
 */
fun Context.getMemoryInfo(): ActivityManager.MemoryInfo {
    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    return ActivityManager.MemoryInfo().also {
        activityManager.getMemoryInfo(it)
    }
}

class EasyMemoryInfo(
    context: Context
) : ActivityManager.MemoryInfo() {

    private val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    fun update() {
        activityManager.getMemoryInfo(this)
    }
}