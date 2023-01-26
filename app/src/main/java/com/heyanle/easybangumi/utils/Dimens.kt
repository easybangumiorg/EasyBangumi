package com.heyanle.easybangumi.utils

import android.content.Context
import com.heyanle.easybangumi.BangumiApp

/**
 * Created by HeYanLe on 2023/1/12 22:25.
 * https://github.com/heyanLE
 */
fun dip2px(context: Context, dpValue: Float): Int {
    val scale: Float = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun Float.dip2px(): Int {
    return dip2px(BangumiApp.INSTANCE, this)
}

fun Int.dip2px(): Int {
    return dip2px(BangumiApp.INSTANCE, this.toFloat())
}
