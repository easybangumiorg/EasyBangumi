package com.heyanle.easybangumi.ui.home.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.lib_anim.IHomeParser
import com.heyanle.lib_anim.ISourceParser
import com.heyanle.lib_anim.entity.Bangumi
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/8 22:55.
 * https://github.com/heyanLE
 */
class AnimHomeViewModel(
    val homes: List<IHomeParser>
) : ViewModel() {

    sealed class HomeAnimState(
        val curIndex: Int
    ) {
        object None : HomeAnimState(-1)

        // 加载中
        class Loading(curIndex: Int) : HomeAnimState(curIndex) {
            override fun toString(): String {
                return "HomeAnimState.Loading(curIndex=$curIndex)"
            }
        }

        // 加载完成
        class Completely(
            curIndex: Int,
            val data: LinkedHashMap<String, List<Bangumi>>,
            val keyList: List<String>
        ) : HomeAnimState(curIndex) {
            override fun toString(): String {
                return "HomeAnimState.Completely(curIndex=$curIndex)"
            }
        }

        // 加载错误
        class Error(
            curIndex: Int,
            val error: ISourceParser.ParserResult.Error<LinkedHashMap<String, List<Bangumi>>>
        ) : HomeAnimState(curIndex)


    }

    sealed class HomeAnimEvent(val currentIndex: Int) {
        // 修改首页源 （走缓存）
        class ChangeTab(index: Int) : HomeAnimEvent(index)

        // 刷新首页源 （强制不走缓存）
        class RefreshTab(index: Int) : HomeAnimEvent(index)
    }

    companion object {
        private const val OKKV_KEY_SOURCE_INDEX = "source_index"
    }

    private var okkvCurrentHomeSourceIndex by okkv<Int>(OKKV_KEY_SOURCE_INDEX, 0)
    private val eventFlow = MutableStateFlow<HomeAnimEvent>(
        // 初始事件为 ChangeTab 才会走代理
        HomeAnimEvent.ChangeTab(okkvCurrentHomeSourceIndex)
    )


    private val homeData = HashMap<Int, LinkedHashMap<String, List<Bangumi>>>()

    private val _homeResult = MutableStateFlow<HomeAnimState>(HomeAnimState.None)
    val homeResultFlow: Flow<HomeAnimState> = _homeResult

    init {
        viewModelScope.launch {
            eventFlow.collectLatest() { event ->

                val index = event.currentIndex
                Log.d("AnimHomeViewHolder", "index->$index")
                // 下标对应番剧源检查
                val keys = homes
                if (keys.isEmpty() || index < 0) {
                    _homeResult.emit(
                        HomeAnimState.Error(
                            index,
                            ISourceParser.ParserResult.Error(
                                IllegalAccessException("Source not found"),
                                false
                            )
                        )
                    )
                    return@collectLatest
                }
                if (index >= keys.size) {

                    eventFlow.emit(HomeAnimEvent.ChangeTab(0))
                    return@collectLatest
                }
                // buffer, ChangeTab 事件才尝试走代理
                if (event is HomeAnimEvent.ChangeTab && homeData.containsKey(index) && homeData[index]?.isNotEmpty() == true) {
                    val ks = arrayListOf<String>()
                    homeData[index]?.forEach { (t, _) ->
                        ks.add(t)
                    }
                    // 加载成功
                    _homeResult.emit(
                        HomeAnimState.Completely(
                            index,
                            homeData[index] ?: linkedMapOf(),
                            ks
                        )
                    )
                } else {
                    // 先触发 Loading
                    _homeResult.emit(HomeAnimState.Loading(index))
                    val res = keys[index].home()
                    if (eventFlow.value.currentIndex != index) {
                        // 迟到的 resp
                        res.complete {
                            // 设置 缓存后不发送事件
                            homeData[index] = it.data
                        }
                        return@collectLatest
                    }
                    res.complete {
                        homeData[index] = it.data
                        val ks = arrayListOf<String>()
                        it.data.forEach { (t, _) ->
                            ks.add(t)
                        }
                        // 加载成功
                        _homeResult.emit(HomeAnimState.Completely(index, it.data, ks))
                    }.error {
                        _homeResult.emit(HomeAnimState.Error(index, it))
                    }
                }
            }
        }
    }


    fun changeHomeSource(index: Int) {
        viewModelScope.launch {
            if (index < 0 || homes.isEmpty()) {
                return@launch
            }
            okkvCurrentHomeSourceIndex = index
            eventFlow.emit(HomeAnimEvent.ChangeTab(index))
        }

    }

    fun refresh() {
        val index = eventFlow.value.currentIndex
        eventFlow.value = HomeAnimEvent.RefreshTab(index)
    }

}

class AnimHomeViewModelFactory(
    private val homes: List<IHomeParser>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnimHomeViewModel::class.java))
            return AnimHomeViewModel(homes) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}