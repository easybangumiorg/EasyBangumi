package com.heyanle.easybangumi.utils

import com.heyanle.easybangumi.BangumiApp

/**
 * Created by HeYanLe on 2022/12/23 17:53.
 * https://github.com/heyanLE
 */

fun stringRes(resId: Int, vararg formatArgs: Any): String{
    return BangumiApp.INSTANCE.getString(resId, formatArgs)
}