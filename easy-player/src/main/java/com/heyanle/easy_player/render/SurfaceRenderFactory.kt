package com.heyanle.easy_player.render

import android.content.Context

/**
 * Created by HeYanLe on 2022/10/23 15:52.
 * https://github.com/heyanLE
 */
class SurfaceRenderFactory: IRenderFactory {
    override fun createRender(context: Context): IRender {
        return SurfaceRender(context)
    }
}