package org.easybangumi.next.shared.foundation.view_model

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import org.easybangumi.next.lib.utils.safeCancel
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
    private val closeable: MutableList<AutoCloseable> = mutableListOf()
    private val childrenScope: MutableList<CoroutineScope> = mutableListOf()

    protected fun<T: BaseViewModel> childViewModel(
        block: () -> T
    ): Lazy<T> {
        return lazy {
            val child = block()
            children.add(child)
            child
        }
    }

    protected fun newChildrenScope(): CoroutineScope {
        val scope = CoroutineScope(viewModelScope.coroutineContext)
        childrenScope.add(scope)
        return scope
    }

    protected fun registerCloseable(
        closeable: AutoCloseable
    ) {
        this.closeable.add(closeable)
    }

    @CallSuper
    override fun onCleared() {
        children.forEach { it.onCleared() }
        children.clear()

        closeable.forEach { it.close() }
        closeable.clear()

        childrenScope.forEach { it.safeCancel() }
        childrenScope.clear()

        super.onCleared()
    }

}