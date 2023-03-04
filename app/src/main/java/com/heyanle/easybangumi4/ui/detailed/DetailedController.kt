package com.heyanle.easybangumi4.ui.detailed

import androidx.collection.LruCache
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary

/**
 * Created by HeYanLe on 2023/3/4 16:31.
 * https://github.com/heyanLE
 */
object DetailedController {

    private val viewModelOwnerStore = object: LruCache<CartoonSummary, ViewModelStore> (3){
        override fun entryRemoved(
            evicted: Boolean,
            key: CartoonSummary,
            oldValue: ViewModelStore,
            newValue: ViewModelStore?
        ) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            oldValue.clear()
        }
    }

    fun getViewModelStoreOwner(summer: CartoonSummary) = object: ViewModelStoreOwner {

        override val viewModelStore: ViewModelStore
            get() {
                var viewModelStore = viewModelOwnerStore.get(summer)
                if (viewModelStore == null) {
                    viewModelStore = ViewModelStore()
                    viewModelOwnerStore.put(summer, viewModelStore)
                }
                return viewModelStore
            }
    }


}