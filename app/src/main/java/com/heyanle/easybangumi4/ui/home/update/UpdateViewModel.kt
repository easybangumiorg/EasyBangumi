package com.heyanle.easybangumi4.ui.home.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.db.entity.CartoonStar
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.insertSeparators
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toDateKey
import com.heyanle.easybangumi4.utils.toRelativeString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Created by HeYanLe on 2023/3/20 22:15.
 * https://github.com/heyanLE
 */
class UpdateViewModel : ViewModel() {


    data class State(
        val isLoading: Boolean = true,
        val isUpdating: Boolean = false,
        val updateCartoonList: List<UpdateItem> = emptyList(),
        val updateCount: Int = 0,
        val lastUpdateTime: Long = -1,
        val lastUpdateError: String? = null,
    )

    sealed class UpdateItem {
        data class Header(val header: String): UpdateItem()

        data class Cartoon(val star: CartoonStar): UpdateItem()
    }

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow = _stateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getCartoonStarFlow(),
                UpdateMaster.isLoading,
                UpdateMaster.loadingError,
                UpdateMaster.lastUpdateTime
            ) { list, isUpdating, loadingError, lastUpdateTime ->
                _stateFlow.update {
                    it.copy(
                        isLoading = false,
                        isUpdating = isUpdating,
                        updateCartoonList = list.toUpdateItems(),
                        updateCount = list.size,
                        lastUpdateTime = lastUpdateTime,
                        lastUpdateError = loadingError,
                    )
                }

            }
        }
    }

    private fun List<CartoonStar>.toUpdateItems(): List<UpdateItem> {
        return map {
            UpdateItem.Cartoon(it)
        }.insertSeparators{before, after ->

            val beforeDate = before?.star?.lastUpdateTime?.toDateKey() ?: Date(0)
            val afterDate = after?.star?.lastUpdateTime?.toDateKey() ?: Date(0)
            when {
                beforeDate.time != afterDate.time && afterDate.time != 0L -> {
                    val text = afterDate.toRelativeString(
                        context = APP,
                    )
                    UpdateItem.Header(text)
                }
                // Return null to avoid adding a separator between two items.
                else -> null
            }
        }
    }

    fun update(isStrict: Boolean) {
        if (UpdateMaster.tryUpdate(isStrict)) {
            stringRes(if (isStrict) com.heyanle.easy_i18n.R.string.start_update_strict else com.heyanle.easy_i18n.R.string.start_update)
                .moeSnackBar()
        }else{
            stringRes(com.heyanle.easy_i18n.R.string.doing_update_wait).moeSnackBar()
        }
    }

    private fun getCartoonStarFlow(): Flow<List<CartoonStar>> {
        return DB.cartoonStar.flowUpdate()
    }
}