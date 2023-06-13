package com.heyanle.extension_load

import android.content.Context
import com.heyanle.extension_api.IconFactory
import com.heyanle.extension_api.iconFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by HeYanLe on 2023/2/22 23:25.
 * https://github.com/heyanLE
 */
object ExtensionInit {

    private val initLock = AtomicBoolean(false)

    fun init(
        context: Context,
        ic: IconFactory
    ){
        if(initLock.compareAndSet(false, true)){
            iconFactory = ic
            ExtensionController.init(context)
        }

    }
}