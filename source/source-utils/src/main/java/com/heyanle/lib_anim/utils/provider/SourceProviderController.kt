package com.heyanle.lib_anim.utils.provider

import com.heyanle.lib_anim.utils.SourceContext

/**
 * Created by HeYanLe on 2023/8/4 23:35.
 * https://github.com/heyanLE
 */
lateinit var sourceProviderController: SourceProviderController
interface SourceProviderController {

    fun getProvider(sourceContext: SourceContext): SourceProvider?
}