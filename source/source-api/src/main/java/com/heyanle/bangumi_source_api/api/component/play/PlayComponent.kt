package com.heyanle.bangumi_source_api.api.component.play

import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.component.Component
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo

/**
 * Created by HeYanLe on 2023/2/27 22:00.
 * https://github.com/heyanLE
 */
interface PlayComponent: Component {



    /**
     * 获取播放信息
     * @param playLine 对应的播放线路
     * @param episodeIndex 集数
     */
    suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episodeIndex: Int
    ): SourceResult<PlayerInfo>




}