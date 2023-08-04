package com.heyanle.bangumi_source_api.api.component.configuration

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/8/4 22:57.
 * https://github.com/heyanLE
 */
@Keep
sealed class SourceConfig {

    abstract val label: String
    abstract val key: String
    abstract val def: String

    class Selection(
        override val label: String,
        override val key: String,
        override val def: String,
        val selections: List<String>
    ): SourceConfig()

    class Edit(
        override val label: String,
        override val key: String,
        override val def: String,
    ): SourceConfig()

    // 如果为开关配置，则获取到的数据为 "true" 或者 "false"
    class Switch(
        override val label: String,
        override val key: String,
        private val defBoolean: Boolean,
    ): SourceConfig() {

        override val def: String
            get() = defBoolean.toString()
    }

}