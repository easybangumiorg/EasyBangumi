package com.heyanle.easybangumi4.source

import com.heyanle.extension_load.ExtensionController
import com.heyanle.extension_load.model.Extension
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/22 21:59.
 * https://github.com/heyanLE
 */
object ExtensionSource {


    val scope = MainScope()

    fun init() {
        scope.launch {
            ExtensionController.installedExtensionsFlow.collectLatest { state ->
                (state as? ExtensionController.ExtensionState.Extensions)?.let { extensions ->

                    extensions.extensions.filterIsInstance<Extension.Installed>()
                        .forEach {
                            SourceLibraryMaster.refreshSources(it.sources)
                        }
                }
            }
        }
    }

}