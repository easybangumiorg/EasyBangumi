package org.easybangumi.next.shared.foundation.view_model

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import org.easybangumi.next.shared.foundation.view_model.parent.ParentViewModel
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
abstract class BaseViewModel: ViewModel(), KoinComponent {

    private val children: MutableList<BaseViewModel> = mutableListOf()

    protected fun<T: BaseViewModel> childViewModel(
        block: () -> T
    ): Lazy<T> {
        return lazy {
            val child = block()
            children.add(child)
            child
        }
    }

    @CallSuper
    override fun onCleared() {
        if (this is ParentViewModel<*>) {
            clearChildren()
        }
        children.forEach { it.onCleared() }
        super.onCleared()
    }

}