package com.heyanle.bangumi_source_api.api.configuration

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/2/22 19:56.
 * https://github.com/heyanLE
 */
@Keep
class ConfigField(
    val sourceKey: String,
    val configKey: String,
    val label: String,
    val def: String,
    val type: ConfigType = ConfigType.Edit
) {
    fun getValue(): String = configFactory.currentConfigString(this)
}



fun ConfigSource.config(
    configKey: String,
    label: String,
    def: String,
    type: ConfigType = ConfigType.Edit
): ConfigField {
    return ConfigField(this.key, configKey, label, def, type)
}





