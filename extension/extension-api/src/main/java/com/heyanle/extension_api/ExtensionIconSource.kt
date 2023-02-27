package com.heyanle.extension_api

import android.graphics.drawable.Drawable
import com.heyanle.bangumi_source_api.api.IconSource

/**
 * Created by HeYanLe on 2023/2/22 20:14.
 * https://github.com/heyanLE
 */
interface ExtensionIconSource: IconSource {

    fun getIconResourcesId(): Int?
    override fun getIconFactory(): () -> Drawable? {
        return {
            iconFactory.getIcon(this)
        }
    }

}