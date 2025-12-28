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
    ): MediaSourcePreference() {
        override fun check(): Boolean {
            return key.isNotBlank() && label.isNotBlank() && selections.isNotEmpty() && selections.contains(def)
        }
    }

    class Edit(
        override val label: String,
        override val key: String,
        override val def: String,
    ): MediaSourcePreference() {
        override fun check(): Boolean {
            return key.isNotBlank() && label.isNotBlank()
        }
    }

    // 如果为开关配置，则获取到的数据为 "true" 或者 "false"
    class Switch(
        override val label: String,
        override val key: String,
        private val defBoolean: Boolean,
    ): MediaSourcePreference() {


        override fun check(): Boolean {
            return key.isNotBlank() && label.isNotBlank()
        }

        override val def: String
            get() = defBoolean.toString()
    }

    abstract fun check(): Boolean
    fun ifAvailable(): MediaSourcePreference? {
        return if (check()) {
            this
        } else {
            null
        }
    }

}