package com.alien.gpuimage.utils

import android.util.Log

object Logger {

    @JvmStatic
    fun d(tag: String, msg: String) {
        val formatMsg = "[" + Thread.currentThread().name + "]" + "[" + tag + "] " + msg
        Log.d(tag, formatMsg)
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        val formatMsg = "[" + Thread.currentThread().name + "]" + "[" + tag + "] " + msg
        Log.e(tag, formatMsg)
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        val formatMsg = "[" + Thread.currentThread().name + "]" + "[" + tag + "] " + msg
        Log.i(tag, formatMsg)
    }

    @JvmStatic
    fun w(tag: String, msg: String) {
        val formatMsg = "[" + Thread.currentThread().name + "]" + "[" + tag + "] " + msg
        Log.w(tag, formatMsg)
    }
}