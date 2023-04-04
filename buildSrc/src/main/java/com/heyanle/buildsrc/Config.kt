package com.heyanle.buildsrc

/**
 * Created by HeYanLe on 2023/1/29 20:10.
 * https://github.com/heyanLE
 */
object Config {

    const val APP_CENTER_SECRET = "APP_CENTER_SECRET"

    fun getPrivateValue(key: String): String {
        return kotlin.runCatching {
            Class.forName("com.heyanle.buildsrc.PrivateValue").getField(key)
                .get(null) as String
        }.getOrElse {
            ""
        }
    }


}