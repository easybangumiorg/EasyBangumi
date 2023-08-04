package com.heyanle.bangumi_source_api.api.component.configuration

import androidx.annotation.Keep
import com.heyanle.bangumi_source_api.api.component.Component

/**
 * Created by HeYanLe on 2023/8/4 22:56.
 * https://github.com/heyanLE
 */
@Keep
interface ConfigComponent: Component {

    fun configs(): List<SourceConfig>

}