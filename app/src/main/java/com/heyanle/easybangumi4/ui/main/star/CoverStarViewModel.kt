package com.heyanle.easybangumi4.ui.main.star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * CartoonCover 的 star 逻辑抽取
 * Created by heyanlin on 2023/8/4.
 * https://github.com/heyanLE
 */
class CoverStarViewModel : ViewModel() {

    private val cartoonInfoDao: CartoonInfoDao by Inject.injectLazy()
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
            val old = cartoonInfoDao.getByCartoonSummary(cartoonCover.id, cartoonCover.source,)
            if(old == null){
                cartoonInfoDao.insert(CartoonInfo.fromCartoonCover(cartoonCover).copy(starTime = System.currentTimeMillis()))
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