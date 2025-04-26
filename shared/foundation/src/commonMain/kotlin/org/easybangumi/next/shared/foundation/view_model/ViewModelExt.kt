package org.easybangumi.next.shared.foundation.view_model

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.selects.OnCancellationConstructor
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
 *
 * 使用：
 *    val viewModel = vm(::MyViewModel, "param1", "param2")
 */



@Composable
inline fun <reified VM : ViewModel> vm(
    noinline constructor: () -> VM,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
): VM {
    return viewModel(viewModelStoreOwner = viewModelStoreOwner, factory = ViewModelFactoryZero(constructor))
}

@Composable
inline fun <reified VM : ViewModel, P> vm(
    noinline constructor: (P) -> VM,
    params: P,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
): VM {
    return viewModel(viewModelStoreOwner = viewModelStoreOwner, factory = ViewModelFactoryOne(constructor, params))
}



@Composable
inline fun <reified VM : ViewModel, P1, P2> vm(
    noinline constructor: (P1, P2) -> VM,
    params1: P1,
    params2: P2,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
): VM {
    return viewModel(viewModelStoreOwner = viewModelStoreOwner, factory = ViewModelFactoryTwo(constructor, params1, params2))
}

@Composable
inline fun <reified VM : ViewModel, P1, P2, P3> vm(
    noinline constructor: (P1, P2, P3) -> VM,
    params1: P1,
    params2: P2,
    params3: P3,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
): VM {
    return viewModel(viewModelStoreOwner = viewModelStoreOwner, factory = ViewModelFactoryThree(constructor, params1, params2, params3))
}

class ViewModelFactoryZero<V: ViewModel>(
    private val constructor: () -> V,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val v = constructor()
        check(v is BaseViewModel) {
            "ViewModel must be a subclass of BaseViewModel"
        }
        if (!modelClass.isInstance(v)) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        return v as T
    }
}


class ViewModelFactoryOne<V : ViewModel, P>(
    private val constructor: (P) -> V,
    private val params: P,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val v = constructor(params)
        check(v is BaseViewModel) {
            "ViewModel must be a subclass of BaseViewModel"
        }
        if (!modelClass.isInstance(v)) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        return v as T
    }
}

class ViewModelFactoryTwo<V : ViewModel, P1, P2>(
    private val constructor: (P1, P2) -> V,
    private val params1: P1,
    private val params2: P2,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val v = constructor(params1, params2)
        check(v is BaseViewModel) {
            "ViewModel must be a subclass of BaseViewModel"
        }
        if (!modelClass.isInstance(v)) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        return v as T
    }
}
class ViewModelFactoryThree<V : ViewModel, P1, P2, P3>(
    private val constructor: (P1, P2, P3) -> V,
    private val params1: P1,
    private val params2: P2,
    private val params3: P3,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val v = constructor(params1, params2, params3)
        check(v is BaseViewModel) {
            "ViewModel must be a subclass of BaseViewModel"
        }
        if (!modelClass.isInstance(v)) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        return v as T
    }
}


