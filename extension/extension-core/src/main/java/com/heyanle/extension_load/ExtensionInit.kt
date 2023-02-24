package com.heyanle.extension_load

import android.content.Context
import com.heyanle.extension_api.IconFactory
import com.heyanle.extension_api.iconFactory

/**
 * Created by HeYanLe on 2023/2/22 23:25.
 * https://github.com/heyanLE
 */
object ExtensionInit {

    fun init(
        context: Context,
        ic: IconFactory
    ){
        iconFactory = ic
        ExtensionController.init(context)
    }
}