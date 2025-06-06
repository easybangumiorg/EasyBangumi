package org.easybangumi.next.rhino

import org.easybangumi.next.lib.logger.logger
import org.mozilla.javascript.Function
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJavaObject

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */

typealias RhinoContext = Context
typealias RhinoFunction = Function

internal val logger = logger("rhino")

fun Any?.jsUnwrap(): Any? {
    return if(this is NativeJavaObject){
        this.unwrap().apply {
            logger.info("jsUnwrap -> $this")
        }
    }else{
        this
    }
}