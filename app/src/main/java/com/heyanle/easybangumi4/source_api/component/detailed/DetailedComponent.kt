package com.heyanle.easybangumi4.source_api.component.detailed


import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.Component
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.PlayLine

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