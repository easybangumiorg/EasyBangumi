package com.heyanle.easybangumi4.ui.source_push

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.plugin.source.PluginV3
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.plugin.source.push.SourcePushController
import com.heyanle.easybangumi4.plugin.source.push.SourcePushTask
import com.heyanle.easybangumi4.ui.common.moeDialogAlert
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Created by HeYanLe on 2024/10/27 14:27.
 * https://github.com/heyanLE
 */

class SourcePushViewModel: ViewModel() {

    companion object {

    }

    private val sourcePushController: SourcePushController by Inject.injectLazy()
    private val sourceController: SourceController by Inject.injectLazy()

    data class State (
        val textMap: Map<SourcePushType, String> = mapOf(),
        val currentType: SourcePushType = SourcePushType.FromFileUrl,
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
            sourcePushController.state.collectLatest { pushState ->
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

    fun changeType(type: SourcePushType){
        _state.update {
            it.copy(
                currentType = type
            )
        }
    }

    fun push(){
        val text = state.value.textMap[state.value.currentType] ?: return
        sourcePushController.push(
            SourcePushTask.Param(
                state.value.currentType.identify,
                text
            )
        )
    }

    fun chooseJSFile(){
        LauncherBus.current?.getJsFile { uri ->
            if (uri == null) {
                stringRes(com.heyanle.easy_i18n.R.string.no_document).moeDialogAlert()
                return@getJsFile
            }

            viewModelScope.launch {
                try {
                    val uniFile = UniFile.fromUri(APP, uri)
                    if (uniFile == null) {
                        stringRes(com.heyanle.easy_i18n.R.string.no_document).moeDialogAlert()
                        return@launch
                    }
                    val cacheFolder = File(APP.getCachePath("source_v3_import"))
                    cacheFolder.mkdirs()
                    val cacheFileName = uniFile.name
                        ?.takeIf { it.endsWith(PluginV3.JS_SOURCE_SUFFIX) }
                        ?: "source${PluginV3.JS_SOURCE_SUFFIX}"
                    val cacheFile = File(cacheFolder, cacheFileName)
                    uniFile.openInputStream().use { input ->
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val res = sourceController.appendOrUpdateSource(cacheFile)
                    res.onOK {
                        stringRes(com.heyanle.easy_i18n.R.string.extension_push_completely).moeSnackBar()
                    }.onError {
                        (it.throwable?.message ?: it.errorMsg).moeDialogAlert(
                            title = stringRes(com.heyanle.easy_i18n.R.string.extension_push_error)
                        )
                    }
                } catch (e: Throwable) {
                    (e.message ?: stringRes(com.heyanle.easy_i18n.R.string.load_error)).moeDialogAlert(
                        title = stringRes(com.heyanle.easy_i18n.R.string.extension_push_error)
                    )
                }

            }
        }
    }

    fun cleanErrorOrCompletely(){
        sourcePushController.cleanErrorOrCompletely()
    }

    fun cancelCurrent(){
        sourcePushController.cancelCurrent()
    }



}


