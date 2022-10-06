package com.heyanle.easybangumi.utils

import com.heyanle.easybangumi.EasyApplication

/**
 * Created by HeYanLe on 2022/10/5 9:27.
 * https://github.com/heyanLE
 */

fun Int.getStringFromResource(): String{
    return EasyApplication.INSTANCE.getString(this)
}

fun Int.getStringFromResource(vararg args: Any): String{
    return EasyApplication.INSTANCE.getString(this, args)
}