package com.heyanle.easybangumi4.ui.common.cover_star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * CartoonCover 的 star 逻辑抽取
 * Created by heyanlin on 2023/8/4.
 * https://github.com/heyanLE
 */
class CoverStarViewModel : ViewModel() {

    private val cartoonInfoDao: CartoonInfoDao by Inject.injectLazy()
    private val cartoonStarController: CartoonStarController by Inject.injectLazy()

    data class State(
        val startList: List<CartoonInfo> = emptyList(),
        val dialog: Dialog? = null,
    ) {
        val starIdList: List<String> by lazy {
            startList.map { it.id }
        }

        val identifySet: Set<String> by lazy {
            startList.map { it.toIdentify() }.toSet()
        }
    }

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow = _stateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            cartoonInfoDao.flowAllStar().collectLatest { list ->
                _stateFlow.update {
                    it.copy(
                        startList = list
                    )
                }
            }
        }
    }


    sealed class Dialog {
        data class StarDialogState(
            val cartoon: CartoonCover,
            val tagList: List<CartoonTag>,
        ) : Dialog()

    }


    fun dispatchStar(cartoonCover: CartoonCover) {
        viewModelScope.launch {
            val old = cartoonInfoDao.getByCartoonSummary(cartoonCover.id, cartoonCover.source)
            if (old != null && old.starTime > 0) {
                cartoonInfoDao.modify(old.copy(starTime = 0, tags = "", upTime = 0))
            }
            val tl = cartoonStarController.cartoonTagFlow.first().tagList
            if (tl.find { !it.isInner && !it.isDefault } != null) {
                _stateFlow.update {
                    it.copy(
                        dialog = Dialog.StarDialogState(
                            cartoonCover,
                            tl.filter { !it.isInner }.sortedBy { it.order })
                    )
                }
            } else {
                realStar(cartoonCover)
            }
        }
    }

    fun realStar(cartoonCover: CartoonCover, tagList: List<CartoonTag>? = null) {
        viewModelScope.launch {
            val old = cartoonInfoDao.getByCartoonSummary(cartoonCover.id, cartoonCover.source)
            if (old == null) {
                cartoonInfoDao.insert(
                    CartoonInfo.fromCartoonCover(cartoonCover, tagList)
                        .copy(starTime = System.currentTimeMillis())
                )
            } else {
                if (old.starTime > 0) {
                    cartoonInfoDao.modify(old.copy(starTime = 0, tags = "", upTime = 0))
                } else {
                    cartoonInfoDao.modify(
                        old.copy(
                            starTime = System.currentTimeMillis(),
                            tags = tagList?.joinToString(", ") { it.label } ?: ""))
                }
            }
        }
    }

    fun dismissDialog() {
        _stateFlow.update {
            it.copy(
                dialog = null
            )
        }
    }
}