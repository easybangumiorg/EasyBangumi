package org.easybangumi.next.shared.plugin.api.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.easybangumi.next.shared.plugin.api.SourceResult

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
class ComponentBusiness <T: Component> (
    private val innerComponent: T,
){
    suspend fun <R> async(block: suspend T.(CoroutineScope) -> SourceResult<R>): Deferred<SourceResult<R>> {
        return innerComponent.source.scope.async {
            runCatching {
                innerComponent.block(this)
            }.getOrElse {
                SourceResult.error(
                    errorMsg = it.message?:"${it}",
                    throwable = it,
                )
            }

        }
    }
    suspend fun <R> run(block: suspend T.(CoroutineScope) -> SourceResult<R>): SourceResult<R> {
        return innerComponent.source.scope.async {
            runCatching {
                innerComponent.block(this)
            }.getOrElse {
                SourceResult.error(
                    errorMsg = it.message?:"${it}",
                    throwable = it,
                )
            }

        }.await()
    }

    fun <R> runDirect(block: T.() -> R): R {
        return innerComponent.block()
    }
}