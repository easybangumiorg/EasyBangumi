package org.easybangumi.next.lib.utils

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
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
