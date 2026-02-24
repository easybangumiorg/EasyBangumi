package com.heyanle.extension_api

/**
 * Created by heyanle on 2024/6/2.
 * https://github.com/heyanLE
 */
lateinit var nativeHelper: NativeHelper

fun Extension.tryLoadNativeLib(libName: String) = nativeHelper.tryLoadNativeLib(this, libName)

interface NativeHelper {

    // 最终会加载 path/[ABI]/lib[libName].so 的文件
    fun tryLoadNativeLib(extension: Extension, libName: String): Boolean

}