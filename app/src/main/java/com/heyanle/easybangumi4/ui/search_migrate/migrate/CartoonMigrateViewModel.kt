package com.heyanle.easybangumi4.ui.search_migrate.migrate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.ui.search_migrate.PagingSearchSource
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/11/22.
 */
class CartoonMigrateViewModel(
    defSearchKey: String,
    defSourceKey: List<String>,
) : ViewModel() {

    val searchBarText = mutableStateOf(defSearchKey)


    // 真正搜索的 keyword
    private val _searchFlow = MutableStateFlow(defSearchKey)


    private val _sourceKeys = MutableStateFlow<List<String>>(defSourceKey)

    private val sourceStateCase: SourceStateCase by Injekt.injectLazy()

    data class MigrateItem(
        val searchComponent: SearchComponent,
        val searchKey: String,
        val flow: Flow<PagingData<CartoonCover>>
    )

    sealed class MigrateState {
        data object Loading: MigrateState()

        data object Empty: MigrateState()

        class Info(
            val items: List<MigrateItem>,
        ): MigrateState()
    }

    private val _migrateItemFlow = MutableStateFlow<List<MigrateItem>>(emptyList())
    val migrateItemFlow = _migrateItemFlow.asStateFlow()

    private val _migrateStateFlow = MutableStateFlow<MigrateState>(MigrateState.Loading)
    val migrateStateFlow = _migrateStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _searchFlow,
                _sourceKeys,
                sourceStateCase.flowBundle().stateIn(viewModelScope, SharingStarted.Lazily, null),
            ) { searchKey, sourceKeys, sourceBundle ->
                if (searchKey.isEmpty()) {
                    MigrateState.Empty
                } else if (sourceBundle == null) {
                    MigrateState.Loading
                }else {
                    if (sourceKeys.isEmpty()){
                        sourceBundle.searches()
                    }
                    val list = if (sourceKeys.isEmpty()){
                        sourceBundle.searches().asSequence()
                    }else {
                        sourceKeys.asSequence().flatMap {
                            val search = sourceBundle.search(it)
                            if (search == null) {
                                emptyList()
                            } else {
                                listOf(search)
                            }
                        }
                    }.map {
                        MigrateItem(
                            searchComponent = it,
                            searchKey = searchKey,
                            flow = getPager(searchKey, it).flow.cachedIn(viewModelScope)
                        )
                    }
                    MigrateState.Info(list.toList())

                }
            }.collectLatest { sta ->
                _migrateStateFlow.update {
                    sta
                }
            }
        }
    }

    fun search() {
        _searchFlow.update {
            searchBarText.value
        }
    }

    fun changeSearchKey(keys: List<String>) {
        _sourceKeys.update {
            keys
        }
    }

    private fun getPager(
        keyword: String,
        searchComponent: SearchComponent
    ): Pager<Int, CartoonCover> {
        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = searchComponent.getFirstSearchKey(keyword)
        ) {
            PagingSearchSource(searchComponent, keyword)
        }
    }

}

class CartoonMigrateViewModelFactory(
    private val defSearchKey: String,
    private val defSourceKey: List<String>,
) : ViewModelProvider.Factory {

    companion object {

        @Composable
        fun newViewModel(
            defSearchKey: String,
            defSourceKey: List<String>,
        ): CartoonMigrateViewModel {
            return viewModel(factory = CartoonMigrateViewModelFactory(defSearchKey, defSourceKey))
        }
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartoonMigrateViewModel::class.java))
            return CartoonMigrateViewModel(defSearchKey, defSourceKey) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}