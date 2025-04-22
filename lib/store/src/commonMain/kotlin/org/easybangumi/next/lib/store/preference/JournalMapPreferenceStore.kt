package org.easybangumi.next.lib.store.preference

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.easybangumi.next.lib.serialization.deserialize
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.store.JournalMapHelper

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
class JournalMapPreferenceStore(
    private val journalMapHelper: JournalMapHelper,
) : PreferenceStore {

    class JournalPreference<T>(
        private val journalMapHelper: JournalMapHelper,
        private val key: String,
        private val defaultValue: T,
        private val serializer: (T) -> String,
        private val deserializer: (String) -> T
    ): Preference<T> {

        override fun key(): String {
            return key
        }

        override fun defaultValue(): T {
            return defaultValue
        }

        override fun isSet(): Boolean {
            return journalMapHelper.isSetSync(key)
        }

        override fun get(): T {
            val value = journalMapHelper.getSync(key)
            return deserializer(value)
        }

        override fun set(value: T) {
            val stringValue = serializer(value)
            journalMapHelper.put(key, stringValue)
        }

        override fun delete() {
            journalMapHelper.put(key, "")
        }

        override fun flow(): Flow<T> {
            return journalMapHelper.flowMap().map {
                it[key]
            }.map {
                if (it == null) {
                    defaultValue
                } else {
                    deserializer(it)
                }
            }
        }

        override fun stateIn(scope: CoroutineScope): StateFlow<T> {
            return flow().stateIn(scope, SharingStarted.Lazily, get())
        }
    }

    override fun getString(
        key: String,
        default: String
    ): Preference<String> {
        return JournalPreference(
            journalMapHelper = journalMapHelper,
            key = key,
            defaultValue = default,
            serializer = { it },
            deserializer = { it }
        )
    }

    override fun getInt(
        key: String,
        default: Int
    ): Preference<Int> {
        return JournalPreference(
            journalMapHelper = journalMapHelper,
            key = key,
            defaultValue = default,
            serializer = { it.toString() },
            deserializer = { it.toIntOrNull()?:default }
        )
    }

    override fun getLong(
        key: String,
        default: Long
    ): Preference<Long> {
        return JournalPreference(
            journalMapHelper = journalMapHelper,
            key = key,
            defaultValue = default,
            serializer = { it.toString() },
            deserializer = { it.toLongOrNull()?:default }
        )
    }

    override fun getFloat(
        key: String,
        default: Float
    ): Preference<Float> {
        return JournalPreference(
            journalMapHelper = journalMapHelper,
            key = key,
            defaultValue = default,
            serializer = { it.toString() },
            deserializer = { it.toFloatOrNull()?:default }
        )
    }

    override fun getBoolean(
        key: String,
        default: Boolean
    ): Preference<Boolean> {
        return JournalPreference(
            journalMapHelper = journalMapHelper,
            key = key,
            defaultValue = default,
            serializer = { it.toString() },
            deserializer = { it.toBooleanStrictOrNull()?:default }
        )
    }

    override fun getStringSet(
        key: String,
        defaultValue: Set<String>
    ): Preference<Set<String>> {
        return JournalPreference(
            journalMapHelper = journalMapHelper,
            key = key,
            defaultValue = defaultValue,
            serializer = {
                jsonSerializer.serialize(it.toList())
            },
            deserializer = {
                val list = jsonSerializer.deserialize<List<String>>(it, defaultValue = defaultValue.toList())
                list?.toSet()?:defaultValue
            }
        )
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T
    ): Preference<T> {
        return JournalPreference(
            journalMapHelper = journalMapHelper,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer
        )
    }

    override fun keySet(): Set<String> {
        return journalMapHelper.mapSync().keys
    }
}