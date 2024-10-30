package com.heyanle.easybangumi4.ui.extension_push.from_url

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Created by heyanlin on 2024/10/29.
 */
class ExtensionFromUrlViewModel: ViewModel() {

    data class State(
        val text: String = "",
        val dialog: Dialog? = null,
    )
    private val _state = MutableStateFlow<State>(State())
    val state = _state.asStateFlow()

    private val extensionController: ExtensionController by Inject.injectLazy()

    sealed class Dialog {
        class PushingDialog(
            val loadingMsg: String
        ): Dialog()

    }
    fun onTextChange(text: String) {
        _state.update {
            it.copy(text = text)
        }
    }

    fun onPush() {
        // step 1 检查输入

        // step 2 下载

        // step 3
    }

    fun cancel() {

    }

    private fun dispatchLoading(msg: String) {
        _state.update {
            it.copy(
                dialog = Dialog.PushingDialog(msg)
            )
        }
    }


}