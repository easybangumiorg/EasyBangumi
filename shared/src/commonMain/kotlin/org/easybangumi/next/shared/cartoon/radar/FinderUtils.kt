package org.easybangumi.next.shared.cartoon.radar

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
// 编辑距离算法
fun String.editDistance(other: String): Int {
    val dp = Array(this.length + 1) { IntArray(other.length + 1) }
    for (i in 0..this.length) {
        for (j in 0..other.length) {
            if (i == 0) {
                dp[i][j] = j
            } else if (j == 0) {
                dp[i][j] = i
            } else if (this[i - 1] == other[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1]
            } else {
                dp[i][j] = minOf(
                    dp[i - 1][j - 1] + 1, // 替换
                    dp[i - 1][j] + 1,     // 删除
                    dp[i][j - 1] + 1      // 插入
                )
            }
        }
    }
    return dp[this.length][other.length]
}