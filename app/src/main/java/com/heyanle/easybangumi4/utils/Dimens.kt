package com.heyanle.easybangumi4.utils

import android.content.Context
import com.heyanle.easybangumi4.APP

/**
 * Created by HeYanLe on 2023/1/12 22:25.
 * https://github.com/heyanLE
 */
fun dip2px(context: Context, dpValue: Float): Int {
    val scale: Float = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun px2dip(context: Context, pxValue: Float): Float {
    val scale: Float = context.resources.displayMetrics.density
    return (pxValue / scale)
}

fun Float.dip2px(): Int {
    return dip2px(APP, this)
}

fun Int.dip2px(): Int {
    return dip2px(APP, this.toFloat())
}

fun Float.px2dip(): Float {
    return px2dip(APP, this)
}

fun Int.px2dip(): Float {
    return px2dip(APP, this.toFloat())
}
