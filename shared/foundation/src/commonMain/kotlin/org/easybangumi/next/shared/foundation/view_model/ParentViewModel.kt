package org.easybangumi.next.shared.foundation.view_model

import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

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
abstract class ParentViewModel<UI_STATE, LOGIC_STATE, CHILD_KEY> : AbsViewModel<UI_STATE, LOGIC_STATE>() {

    private val ownerMap: ViewModelOwnerMap<CHILD_KEY> by lazy {
        ViewModelOwnerMap()
    }

    protected fun cleanChild() {
        ownerMap.clear()
    }

    @Composable
    fun child(key: CHILD_KEY, content: @Composable ()->Unit) {
        val child = ownerMap.getViewModelStoreOwner(key)
        CompositionLocalProvider(
            LocalViewModelStoreOwner provides child,
        ) {
            content()
        }
    }

    @CallSuper
    override fun onCleared() {
        cleanChild()
        super.onCleared()
    }


}