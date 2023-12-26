package com.heyanle.easybangumi4.utils

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Created by HeYanLe on 2023/3/25 15:42.
 * https://github.com/heyanLE
 */
class ViewModelOwnerMap<T> {

    private val viewModelOwnerStore = hashMapOf<T, ViewModelStore>()

    fun getViewModelStoreOwner(key: T) = object: ViewModelStoreOwner {

        override val viewModelStore: ViewModelStore
            get() {
                var viewModelStore = viewModelOwnerStore[key]
                if (viewModelStore == null) {
                    viewModelStore = ViewModelStore()
                    viewModelOwnerStore[key] = viewModelStore
                }
                return viewModelStore
            }
    }

    fun clear(){
        viewModelOwnerStore.iterator().forEach {
            it.value.clear()
        }
        viewModelOwnerStore.clear()
    }

}