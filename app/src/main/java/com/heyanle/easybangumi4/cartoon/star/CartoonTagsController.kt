package com.heyanle.easybangumi4.cartoon.star

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonTagOld
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonTagDao
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.utils.stringRes
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
                res.add(CartoonTagOld(ALL_TAG_ID, stringRes(R.string.all_word), -1))
            }
            res
        }


    suspend fun refresh(tags: List<CartoonTagOld>) {
        cartoonTagDao.updateAll(tags)
    }

    suspend fun remove(cartoonTag: CartoonTagOld){
        cartoonTagDao.delete(cartoonTag)
    }


}

fun CartoonTagOld.tagLabel(): String {
    return when (id) {
        CartoonTagsController.ALL_TAG_ID -> stringRes(R.string.all_word)
        CartoonTagsController.UPDATE_TAG_ID -> stringRes(R.string.update)
        else -> label
    }
}

fun CartoonTagOld.isUpdate(): Boolean{
    return false
}

fun CartoonTagOld.isALL(): Boolean{
    return id == CartoonTagsController.ALL_TAG_ID
}

fun CartoonTagOld.isInner(): Boolean {
    return isALL()
}