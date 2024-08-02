package com.heyanle.easybangumi4.cartoon.story.download_v1.runtime

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download_v1.CartoonDownloadPreference
import com.heyanle.easybangumi4.cartoon.story.download_v1.step.BaseStep
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadRuntimeFactory(
    private val cartoonDownloadPreference: CartoonDownloadPreference
) {

    companion object {
        val runtimeLocal = ThreadLocal<CartoonDownloadRuntime>()
    }

    class CartoonDownloadRunnable(
        val cartoonDownloadRuntime: CartoonDownloadRuntime
    ) : Runnable {
        override fun run() {

            synchronized(cartoonDownloadRuntime.lock) {
                runtimeLocal.set(cartoonDownloadRuntime)
                while (true) {
                    if (cartoonDownloadRuntime.state == 0 || cartoonDownloadRuntime.state == 2) {
                        try {
                            val stepChain = cartoonDownloadRuntime.req.stepChain
                            if (cartoonDownloadRuntime.state == 2){
                                cartoonDownloadRuntime.state = 0
                                cartoonDownloadRuntime.stepIndex ++
                            }
                            if (cartoonDownloadRuntime.stepIndex >= stepChain.size) {
                                cartoonDownloadRuntime.state = 4
                                cartoonDownloadRuntime.dispatchStateToBus()
                                break
                            }
                            cartoonDownloadRuntime.dispatchStateToBus()
                            val stepName = stepChain[cartoonDownloadRuntime.stepIndex]
                            val step = Inject.get<BaseStep>(stepName)
                            cartoonDownloadRuntime.currentStep = step
                            step.invoke()
                            cartoonDownloadRuntime.currentStep = null
                            continue
                        } catch (e: Throwable) {
                            cartoonDownloadRuntime.error = e
                            cartoonDownloadRuntime.errorMsg = e.message ?: ""
                            cartoonDownloadRuntime.state = 3
                            cartoonDownloadRuntime.dispatchStateToBus()
                        }
                    }
                    break
                }
            }
        }
    }

    fun newRuntime(cartoonDownloadReq: CartoonDownloadReq): CartoonDownloadRuntime {
        val runtime = CartoonDownloadRuntime(cartoonDownloadReq)
        runtime.runnable = CartoonDownloadRunnable(runtime)
        runtime.decodeType = cartoonDownloadPreference.downloadEncode.get()
        return runtime

    }

}