package com.heyanle.easybangumi4.source_api.entity

import java.net.URLEncoder

/**
 * 历史遗留问题，插件化不能随便加和继承，只能扩展进去了
 * Created by heyanlin on 2023/8/4.
 * https://github.com/heyanLE
 */

fun CartoonCover.toIdentify(): String {
    return "${id},${source}"
}

fun Cartoon.toIdentify(): String {
    return "${id},${source}"
}

fun CartoonCover.matchIdentify(identify: String): Boolean {
    return toIdentify() == identify
}

fun Cartoon.matchIdentify(identify: String): Boolean {
    return toIdentify() == identify
}