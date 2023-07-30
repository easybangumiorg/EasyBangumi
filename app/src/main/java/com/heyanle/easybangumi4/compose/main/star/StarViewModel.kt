package com.heyanle.easybangumi4.compose.main.star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.base.entity.CartoonStar
import com.heyanle.easybangumi4.compose.common.moeSnackBar
import com.heyanle.easybangumi4.compose.main.update.CartoonUpdateController
import com.heyanle.easybangumi4.preferences.CartoonPreferences
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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

    companion object {
        const val DEFAULT_TAG = "{default}}"
        const val UPDATE_TAG = "{update}}"

        fun tagLabel(tag: String): String {
            return when (tag) {
                DEFAULT_TAG -> stringRes(com.heyanle.easy_i18n.R.string.default_word)
                UPDATE_TAG -> stringRes(com.heyanle.easy_i18n.R.string.update)
                else -> tag
            }
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val searchQuery: String? = null,
        val starCount: Int = 0,
        val tabs: List<String> = listOf(UPDATE_TAG, DEFAULT_TAG),
        val curTab: String? = "",
        val data: Map<String, List<CartoonStar>>,
        val selection: Set<CartoonStar> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    )

    sealed class DialogState {
        data class ChangeTag(
            val selection: Set<CartoonStar>,
        ) : DialogState() {
            fun getTags(): List<String> {
                val tags = mutableSetOf<String>()
                selection.forEach {
                    it.tags.split(",").map { it.trim() }.forEach {
                        tags.add(it)
                    }
                }
                return tags.toList()
            }
        }

        data class Delete(
            val selection: Set<CartoonStar>,
        ) : DialogState()
    }

    private val cartoonStarDao: CartoonStarDao by Injekt.injectLazy()
    private val cartoonPreferences: CartoonPreferences by Injekt.injectLazy()
    private val settingPreferences: SettingPreferences by Injekt.injectLazy()

    private val updateController: CartoonUpdateController by Injekt.injectLazy()

    private val _stateFlow = MutableStateFlow(State(data = emptyMap()))
    val stateFlow = _stateFlow.asStateFlow()

    private val _tagsFlow = cartoonPreferences.tags.flow().map { cartoonTags ->
        cartoonTags.sortedBy { it.order }
    }.stateIn(viewModelScope, SharingStarted.Lazily, cartoonPreferences.tags.get())


    // 最后一个选择的，用于长按区间反选
    private var lastSelectCartoon: CartoonStar? = null
    private var lastSelectTag: String? = null

    init {
        // 处理搜索和加载
        viewModelScope.launch {
            combine(
                cartoonStarDao.flowAll(),
                stateFlow.map { it.searchQuery }.distinctUntilChanged(),
            ) { starList, searchKey ->
                if (searchKey.isNullOrEmpty()) {
                    _stateFlow.update {
                        it.copy(
                            starCount = starList.size,
                            isLoading = false,
                            data = starList.toMap()
                        )
                    }
                } else {
                    _stateFlow.update {
                        it.copy(
                            starCount = starList.size,
                            isLoading = false,
                            data = starList.filter { it.matches(searchKey) }.toMap()
                        )
                    }
                }
            }.collect()
        }
        viewModelScope.launch {
            combine(
                settingPreferences.isShowUpdateInStar.flow().distinctUntilChanged()
                    .stateIn(viewModelScope),
                cartoonPreferences.tags.flow().map { cartoonTags ->
                    cartoonTags.sortedBy { it.order }
                }.distinctUntilChanged().stateIn(viewModelScope)
            ) { showUpdate, tabs ->
                val ts = tabs.asSequence().filter {
                    it.label != UPDATE_TAG && it.label != DEFAULT_TAG
                }.sortedBy {
                    it.order
                }.map {
                    it.label
                }.toMutableList()
                ts.add(0, DEFAULT_TAG)
                if (showUpdate) {
                    ts.add(0, UPDATE_TAG)
                }
                _stateFlow.update {
                    it.copy(
                        tabs = ts,
                        curTab = if (it.curTab == null || !ts.contains(it.curTab)) {
                            DEFAULT_TAG
                        } else {
                            it.curTab
                        }
                    )
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

    fun changeTagSelection(selection: Set<CartoonStar>, tags: List<String>){
        viewModelScope.launch {
            val tag = tags.joinToString {
                ", "
            }
            val target = selection.asSequence().map {
                it.tags = tag
                it
            }.toList()
            cartoonStarDao.modify(target)
        }
    }

    // 搜索
    fun onSearch(searchQuery: String?) {
        _stateFlow.update { it.copy(searchQuery = searchQuery) }
    }

    // 切换 tab
    fun changeTab(tab: String) {
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
        _stateFlow.update {
            lastSelectCartoon = cartoonStar
            lastSelectTag = stateFlow.value.curTab
            val selection = if (it.selection.contains(cartoonStar)) {
                it.selection.minus(cartoonStar)
            } else it.selection.plus(cartoonStar)
            it.copy(selection = selection)
        }
    }

    fun onSelectionLongPress(cartoonStar: CartoonStar) {
        if (lastSelectTag != null && lastSelectTag == stateFlow.value.curTab) {
            lastSelectCartoon = cartoonStar
            lastSelectTag = stateFlow.value.curTab
            _stateFlow.update {
                val selection = it.selection.toMutableSet()
                val lastList = it.data[lastSelectTag] ?: listOf()
                var a = lastList.indexOf(lastSelectCartoon)
                val b = lastList.indexOf(cartoonStar)
                if (b > a) {
                    a += 1
                } else if (a < b) {
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
        _stateFlow.update {
            val selection = it.selection
            it.copy(dialog = DialogState.ChangeTag(selection))
        }
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
            if (stateFlow.value.curTab == UPDATE_TAG || stateFlow.value.curTab == DEFAULT_TAG) {
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

    private fun List<CartoonStar>.toMap(): Map<String, List<CartoonStar>> {
        val map = hashMapOf<String, ArrayList<CartoonStar>>()
        forEach { star ->
            star.tags.split(",").map { it.trim() }
                .forEach {
                    val l = map[it] ?: arrayListOf()
                    l.add(star)
                    map[it] = l
                }
        }
        val all = arrayListOf<CartoonStar>()
        all.addAll(this)
        map[DEFAULT_TAG] = all
        return map
    }

}