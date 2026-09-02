package com.heyanle.easybangumi4.cartoon.story.download

import com.heyanle.easybangumi4.cartoon.entity.DownloadDestination
import com.heyanle.easybangumi4.cartoon.story.download.action.CopyAndNfoAction
import com.heyanle.easybangumi4.cartoon.story.download.action.CopyToFlatAction
import com.heyanle.easybangumi4.cartoon.story.download.action.ParseAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TranscodeAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TransformerAction
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadAction

/**
 * 纯领域计划：完整模式保持旧链，快速模式只在传输阶段解析具体引擎。
 * 仅最后落盘一步按目的地区分：本地番源条目走 CopyAndNfoAction，扁平目录走 CopyToFlatAction。
 */
object DownloadTaskPlan {

    fun steps(quickMode: Boolean, destination: Int): List<String> {
        val lastStep = if (destination == DownloadDestination.FLAT) {
            CopyToFlatAction.NAME
        } else {
            CopyAndNfoAction.NAME
        }
        return if (quickMode) {
            listOf(
                ParseAction.NAME,
                QuickDownloadAction.NAME,
                TranscodeAction.NAME,
                lastStep,
            )
        } else {
            listOf(
                ParseAction.NAME,
                TransformerAction.NAME,
                lastStep,
            )
        }
    }

    fun steps(quickMode: Boolean): List<String> {
        return steps(quickMode, DownloadDestination.LOCAL_STORY)
    }
}
