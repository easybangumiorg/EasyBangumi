package com.heyanle.bangumi_source_api.api.component.update

import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.component.Component
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.PlayLine

/**
 * Created by HeYanLe on 2023/3/4 15:38.
 * https://github.com/heyanLE
 */
interface UpdateComponent: Component {

    /**
     * 更新番剧
     * 如果有更新需要将 Cartoon.isUpdate 置位 true
     */
    suspend fun update(cartoon: Cartoon, oldPlayLine: List<PlayLine>): SourceResult<Cartoon>
}