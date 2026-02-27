package org.easybangumi.next.shared.compose.tags

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.cartoon.collection.CartoonCollectionController
import org.easybangumi.next.shared.data.cartoon.CartoonTag
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.koin.core.component.inject

class TagsManagerVM : StateViewModel<TagsManagerVM.State>(State()) {

    private val collectionController: CartoonCollectionController by inject()

    data class State(
        val tagList: List<CartoonTag> = emptyList(),
        val dialog: Dialog? = null,
    )

    sealed class Dialog {
        class Delete(val deleteTag: CartoonTag) : Dialog()
        class Rename(val renameTag: CartoonTag) : Dialog()
        data object Create : Dialog()
    }

    init {
        viewModelScope.launch {
            collectionController.collectionFlow.collectLatest { cs ->
                update {
                    it.copy(tagList = cs.tagList)
                }
            }
        }
    }

    fun move(from: Int, to: Int) {
        update {
            val mutable = it.tagList.toMutableList()
            mutable.add(to, mutable.removeAt(from))
            it.copy(tagList = mutable)
        }
    }

    fun onDragEnd() {
        viewModelScope.launch {
            collectionController.setTagOrder(state.value.tagList)
        }
    }

    fun onCreate(label: String) {
        viewModelScope.launch {
            collectionController.addTag(label)
        }
    }

    fun onRename(tag: CartoonTag, newLabel: String) {
        if (tag.isDefault || tag.isBangumi) return
        viewModelScope.launch {
            collectionController.renameTag(tag, newLabel)
        }
    }

    fun onDelete(tag: CartoonTag) {
        if (tag.isDefault || tag.isBangumi) return
        viewModelScope.launch {
            collectionController.removeTag(tag)
        }
    }

    fun onSetShow(tag: CartoonTag, show: Boolean) {
        viewModelScope.launch {
            collectionController.setTagShow(tag, show)
        }
    }

    fun dialogCreate() { update { it.copy(dialog = Dialog.Create) } }
    fun dialogRename(tag: CartoonTag) {
        if (!tag.isDefault && !tag.isBangumi) {
            update { it.copy(dialog = Dialog.Rename(tag)) }
        }
    }
    fun dialogDelete(tag: CartoonTag) { update { it.copy(dialog = Dialog.Delete(tag)) } }
    fun dialogDismiss() { update { it.copy(dialog = null) } }
}
