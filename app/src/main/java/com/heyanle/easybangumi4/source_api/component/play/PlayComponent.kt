package com.heyanle.easybangumi4.source_api.component.play


import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.Component
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo

/**
 * Created by HeYanLe on 2023/10/18 23:28.
 * https://github.com/heyanLE
 */
interface PlayComponent: Component {

    /**
     * 获取播放信息
     * @param playLine 对应的播放线路
     * @param episode 集
     */
    suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
    ): SourceResult<PlayerInfo>

    suspend fun getPlayInfoWithCheck(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        iWebProxy: IWebProxy,
    ): SourceResult<PlayerInfo> {
        return SourceResult.Error(IllegalStateException("not support"))
    }
}