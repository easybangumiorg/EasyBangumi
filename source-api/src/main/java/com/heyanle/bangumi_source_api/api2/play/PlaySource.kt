package com.heyanle.bangumi_source_api.api2.play

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.component.Component
import com.heyanle.bangumi_source_api.api2.entity.Cartoon
import com.heyanle.bangumi_source_api.api2.entity.CartoonSummary

/**
 * Created by HeYanLe on 2023/2/18 21:52.
 * https://github.com/heyanLE
 */
interface PlaySource: Source {

    /**
     * 获取番剧详情
     */
    suspend fun detail(cartoonSummary: CartoonSummary): Source.SourceResult<Cartoon>

    /**
     * 获取播放线路
     */
    suspend fun playLine(cartoonSummary: CartoonSummary): Source.SourceResult<List<PlayLine>>

    /**
     * 获取播放信息（播放地址或文件路径）
     * @param playLine 对应的播放线路
     * @param episodeIndex 集数
     */
    suspend fun playInfo(cartoonSummary: CartoonSummary, playLine: PlayLine, episodeIndex: Int): Source.SourceResult<PlayerInfo>

    // 检查番剧更新，如果有更新需要 isUpdate 为 true
    suspend fun update(cartoon: Cartoon): Source.SourceResult<Cartoon>

}