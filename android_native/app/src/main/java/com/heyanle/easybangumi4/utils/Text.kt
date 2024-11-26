package com.heyanle.easybangumi4.utils

/**
 * 屏蔽带有某些关键字的弹幕
 *
 * @return 若屏蔽此字符串，则返回true，否则false
 */
fun String.shield(): Boolean {
    return false
}

/**
 * 格式化合成字符串，生成类似"1 - 2 - 3"这样的字符串，空白或者null不会加上多余分隔符
 */
fun formatMergedStr(delimiter: String, vararg strs: String?) = StringBuilder().apply {
    for (str in strs)
        if (!str.isNullOrBlank())
            append(str).append(delimiter)
}.removeSuffix(delimiter).toString()

/**
 * 如果此CharSequence包含指定的其他多个字符序列中的任意一个作为子字符串，则返回true 。
 */
fun CharSequence.containStrs(vararg strs: CharSequence) = strs.find { contains(it) } != null