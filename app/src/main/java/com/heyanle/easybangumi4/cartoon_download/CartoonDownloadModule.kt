package com.heyanle.easybangumi4.cartoon_download

import android.app.Application
import com.heyanle.easybangumi4.cartoon_download.step.BaseStep
import com.heyanle.easybangumi4.cartoon_download.step.CopyAndNfoStep
import com.heyanle.easybangumi4.cartoon_download.step.ParseStep
import com.heyanle.easybangumi4.cartoon_download.step.TransformerStep
import com.heyanle.easybangumi4.cartoon_local.CartoonLocalController
import com.heyanle.easybangumi4.cartoon_local.LocalCartoonPreference
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addPerKeyFactory
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadModule(
    private val application: Application
) : InjectModule {

    override fun InjectScope.registerInjectables() {
        addPerKeyFactory<BaseStep, String> {
            when(it) {
                ParseStep.NAME -> ParseStep
                TransformerStep.NAME -> TransformerStep
                CopyAndNfoStep.NAME -> CopyAndNfoStep
                else -> throw IllegalArgumentException("not found step: $it")
            }
        }

        addSingletonFactory {
            CartoonDownloadPreference(get())
        }

        addSingletonFactory {
            CartoonDownloadRuntimeFactory()
        }

        addSingletonFactory {
            CartoonDownloadDispatcher(get(), get(), get())
        }

        addSingletonFactory {
            CartoonDownloadController(get())
        }

        addSingletonFactory {
            CartoonLocalController(get())
        }

        addSingletonFactory {
            LocalCartoonPreference(get())
        }
    }
}