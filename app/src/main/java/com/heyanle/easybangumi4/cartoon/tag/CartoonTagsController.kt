package com.heyanle.easybangumi4.cartoon.tag

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


    val tagsList = cartoonTagDao.flowAll().distinctUntilChanged()
        .map {tagsList ->
            val res = tagsList.toMutableList()
            var findAll = false
            tagsList.forEach {
                if (it.id == ALL_TAG_ID) {
                    findAll = true
                }
            }
            if (!findAll) {
                res.add(CartoonTag(ALL_TAG_ID, stringRes(R.string.all_word), -1))
            }
            res
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
        CartoonTagsController.ALL_TAG_ID -> stringRes(R.string.all_word)
        CartoonTagsController.UPDATE_TAG_ID -> stringRes(R.string.update)
        else -> label
    }
}

fun CartoonTag.isUpdate(): Boolean{
    return false
}

fun CartoonTag.isALL(): Boolean{
    return id == CartoonTagsController.ALL_TAG_ID
}

fun CartoonTag.isInner(): Boolean {
    return isALL()
}