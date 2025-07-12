package org.easybangumi.next.shared.plugin.core.javascript.rhino.component.wrapper

import org.easybangumi.next.rhino.RhinoFunction
import org.easybangumi.next.rhino.RhinoScope
import org.easybangumi.next.shared.plugin.api.component.play.PlayComponent
import org.easybangumi.next.shared.plugin.api.component.BaseComponent
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
class RhinoPlayComponentWrapper(
    private val rhinoScope: RhinoScope,
    private val playFunction: RhinoFunction
) : BaseComponent(), RhinoComponentWrapper {

    companion object {
        private const val COMPONENT_NAME_PLAY = "PlayComponent"
        private const val FUNCTION_NAME_PLAY = "play"
    }

    class Factory : RhinoWrapperFactory<RhinoPlayComponentWrapper> {

        override suspend fun create(rhinoScope: RhinoScope): RhinoPlayComponentWrapper? {
            val playFunction = rhinoScope.findFunction("${COMPONENT_NAME_PLAY}_${FUNCTION_NAME_PLAY}")
            if (playFunction == null) {
                return null
            }
            return RhinoPlayComponentWrapper(rhinoScope, playFunction)
        }
    }

//    override suspend fun play(
//        cartoonIndex: CartoonIndex,
//        playerLine: PlayerLine,
//        episode: Episode
//    ): SourceResult<PlayInfo> {
//        return withResult {
//            rhinoScope.callFunction<PlayInfo>(
//                playFunction,
//                cartoonIndex,
//                playerLine,
//                episode
//            )
//        }
//    }

    override fun getComponentClazz(): Array<KClass<*>> {
        return arrayOf(PlayComponent::class)
    }
}