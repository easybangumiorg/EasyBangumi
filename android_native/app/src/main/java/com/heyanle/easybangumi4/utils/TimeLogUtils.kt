package com.heyanle.easybangumi4.utils

/**
 * Created by heyanlin on 2023/11/1.
 */
object TimeLogUtils {

    const val TAG = "TimeLogUtils"

    private var lastTime: Long = System.currentTimeMillis()
    fun i(msg: String){
        val time = System.currentTimeMillis()
        "${time} ${msg} ${time - lastTime}".logi(TAG)
        lastTime = time
    }

}