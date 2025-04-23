package org.easybangumi.next.shared.foundation.view_model

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

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

    fun remove(key: T){
        viewModelOwnerStore.remove(key)?.clear()
    }

    fun clear(){
        viewModelOwnerStore.iterator().forEach {
            it.value.clear()
        }
        viewModelOwnerStore.clear()
    }

}