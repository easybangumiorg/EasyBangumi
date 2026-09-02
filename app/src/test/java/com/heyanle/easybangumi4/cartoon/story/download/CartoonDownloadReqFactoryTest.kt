package com.heyanle.easybangumi4.cartoon.story.download

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalEpisode
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalItem
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.story.download.req.CartoonDownloadReqFactory
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import org.junit.Assert.assertEquals
import org.junit.Test

class CartoonDownloadReqFactoryTest {

    @Test
    fun localStoryNumbers_startAtOne_independentOfSourceEpisodeOrder() {
        val requests = CartoonDownloadReqFactory.newReqList(
            cartoonInfo = cartoonInfo(),
            playLine = playLine(),
            list = listOf(
                Episode("source-0", "先行版", 0),
                Episode("source-12", "正片", 12),
            ),
            targetLocalInfo = localStory(),
        )

        assertEquals(listOf(1, 2), requests.map { it.toEpisode })
        assertEquals(listOf("先行版", "正片"), requests.map { it.toEpisodeTitle })
    }

    @Test
    fun localStoryNumbers_skipOccupiedNumbers_butNeverAutoAssignZero() {
        val requests = CartoonDownloadReqFactory.newReqList(
            cartoonInfo = cartoonInfo(),
            playLine = playLine(),
            list = listOf(
                Episode("source-3", "第三个视频", 3),
                Episode("source-4", "第四个视频", 4),
            ),
            targetLocalInfo = localStory(occupiedNumbers = listOf(1, 3)),
        )

        assertEquals(listOf(2, 4), requests.map { it.toEpisode })
    }

    private fun cartoonInfo() = CartoonInfo(
        id = "cartoon-id",
        source = "source-id",
        name = "测试番剧",
        coverUrl = "",
        intro = "",
        url = "",
    )

    private fun playLine() = PlayLine("line-id", "线路", arrayListOf())

    private fun localStory(occupiedNumbers: List<Int> = emptyList()) = CartoonStoryItem(
        cartoonLocalItem = CartoonLocalItem(
            folderUri = "",
            nfoUri = "",
            itemId = "local-id",
            title = "本地番剧",
            desc = "",
            cover = "",
            genre = emptyList(),
            episodes = occupiedNumbers.map { number ->
                CartoonLocalEpisode(
                    title = "已有视频 $number",
                    episode = number,
                    addTime = "",
                    mediaUri = "content://local/$number",
                    nfoUri = "",
                )
            },
        ),
        downloadInfoList = emptyList(),
    )
}
