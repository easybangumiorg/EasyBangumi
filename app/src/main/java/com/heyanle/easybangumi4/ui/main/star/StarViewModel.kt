package com.heyanle.easybangumi4.ui.main.star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.star.CartoonTagsController
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.cartoon.CartoonUpdateController
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.common.proc.FilterState
import com.heyanle.easybangumi4.ui.common.proc.FilterWith
import com.heyanle.easybangumi4.ui.common.proc.SortBy
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/7/29 22:36.
 * https://github.com/heyanLE
 */
class StarViewModel : ViewModel() {


    data class State(
        val isLoading: Boolean = true,
        val searchQuery: String? = null,
        val starCount: Int = 0,
        val curTab: CartoonTag? = null,
        val tagList: List<CartoonTag> = emptyList(),
        val data: Map<String, List<CartoonInfo>> = emptyMap(),
        val selection: Set<CartoonInfo> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    ) {
        val isFilter = curTab?.isInFilter ?: false
    }

    sealed class DialogState {
        data class ChangeTag(
            val selection: Set<CartoonInfo>,
            val tagList: List<CartoonTag>,
        ) : DialogState() {

            val tagMap: Map<String, CartoonTag> by lazy {
                val res = hashMapOf<String, CartoonTag>()
                tagList.forEach {
                    res[it.label] = it
                }
                res
            }

            fun getTags(): List<CartoonTag> {
                val tags = mutableSetOf<CartoonTag>()
                selection.forEach {
                    it.tagList.forEach {
                        tagMap[it]?.let { tag ->
                            tags.add(tag)
                        }
                    }
                }
                return tags.toList()
            }
        }

        data class Delete(
            val selection: Set<CartoonInfo>,
        ) : DialogState()

        data object Proc : DialogState()

        data class MigrateSource(
            val selection: Set<CartoonInfo>,
        ) : DialogState()
    }

    private val cartoonStarController: CartoonStarController by Inject.injectLazy()
    private val cartoonInfoDao: CartoonInfoDao by Inject.injectLazy()
    private val settingPreferences: SettingPreferences by Inject.injectLazy()
    private val cartoonTagsController: CartoonTagsController by Inject.injectLazy()

    private val updateController: CartoonUpdateController by Inject.injectLazy()

    private val _stateFlow = MutableStateFlow(State(data = emptyMap()))
    val stateFlow = _stateFlow.asStateFlow()



    // 最后一个选择的，用于长按区间反选
    private var lastSelectCartoon: CartoonInfo? = null
    private var lastSelectTag: CartoonTag? = null

    init {
        viewModelScope.launch {
            combine(
                cartoonStarController.cartoonTagFlow,
                // cartoonStarControllerOld.flowCartoon().distinctUntilChanged().stateIn(viewModelScope),
                stateFlow.map { it.searchQuery }.distinctUntilChanged(),
            ) {starState, searchKey ->

                if (searchKey.isNullOrEmpty()) {
                    _stateFlow.update { sta ->
                        sta.copy(
                            tagList = starState.tagList.filter { it.show },
                            data = starState.label2Cartoon,
                            curTab = starState.tagList.firstOrNull { it.label == sta.curTab?.label } ?: starState.tagList.firstOrNull(),
                            starCount = starState.cartoonInfoList.size,
                            isLoading = false,
                        )
                    }
                } else {
                    _stateFlow.update { sta ->
                        sta.copy(
                            tagList = starState.tagList.filter { it.show },
                            data = starState.label2Cartoon.map {
                                it.key to it.value.filter { it.matches(searchKey) }
                            }.toMap(),
                            curTab = starState.tagList.firstOrNull { it.label == sta.curTab?.label } ?: starState.tagList.firstOrNull(),
                            starCount = starState.cartoonInfoList.size,
                            isLoading = false,
                        )
                    }
                }

            }.collect()
        }
    }

    fun deleteSelection(selection: Set<CartoonInfo>) {
        viewModelScope.launch {
            cartoonInfoDao.deleteStar(selection.toList())
            dialogDismiss()
            onSelectionExit()
        }
    }

    fun changeTagSelection(selection: Set<CartoonInfo>, tags: List<CartoonTag>) {
        viewModelScope.launch {
            val tt = tags.map { it.label }.toList()
            val tag = tt.joinToString(", ") {
                it.toString()
            }
            tt.loge("StarViewModel")
            tag.loge("StarViewModel")
            val target = selection.map {
                it.copy(tags = tag)
            }.toList()
            cartoonInfoDao.modify(target)
        }
    }

    // 搜索
    fun onSearch(searchQuery: String?) {
        _stateFlow.update { it.copy(searchQuery = searchQuery) }
    }

    // 切换 tab
    fun changeTab(tab: CartoonTag) {
        lastSelectCartoon = null
        _stateFlow.update {
            if (it.tagList.contains(tab)) {
                it.copy(
                    curTab = tab
                )
            } else {
                it
            }
        }
    }

    // 多选
    fun onSelectionExit() {
        lastSelectCartoon = null
        lastSelectTag = null
        _stateFlow.update {
            it.copy(selection = emptySet())
        }
    }

