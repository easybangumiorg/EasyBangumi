package com.heyanle.easybangumi4.ui.main.star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonTagDao
import com.heyanle.easybangumi4.cartoon.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.cartoon.tags.CartoonTagsController
import com.heyanle.easybangumi4.cartoon.tags.isALL
import com.heyanle.easybangumi4.cartoon.tags.isUpdate
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.source.CartoonUpdateController
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
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
        val data: Map<CartoonTag, List<CartoonStar>>,
        val selection: Set<CartoonStar> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    )

    sealed class DialogState {
//        data class ChangeTag(
//            val selection: Set<CartoonStar>,
//            val tagMap: Map<Int, CartoonTag>,
//        ) : DialogState() {
//            fun getTags(): List<CartoonTag> {
//                val tags = mutableSetOf<CartoonTag>()
//                selection.forEach {
//                    it.tags.split(",").map { it.trim() }.forEach {
//                        it.toIntOrNull()?.let { id ->
//                            tagMap[id]?.let { tag ->
//                                tags.add(tag)
//                            }
//                        }
//
//                    }
//                }
//                return tags.toList()
//            }
//        }

        data class Delete(
            val selection: Set<CartoonStar>,
        ) : DialogState()

        data object Proc: DialogState()
    }

    private val cartoonStarController: CartoonStarController by Injekt.injectLazy()
    private val cartoonStarDao: CartoonStarDao by Injekt.injectLazy()
    private val settingPreferences: SettingPreferences by Injekt.injectLazy()
    private val cartoonTagsController: CartoonTagsController by Injekt.injectLazy()

    private val updateController: CartoonUpdateController by Injekt.injectLazy()

    private val _stateFlow = MutableStateFlow(State(data = emptyMap()))
    val stateFlow = _stateFlow.asStateFlow()


    // 最后一个选择的，用于长按区间反选
    private var lastSelectCartoon: CartoonStar? = null
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

    fun deleteSelection(selection: Set<CartoonStar>) {
        viewModelScope.launch {
            cartoonStarDao.delete(selection.toList())
            dialogDismiss()
            onSelectionExit()
        }
    }

    fun changeTagSelection(selection: Set<CartoonStar>, tags: List<CartoonTag>) {
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
            cartoonStarDao.modify(target)
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

    fun onSelectionChange(cartoonStar: CartoonStar) {
        lastSelectCartoon = cartoonStar
        lastSelectTag = stateFlow.value.curTab
        _stateFlow.update {

            val selection = if (it.selection.contains(cartoonStar)) {
                it.selection.minus(cartoonStar)
            } else it.selection.plus(cartoonStar)
            it.copy(selection = selection)
        }
    }

    fun onSelectionLongPress(cartoonStar: CartoonStar) {
        if (lastSelectTag != null && lastSelectTag == stateFlow.value.curTab) {
            _stateFlow.update {
                val selection = it.selection.toMutableSet()
                val lastList = it.data[lastSelectTag] ?: listOf()
                var a = lastList.indexOf(lastSelectCartoon)
                val b = lastList.indexOf(cartoonStar)
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
            lastSelectCartoon = cartoonStar
            lastSelectTag = stateFlow.value.curTab
        } else {
            // 如果和上一个不在同一个收藏夹就走普通点击逻辑
            onSelectionChange(cartoonStar)
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
//        _stateFlow.update {
//            val selection = it.selection
//            it.copy(dialog = DialogState.ChangeTag(selection, tagMapFlow.value))
//        }
    }

    fun onUpdateSelection() {
        val list = stateFlow.value.selection.toList() ?: emptyList()
        onSelectionExit()
        if (updateController.tryUpdate(list)) {
            stringRes(com.heyanle.easy_i18n.R.string.start_update_selection).moeSnackBar()
        } else {
            stringRes(com.heyanle.easy_i18n.R.string.doing_update_wait).moeSnackBar()
        }
    }

    fun onUpdateAll() {
        viewModelScope.launch {
            //val list = if (stateFlow.value.curTab == UPDATE_TAG) cartoonStarDao.getAll() else stateFlow.value.data[stateFlow.value.curTab]?: emptyList()
            if (stateFlow.value.curTab?.isUpdate() == true || stateFlow.value.curTab?.isALL() == true) {
                if (updateController.tryUpdate(true)) {
                    stringRes(com.heyanle.easy_i18n.R.string.start_update_strict).moeSnackBar()
                } else {
                    stringRes(com.heyanle.easy_i18n.R.string.doing_update_wait).moeSnackBar()
                }
            } else {
                val list = stateFlow.value.data[stateFlow.value.curTab] ?: emptyList()
                if (updateController.tryUpdate(list)) {
                    stringRes(com.heyanle.easy_i18n.R.string.start_update_tag).moeSnackBar()
                } else {
                    stringRes(com.heyanle.easy_i18n.R.string.doing_update_wait).moeSnackBar()
                }
            }

        }

    }

    fun dialogDismiss() {
        //onSelectionExit()
        _stateFlow.update {
            it.copy(dialog = null)
        }
    }

    private fun List<CartoonStar>.toMap(tagMap: Map<Int, CartoonTag>): Map<CartoonTag, List<CartoonStar>> {
        val map = hashMapOf<CartoonTag, ArrayList<CartoonStar>>()
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
            val all = arrayListOf<CartoonStar>()
            all.addAll(this)
            map[it] = all
        }
        return map
    }

}