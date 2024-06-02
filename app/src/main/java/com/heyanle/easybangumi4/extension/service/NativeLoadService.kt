package com.heyanle.easybangumi4.extension.service

/**
 * Created by heyanle on 2024/6/2.
 * https://github.com/heyanLE
 */
class NativeLoadService(
    private val classLoader: ClassLoader
) {

    fun load(path: String): Boolean{
        try {
            val clazz = classLoader.loadClass("com.heyanle.extension_core.NativeLoadService") ?: return false
            val method = clazz.getDeclaredMethod("load", String::class.java) ?: return false
            method.invoke(null, path)
            return true
        }catch (e: Exception){
            e.printStackTrace()
            return false
        }
    }

}