package com.heyanle.bangumi_source_api.api.configuration

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/2/22 20:00.
 * https://github.com/heyanLE
 */
@Keep
sealed class ConfigType {

    object Edit : ConfigType()

    class Selection(
        val list: List<String>,
    ) : ConfigType()
}