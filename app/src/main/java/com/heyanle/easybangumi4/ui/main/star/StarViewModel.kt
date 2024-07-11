package com.heyanle.easybangumi4.ui.main.star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.tag.CartoonTagsController
import com.heyanle.easybangumi4.cartoon.tag.isALL
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.cartoon.CartoonUpdateController
import com.heyanle.easybangumi4.cartoon.entity.CartoonTagWrapper
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
import kotlinx.coroutines.flow.stateIn
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
        val tabs: List<CartoonTagWrapper> = listOf(),
        val curTab: CartoonTagWrapper? = null,
        val data: Map<CartoonTagWrapper, List<CartoonInfo>>,
        val selection: Set<CartoonInfo> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    )

    sealed class DialogState {
        data class ChangeTag(
            val selection: Set<CartoonInfo>,
            val tagList: List<CartoonTagWrapper>,
        ) : DialogState() {

            val tagMap: Map<Int, CartoonTagWrapper> by lazy {
                val res = hashMapOf<Int, CartoonTagWrapper>()
                tagList.forEach {
                    res[it.id] = it
                }
                res
            }

            fun getTags(): List<CartoonTagWrapper> {
                val tags = mutableSetOf<CartoonTagWrapper>()
                selection.forEach {
                    it.tags.split(",").map { it.trim() }.forEach {
                        it.toIntOrNull()?.let { id ->
                            tagMap[id]?.let { tag ->
                                tags.add(tag)
                            }
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

    val tagSortFilterState = cartoonStarController.tagSortFilterStateItem

    val isFilter = combine(
        cartoonStarController.tagSortFilterStateItem,
        stateFlow
    ) { sortFilterItem, state ->
        val currentTag = state.curTab ?: return@combine false
        val stat = sortFilterItem[currentTag.id] ?: return@combine false
        for (entry in stat.filterState) {
            if (entry.value == FilterState.STATUS_ON || entry.value == FilterState.STATUS_EXCLUDE) {
                return@combine true
            }
        }
        false
    }


    // 最后一个选择的，用于长按区间反选
    private var lastSelectCartoon: CartoonInfo? = null
    private var lastSelectTag: CartoonTagWrapper? = null

    init {
        viewModelScope.launch {
            combine(
                cartoonStarController.flowCartoonTag.distinctUntilChanged().stateIn(viewModelScope),
                // cartoonStarControllerOld.flowCartoon().distinctUntilChanged().stateIn(viewModelScope),
                stateFlow.map { it.searchQuery }.distinctUntilChanged(),
            ) {starMap, searchKey ->

                val allEntity = starMap.entries.find { it.key.id == CartoonTagsController.ALL_TAG_ID }

                val tagList = starMap.keys.toList().sortedBy {
                    it.order
                }


                val tagsMap = HashMap<Int, CartoonTagWrapper>()
                tagList.forEach {
                    tagsMap[it.id] = it
                }
                if (searchKey.isNullOrEmpty()) {

                    _stateFlow.update {
                        it.copy(
                            tabs = tagList,
                            curTab = it.curTab?.id?.let { tagsMap[it] }
                                ?: tagsMap[CartoonTagsController.ALL_TAG_ID],
                            starCount = allEntity?.value?.size?:0,
                            isLoading = false,
                            data = starMap
                        )
                    }
                } else {
                    _stateFlow.update {
                        it.copy(
                            tabs = tagList,
                            curTab = it.curTab?.id?.let { tagsMap[it] }
                                ?: tagsMap[CartoonTagsController.ALL_TAG_ID],
                            starCount = allEntity?.value?.size?:0,
                            isLoading = false,
                            data = starMap
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

    fun changeTagSelection(selection: Set<CartoonInfo>, tags: List<CartoonTagWrapper>) {
        viewModelScope.launch {
            val tt = tags.map { it.id }.toList()
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
    fun changeTab(tab: CartoonTagWrapper) {
        _stateFlow.update {
            if (it.tabs.contains(tab)) {
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
            val dd = it.data[it.curTab] ?: emptyList()
            it.copy(
                selection = it.selection.plus(dd)
            )
        }
    }

    fun onSelectInvert() {
        _stateFlow.update {
            val dd = it.data[it.curTab] ?: emptyList()
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
        if (lastSelectTag != null && lastSelectTag == stateFlow.value.curTab) {
            _stateFlow.update {
                val selection = it.selection.toMutableSet()
                val lastList = it.data[lastSelectTag] ?: listOf()
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
            it.copy(dialog = DialogState.ChangeTag(it.selection, it.tabs))
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
            if (stateFlow.value.curTab?.cartoonTag?.isALL() == true) {
                updateController.updateAll()
                stringRes(com.heyanle.easy_i18n.R.string.start_update_strict).moeSnackBar()
            } else {
                val list = stateFlow.value.data[stateFlow.value.curTab] ?: emptyList()
                updateController.update(list)
                stringRes(com.heyanle.easy_i18n.R.string.start_update_tag).moeSnackBar()
            }

        }

    }

    fun onMigrate(selection: Set<CartoonInfo>, source: List<String>) {

    }

    fun onFilterChange(
        cartoonTagWrapper: CartoonTagWrapper,
        filterWith: FilterWith<CartoonInfo>,
        state: Int
    ) {
        var realItem = cartoonTagWrapper.tagSortFilterState
        val current = realItem.filterState.toMutableMap()
        when (state) {
            FilterState.STATUS_OFF -> {
                current[filterWith.id] = FilterState.STATUS_ON
            }

            FilterState.STATUS_ON -> {
                current[filterWith.id] = FilterState.STATUS_EXCLUDE
            }

            else -> {
                current[filterWith.id] = FilterState.STATUS_OFF
            }
        }
        realItem = realItem.copy(filterState = current)
        cartoonStarController.changeState(cartoonTagWrapper.id, realItem)
    }

    fun onSortChange(
        cartoonTagWrapper: CartoonTagWrapper,
        sortBy: SortBy<CartoonInfo>,
        state: Int
    ) {
        var realItem = cartoonTagWrapper.tagSortFilterState

        when (state) {
            SortState.STATUS_OFF -> {
                realItem = realItem.copy(
                    sortId = sortBy.id,
                    isReverse = false
                )
            }

            SortState.STATUS_ON -> {
                realItem = realItem.copy(
                    sortId = sortBy.id,
                    isReverse = true
                )
            }

            else -> {
                realItem = realItem.copy(
                    sortId = sortBy.id,
                    isReverse = false
                )
            }
        }
        cartoonStarController.changeState(cartoonTagWrapper.id, realItem)
    }


    fun onCustomChange(
        cartoonTagWrapper: CartoonTagWrapper,
    ) {
        cartoonStarController.changeState(
            cartoonTagWrapper.id,
            cartoonTagWrapper.tagSortFilterState.copy(tagId = cartoonTagWrapper.id, isCustomSetting = !cartoonTagWrapper.tagSortFilterState.isCustomSetting)
        )
    }

    fun dialogDismiss() {
        //onSelectionExit()
        _stateFlow.update {
            it.copy(dialog = null)
        }
    }

}