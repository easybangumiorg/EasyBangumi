package com.heyanle.easybangumi.utils

import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.*
import kotlin.coroutines.Continuation

/**
 * Created by HeYanLe on 2021/11/21 12:46.
 * https://github.com/heyanLE
 */
abstract class SuspendInvocationHandler : InvocationHandler {
    override fun invoke(p0: Any?, p1: Method?, p2: Array<out Any>?): Any {
        val proxy = p0 ?: return Unit
        val method = p1 ?: return Unit
        val args = p2 ?: emptyArray()
        if (args.isEmpty()){
            return method.invoke(proxy, args)?:Unit
        }
        val con = args.last()
        if(con !is Continuation<*>){
            return method.invoke(proxy, args)?:Unit
        }

        return ::suspendInvoke.call(proxy, method, args.copyOfRange(0, args.size-1), con)
    }

    abstract suspend fun suspendInvoke(proxy: Any, method: Method, args: Array<out Any>): Any
}