package com.heyanle.easybangumi4.ui.search_migrate.migrate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.case.CartoonInfoCase
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.utils.ViewModelOwnerMap
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2023/12/23.
 * https://github.com/heyanLE
 */
class MigrateViewModel(
    private val summaries: List<CartoonSummary>,
    private val sources: List<String>,
): ViewModel() {


    private val ownerMap = ViewModelOwnerMap<CartoonInfo>()

    private val cartoonInfoCase: CartoonInfoCase by Injekt.injectLazy()


    data class MigrateState(
        val isLoading: Boolean = true,
        val infoList: List<CartoonInfo> = emptyList(),
        val selection: Set<CartoonInfo> = emptySet(),
    )
    private val _infoListFlow = MutableStateFlow<MigrateState>(MigrateState())
    val infoListFlow = _infoListFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val infoList = summaries.map {
                cartoonInfoCase.awaitCartoonInfoWithPlayLines(it.id, it.source, it.url)
            }.filterIsInstance<DataResult.Ok<CartoonInfo>>()
                .map {
                    it.data
                }
            _infoListFlow.update {
                it.copy(
                    false,
                    infoList
                )
            }
        }
    }

    fun getOwner(cartoonInfo: CartoonInfo) = ownerMap.getViewModelStoreOwner(cartoonInfo)
    fun getItemViewModelFactory(cartoonInfo: CartoonInfo) = MigrateItemViewModelFactory(cartoonInfo, sources)

    override fun onCleared() {
        super.onCleared()
        ownerMap.clear()
    }


    fun selectChange(cartoonInfo: CartoonInfo){}

    fun selectLongPress(cartoonInfo: CartoonInfo){}

    fun selectExit(cartoonInfo: CartoonInfo){}

    fun selectAll() {}

    fun selectInvert() {}

}

class MigrateViewModelFactory(
    private val summaries: List<CartoonSummary>,
    private val sources: List<String>,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MigrateViewModel::class.java))
            return MigrateViewModel(summaries, sources) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}