package org.easybangumi.next.shared.foundation.view_model.parent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.easybangumi.next.shared.foundation.view_model.ViewModelOwnerMap

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
class ParentViewModelDelegate<KEY: Any>: ParentViewModel<KEY> {

    override val storeMap: ViewModelOwnerMap<KEY> by lazy {
        ViewModelOwnerMap()
    }

    @Composable
    override fun child(key: KEY, content: @Composable (() -> Unit)) {
        val child = storeMap.getViewModelStoreOwner(key)
        CompositionLocalProvider(
            LocalViewModelStoreOwner provides child,
        ) {
            content()
        }
    }

    override fun clearChildren() {
        storeMap.clear()
    }
}