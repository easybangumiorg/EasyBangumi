package plugin.easy_config

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import kotlin.reflect.KType

/**
 * Represents a collection of configuration properties for a specific purpose or module.
 * It's designed to encapsulate various types of configuration properties in a unified structure.
 *
 * @property properties A list of configuration property types. Each entry in this list is an instance
 * of [ConfigPropertyTypes], which can represent different kinds of configuration values, including
 * primitive types, complex object types, and lists. This allows for a flexible and extensible design
 * where different kinds of configuration properties can be added as needed.
 */
data class ConfigProperties(

    @Nested
    val properties: List<ConfigPropertyTypes<*>>,
) {

    sealed class ConfigPropertyTypes<T>(
        @Input
        val name: String,
        @Internal
        val clazz: Class<T>,
        @Input
        val template: String,
        @Input
        @Optional
        val value: T
    ) {

        class IntPropertyType(
            name: String,
            value: Int
        ) : ConfigPropertyTypes<Int>(name, Int::class.java, "%L", value)

        class StringPropertyType(
            name: String,
            value: String
        ) : ConfigPropertyTypes<String>(name, String::class.java, "%S", value)

        class BooleanPropertyType(
            name: String,
            value: Boolean
        ) : ConfigPropertyTypes<Boolean>(name, Boolean::class.java, "%L", value)


        companion object {
            fun from(
                name: String,
                value: Any
            ): ConfigPropertyTypes<*>? {
                return when (value) {
                    is Int -> IntPropertyType(name, value)
                    is String -> StringPropertyType(name, value)
                    is Boolean -> BooleanPropertyType(name, value)
                    else -> null
                }
            }
        }

    }
}
