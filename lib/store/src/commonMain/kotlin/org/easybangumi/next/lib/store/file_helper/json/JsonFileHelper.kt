package org.easybangumi.next.lib.store.file_helper.json

import kotlinx.coroutines.CoroutineScope
import org.easybangumi.next.lib.global.Global
import org.easybangumi.next.lib.serialization.deserialize
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.serialization.serialize
import org.easybangumi.next.lib.store.file_helper.AbsFileHelper
import org.easybangumi.next.lib.unifile.UFD
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
class JsonFileHelper<T: Any>(
    folder: UFD,
    name: String,
    clazz: KClass<T>,
    def: T,
    scope: CoroutineScope
): AbsFileHelper<T>(folder, name, clazz, def, scope) {

    override fun suffix(): String {
        return "json"
    }

    override fun serializer(clazz: KClass<T>, data: T): String {
        return Global.jsonSerializer().serialize(data)
    }

    override fun deserializer(clazz: KClass<T>, source: String): T? {
        return Global.jsonSerializer().deserialize(source, clazz, null)
    }
}