package com.heyanle.bangumi_source_api.api2.play

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.entity.Cartoon
import com.heyanle.bangumi_source_api.api2.entity.CartoonSummary

/**
 * Created by HeYanLe on 2023/2/18 21:52.
 * https://github.com/heyanLE
 */
interface PlaySource: Source {

    suspend fun detail(cartoonSummary: CartoonSummary): Source.SourceResult<Cartoon>

    suspend fun playLine(cartoonSummary: CartoonSummary): Source.SourceResult<List<PlayLine>>

    suspend fun playInfo(cartoonSummary: CartoonSummary, playLine: PlayLine): Source.SourceResult<PlayerInfo>

    // 检查番剧更新，如果有更新需要 isUpdate 为 true
    suspend fun update(cartoon: Cartoon): Source.SourceResult<Cartoon>

}