package com.heyanle.easybangumi4.ui.main.star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.cartoon.tag.CartoonTagsController
import com.heyanle.easybangumi4.cartoon.tag.isALL
import com.heyanle.easybangumi4.cartoon.tag.isUpdate
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source.CartoonUpdateController
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.common.proc.FilterState
import com.heyanle.easybangumi4.ui.common.proc.FilterWith
import com.heyanle.easybangumi4.ui.common.proc.SortBy
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
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
        val tabs: List<CartoonTag> = listOf(),
        val curTab: CartoonTag? = null,
        val data: Map<CartoonTag, List<CartoonInfo>>,
        val selection: Set<CartoonInfo> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    )

    sealed class DialogState {
        data class ChangeTag(
            val selection: Set<CartoonInfo>,
            val tagList: List<CartoonTag>,
        ) : DialogState() {

            val tagMap: Map<Int, CartoonTag> by lazy {
                val res = hashMapOf<Int, CartoonTag>()
                tagList.forEach {
                    res[it.id] = it
                }
                res
            }

            fun getTags(): List<CartoonTag> {
                val tags = mutableSetOf<CartoonTag>()
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
    }

    private val cartoonStarController: CartoonStarController by Injekt.injectLazy()
    private val cartoonInfoDao: CartoonInfoDao by Injekt.injectLazy()
    private val settingPreferences: SettingPreferences by Injekt.injectLazy()
    private val cartoonTagsController: CartoonTagsController by Injekt.injectLazy()

    private val updateController: CartoonUpdateController by Injekt.injectLazy()

    private val _stateFlow = MutableStateFlow(State(data = emptyMap()))
    val stateFlow = _stateFlow.asStateFlow()

    val isFilter = cartoonStarController.filterState.statusMap.map {
        for (entry in it) {
            if (entry.value == FilterState.STATUS_ON || entry.value == FilterState.STATUS_EXCLUDE) {
                return@map true
            }
        }
        false
    }


    // 最后一个选择的，用于长按区间反选
    private var lastSelectCartoon: CartoonInfo? = null
    private var lastSelectTag: CartoonTag? = null

    init {
        viewModelScope.launch {
            combine(
                cartoonTagsController.tagsList.map { it.sortedBy { it.order } }
                    .distinctUntilChanged().stateIn(viewModelScope),
                cartoonStarController.flowCartoon().distinctUntilChanged().stateIn(viewModelScope),
                stateFlow.map { it.searchQuery }.distinctUntilChanged(),
            ) { tagList, starList, searchKey ->
                val tagsMap = HashMap<Int, CartoonTag>()
                tagList.forEach {
                    tagsMap[it.id] = it
                }
                if (searchKey.isNullOrEmpty()) {

                    _stateFlow.update {
                        it.copy(
                            tabs = tagList,
                            curTab = it.curTab?.id?.let { tagsMap[it] },
                            starCount = starList.size,
                            isLoading = false,
                            data = starList.toMap(tagsMap)
                        )
                    }
                } else {
                    _stateFlow.update {
                        it.copy(
                            tabs = tagList,
                            curTab = it.curTab?.id?.let { tagsMap[it] },
                            starCount = starList.size,
                            isLoading = false,
                            data = starList.filter { it.matches(searchKey) }.toMap(tagsMap)
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
    fun changeTab(tab: CartoonTag) {
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

    fun dialogProc() {
        _stateFlow.update {
            it.copy(
                dialog = DialogState.Proc
            )
        }
    }

    fun onUpdateSelection() {
//        val list = stateFlow.value.selection.toList() ?: emptyList()
//        onSelectionExit()
//        if (updateController.tryUpdate(list)) {
//            stringRes(com.heyanle.easy_i18n.R.string.start_update_selection).moeSnackBar()
//        } else {
//            stringRes(com.heyanle.easy_i18n.R.string.doing_update_wait).moeSnackBar()
//        }
    }

    fun onUpdateAll() {
        viewModelScope.launch {
            //val list = if (stateFlow.value.curTab == UPDATE_TAG) cartoonStarDao.getAll() else stateFlow.value.data[stateFlow.value.curTab]?: emptyList()
            if (stateFlow.value.curTab?.isUpdate() == true || stateFlow.value.curTab?.isALL() == true) {
                updateController.updateAll()
                stringRes(com.heyanle.easy_i18n.R.string.start_update_strict).moeSnackBar()
            } else {
                val list = stateFlow.value.data[stateFlow.value.curTab] ?: emptyList()
                updateController.update(list)
                stringRes(com.heyanle.easy_i18n.R.string.start_update_tag).moeSnackBar()
            }

        }

    }

    fun getFilterState(): FilterState<CartoonInfo> {
        return cartoonStarController.filterState
    }

    fun getSortState(): SortState<CartoonInfo> {
        return cartoonStarController.sortState
    }

    fun onFilterChange(filterWith: FilterWith<CartoonInfo>, state: Int) {
        cartoonStarController.onFilterChange(filterWith, state)
    }

    fun onSortChange(sortBy: SortBy<CartoonInfo>, state: Int) {
        cartoonStarController.onSortChange(sortBy, state)
    }

    fun dialogDismiss() {
        //onSelectionExit()
        _stateFlow.update {
            it.copy(dialog = null)
        }
    }

    private fun List<CartoonInfo>.toMap(tagMap: Map<Int, CartoonTag>): Map<CartoonTag, List<CartoonInfo>> {
        val map = hashMapOf<CartoonTag, ArrayList<CartoonInfo>>()
        tagMap.asIterable().forEach {
            map[it.value] = arrayListOf()
        }
        forEach { star ->
            star.tags.split(",").map { it.trim() }
                .forEach {
                    it.toIntOrNull()?.let { id ->
                        tagMap[id]?.let { tag ->
                            val l = map[tag] ?: arrayListOf()
                            l.add(star)
                            map[tag] = l
                        }
                    }

                }
        }
        tagMap[CartoonTagsController.ALL_TAG_ID]?.let {
            val all = arrayListOf<CartoonInfo>()
            all.addAll(this)
            map[it] = all
        }
        return map
    }

}