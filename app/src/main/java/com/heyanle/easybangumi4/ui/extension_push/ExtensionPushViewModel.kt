package com.heyanle.easybangumi4.ui.extension_push

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.push.ExtensionPushController
import com.heyanle.easybangumi4.plugin.extension.push.ExtensionPushTask
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Created by HeYanLe on 2024/10/27 14:27.
 * https://github.com/heyanLE
 */

class ExtensionPushViewModel: ViewModel() {

    companion object {

    }

    private val extensionController: ExtensionController by Inject.injectLazy()
    private val extensionPushController: ExtensionPushController by Inject.injectLazy()

    data class State (
        val textMap: Map<ExtensionPushType, String> = mapOf(),
        val currentType: ExtensionPushType = ExtensionPushType.FromFileUrl,
        val dialog: Dialog? = null
    ){
        val text: String
            get() = textMap[currentType] ?: ""
    }
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    sealed class Dialog {
        data class Loading(val msg: String): Dialog()
        data class ErrorOrCompletely(val msg: String): Dialog()
    }

    init {
        viewModelScope.launch {
            extensionPushController.state.collectLatest { pushState ->
                _state.update {
                    it.copy(
                        dialog =  if (pushState.isDoing) {
                            Dialog.Loading(pushState.loadingMsg)
                        } else if (pushState.isError){
                            Dialog.ErrorOrCompletely(pushState.errorMsg)
                        } else if (pushState.isCompletely){
                            Dialog.ErrorOrCompletely(pushState.completelyMsg)
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    fun changeText(text: String){
        _state.update {
            it.copy(
                textMap = it.textMap.toMutableMap().apply {
                    put(it.currentType, text)
                }
            )
        }
    }

    fun changeType(type: ExtensionPushType){
        _state.update {
            it.copy(
                currentType = type
            )
        }
    }

    fun push(){
        val text = state.value.textMap[state.value.currentType] ?: return
        extensionPushController.push(
            ExtensionPushTask.Param(
                state.value.currentType.identify,
                text
            )
        )
    }

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

    fun cleanErrorOrCompletely(){
        extensionPushController.cleanErrorOrCompletely()
    }

    fun cancelCurrent(){
        extensionPushController.cancelCurrent()
    }



}