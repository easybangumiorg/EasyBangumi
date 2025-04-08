package org.easybangumi.next.shared.foundation.view_model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Created by heyanlin on 2025/3/5.
 */
open class ParentViewModel<T>: ViewModel() {

    protected val ownerMap: ViewModelOwnerMap<T> = ViewModelOwnerMap()

    override fun onCleared() {
        cleanChildren()
        super.onCleared()

    }

    protected fun getViewModelOwner(key: T) = ownerMap.getViewModelStoreOwner(key)

    protected fun removeChildOwner(key: T){
        ownerMap.remove(key)
    }

    protected fun cleanChildren(){
        ownerMap.clear()
    }

    @Composable
    fun child(key: T, content: @Composable () -> Unit){
        val owner = getViewModelOwner(key)
        CompositionLocalProvider(
            LocalViewModelStoreOwner provides owner,
        ) {
            content()
        }
    }


}

