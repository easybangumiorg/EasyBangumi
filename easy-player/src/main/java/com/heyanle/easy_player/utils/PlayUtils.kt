package com.heyanle.easy_player.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.TypedValue

/**
 * Create by heyanlin on 2022/10/24
 */
object PlayUtils {

    fun findActivity(context: Context): Activity?{
        if(context is Activity){
            return context
        } else if(context is ContextWrapper){
            return findActivity(context.baseContext)
        }
        return null
    }

    /**
     * dp转为px
     */
    fun dp2px(context: Context, dpValue: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            context.resources.displayMetrics
        ).toInt()
    }

    /**
     * sp转为px
     */
    fun sp2px(context: Context, dpValue: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            dpValue,
            context.resources.displayMetrics
        ).toInt()
    }
}