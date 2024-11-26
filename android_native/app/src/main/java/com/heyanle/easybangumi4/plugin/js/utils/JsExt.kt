package com.heyanle.easybangumi4.plugin.js.utils

import com.heyanle.easybangumi4.utils.logi
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.NativeJavaObject

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */

typealias JSContext = Context
typealias JSFunction = Function

fun Any?.jsUnwrap(): Any? {
    return if(this is NativeJavaObject){
        this.unwrap().apply {
            this.logi("jsUnwrap")
        }
    }else{
        this
    }
}
