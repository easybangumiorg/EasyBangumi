package com.heyanle.easybangumi4.source_api.component.update


import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.Component
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.PlayLine

/**
 * Created by HeYanLe on 2023/10/18 23:47.
 * https://github.com/heyanLE
 */
interface UpdateComponent: Component {

    /**
     * 更新番剧
     * 如果有更新需要将 Cartoon.isUpdate 置位 true
     */
    suspend fun update(cartoon: Cartoon, oldPlayLine: List<PlayLine>): SourceResult<Cartoon>
}