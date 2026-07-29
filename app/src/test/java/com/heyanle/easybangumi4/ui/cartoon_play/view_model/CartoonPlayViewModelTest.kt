package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class CartoonPlayViewModelTest {

    @Test
    fun sortRefreshKeepsPlaybackIdentityAndSelectedLine() {
        val episodeOne = Episode("episode-1", "第 2 集", 2)
        val episodeTwo = Episode("episode-2", "第 10 集", 10)
        val firstLine = PlayLine("line-1", "线路一", arrayListOf(episodeOne))
        val secondLine = PlayLine("line-2", "线路二", arrayListOf(episodeOne, episodeTwo))
        val initialLines = listOf(
            wrapper(firstLine, compareBy { it.order }),
            wrapper(secondLine, compareBy { it.order }),
        )
        val refreshedLines = listOf(
            wrapper(firstLine, compareBy { it.label }),
            wrapper(secondLine, compareBy { it.label }),
        )
        val viewModel = CartoonPlayViewModel(enterFor("line-2", "episode-2"))

        viewModel.onCartoonInfoChange(cartoonInfo(), initialLines)
        val initialState = viewModel.curringPlayState.value!!

        viewModel.onCartoonInfoChange(
            cartoonInfo(sortByKey = "label"),
            refreshedLines,
        )

        val refreshedState = viewModel.curringPlayState.value!!
        assertSame(initialState, refreshedState)
        assertEquals("line-2", refreshedState.playLine.playLine.id)
        assertEquals("episode-2", refreshedState.episode.id)
        assertEquals("line-2", viewModel.selectedLineId)
        assertEquals(1, viewModel.resolveSelectedLineIndex(refreshedLines))
        assertNotSame(refreshedLines[1], refreshedState.playLine)
    }

    @Test
    fun tryNextUsesLatestSortedLineSnapshot() {
        val episodeWithOrderTwo = Episode("episode-2", "第 2 集", 2)
        val episodeWithOrderTen = Episode("episode-10", "第 10 集", 10)
        val playLine = PlayLine(
            "line-1",
            "线路一",
            arrayListOf(episodeWithOrderTwo, episodeWithOrderTen),
        )
        val orderWrapper = wrapper(playLine, compareBy { it.order })
        val labelWrapper = wrapper(playLine, compareBy { it.label })
        val viewModel = CartoonPlayViewModel(enterFor("line-1", "episode-10"))

        viewModel.onCartoonInfoChange(cartoonInfo(), listOf(orderWrapper))
        viewModel.onCartoonInfoChange(
            cartoonInfo(sortByKey = "label"),
            listOf(labelWrapper),
        )
        viewModel.tryNext()

        val nextState = viewModel.curringPlayState.value!!
        assertSame(labelWrapper, nextState.playLine)
        assertEquals("episode-2", nextState.episode.id)
        assertEquals("line-1", viewModel.selectedLineId)
    }

    @Test
    fun explicitPlaySelectionSynchronizesStableLineId() {
        val episode = Episode("episode-1", "第 1 集", 1)
        val firstLine = wrapper(
            PlayLine("line-1", "线路一", arrayListOf(episode)),
            compareBy { it.order },
        )
        val secondLine = wrapper(
            PlayLine("line-2", "线路二", arrayListOf(episode)),
            compareBy { it.order },
        )
        val lines = listOf(firstLine, secondLine)
        val viewModel = CartoonPlayViewModel(enterFor("line-1", "episode-1"))
        val info = cartoonInfo()
        viewModel.onCartoonInfoChange(info, lines)

        viewModel.changePlay(info, secondLine, episode)

        assertEquals("line-2", viewModel.selectedLineId)
        assertEquals(1, viewModel.selectedLineIndex)
        assertEquals(1, viewModel.resolveSelectedLineIndex(lines))
    }

    private fun wrapper(
        playLine: PlayLine,
        comparator: Comparator<Episode>,
    ): PlayLineWrapper = PlayLineWrapper(
        playLine = playLine,
        comparator = comparator,
    )

    private fun cartoonInfo(sortByKey: String = ""): CartoonInfo = CartoonInfo(
        id = "cartoon-1",
        source = "source-1",
        name = "测试番剧",
        coverUrl = "",
        intro = "",
        url = "",
        isDetailed = true,
        sortByKey = sortByKey,
    )

    private fun enterFor(
        lineId: String,
        episodeId: String,
    ) = CartoonPlayViewModel.EnterData(
        playLineId = lineId,
        playLineLabel = "",
        playLineIndex = -1,
        episodeId = episodeId,
        episodeLabel = "",
        episodeOrder = -1,
        episodeIndex = -1,
        adviceProgress = -1L,
    )
}
