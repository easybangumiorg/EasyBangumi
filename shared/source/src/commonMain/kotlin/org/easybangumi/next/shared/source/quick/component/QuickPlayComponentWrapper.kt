package org.easybangumi.next.shared.source.quick.component

import com.dokar.quickjs.QuickJs
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.serialization.serialize
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.play.IPlayComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
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
class QuickPlayComponentWrapper(
    playComponent: IPlayComponent
): QuickComponentWrapper,
    BaseComponent(),
    PlayComponent,
    IPlayComponent by playComponent
{

    companion object {

        const val COMPONENT_NAME_PLAY = "PlayComponent"
        const val FUNCTION_NAME_GET_PLAY_LINES = "getPlayLines"
        const val FUNCTION_NAME_GET_PLAY_INFO = "getPlayInfo"

    }


    class Factory: QuickComponentWrapper.Factory<QuickPlayComponentWrapper> {

        override suspend fun create(quickJs: QuickJs): QuickPlayComponentWrapper? {
            // 1. check functions
            if (!quickJs.checkFunctionExists(
                "${COMPONENT_NAME_PLAY}_${FUNCTION_NAME_GET_PLAY_LINES}",
                "${COMPONENT_NAME_PLAY}_${FUNCTION_NAME_GET_PLAY_INFO}",
            )) {
                return null
            }

            // 2. create proxy
            val playComponentProxy: IPlayComponent = object: IPlayComponent {
                override suspend fun getPlayLines(cartoonIndex: CartoonIndex): DataState<List<PlayerLine>> {
                    return quickJs.callFunctionWithDataState("${COMPONENT_NAME_PLAY}_${FUNCTION_NAME_GET_PLAY_LINES}", args = arrayOf(cartoonIndex)).toDataState<List<PlayerLine>>()
                        ?: DataState.Error("QuickJS PlayComponent getPlayLines returned invalid data: null")

                }

                override suspend fun getPlayInfo(
                    cartoonIndex: CartoonIndex,
                    playerLine: PlayerLine,
                    episode: Episode
                ): DataState<PlayInfo> {
                    return quickJs.callFunctionWithDataState("${COMPONENT_NAME_PLAY}_${FUNCTION_NAME_GET_PLAY_INFO}", args = arrayOf(cartoonIndex, playerLine, episode)).toDataState<PlayInfo>()
                        ?: DataState.Error("QuickJS PlayComponent getPlayInfo returned invalid data: null")
                }
            }

            // 3. return wrapper
            return QuickPlayComponentWrapper(
                playComponent = playComponentProxy
            )
        }

    }

    override fun getComponentClazz(): Array<KClass<*>> {
        return arrayOf(PlayComponent::class)
    }



}