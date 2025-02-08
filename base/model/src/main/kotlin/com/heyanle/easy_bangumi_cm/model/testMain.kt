package com.heyanle.easy_bangumi_cm.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaNodeType
import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.StubType
import com.heyanle.easy_bangumi_cm.model.meida.local.model.AudioFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.EbookFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.ImageFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.MediaNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.RepoRootNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.SubtitleFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.VideoFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.videoStackedFileRules

/// 何言没有提供单元测试库，只能使用这样的脏办法进行测试
fun main() {

    // 测试用的数据
    val repo = RepoRootNode("/home/Bangumi", "Bangumi") {
        children.add(MediaNode("/home/Bangumi/test", "test") {
            type = MediaNodeType.PROJECT
            resources.add(VideoFileNode("/home/Bangumi/test/test.mkv", "test"))
            resources.add(SubtitleFileNode("/home/Bangumi/test/test2.srt", "test2"))
            resources.add(AudioFileNode("/home/Bangumi/test/test3.mp3", "test3"))
            resources.add(EbookFileNode("/home/Bangumi/test2/test.mkv", "test"))
            resources.add(ImageFileNode("/home/Bangumi/test2/test2.mkv", "test2"))
        })
        children.add(MediaNode("/home/Bangumi/test2", "test2") {
            type = MediaNodeType.PROJECT
            resources.add(VideoFileNode("/home/Bangumi/test2/test3.mkv", "test3"))
            children.add(MediaNode("/home/Bangumi/test2/test4", "test4") {
                resources.add(VideoFileNode("/home/Bangumi/test2/test4/test4.mkv", "test4"))
                resources.add(VideoFileNode("/home/Bangumi/test2/test4/test4 part2.mkv", "test4 part2"))
                type = MediaNodeType.SUBPROJECT
                children.add(MediaNode("/home/Bangumi/test2/test4/subtitles", "subtitles") {
                    type = MediaNodeType.RESOURCE
                    resources.add(SubtitleFileNode("/home/Bangumi/test2/test4/subtitles/test4.srt", "test4"))
                    resources.add(SubtitleFileNode("/home/Bangumi/test2/test4/subtitles/test4 part2.srt", "test4 part2"))
                })
            })
        })
    }

    repo.printTree()




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
