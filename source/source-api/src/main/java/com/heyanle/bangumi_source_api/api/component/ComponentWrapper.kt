package com.heyanle.bangumi_source_api.api.component

import androidx.annotation.Keep
import com.heyanle.bangumi_source_api.api.Source

/**
 * Created by HeYanLe on 2023/3/1 17:24.
 * https://github.com/heyanLE
 */
@Keep
open class ComponentWrapper(
    override var source: Source,
): Component