package com.heyanle.easybangumi4.cartoon.star

import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.ui.common.proc.FilterState
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

/**
 * Created by heyanle on 2024/7/14.
 * https://github.com/heyanLE
 */
class CartoonStarController(
    private val cartoonInfoDao: CartoonInfoDao,
    private val androidPreferenceStore: AndroidPreferenceStore,
    private val jsonFileProvider: JsonFileProvider,
) {

    private val dispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    data class StarState(
        val tagList: List<CartoonTag> = emptyList(),
        val label2Cartoon: Map<String, List<CartoonInfo>> = emptyMap(),
        val cartoonInfoList : List<CartoonInfo> = emptyList()
    )

    // 1. 保证有内部 tag （全部，本地，更新），如果 tagList 里没有会补充
    // 2. 保证所有 cartoonInfo 里的 tag 都有对应的 CartoonTag，如果 tagList 里没有会补充
    val cartoonTagFlow = combine(
        cartoonInfoDao.flowAllStar().distinctUntilChanged(),
        jsonFileProvider.cartoonTag.flow
    ) { cartoonInfoList, tagListRes ->
        (tagListRes.okOrNull() ?: emptyList())
            .process(cartoonInfoList)
    }.stateIn(scope, SharingStarted.Lazily, StarState())

    fun remove(cartoonTag: CartoonTag) {
        if (cartoonTag.isInner) {
            return
        }

        jsonFileProvider.cartoonTag.update {
            it.filter { it.label != cartoonTag.label }
        }
    }

    fun insert(label: String) {
        jsonFileProvider.cartoonTag.update {
           it + CartoonTag.create(label)
        }
    }

    fun modifier(cartoonTag: CartoonTag) {
        jsonFileProvider.cartoonTag.update {
            var i = it
            if(!it.any { it.label == cartoonTag.label }) {
                i = it + cartoonTag
            }
            if (cartoonTag.isCustomSetting)
                i.map {
                    if (it.label == cartoonTag.label) {
                        cartoonTag
                    } else {
                        it
                    }
                }
            else {
                i.map {
                    when (it.label) {
                        CartoonTag.ALL_TAG_LABEL -> {
                            it.copy(
                                sortId = cartoonTag.sortId,
                                isReverse = cartoonTag.isReverse,
                                filterState = cartoonTag.filterState
                            )
                        }
                        cartoonTag.label -> {
                            cartoonTag
                        }
                        else -> {
                            it
                        }
                    }
                }

            }
        }
    }
    fun modifier(cartoonTag: List<CartoonTag>) {
        jsonFileProvider.cartoonTag.set(cartoonTag)
    }

    private fun List<CartoonTag>.process(
        cartoonInfoList: List<CartoonInfo>
    ) : StarState {


        val oriInnerTag = CartoonTag.innerLabel
        val innerTag = oriInnerTag.toMutableSet()
        // 确保 tag 里有内置 tag 和所有 cartoonInfo 中的 tag
        val label2Tag = HashMap<String, CartoonTag>()

        for (cartoonTag in this) {
            if (innerTag.contains(cartoonTag.label)) {
                innerTag.remove(cartoonTag.label)
            } else if (cartoonTag.isInner) {
                continue
            }
            label2Tag[cartoonTag.label] = cartoonTag
        }
        // 补充缺少的内部 tag
        innerTag.forEach {
            label2Tag[it] = CartoonTag.create(it)
        }

        val allTag = label2Tag[CartoonTag.ALL_TAG_LABEL] ?: CartoonTag.create(CartoonTag.ALL_TAG_LABEL)

        // 打包
        val pack = HashMap<String, MutableList<CartoonInfo>>()

        for (entry in label2Tag.entries) {
            pack[entry.value.label] = arrayListOf()
        }


        for (cartoonInfo in cartoonInfoList) {
            for (tag in cartoonInfo.tagList) {
                // 内部 tag 额外处理
                if (oriInnerTag.contains(tag)) {
                    continue
                }
                val cartoonTag = label2Tag[tag]
                if (cartoonTag != null) {
                    pack[cartoonTag.label]?.add(cartoonInfo)
                }
            }

            // 内部 tag 处理
            pack[CartoonTag.ALL_TAG_LABEL]?.add(cartoonInfo)

            if (cartoonInfo.tagList.isEmpty()) {
                pack[CartoonTag.DEFAULT_TAG_LABEL]?.add(cartoonInfo)
            }


        }

        val res = HashMap<String, List<CartoonInfo>>()

        // 打包完毕，后面是排序，过滤，置顶

        for (entry in pack.entries) {

            val pinList = arrayListOf<CartoonInfo>()
            val normalList = arrayListOf<CartoonInfo>()

            val tag = label2Tag[entry.key] ?: continue

            val sortId = if (tag.isCustomSetting) tag.sortId else allTag.sortId
            val currentSort = CartoonInfoSortFilterConst.sortByList.firstOrNull() {
                it.id == sortId
            } ?: CartoonInfoSortFilterConst.sortByStarTime

            val isSortReverse = if (tag.isCustomSetting) tag.isReverse else allTag.isReverse

            val filterState = if (tag.isCustomSetting) tag.filterState else allTag.filterState

            val onFilter = CartoonInfoSortFilterConst.filterWithList.filter {
                filterState[it.id] == FilterState.STATUS_ON
            }
            val excludeFilter = CartoonInfoSortFilterConst.filterWithList.filter {
                filterState[it.id] == FilterState.STATUS_EXCLUDE
            }

            for (cartoonInfo in entry.value) {
                var check = true
                for (filterWith in onFilter) {
                    if (!filterWith.filter(cartoonInfo)) {
                        check = false
                        break
                    }
                }
                if (!check) {
                    continue
                }
                for (filterWith in excludeFilter) {
                    if (filterWith.filter(cartoonInfo)) {
                        check = false
                        break
                    }
                }
                if (!check) {
                    continue
                }

                if (cartoonInfo.isUp()){
                    pinList.add(cartoonInfo)
                } else {
                    normalList.add(cartoonInfo)
                }
            }

            pinList.sortBy {
                it.upTime
            }

            normalList.sortWith { o1, o2 ->
                val r = currentSort.comparator.compare(o1, o2)
                if (isSortReverse) -r else r
            }

            res[entry.key] = pinList + normalList
        }
        return StarState(
            tagList = label2Tag.values.toList().map {
                if (!it.isInner && !it.isCustomSetting) {
                    it.copy(
                        sortId = allTag.sortId,
                        isReverse = allTag.isReverse,
                        filterState = allTag.filterState
                    )
                } else {
                    it
                }
            }.sortedBy { it.order },
            label2Cartoon = res,
            cartoonInfoList = cartoonInfoList,
        )
    }


}