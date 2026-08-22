package com.heyanle.easybangumi4.cartoon.story.download

import com.heyanle.easybangumi4.cartoon.story.download.action.CopyAndNfoAction
import com.heyanle.easybangumi4.cartoon.story.download.action.ParseAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TranscodeAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TransformerAction
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadAction
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadTaskPlanTest {

    @Test
    fun `full mode keeps legacy transformer pipeline`() {
        assertEquals(
            listOf(ParseAction.NAME, TransformerAction.NAME, CopyAndNfoAction.NAME),
            DownloadTaskPlan.steps(quickMode = false),
        )
    }

    @Test
    fun `quick mode delegates only transfer stage to engine registry`() {
        assertEquals(
            listOf(
                ParseAction.NAME,
                QuickDownloadAction.NAME,
                TranscodeAction.NAME,
                CopyAndNfoAction.NAME,
            ),
            DownloadTaskPlan.steps(quickMode = true),
        )
    }
}
