package com.heyanle.bangumi_source_api.api.configuration

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/2/22 19:56.
 * https://github.com/heyanLE
 */
@Keep
sealed class ConfigField<T> {

    abstract val sourceKey: String
    abstract val configKey: String
    abstract val label: String
    abstract val def: T
    abstract fun getValue(): T

    class ConfigBoolean(
        override val sourceKey: String,
        override val configKey: String,
        override val label: String,
        override val def: Boolean,
    ) : ConfigField<Boolean>() {
        override fun getValue(): Boolean {
            return configFactory.currentConfigBoolean(this)
        }
    }

    class ConfigString(
        override val sourceKey: String,
        override val configKey: String,
        override val label: String,
        override val def: String,
        val type: ConfigType<String>,
    ) : ConfigField<String>() {
        override fun getValue(): String {
            return configFactory.currentConfigString(this)
        }
    }

    class ConfigInt(
        override val sourceKey: String,
        override val configKey: String,
        override val label: String,
        override val def: Int,
        val type: ConfigType<Int>,
    ) : ConfigField<Int>() {
        override fun getValue(): Int {
            return configFactory.currentConfigInt(this)
        }
    }

    class ConfigLong(
        override val sourceKey: String,
        override val configKey: String,
        override val label: String,
        override val def: Long,
        val type: ConfigType<Long>,
    ) : ConfigField<Long>() {
        override fun getValue(): Long {
            return configFactory.currentConfigLong(this)
        }
    }

    class ConfigDouble(
        override val sourceKey: String,
        override val configKey: String,
        override val label: String,
        override val def: Double,
        val type: ConfigType<Double>,
    ) : ConfigField<Double>() {
        override fun getValue(): Double {
            return configFactory.currentConfigDouble(this)
        }
    }

}

fun ConfigSource.config(configKey: String, label: String, def: Boolean): ConfigField.ConfigBoolean {
    return ConfigField.ConfigBoolean(this.key, configKey, label, def)
}

fun ConfigSource.config(
    configKey: String,
    label: String,
    def: String,
    type: ConfigType<String> = ConfigType.Edit()
): ConfigField.ConfigString {
    return ConfigField.ConfigString(this.key, configKey, label, def, type)
}

fun ConfigSource.config(
    configKey: String,
    label: String,
    def: Int,
    type: ConfigType<Int> = ConfigType.Edit()
): ConfigField.ConfigInt {
    return ConfigField.ConfigInt(this.key, configKey, label, def, type)
}

fun ConfigSource.config(
    configKey: String,
    label: String,
    def: Long,
    type: ConfigType<Long> = ConfigType.Edit()
): ConfigField.ConfigLong {
    return ConfigField.ConfigLong(this.key, configKey, label, def, type)
}

fun ConfigSource.config(
    configKey: String,
    label: String,
    def: Double,
    type: ConfigType<Double> = ConfigType.Edit()
): ConfigField.ConfigDouble {
    return ConfigField.ConfigDouble(this.key, configKey, label, def, type)
}