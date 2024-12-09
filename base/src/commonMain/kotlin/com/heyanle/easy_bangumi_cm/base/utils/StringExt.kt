package com.heyanle.easy_bangumi_cm.base.utils

/**
 * Created by heyanlin on 2024/12/4.
 */
private val regCharSet = setOf<Char>(
    '*', '.', '?', '+', '$', '^', '[', ']', '(', ')', '{', '}', '|', '\\', '/'
)

fun String.getMatchReg(): Regex {
    return runCatching {
        buildString {
            append("(.*)(")
            append(this@getMatchReg.toCharArray().toList().filter { it != ' ' }.joinToString(")(.*)(") {
                if (regCharSet.contains(it)) {
                    "\${it}"
                } else it + ""

            })
            append(")(.*)")
        }.toRegex(RegexOption.IGNORE_CASE)
    }.getOrElse {
        // 出错了就什么都不给你匹配
        "".toRegex(RegexOption.IGNORE_CASE)
    }
}