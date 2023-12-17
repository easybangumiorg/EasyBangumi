package com.heyanle.easybangumi4.ui.main.star

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.case.CartoonInfoCase
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.toIdentify
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * CartoonCover 的 star 逻辑抽取
 * Created by heyanlin on 2023/8/4.
 * https://github.com/heyanLE
 */
class CoverStarViewModel : ViewModel() {

    private val cartoonInfoDao: CartoonInfoDao by Injekt.injectLazy()
    val starFlow = cartoonInfoDao.flowAllStar()
    val setFlow = starFlow.map {
        val set = mutableSetOf<String>()
        it.forEach {
            set.add(it.toIdentify())
        }
        set
    }





    fun star(cartoonCover: CartoonCover) {
        viewModelScope.launch {
            val old = cartoonInfoDao.getByCartoonSummary(cartoonCover.id, cartoonCover.source, cartoonCover.url)
            if(old == null){
                cartoonInfoDao.insert(CartoonInfo.fromCartoonCover(cartoonCover))
            }else{
                if(old.starTime > 0){
                    cartoonInfoDao.modify(old.copy(starTime = 0, tags = "", upTime = 0))
                }else{
                    cartoonInfoDao.modify(old.copy(starTime = System.currentTimeMillis()))
                }
            }
        }
    }
}