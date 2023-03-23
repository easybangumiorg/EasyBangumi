package com.heyanle.bangumi_source_api.api.configuration

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/2/22 20:00.
 * https://github.com/heyanLE
 */
@Keep
sealed class ConfigType<T> {

    class Edit<T> : ConfigType<T>()

    class Selection<T>(
        val list: List<T>,
    ) : ConfigType<T>()

}