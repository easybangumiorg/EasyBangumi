package com.heyanle.easybangumi4.cartoon.tags

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonTagDao
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Created by heyanlin on 2023/11/2.
 */
class CartoonTagsController(
    private val settingPreferences: SettingPreferences,
    private val cartoonTagDao: CartoonTagDao
) {

    companion object {
        const val ALL_TAG_ID = -1
        const val UPDATE_TAG_ID = -2
    }


    val tagsList = combine(
        settingPreferences.isShowUpdateInStar.flow().distinctUntilChanged(),
        cartoonTagDao.flowAll().distinctUntilChanged()
    ) { isShowUpdate, tagsList ->
        val res = tagsList.toMutableList()
        var findAll = false
        var findUpdate = false
        tagsList.forEach {
            if (it.id == ALL_TAG_ID) {
                findAll = true
            }
            if (it.id == UPDATE_TAG_ID) {
                findUpdate = true
            }
        }
        if (!findAll) {
            res.add(CartoonTag(ALL_TAG_ID, stringRes(R.string.all_word), -1))
        }
        if (!findUpdate && isShowUpdate) {
            res.add(CartoonTag(UPDATE_TAG_ID, stringRes(R.string.update), -2))
        }
        if (!isShowUpdate) {
            res.filter { it.id != UPDATE_TAG_ID }
        } else {
            res
        }
    }


    suspend fun refresh(tags: List<CartoonTag>) {
        cartoonTagDao.updateAll(tags)
    }

    suspend fun remove(cartoonTag: CartoonTag){
        cartoonTagDao.delete(cartoonTag)
    }


}

fun CartoonTag.tagLabel(): String {
    return when (id) {
        CartoonTagsController.ALL_TAG_ID -> stringRes(R.string.default_word)
        CartoonTagsController.UPDATE_TAG_ID -> stringRes(R.string.update)
        else -> label
    }
}

fun CartoonTag.isUpdate(): Boolean{
    return id == CartoonTagsController.UPDATE_TAG_ID
}

fun CartoonTag.isALL(): Boolean{
    return id == CartoonTagsController.ALL_TAG_ID
}