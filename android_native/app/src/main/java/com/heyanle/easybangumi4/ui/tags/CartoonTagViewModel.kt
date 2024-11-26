package com.heyanle.easybangumi4.ui.tags

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonTagOld
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.cartoon.star.CartoonTagsController
import com.heyanle.easybangumi4.cartoon.star.isALL
import com.heyanle.easybangumi4.cartoon.star.isUpdate
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/8/6 16:51.
 * https://github.com/heyanLE
 */
class CartoonTagViewModel : ViewModel() {

    var dialog by mutableStateOf<Dialog?>(null)
        private set
    var tags by mutableStateOf<List<CartoonTag>>(emptyList())
        private set

    private val cartoonInfoDao: CartoonInfoDao by Inject.injectLazy()
    private val cartoonStarController: CartoonStarController by Inject.injectLazy()

    sealed class Dialog {

        class Delete(
            val deleteTag: CartoonTag
        ) : Dialog()

        class DeleteSelection(
            val selection: List<CartoonTag>
        ) : Dialog()

        class Rename(
            val renameTag: CartoonTag
        ) : Dialog()

        data object Create : Dialog()
    }

    init {
        viewModelScope.launch {
            cartoonStarController.cartoonTagFlow.map { it.tagList }.collectLatest {
                tags = it.sortedBy { it.order }
            }
        }
    }

    fun move(from: Int, to: Int) {
        tags = tags.toMutableList().apply {
            add(to, removeAt(from))
        }
    }

    fun onDragEnd() {
        viewModelScope.launch {
            val ts = tags.mapIndexed { index, cartoonTag ->
                cartoonTag.copy(order = index)
            }
            cartoonStarController.modifier(ts)
            //cartoonTagDao.updateAll(ts)
        }
    }

    fun dialogDelete(cartoonTag: CartoonTag) {
        dialog = Dialog.Delete(cartoonTag)
    }

    fun dialogRename(cartoonTag: CartoonTag) {
        if (!cartoonTag.isInner) {
            dialog = Dialog.Rename(cartoonTag)
        }
    }

    fun dialogCreate() {
        dialog = Dialog.Create
    }


    fun dialogDismiss() {
        dialog = null
    }

    fun onDelete(cartoonTag: CartoonTag) {
        if (cartoonTag.isInner) {
            return
        }
        viewModelScope.launch {
            cartoonStarController.remove(cartoonTag)

        }
    }


    fun onSetShow(cartoonTag: CartoonTag, show: Boolean) {
        if (!show && !tags.any {
                it.label != cartoonTag.label && it.show
            }) {
            return
        }
        viewModelScope.launch {
            cartoonStarController.modifier(
                cartoonTag.copy(
                    show = show
                )
            )
        }
    }

    fun onRename(cartoonTag: CartoonTag, label: String) {
        if (cartoonTag.isInner) {
            return
        }
        if (tags.any {
                it != cartoonTag && it.label == label
            }) {
            return
        }
        viewModelScope.launch {
            cartoonStarController.modifier(
                cartoonTag.copy(
                    label = label
                )
            )
            cartoonInfoDao.renameTag(cartoonTag.label, label)

        }
    }

    fun onCreate(label: String) {
        viewModelScope.launch {
            cartoonStarController.insert(label)
        }
    }

}