package org.easybangumi.next.shared.foundation.view_model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
abstract class StateViewModel<STATE : Any>(
    initState: STATE
) : BaseViewModel(), LogicUI<STATE, STATE> {

    private val _ui: MutableState<STATE> by lazy {
        mutableStateOf(initState)
    }
    override val ui: State<STATE> get() = _ui

    override val logic: StateFlow<STATE>
        get() = state

    protected val state: MutableStateFlow<STATE> by lazy {
        MutableStateFlow<STATE>(initState)
    }

    init {
        viewModelScope.launch {
            logic.collect { state ->
                _ui.value = state
            }
        }
    }

    protected fun update(
        block: (STATE) -> STATE
    ) {
       state.update(block)
    }

}