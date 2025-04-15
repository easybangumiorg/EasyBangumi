package org.easybangumi.next.shared.plugin.api.component.pref


/**
 * Created by HeYanLe on 2024/12/8 22:08.
 * https://github.com/heyanLE
 */

sealed class SourcePreference {

    abstract val label: String
    abstract val key: String
    abstract val def: String

    class Selection(
        override val label: String,
        override val key: String,
        override val def: String,
        val selections: List<String>
    ): SourcePreference()

    class Edit(
        override val label: String,
        override val key: String,
        override val def: String,
    ): SourcePreference()

    // 如果为开关配置，则获取到的数据为 "true" 或者 "false"
    class Switch(
        override val label: String,
        override val key: String,
        private val defBoolean: Boolean,
    ): SourcePreference() {

        override val def: String
            get() = defBoolean.toString()
    }

}