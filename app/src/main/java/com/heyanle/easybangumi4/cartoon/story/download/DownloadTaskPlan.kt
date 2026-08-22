package com.heyanle.easybangumi4.cartoon.story.download

import com.heyanle.easybangumi4.cartoon.story.download.action.CopyAndNfoAction
import com.heyanle.easybangumi4.cartoon.story.download.action.ParseAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TranscodeAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TransformerAction
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadAction

/**
 * 纯领域计划：完整模式保持旧链，快速模式只在传输阶段解析具体引擎。
 */
object DownloadTaskPlan {
    private val quickSteps = listOf(
        ParseAction.NAME,
        QuickDownloadAction.NAME,
        TranscodeAction.NAME,
        CopyAndNfoAction.NAME,
    )

    private val fullSteps = listOf(
        ParseAction.NAME,
        TransformerAction.NAME,
        CopyAndNfoAction.NAME,
    )

    fun steps(quickMode: Boolean): List<String> {
        return if (quickMode) quickSteps else fullSteps
    }
}
