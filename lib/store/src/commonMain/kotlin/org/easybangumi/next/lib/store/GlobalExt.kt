package org.easybangumi.next.lib.store

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.easybangumi.next.lib.global.Global
import org.easybangumi.next.lib.global.newSinge
import org.easybangumi.next.lib.global.single
import org.easybangumi.next.lib.store.file_helper.FileHelper
import org.easybangumi.next.lib.store.file_helper.json.JsonFileHelper
import org.easybangumi.next.lib.store.preference.PreferenceStore
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

expect fun Global.preferenceStore(): PreferenceStore

object FileHelperStore

fun Global.fileHelperStore(): FileHelperStore = FileHelperStore

private val fileSingleDispatcher: CoroutineDispatcher by lazy {
    Global.coroutineProvider().newSinge("FileHelperStore")
}

fun <T : Any> FileHelperStore.jsonFileHelper(
    ufd: UFD,
    name: String,
    clazz: KClass<T>,
    def: T,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + fileSingleDispatcher)
) = JsonFileHelper<T>(ufd, name, clazz, def, scope)