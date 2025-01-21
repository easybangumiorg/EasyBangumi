package com.heyanle.easy_bangumi_cm.shared.utils

import com.heyanle.easy_bangumi_cm.repository.cartoon.CartoonInfo
import com.heyanle.easy_bangumi_cm.shared.utils.string.getMatchReg

/**
 * Created by heyanlin on 2025/1/21.
 */
fun CartoonInfo.matches(query: String): Boolean {
    var matched = false
    for (match in query.split(',')) {
        val regex = match.getMatchReg()
        if (name.matches(regex)) {
            matched = true
            break
        }
    }
    return matched
}