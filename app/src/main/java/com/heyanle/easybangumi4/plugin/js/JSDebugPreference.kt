package com.heyanle.easybangumi4.plugin.js

import android.content.Context
import android.net.Uri
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.cartoon.story.local.LocalCartoonPreference
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProvider
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionCryLoader
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.utils.PackageHelper
import com.heyanle.easybangumi4.utils.aesDecryptTo
import com.heyanle.easybangumi4.utils.aesEncryptTo
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.stringRes
import com.hippo.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Throws


/**
 * Created by HeYanLe on 2024/11/3 17:28.
 * https://github.com/heyanLE
 */

class JSDebugPreference(
    private val context: Context,
    private val preferenceStore: PreferenceStore,
    private val localCartoonPreference: LocalCartoonPreference,
) {

    // Developer

    // 是否加载内置 js 扩展
    val needLoadInnerJsExtension = preferenceStore.getBoolean("load_inner_js_extension", false)
    val innerJsExtension = preferenceStore.getString("inner_js_extension", "")

    private val cacheFolder = context.getCachePath("JSDebug")
    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    data class State(
        val loading: Boolean = true,
    )
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun encryptJS(){

        scope.launch {
            _state.update {
                it.copy(
                    loading = true
                )
            }

            val js = chooseJS()
            if (js == null) {
                _state.update {
                    it.copy(
                        loading = false
                    )
                }
                stringRes(com.heyanle.easy_i18n.R.string.no_document).moeDialog()
                return@launch
            }

            val encryptJs = createEncryptJS()
            if(encryptJs == null){
                _state.update {
                    it.copy(
                        loading = false
                    )
                }
                stringRes(com.heyanle.easy_i18n.R.string.no_document).moeDialog()
                return@launch
            }
            try {

                val jsUniFile = UniFile.fromUri(context, js) ?: throw Throwable("jsUniFile is null")
                val encryptJsUniFile = UniFile.fromUri(context, encryptJs) ?: throw Throwable("jsUniFile is null")

                val folder = File(cacheFolder)
                folder.mkdirs()

                val cacheSourceFile = File(cacheFolder, "encrypt.js")
                val cacheTempFile = File(cacheFolder, "encrypt.ebg.jsc.temp")
                val cacheFile = File(cacheFolder, "encrypt.ebg.jsc")

                jsUniFile.openInputStream().use { input ->
                    cacheSourceFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                cacheTempFile.aesEncryptTo(cacheTempFile, PackageHelper.appSignature, JSExtensionCryLoader.CHUNK_SIZE)
                if (!cacheTempFile.exists() || cacheTempFile.length() <= 0L) {
                    throw Throwable("encrypt failed")
                }

                cacheFile.delete()
                cacheFile.createNewFile()
                cacheTempFile.inputStream().buffered().use { i ->
                    cacheFile.outputStream().buffered().use { o ->
                        o.bufferedWriter().let {
                            it.write(JSExtensionCryLoader.FIRST_LINE_MARK)
                            it.write("\n")
                        }
                        i.copyTo(o)
                    }
                }

                encryptJsUniFile.openOutputStream().use { output ->
                    cacheFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }

            }catch (e: Throwable) {
                e.printStackTrace()
                e.message?.moeDialog()
                _state.update {
                    it.copy(
                        loading = false
                    )
                }
            }



        }

    }


    private suspend fun chooseJS(): Uri? {
        return suspendCoroutine { con ->
            LauncherBus.current?.getJsFile {
                con.resume(it)
            }
        }
    }

    private suspend fun createEncryptJS(): Uri? {
        val req = LauncherBus.CreateDocumentReq(
            "text/javascript",
            "extension${JsExtensionProvider.EXTENSION_CRY_SUFFIX}",
            localCartoonPreference.localUri.value
        )
        return suspendCoroutine { con ->
            LauncherBus.current?.createDocument(req) {
                con.resume(it)
            }
        }
    }

}