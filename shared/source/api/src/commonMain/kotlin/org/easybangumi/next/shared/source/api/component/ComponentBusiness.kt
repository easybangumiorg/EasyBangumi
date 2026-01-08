package org.easybangumi.next.shared.source.api.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.source.Source
import org.easybangumi.next.shared.source.api.source.SourceManifest
import kotlin.coroutines.coroutineContext

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
typealias ComponentBusinessPair<X, Y> = Pair<ComponentBusiness<X>, ComponentBusiness<Y>>
typealias FinderComponentPair = ComponentBusinessPair<SearchComponent, PlayComponent>

fun FinderComponentPair.getSource(): Source {
    return this.first.source
}

fun FinderComponentPair.getManifest(): SourceManifest {
    return this.first.source.manifest
}

open class ComponentBusiness <T: Component> (
    protected val innerComponent: T,
){
    val source = innerComponent.source
    suspend fun <R> async(block: suspend T.(CoroutineScope) -> DataState<R>): Deferred<DataState<R>> {
        return innerComponent.source.scope.async {
            runCatching {
                innerComponent.block(this)
            }.getOrElse {
                DataState.error(
                    errorMsg = it.message?:"$it",
                    throwable = it,
                )
            }

        }
    }
    suspend fun <R> run(block: suspend T.(CoroutineScope) -> DataState<R>): DataState<R> {
        return innerComponent.source.scope.async {
            runCatching {
                innerComponent.block(this)
            }.getOrElse {
                DataState.error(
                    errorMsg = it.message?:"$it",
                    throwable = it,
                )
            }

        }.await()
    }

    suspend fun <R> runSuspendDirect(block:suspend T.() -> R): R {
        return innerComponent.block()
    }

    fun <R> runDirect( block: T.() -> R): R {
        return innerComponent.block()
    }
}