package com.heyanle.easybangumi.anim.home

import androidx.lifecycle.ViewModel
import com.heyanle.easybangumi.anim.AnimSourceFactory
import com.heyanle.lib_anim.ISourceParser
import com.heyanle.lib_anim.entity.Bangumi
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Created by HeYanLe on 2022/9/18 15:48.
 * https://github.com/heyanLE
 */
class AnimHomeViewModel: ViewModel() {

    sealed class HomeAnimState {
        // 加载中
        data class Loading(val curIndex: Int): HomeAnimState()

        // 加载完成
        data class Completely(val curIndex: Int, val data: LinkedHashMap<String, List<Bangumi>>): HomeAnimState()

        // 加载错误
        data class Error(val curIndex: Int, val error: ISourceParser.ParserResult.Error<LinkedHashMap<String, List<Bangumi>>>): HomeAnimState()
    }

    sealed class HomeAnimEvent(val currentIndex: Int) {
        // 修改首页源 （走缓存）
        class ChangeTab(index: Int): HomeAnimEvent(index)

        // 刷新首页源 （强制不走缓存）
        class RefreshTab(index: Int): HomeAnimEvent(index)
    }

    companion object {
        private const val OKKV_KEY_SOURCE_INDEX = "source_index"
    }

    private var okkvCurrentHomeSourceIndex by okkv<Int>(OKKV_KEY_SOURCE_INDEX, 0)
    private val eventFlow = MutableStateFlow<HomeAnimEvent>(HomeAnimEvent.RefreshTab(okkvCurrentHomeSourceIndex))

    private val homeData = HashMap<Int, LinkedHashMap<String, List<Bangumi>>>()

    private val _homeResult = flow<HomeAnimState> {
        eventFlow.collect(){ event ->
            val index = event.currentIndex
            // 先触发 Loading
            emit(HomeAnimState.Loading(index))
            // 下标对应番剧源检查
            val keys = AnimSourceFactory.homeKeys()
            if(keys.isEmpty() || index < 0 || index >= keys.size){
                emit(HomeAnimState.Error(index, ISourceParser.ParserResult.Error(IllegalAccessException("Source not found"), false)))
                return@collect
            }
            // buffer, ChangeTab 事件才尝试走代理
            if(event is HomeAnimEvent.ChangeTab && homeData.containsKey(index) && homeData[index]?.isNotEmpty() == true){
                // 加载成功
                emit(HomeAnimState.Completely(index, homeData[index]?: linkedMapOf()))
            }else{
                val res = AnimSourceFactory.home(keys[index])?.home()
                if(eventFlow.value.currentIndex != index){
                    // 迟到的 resp
                    res?.complete {
                        // 设置 缓存后不发送事件
                        homeData[index] = it.data
                    }
                    return@collect
                }
                if(res == null){
                    // 加载失败
                    emit(HomeAnimState.Error(index,ISourceParser.ParserResult.Error(IllegalAccessException("Result is null"), true) ))
                }else{
                    res.complete {
                        homeData[index] = it.data
                        // 加载成功
                        emit(HomeAnimState.Completely(index, it.data))
                    }.error {
                        emit(HomeAnimState.Error(index, it))
                    }
                }

            }

        }
    }
    val homeResultFlow:Flow<HomeAnimState> = _homeResult


    fun changeHomeSource(index: Int){
        val keys = AnimSourceFactory.homeKeys()
        if(index < 0 || index >= keys.size){
            return
        }
        okkvCurrentHomeSourceIndex = index
        eventFlow.value = HomeAnimEvent.ChangeTab(index)
    }

    fun refresh(){
        val index = eventFlow.value.currentIndex
        eventFlow.value = HomeAnimEvent.RefreshTab(index)
    }

}