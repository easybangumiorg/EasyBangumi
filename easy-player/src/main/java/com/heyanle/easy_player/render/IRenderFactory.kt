package com.heyanle.easy_player.render

import android.content.Context

/**
 * Created by HeYanLe on 2022/10/23 15:07.
 * https://github.com/heyanLE
 */
interface IRenderFactory {

    fun createRender(context: Context): IRender

}