package com.heyanle.easybangumi4.compose.main.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.base.entity.CartoonStar
import com.heyanle.easybangumi4.compose.common.moeSnackBar
import com.heyanle.easybangumi4.utils.insertSeparators
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toDateKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                UpdateMaster.isUpdate,
                UpdateMaster.loadingError,
                UpdateMaster.lastUpdateTime,
            ) { list, isUpdating, loadingError, lastUpdateTime ->

                val transform: (State)->State = {
                    it.copy(
                        isLoading = false,
                        isUpdating = isUpdating,
                        updateCartoonList = list.toUpdateItems(),
                        updateCount = list.size,
                        lastUpdateTime = lastUpdateTime,
                        lastUpdateError = loadingError,
                    )
                }
                transform
            }.collectLatest {transform ->
                _stateFlow.update {
                    transform(it)
                }
            }


        }

//        viewModelScope.launch {
//            combine(
//                getCartoonStarFlow(),
//                stateFlow.map { it.isLoading }.distinctUntilChanged(),
//            ) { list, state ->
//                list.size.loge("UpdateViewModel")
//            }
//        }
    }

    private fun List<CartoonStar>.toUpdateItems(): List<UpdateItem> {
        return map {
            UpdateItem.Cartoon(it)
        }.insertSeparators{before, after ->

            val beforeDate = before?.star?.lastUpdateTime?.toDateKey() ?: Date(0)
            val afterDate = after?.star?.lastUpdateTime?.toDateKey() ?: Date(0)
            when {
                beforeDate.time != afterDate.time && afterDate.time != 0L -> {

                    val afterCalendar = Calendar.getInstance(Locale.getDefault())
                    afterCalendar.time = afterDate
                    afterCalendar.clear(Calendar.HOUR_OF_DAY)
                    afterCalendar.clear(Calendar.MINUTE)
                    afterCalendar.clear(Calendar.SECOND)
                    afterCalendar.clear(Calendar.MILLISECOND)

                    val todayCalendar =  Calendar.getInstance(Locale.getDefault())
                    todayCalendar.time = Date(System.currentTimeMillis())
                    todayCalendar.clear(Calendar.HOUR_OF_DAY)
                    todayCalendar.clear(Calendar.MINUTE)
                    todayCalendar.clear(Calendar.SECOND)
                    todayCalendar.clear(Calendar.MILLISECOND)

                    val diffDays = (todayCalendar.timeInMillis - afterCalendar.timeInMillis)/(24 * 60 * 60 * 1000)
                    val text = when(diffDays){
                        0L -> stringRes(com.heyanle.easy_i18n.R.string.today)
                        1L -> stringRes(com.heyanle.easy_i18n.R.string.yesterday)
                        else -> {
                            if(diffDays <= 7L){
                                stringRes(com.heyanle.easy_i18n.R.string.day_age, diffDays)
                            }else{
                                DateFormat.getDateInstance(DateFormat.SHORT).format(afterDate)
                            }
                        }
                    }
                    UpdateItem.Header(text)
                }
                // Return null to avoid adding a separator between two items.
                else -> null
            }
        }
    }

    fun update(isStrict: Boolean) {
        viewModelScope.launch {
            if (UpdateMaster.tryUpdate(isStrict)) {
                stringRes(if (isStrict) com.heyanle.easy_i18n.R.string.start_update_strict else com.heyanle.easy_i18n.R.string.start_update)
                    .moeSnackBar()
            }else{
                stringRes(com.heyanle.easy_i18n.R.string.doing_update_wait).moeSnackBar()
            }
        }

    }

    private fun getCartoonStarFlow(): Flow<List<CartoonStar>> {
        return DB.cartoonStar.flowAll().map {
            it.filter {
                it.isUpdate && it.isInitializer
            }
        }
    }
}