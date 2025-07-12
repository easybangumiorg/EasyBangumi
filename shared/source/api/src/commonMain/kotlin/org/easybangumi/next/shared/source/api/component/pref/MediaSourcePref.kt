package org.easybangumi.next.shared.source.api.component.pref


/**
 * Created by HeYanLe on 2024/12/8 22:08.
 * https://github.com/heyanLE
 */

sealed class MediaSourcePreference {

    abstract val label: String
    abstract val key: String
    abstract val def: String

    class Selection(
        override val label: String,
        override val key: String,
        override val def: String,
        val selections: List<String>
    ): MediaSourcePreference()

    class Edit(
        override val label: String,
        override val key: String,
        override val def: String,
    ): MediaSourcePreference()

    // 如果为开关配置，则获取到的数据为 "true" 或者 "false"
    class Switch(
        override val label: String,
        override val key: String,
        private val defBoolean: Boolean,
    ): MediaSourcePreference() {

        override val def: String
            get() = defBoolean.toString()
    }

}