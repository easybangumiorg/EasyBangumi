package com.heyanle.easybangumi.utils

import androidx.appcompat.widget.Toolbar
import android.view.Gravity
import android.view.View

import android.widget.TextView


/**
 * Created by HeYanLe on 2021/9/20 16:29.
 * https://github.com/heyanLE
 */


fun View.visible(){
    visibility = View.VISIBLE
}

fun View.invisible(){
    visibility = View.INVISIBLE
}

fun View.gone(){
    visibility = View.GONE
}

fun toolbarCenter(toolbar: Toolbar){
    val title = "title"
    val originalTitle = toolbar.title
    toolbar.title = title
    for (i in 0 until toolbar.childCount) {
        val view = toolbar.getChildAt(i)
        if (view is TextView) {
            if (title.contentEquals(view.text)) {
                view.gravity = Gravity.CENTER
                val params = Toolbar.LayoutParams(
                    Toolbar.LayoutParams.WRAP_CONTENT,
                    Toolbar.LayoutParams.MATCH_PARENT
                )
                params.gravity = Gravity.CENTER
                view.layoutParams = params
            }
        }
        toolbar.title = originalTitle
    }
}