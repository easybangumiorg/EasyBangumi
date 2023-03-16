package com.heyanle.easybangumi4.ui.cartoon_play


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.easybangumi4.DB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/7 21:18.
 * https://github.com/heyanLE
 */
class CartoonPlayViewModel: ViewModel() {

    data class EnterData(
        val playLineIndex: Int,
        val episode: Int,
        val adviceProgress: Long,
    ){
        override fun toString(): String {
            return "EnterData(playLineIndex=$playLineIndex, episode=$episode, adviceProgress=$adviceProgress)"
        }
    }

    var selectedLineIndex by mutableStateOf(0)

    fun onDetailedLoaded(
        cartoonSummary: CartoonSummary,
        info: DetailedViewModel.DetailedState.Info,
        enter: EnterData?
    ){
        //enter.loge("CartoonPlay")
        viewModelScope.launch {
            val realEnter = getRealEnterDataWhenFirst(cartoonSummary, enter, true)
            if(info.playLine.isEmpty()){
                return@launch
            }
            var lineIndex = if(realEnter.playLineIndex >= 0 && realEnter.playLineIndex < info.playLine.size) realEnter.playLineIndex else 0
            if(info.playLine[lineIndex].episode.isEmpty()){
                for(i in info.playLine.indices){
                    if(info.playLine[i].episode.isNotEmpty()){
                        lineIndex = i
                        break
                    }
                }
            }
            selectedLineIndex = lineIndex
            if(info.playLine[lineIndex].episode.isEmpty()){
                return@launch
            }
            //realEnter.loge("CartoonPlay")
            CartoonPlayingManager.changeLine(cartoonSummary.source, info.detail, lineIndex ,info.playLine[lineIndex], realEnter.episode, realEnter.adviceProgress)
        }

    }

    private suspend fun getRealEnterDataWhenFirst(
        cartoonSummary: CartoonSummary,
        enterData: EnterData?,
        useHistory: Boolean = true
    ): EnterData {
        if (enterData == null || enterData.playLineIndex == -1) {
            return if (useHistory) getEnterDataFromHistory(cartoonSummary) else {
                EnterData(0, 0, 0)
            }
        }
        return enterData
    }

    private suspend fun getEnterDataFromHistory(cartoonSummary: CartoonSummary,): EnterData {
        return withContext(Dispatchers.IO) {
            val hist = DB.cartoonHistory.getFromCartoonSummary(
                cartoonSummary.id,
                cartoonSummary.source,
                cartoonSummary.url,
            )
            if (hist == null || hist.lastLinesIndex == -1) {
                return@withContext EnterData(0, 0, 0)
            }
            return@withContext EnterData(
                hist.lastLinesIndex,
                hist.lastEpisodeIndex.coerceAtLeast(0),
                hist.lastProcessTime.coerceAtLeast(0)
            )
        }
    }

}