package com.heyanle.easybangumi.utils

import android.content.Context
import android.R.id

import android.util.TypedValue




/**
 * Created by HeYanLe on 2021/9/20 23:18.
 * https://github.com/heyanLE
 */
fun getAttrColor(context: Context, id: Int): Int{
    val typedValue = TypedValue()
    context.theme.resolveAttribute(id, typedValue, true)
    return typedValue.data
}