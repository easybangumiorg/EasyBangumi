package com.heyanle.easybangumi4.ui.extension_push

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Created by HeYanLe on 2024/10/27 14:27.
 * https://github.com/heyanLE
 */

class ExtensionPushViewModel: ViewModel() {

    private val extensionController: ExtensionController by Inject.injectLazy()

    data class State(
        val loading: Boolean = false,
        val sourceType: Int = 0, // 0: js 文件连接，一行一个, 1: js 代码, 2: js 仓库连接

        val editTextI: String = "",
        val editTextII: String = "",
        val editTextIII: String = "",
    ){
        fun getText(): String{
            return when(sourceType){
                0 -> editTextI
                1 -> editTextII
                2 -> editTextIII
                else -> ""
            }
        }
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun chooseJSFile(){
        LauncherBus.current?.getJsFile { uri ->
            if (uri == null) {
                stringRes(com.heyanle.easy_i18n.R.string.no_document).moeDialog()
                return@getJsFile
            }

            viewModelScope.launch {
                val ex = extensionController.appendExtensionUri(uri, ExtensionInfo.TYPE_JS_FILE)
                if (ex == null) {
                    stringRes(com.heyanle.easy_i18n.R.string.extension_push_completely).moeSnackBar()
                } else {
                    (ex.message?: stringRes(com.heyanle.easy_i18n.R.string.load_error)).moeDialog(
                        title = stringRes(com.heyanle.easy_i18n.R.string.extension_push_error)
                    )
                }
            }
        }
    }

    fun onSourceTypeChange(type: Int){
        _state.update {
            it.copy(sourceType = type)
        }
    }

    fun onTextChange(type: Int, text: String) {
        _state.update {
            when(type){
                0 -> it.copy(editTextI = text)
                1 -> it.copy(editTextII = text)
                2 -> it.copy(editTextIII = text)
                else -> it
            }
        }
    }

}