package com.heyanle.bangumi_source_api.api.search

import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.SourceWrapper

/**
 * Created by HeYanLe on 2023/2/27 22:43.
 * https://github.com/heyanLE
 */
abstract class SearchSourceWrapper(
    source: Source
): SourceWrapper(source), SearchSource