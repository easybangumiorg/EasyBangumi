package com.heyanle.easy_bangumi_cm

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.quickJs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Created by heyanlin on 2024/12/11.
 */
class JSRuntime(
    val dispatcher: CoroutineDispatcher,
) {

    private val quickJs = QuickJs.create(dispatcher)

    suspend fun run(){

    }

    fun gc(){
        quickJs.gc()
    }

}