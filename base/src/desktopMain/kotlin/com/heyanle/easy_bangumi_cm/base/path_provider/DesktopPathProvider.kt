package com.heyanle.easy_bangumi_cm.base.path_provider


/**
 * Created by HeYanLe on 2024/12/1 23:22.
 * https://github.com/heyanLE
 */

class DesktopPathProvider: PathProvider {

    private val PROP = System.getProperty("compose.application.resources.dir")

    override fun getCachePath(type: String): String {
        TODO("Not yet implemented")
    }

    override fun getFilePath(type: String): String {
        TODO("Not yet implemented")
    }
}