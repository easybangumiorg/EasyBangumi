package com.heyanle.easy_extension

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.io.File
import java.util.concurrent.Executors

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

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    fun init(){

    }


    fun newFileExtension(
        name: String,
    ){


    }

}