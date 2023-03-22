package com.heyanle.easybangumi4.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Created by HeYanLe on 2023/3/20 16:22.
 * https://github.com/heyanLE
 */
class HomeViewModel: ViewModel() {

    var customBottomBar by mutableStateOf<(@Composable ()->Unit)?>(null)

    private val viewModelOwnerMap = hashMapOf<HomePage, ViewModelStoreOwner>()


    fun getVMOwner(page: HomePage): ViewModelStoreOwner{
        val old = viewModelOwnerMap[page]
        return if(old == null){
            val owner = object: ViewModelStoreOwner{
                override val viewModelStore: ViewModelStore
                    get() = ViewModelStore()
            }
            viewModelOwnerMap[page] = owner
            owner
        }else{
            old
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelOwnerMap.iterator().forEach {
            it.value.viewModelStore.clear()
        }
    }

}