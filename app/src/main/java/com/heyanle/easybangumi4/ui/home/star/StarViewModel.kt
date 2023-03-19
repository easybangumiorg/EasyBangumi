package com.heyanle.easybangumi4.ui.home.star

import androidx.lifecycle.ViewModel
import com.heyanle.easybangumi4.db.entity.CartoonStar

/**
 * Created by HeYanLe on 2023/3/18 19:24.
 * https://github.com/heyanLE
 */
class StarViewModel: ViewModel() {

    data class State(
        val isInitializer: Boolean = false,
        val isLoading: Boolean = true,
        val searchQuery: String = "",
        val starCartoonList: List<CartoonStar>,
        val selection: List<CartoonStar> = emptyList(),
        val hasActiveFilters: Boolean = false,
        val showingCartoonCount: Int = 0,
        val dialog: DialogState? = null
    )

    sealed class DialogState {}


}