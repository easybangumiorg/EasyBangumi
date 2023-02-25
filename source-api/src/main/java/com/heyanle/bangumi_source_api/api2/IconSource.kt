package com.heyanle.bangumi_source_api.api2

import android.graphics.drawable.Drawable

/**
 * Created by HeYanLe on 2023/2/22 20:12.
 * https://github.com/heyanLE
 */
interface IconSource : Source {

    fun getIconFactory(): () -> (Drawable?)

}