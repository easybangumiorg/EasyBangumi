package com.heyanle.easy_extension

import android.content.Context

/**
 * Created by heyanlin on 2023/10/24.
 */
class ExtensionController(
    private val context: Context,
    //private val extensionLoader: ExtensionLoader
) {

    sealed class ExtensionState {

        data object None : ExtensionState()

        data object Loading : ExtensionState()

        class Extensions(
            val extensions: List<Extension>
        ) : ExtensionState()
    }

    fun init(){

    }

    fun newFileExtension(){}

}