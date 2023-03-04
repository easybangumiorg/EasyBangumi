package com.heyanle.bangumi_source_api.api.component.detailed

import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.component.Component
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary

/**
 * Created by HeYanLe on 2023/3/4 14:37.
 * https://github.com/heyanLE
 */
interface DetailedComponent : Component {

    suspend fun getDetailed(
        summary: CartoonSummary
    ): SourceResult<Cartoon>


}