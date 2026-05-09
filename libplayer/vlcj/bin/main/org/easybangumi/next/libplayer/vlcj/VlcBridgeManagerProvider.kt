package org.easybangumi.next.libplayer.vlcj

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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
class VlcBridgeManagerProvider(
    private val ioScope: CoroutineScope,
    // vlc 参数
    private val libvlcArgs: Collection<String> = emptyList(),
) {


    sealed class State {
        object None: State()
        object Initializing: State()
        data class Initialized(val manager: VlcjBridgeManager): State()
    }

    private val _stateFlow: MutableStateFlow<State> = MutableStateFlow(State.None)
    val stateFlow = _stateFlow.asStateFlow()

    fun tryInit (){
        if (_stateFlow.value is State.None) {
            ioScope.launch {
                innerInit()
            }
        }

    }

    suspend fun getManager(): VlcjBridgeManager {
        if (_stateFlow.value is State.Initialized) {
            return (_stateFlow.value as State.Initialized).manager
        }
        tryInit()
        return (_stateFlow.filterIsInstance<State.Initialized>().first().manager)
    }


    private fun innerInit() {
        if (_stateFlow.compareAndSet(State.None, State.Initializing)) {
            val manager = VlcjBridgeManager(MediaPlayerFactory(libvlcArgs))
            _stateFlow.update {
                State.Initialized(manager)
            }
        }
    }

}