package com.heyanle.easy_bangumi_cm.base


/**
 * Created by HeYanLe on 2024/12/3 0:10.
 * https://github.com/heyanLE
 */

interface Platform {

    val namespace: String
    val platformName: String

    val versionCode : Int
    val versionName : String

    val isRelease: Boolean

    val isAndroid: Boolean
    val isIos: Boolean
    val isDesktop: Boolean


}