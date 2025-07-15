package org.easybangumi.next.shared.source.rhino.component

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.rhino.RhinoScope
import org.easybangumi.next.shared.data.cartoon.CartoonPlayCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.play.IPlayComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
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
    private val playComponent: IPlayComponent,
): BaseComponent(),
    RhinoComponentWrapper,
    PlayComponent,
    IPlayComponent by playComponent {

    companion object {
        const val COMPONENT_NAME_PLAY = "PlayComponent"
        const val FUNCTION_NAME_SEARCH_PLAY_COVERS = "searchPlayCovers"
        const val FUNCTION_NAME_GET_PLAY_LINES = "getPlayLines"
        const val FUNCTION_NAME_GET_PLAY_INFO = "getPlayInfo"
    }

    class Factory: RhinoComponentWrapper.Factory<RhinoPlayComponentWrapper> {
        override suspend fun create(rhinoScope: RhinoScope): RhinoPlayComponentWrapper? {
            val searchPlayCoverFunction = rhinoScope.findFunction(
                "${COMPONENT_NAME_PLAY}_${FUNCTION_NAME_SEARCH_PLAY_COVERS}",
            ) ?: return null
            val getPlayLinesFunction = rhinoScope.findFunction(
                "${COMPONENT_NAME_PLAY}_${FUNCTION_NAME_GET_PLAY_LINES}",
            ) ?: return null
            val getPlayInfoFunction = rhinoScope.findFunction(
                "${COMPONENT_NAME_PLAY}_${FUNCTION_NAME_GET_PLAY_INFO}",
            ) ?: return null

            val playComponentProxy = object: IPlayComponent {
                override suspend fun searchPlayCovers(
                    param: IPlayComponent.PlayLineSearchParam,
                    limit: Int
                ): DataState<List<CartoonPlayCover>> {
                    return rhinoScope.callFunction(
                        searchPlayCoverFunction,
                        param,
                        limit
                    )
                }

                override suspend fun getPlayLines(
                    cartoonCover: CartoonPlayCover
                ): DataState<List<List<PlayerLine>>> {
                    return rhinoScope.callFunction(
                        getPlayLinesFunction,
                        cartoonCover
                    )
                }

                override suspend fun getPlayInfo(
                    cartoonPlayCover: CartoonPlayCover,
                    playerLine: PlayerLine,
                    episode: Episode
                ): DataState<PlayInfo> {
                    return rhinoScope.callFunction(
                        getPlayInfoFunction,
                        cartoonPlayCover,
                        playerLine,
                        episode
                    )
                }
            }
            return RhinoPlayComponentWrapper(
                playComponentProxy
            )
        }
    }

    override fun getComponentClazz(): Array<KClass<*>> {
        return arrayOf(
            PlayComponent::class
        )
    }
}