package org.easybangumi.next.shared.source.quick.component

import com.dokar.quickjs.QuickJs
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.pref.IPrefComponent
import org.easybangumi.next.shared.source.api.component.pref.MediaSourcePreference
import org.easybangumi.next.shared.source.api.component.pref.PrefComponent
import org.easybangumi.next.shared.source.quick.utils.callFunctionWithDataState
import org.easybangumi.next.shared.source.quick.utils.checkFunctionExists
import org.easybangumi.next.shared.source.quick.utils.toDataState
import kotlin.reflect.KClass

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class QuickPrefComponentWrapper(
    prefComponent: IPrefComponent
): QuickComponentWrapper,
    BaseComponent(),
    PrefComponent,
    IPrefComponent by prefComponent {

    companion object {

        const val COMPONENT_NAME_PREF = "PrefComponent"
        const val FUNCTION_NAME_REGISTER = "register"

    }

    class Factory: QuickComponentWrapper.Factory<QuickPrefComponentWrapper> {

        override suspend fun create(quickJs: QuickJs): QuickPrefComponentWrapper? {
            // 1. check functions
            if (!quickJs.checkFunctionExists(
                "${COMPONENT_NAME_PREF}_${FUNCTION_NAME_REGISTER}",
            )) {
                return null
            }

            // 2. create proxy
            val prefComponentProxy: IPrefComponent = object: IPrefComponent {
                override suspend fun register(): DataState<List<MediaSourcePreference>> {
                    return quickJs.callFunctionWithDataState(
                        "${COMPONENT_NAME_PREF}_${FUNCTION_NAME_REGISTER}",
                        args = emptyArray()
                    ).toDataState<List<QuickMediaSourcePreferenceItem>>()?.map {
                        it.map { it.toMediaSourcePreference() }
                    } ?: throw Exception("QuickJS PrefComponent register returned invalid data: null")
                }
            }

            // 3. return wrapper
            return QuickPrefComponentWrapper(
                prefComponent = prefComponentProxy
            )
        }

    }

    override fun getComponentClazz(): Array<KClass<*>> {
        return arrayOf(PrefComponent::class)
    }

    data class QuickMediaSourcePreferenceItem(
        val type: String,
        val label: String? = null,
        val key: String? = null,
        val defaultString: String? = null,
        val selections: List<String> = emptyList(),
    ) {
        companion object {
            const val TYPE_EDIT = "edit"
            const val TYPE_SELECTION = "selection"
            const val TYPE_SWITCH = "switch"
        }

        fun toMediaSourcePreference(): MediaSourcePreference {
            return when (type) {
                TYPE_EDIT -> MediaSourcePreference.Edit(
                    label = label ?: "",
                    key = key ?: "",
                    def = defaultString ?: ""
                )
                TYPE_SELECTION -> MediaSourcePreference.Selection(
                    label = label ?: "",
                    key = key ?: "",
                    selections = selections,
                    def = defaultString ?: ""
                )
                TYPE_SWITCH -> MediaSourcePreference.Switch(
                    label = label ?: "",
                    key = key ?: "",
                    defBoolean = defaultString?.toBoolean() ?: false
                )
                else -> throw IllegalArgumentException("Unknown preference type: $type")
            }.ifAvailable() ?: throw IllegalArgumentException("Invalid preference data for type: $type")
        }
    }

}