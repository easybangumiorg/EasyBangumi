package com.heyanle.easybangumi4.cartoon.story.download.action

import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Created by heyanle on 2024/8/4.
 * https://github.com/heyanLE
 */
class ParseAction(
    private val sourceStateCase: SourceStateCase
) : BaseAction {

    companion object {
        const val NAME = "ParseAction"
    }

    private val mainScope = MainScope()


    override suspend fun canResume(cartoonDownloadReq: CartoonDownloadReq): Boolean {
        return false
    }

    override suspend fun toggle(cartoonDownloadRuntime: CartoonDownloadRuntime): Boolean {
        return false
    }

    override fun push(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        val job = mainScope.launch {
            cartoonDownloadRuntime.dispatchToBus(
                -1f,
                stringRes(com.heyanle.easy_i18n.R.string.waiting_parse)
            )
            try {
                innerInvoke(cartoonDownloadRuntime)
                if (cartoonDownloadRuntime.isCanceled()) {
                    return@launch
                }
            } catch (e: Throwable) {
                cartoonDownloadRuntime.error(e, e.message)
            }
        }
        synchronized(cartoonDownloadRuntime.lock) {
            cartoonDownloadRuntime.parseJob = job
        }
    }

    private suspend fun innerInvoke(
        cartoonDownloadRuntime: CartoonDownloadRuntime
    ) {
        val bundle =
            sourceStateCase.stateFlowBundle().value ?: throw IllegalStateException("bundle is null")
        val source = cartoonDownloadRuntime.req.fromCartoonInfo.source
        val playComponent =
            bundle.play(source) ?: throw IllegalStateException("playComponent is null")

        cartoonDownloadRuntime.dispatchToBus(
            -1f,
            stringRes(com.heyanle.easy_i18n.R.string.parsing)
        )

        val result = withTimeoutOrNull(50000) {
            playComponent.getPlayInfo(
                cartoonDownloadRuntime.req.fromCartoonInfo.toSummary(),
                cartoonDownloadRuntime.req.fromPlayLine,
                cartoonDownloadRuntime.req.fromEpisode,
            )
        } ?: throw IllegalStateException("parse timeout")
        if (cartoonDownloadRuntime.isCanceled()) {
            return
        }
        result.error {
            synchronized(cartoonDownloadRuntime.lock) {
                if (cartoonDownloadRuntime.retryTime < 3) {
                    cartoonDownloadRuntime.retryTime++
                    cartoonDownloadRuntime.stepRetry()
                    return
                } else {
                    cartoonDownloadRuntime.error(
                        it.throwable,
                        it.throwable.message ?: stringRes(com.heyanle.easy_i18n.R.string.source_error)
                    )
                }
            }
            return
        }.complete {
            it.data.uri.logi("ParseAction")
            synchronized(cartoonDownloadRuntime.lock) {
                if (it.data.uri.isEmpty() && cartoonDownloadRuntime.retryTime < 3) {
                    cartoonDownloadRuntime.retryTime++
                    cartoonDownloadRuntime.stepRetry()
                    return
                }
                cartoonDownloadRuntime.playerInfo = it.data
                cartoonDownloadRuntime.stepCompletely(this)
            }
        }
    }

    override fun onCancel(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        cartoonDownloadRuntime.parseJob?.cancel()
    }
}