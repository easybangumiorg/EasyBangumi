package org.easybangumi.next.testing

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaNodeType
import com.heyanle.easy_bangumi_cm.model.meida.local.model.AudioFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.EbookFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.ImageFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.MediaNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.SubtitleFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.VideoFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.resolver.RepoResolver
import com.heyanle.easy_bangumi_cm.model.meida.local.videoStackedFileRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/// 何言没有提供单元测试库，只能使用这样的脏办法进行测试
fun main() = runBlocking {
    val scope = CoroutineScope(Dispatchers.IO)


    val fs_node = TestFileSystemResolver.resolve("F:\\Bangumi")
    val media_tree = RepoResolver().resolve(fs_node)
    println(media_tree)



    testStackedFileRule()
}

fun testStackedFileRule() = videoStackedFileRules.forEach { rule ->
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
