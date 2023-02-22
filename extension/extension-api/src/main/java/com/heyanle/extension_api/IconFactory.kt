package com.heyanle.extension_api

import android.graphics.drawable.Drawable

/**
 * Created by HeYanLe on 2023/2/22 20:16.
 * https://github.com/heyanLE
 */

lateinit var iconFactory: IconFactory
interface IconFactory {

    fun getIcon(source: ExtensionIconSource): Drawable?

}