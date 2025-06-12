package org.easybangumi.next.rhino

import org.mozilla.javascript.Function
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJavaObject
import org.slf4j.Logger

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */

typealias RhinoContext = Context
typealias RhinoFunction = Function

internal val logger: Logger by RhinoService.logger("RhinoContext")

fun Any?.jsUnwrap(): Any? {
    return if(this is NativeJavaObject){
        this.unwrap().apply {
            logger.debug("jsUnwrap -> {}", this)
        }
    }else{
        this
    }
}