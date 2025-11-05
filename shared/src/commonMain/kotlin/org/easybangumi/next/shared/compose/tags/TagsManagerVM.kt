package org.easybangumi.next.shared.compose.tags

import org.easybangumi.next.shared.data.cartoon.CartoonTag
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.foundation.view_model.StateViewModel

/**
 * Created by heyanlin on 2025/11/5.
 */
class TagsManagerVM: StateViewModel<TagsManagerVM.State>(State()) {

    data class State(
        val tagList: List<CartoonTag> = emptyList(),
        val dialog: Dialog? = null,
    )

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


}