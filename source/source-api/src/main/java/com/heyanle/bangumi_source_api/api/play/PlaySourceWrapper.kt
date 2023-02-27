package com.heyanle.bangumi_source_api.api.play

import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.SourceWrapper

/**
 * Created by HeYanLe on 2023/2/27 22:45.
 * https://github.com/heyanLE
 */
abstract class PlaySourceWrapper(
    source: Source
): SourceWrapper(source), PlaySource {
}