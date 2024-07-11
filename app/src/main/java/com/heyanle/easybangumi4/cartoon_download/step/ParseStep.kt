package com.heyanle.easybangumi4.cartoon_download.step

import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadRuntimeFactory
import com.heyanle.easybangumi4.cartoon_download.entity.CartoonDownloadRuntime
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.source.bundle.SourceBundle
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
object ParseStep: BaseStep {

    const val NAME = "parse"

    private val singleDispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + singleDispatcher)



    override fun invoke() {
        val runtime = CartoonDownloadRuntimeFactory.runtimeLocal.get()
            ?: throw IllegalStateException("runtime is null")
        runtime.state = 1
        runtime.getDownloadInfo().process.value = -1f
        runtime.getDownloadInfo().status.value = stringRes(com.heyanle.easy_i18n.R.string.waiting)
        runtime.getDownloadInfo().subStatus.value = ""

        val sourceStateCase: SourceStateCase = Inject.get()
        val bundle = sourceStateCase.stateFlowBundle().value ?: throw IllegalStateException("bundle is null")
        val source = runtime.req.fromCartoonInfo.source
        val playComponent = bundle.play(source) ?: throw IllegalStateException("playComponent is null")


        val countDownLatch = CountDownLatch(1)
        scope.launch {
            if (runtime.needCancel()) {
                countDownLatch.countDown()
                return@launch
            }
            runtime.parseResult = playComponent.getPlayInfo(
                runtime.req.fromCartoonInfo.toSummary(),
                runtime.req.fromPlayLine,
                runtime.req.fromEpisode,
            )
            countDownLatch.countDown()
        }
        runtime.countDownLatch = countDownLatch
        countDownLatch.await(10, TimeUnit.SECONDS)
        if (runtime.needCancel()) {
            return
        }
        val result = runtime.parseResult
        if(result == null){
            runtime.error(IllegalStateException("parse timeout"), "parse timeout")
            return
        }
        result?.error {
            runtime.error(it.throwable, it.throwable.message ?: stringRes(com.heyanle.easy_i18n.R.string.source_error))
            return
        }?.complete {
            runtime.playerInfo = it.data
            runtime.stepCompletely()
        }



    }

    override fun cancel(runtime: CartoonDownloadRuntime) {
        runtime.countDownLatch?.countDown()
    }
}