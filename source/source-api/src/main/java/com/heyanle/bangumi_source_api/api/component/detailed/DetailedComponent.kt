package com.heyanle.bangumi_source_api.api.component.detailed

import androidx.annotation.Keep
import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.component.Component
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine

/**
 * Created by HeYanLe on 2023/3/4 14:37.
 * https://github.com/heyanLE
 */
@Keep
interface DetailedComponent : Component {

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