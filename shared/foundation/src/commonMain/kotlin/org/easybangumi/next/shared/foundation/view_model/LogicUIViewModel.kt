package org.easybangumi.next.shared.foundation.view_model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

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
abstract class LogicUIViewModel <UI_STATE, LOGIC_STATE>(
    initLogicState: LOGIC_STATE,
    initUiState: UI_STATE
): BaseViewModel(), LogicUI<UI_STATE, LOGIC_STATE> {

    protected val uiState: MutableState<UI_STATE> by lazy {
        mutableStateOf<UI_STATE>(initUiState)
    }
    override val ui: State<UI_STATE> get() =  uiState

    protected val logicState: MutableStateFlow<LOGIC_STATE> by lazy {
        MutableStateFlow<LOGIC_STATE>(initLogicState)
    }
    override val logic: StateFlow<LOGIC_STATE> get() = logicState

    abstract suspend fun logicToUi(logicState: LOGIC_STATE): UI_STATE

    init {
        viewModelScope.launch {
            logic.collectLatest { logicState ->
                uiState.value = logicToUi(logicState)
            }
        }
    }

    protected fun update(
        block: (LOGIC_STATE) -> LOGIC_STATE
    ) {
        logicState.update(block)
    }

}