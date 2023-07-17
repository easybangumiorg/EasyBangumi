package com.heyanle.lib_anim.utils

import java.io.File

/**
 * Created by HeYanLe on 2023/6/22 15:05.
 * https://github.com/heyanLE
 */

lateinit var dataHelper: DataHelper
interface DataHelper {

    fun save(sourceContext: SourceContext, key: String, value: String)

    fun load(sourceContext: SourceContext, key: String, def: String)

    fun rootFile(sourceContext: SourceContext): File

}