package com.heyanle.bangumi_source_api.api.page

import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.SourceWrapper

/**
 * Created by HeYanLe on 2023/2/27 22:46.
 * https://github.com/heyanLE
 */
abstract class PageSourceWrapper(
    source: Source
): SourceWrapper(source), PageSource {
}