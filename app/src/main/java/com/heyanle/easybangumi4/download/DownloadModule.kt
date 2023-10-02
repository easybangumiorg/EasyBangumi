package com.heyanle.easybangumi4.download

import android.app.Application
import com.heyanle.easybangumi4.download.step.AriaStep
import com.heyanle.easybangumi4.download.step.BaseStep
import com.heyanle.easybangumi4.download.step.ParseStep
import com.heyanle.easybangumi4.download.step.TranscodeStep
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.InjektionException
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by heyanlin on 2023/10/2.
 */
class DownloadModule(
    private val application: Application
) : InjektModule {

    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            LocalCartoonController(application)
        }

        addSingletonFactory {
            DownloadController(application, get())
        }

        addSingletonFactory {
            DownloadDispatcher(get(), get(), get())
        }

        addScopedPerKeyFactory<BaseStep, String> {
            when(it){
                ParseStep.NAME -> ParseStep(get(), get(), get())
                AriaStep.NAME -> AriaStep(get(), get())
                TranscodeStep.NAME -> TranscodeStep(application, get())
                else -> throw InjektionException("No registered BaseStep with ${it}")
            }
        }
    }
}