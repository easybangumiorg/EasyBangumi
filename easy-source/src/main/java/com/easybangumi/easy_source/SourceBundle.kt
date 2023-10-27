package com.easybangumi.easy_source

import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.mp.KoinPlatform.getKoin

/**
 * Created by heyanlin on 2023/10/26.
 */
class SourceBundle(

) {

    private val koinScope = linkedMapOf<String, Scope>()

    init {

    }

    fun d(module: Scope){
        getKoin().getOrCreateScope("", named(""))
    }

}