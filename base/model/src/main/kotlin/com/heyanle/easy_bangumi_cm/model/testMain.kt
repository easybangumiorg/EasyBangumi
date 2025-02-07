package com.heyanle.easy_bangumi_cm.model

import com.heyanle.easy_bangumi_cm.model.meida.local.videoStackedFileRules

fun main() {
    val rule = videoStackedFileRules[0]
    rule.match("Bad Boys (2006) part1.mkv")?.let {
        println(it)
    }
    rule.match("Bad Boys (2006) part2.mkv")?.let {
        println(it)
    }
    rule.match("300 (2006) part2")?.let {
        println(it)
    }
    rule.match("300 (2006) part3")?.let {
        println(it)
    }
    rule.match("300 (2006) part1")?.let {
        println(it)
    }
}