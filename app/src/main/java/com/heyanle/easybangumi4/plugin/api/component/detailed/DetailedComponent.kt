package com.heyanle.easybangumi4.plugin.api.component.detailed


import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.Component
import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine

/**
 * Created by HeYanLe on 2023/10/18 23:26.
 * https://github.com/heyanLE
 */
interface DetailedComponent: Component {
    class NonPlayLine(
        playLine: PlayLine
    ): List<PlayLine> by listOf(playLine)

    /**
     * 获取番剧详细信息
     */
    suspend fun getDetailed(
        summary: CartoonSummary
    ): SourceResult<Cartoon>

    /**
     * 获取播放线路
     */
    suspend fun getPlayLine(
        summary: CartoonSummary
    ): SourceResult<List<PlayLine>>

    /**
     * 同时获取
     */
    suspend fun getAll(
        summary: CartoonSummary
    ): SourceResult<Pair<Cartoon, List<PlayLine>>>
}