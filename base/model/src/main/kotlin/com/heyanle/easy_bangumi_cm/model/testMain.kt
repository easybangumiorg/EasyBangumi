package com.heyanle.easy_bangumi_cm.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.StubType
import com.heyanle.easy_bangumi_cm.model.meida.local.model.VideoFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.videoStackedFileRules

/// 何言没有提供单元测试库，只能使用这样的脏办法进行测试
fun main() {
    val node = VideoFileNode("/server/Movies/brave (2007)/brave (2006)-trailer.bluray.mkv", "brave") {
        year = "2006"
        container = "mkv"
        stub = StubType.BLURAY
    }
    println(node)
    testStackedFileRule()
}

fun testStackedFileRule() {
    val rule = videoStackedFileRules[0]
    rule.match("Bad Boys (2006) part1.mkv")?.let {
        check(it.stackName == "Bad Boys (2006)")
        check(it.partType == "part")
        check(it.partNumber == "1")
    }
    rule.match("Bad Boys (2006) part2.mkv")?.let {
        check(it.stackName == "Bad Boys (2006)")
        check(it.partType == "part")
        check(it.partNumber == "2")
    }
    rule.match("300 (2006) part2")?.let {
        check(it.stackName == "300 (2006)")
        check(it.partType == "part")
        check(it.partNumber == "2")
    }
    rule.match("300 (2006) part3")?.let {
        check(it.stackName == "300 (2006)")
        check(it.partType == "part")
        check(it.partNumber == "3")
    }
    rule.match("300 (2006) part1")?.let {
        check(it.stackName == "300 (2006)")
        check(it.partType == "part")
        check(it.partNumber == "1")
    }
}