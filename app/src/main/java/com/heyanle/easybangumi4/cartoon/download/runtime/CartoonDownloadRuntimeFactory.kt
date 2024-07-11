package com.heyanle.easybangumi4.cartoon.download.runtime

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.download.step.BaseStep
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadRuntimeFactory {

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
                                break
                            }
                            cartoonDownloadRuntime.dispatchStateToBus()
                            val stepName = stepChain[cartoonDownloadRuntime.stepIndex]
                            val step = Inject.get<BaseStep>(stepName)
                            step.invoke()
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
        return runtime

    }

}