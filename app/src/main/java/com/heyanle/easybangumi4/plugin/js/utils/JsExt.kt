package com.heyanle.easybangumi4.plugin.js.utils

import org.mozilla.javascript.NativeJavaObject

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */

fun Any?.jsUnwrap(): Any? {
    return if(this is NativeJavaObject){
        this.unwrap()
    }else{
        this
    }
}