    fun onSelectAll() {
        _stateFlow.update {
            val dd = it.data[it.curTab?.label] ?: emptyList()
            it.copy(
                selection = it.selection.plus(dd)
            )
        }
    }

    fun onSelectInvert() {
        _stateFlow.update {
            val dd = it.data[it.curTab?.label] ?: emptyList()
            val selection = it.selection.toMutableSet()
            dd.forEach { star ->
                if (selection.contains(star)) {
                    selection.remove(star)
                } else {
                    selection.add(star)
                }
            }
            it.copy(
                selection = selection
            )
        }
    }

    fun onSelectionChange(cartoon: CartoonInfo) {
        lastSelectCartoon = cartoon
        lastSelectTag = stateFlow.value.curTab
        _stateFlow.update {
            val selection = if (it.selection.contains(cartoon)) {
                it.selection.minus(cartoon)
            } else it.selection.plus(cartoon)
            it.copy(selection = selection)
        }
        if (_stateFlow.value.selection.isEmpty()) {
            lastSelectCartoon = null
            lastSelectTag = null
        }
    }

    fun onSelectionLongPress(cartoonInfo: CartoonInfo) {
        if (lastSelectCartoon != null && lastSelectTag != null && lastSelectTag == stateFlow.value.curTab) {
            _stateFlow.update {
                val selection = it.selection.toMutableSet()
                val lastList = it.data[lastSelectTag?.label] ?: listOf()
                var a = lastList.indexOf(lastSelectCartoon)
                val b = lastList.indexOf(cartoonInfo)
                if (b > a) {
                    a += 1
                } else if (a > b) {
                    a -= 1
                }
                val start = a.coerceAtMost(b)
                val end = a.coerceAtLeast(b)
                for (i in start..end) {
                    if (i >= 0 && i < lastList.size) {
                        val star = lastList[i]
                        if (selection.contains(star)) {
                            selection.remove(star)
                        } else {
                            selection.add(star)
                        }
                    }
                }
                it.copy(
                    selection = selection
                )
            }
            lastSelectCartoon = cartoonInfo
            lastSelectTag = stateFlow.value.curTab
        } else {
            // 如果和上一个不在同一个收藏夹就走普通点击逻辑
            onSelectionChange(cartoonInfo)
        }
    }

    // dialog
    fun dialogDeleteSelection() {
        _stateFlow.update {
            val selection = it.selection
            it.copy(dialog = DialogState.Delete(selection))
        }
    }


    fun dialogChangeTag() {
        _stateFlow.update {
            it.copy(dialog = DialogState.ChangeTag(it.selection, it.tagList.filter { ! it.isInner }))
        }
    }

    fun dialogMigrateSelect() {
        _stateFlow.update {
            it.copy(dialog = DialogState.MigrateSource(it.selection))
        }
    }

    fun dialogProc() {
        _stateFlow.update {
            it.copy(
                dialog = DialogState.Proc
            )
        }
    }

    fun onUpdateSelection() {
        val list = stateFlow.value.selection.toList() ?: emptyList()
        onSelectionExit()
        updateController.update(list)
        stringRes(com.heyanle.easy_i18n.R.string.start_update_selection).moeSnackBar()
    }

    fun onUpSelection() {
        viewModelScope.launch {
            val old = _stateFlow.value
            var start = System.currentTimeMillis()
            val set = old.selection.map {
                it.copy(
                    upTime = if (it.upTime == 0L) start++ else 0
                )
            }
            cartoonInfoDao.modify(set)
            onSelectionExit()
            dialogDismiss()
        }


    }

    fun onUpdate() {
        viewModelScope.launch {
            //val list = if (stateFlow.value.curTab == UPDATE_TAG) cartoonStarDao.getAll() else stateFlow.value.data[stateFlow.value.curTab]?: emptyList()
            if (stateFlow.value.curTab?.label == CartoonTag.ALL_TAG_LABEL) {
                updateController.updateAll()
                stringRes(com.heyanle.easy_i18n.R.string.start_update_strict).moeSnackBar()
            } else {
                val list = stateFlow.value.data[stateFlow.value.curTab?.label] ?: emptyList()
                updateController.update(list)
                stringRes(com.heyanle.easy_i18n.R.string.start_update_tag).moeSnackBar()
            }

        }

    }

    fun onMigrate(selection: Set<CartoonInfo>, source: List<String>) {

    }

    fun tagConfigChange(
        cartoonTag: CartoonTag,

        isCustomSetting: Boolean = cartoonTag.isCustomSetting,

        filterWithId: String? = null ,
        filterState: Int? = null,

        sortById: String? = null,
        isReverse: Boolean? = null,
    ) {

        val n = cartoonTag.copy(
            isCustomSetting = isCustomSetting,
            filterState = if (filterWithId != null && filterState != null )cartoonTag.filterState.toMutableMap().apply {
                this[filterWithId] = filterState
            } else cartoonTag.filterState,

            sortId = sortById ?: cartoonTag.sortId,
            isReverse = isReverse ?: cartoonTag.isReverse
        )
        cartoonStarController.modifier(n)
    }



    fun dialogDismiss() {
        //onSelectionExit()
        _stateFlow.update {
            it.copy(dialog = null)
        }
    }

}