package com.heyanle.easybangumi4.ui.main.star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.cartoon.db.dao.CartoonTagDao
import com.heyanle.easybangumi4.cartoon.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.main.star.update.CartoonUpdateController
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.stringRes
import org.koin.mp.KoinPlatform.getKoin
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
        val DEFAULT_TAG = CartoonTag(-1, stringRes(R.string.default_word), -1)
        val UPDATE_TAG = CartoonTag(-2, stringRes(R.string.update), -2)

        fun tagLabel(tag: CartoonTag): String {
            return when (tag) {
                DEFAULT_TAG -> stringRes(com.heyanle.easy_i18n.R.string.default_word)
                UPDATE_TAG -> stringRes(com.heyanle.easy_i18n.R.string.update)
                else -> tag.label
            }
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val searchQuery: String? = null,
        val starCount: Int = 0,
        val tabs: List<CartoonTag> = listOf(UPDATE_TAG, DEFAULT_TAG),
        val curTab: CartoonTag = DEFAULT_TAG,
        val data: Map<CartoonTag, List<CartoonStar>>,
        val selection: Set<CartoonStar> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    )

    sealed class DialogState {
        data class ChangeTag(
            val selection: Set<CartoonStar>,
            val tagMap: Map<Int, CartoonTag>,
        ) : DialogState() {
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
            val selection: Set<CartoonStar>,
        ) : DialogState()
    }

    private val cartoonTagDao: CartoonTagDao by getKoin().inject()
    private val cartoonStarDao: CartoonStarDao by getKoin().inject()
    private val settingPreferences: SettingPreferences by getKoin().inject()

    private val updateController: CartoonUpdateController by getKoin().inject()

    private val _stateFlow = MutableStateFlow(State(data = emptyMap()))
    val stateFlow = _stateFlow.asStateFlow()

    val tagMapFlow = cartoonTagDao.flowAll().distinctUntilChanged().map {
        val map = HashMap<Int, CartoonTag>()
        it.forEach {
            map[it.id] = it
        }
        map
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())


    // 最后一个选择的，用于长按区间反选
    private var lastSelectCartoon: CartoonStar? = null
    private var lastSelectTag: CartoonTag? = null

    init {
        // 处理搜索和加载
        viewModelScope.launch {
            combine(
                tagMapFlow,
                cartoonStarDao.flowAll(),
                stateFlow.map { it.searchQuery }.distinctUntilChanged(),
            ) { tagMap, starList, searchKey ->
                if (searchKey.isNullOrEmpty()) {
                    _stateFlow.update {
                        it.copy(
                            starCount = starList.size,
                            isLoading = false,
                            data = starList.toMap(tagMap)
                        )
                    }
                } else {
                    _stateFlow.update {
                        it.copy(
                            starCount = starList.size,
                            isLoading = false,
                            data = starList.filter { it.matches(searchKey) }.toMap(tagMap)
                        )
                    }
                }
            }.collect()
        }

        // 处理 默认 和 更新 两个 tag
        viewModelScope.launch {
            combine(
                settingPreferences.isShowUpdateInStar.flow().distinctUntilChanged()
                    .stateIn(viewModelScope),
                cartoonTagDao.flowAll().distinctUntilChanged().map {
                    it.sortedBy { it.order }
                }.stateIn(viewModelScope)
            ) { showUpdate, tabs ->
                val ts = tabs.asSequence().filter {
                    it != UPDATE_TAG && it != DEFAULT_TAG
                }.sortedBy {
                    it.order
                }.toMutableList()
                ts.add(0, DEFAULT_TAG)
                if (showUpdate) {
                    ts.add(0, UPDATE_TAG)
                }
                _stateFlow.update {
                    it.copy(
                        tabs = ts,
                        curTab = if (!ts.contains(it.curTab)) {
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
        _stateFlow.update {
            val selection = it.selection
            it.copy(dialog = DialogState.ChangeTag(selection, tagMapFlow.value))
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
        val all = arrayListOf<CartoonStar>()
        all.addAll(this)
        map[DEFAULT_TAG] = all
        return map
    }

}