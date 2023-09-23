package com.heyanle.easybangumi4.download

import android.app.Application
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by HeYanLe on 2023/9/17 18:38.
 * https://github.com/heyanLE
 */
class DownloadModule(
    private val application: Application
) : InjektModule {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            AriaWrap(get(), get())
        }
        addSingletonFactory {
            ParseWrap(get(), get(), get())
        }

        addSingletonFactory {
            TranscodeWrap(application, get(), get())
        }

        addSingletonFactory {
            BaseDownloadController(application)
        }

        addSingletonFactory {
            DownloadController(application, get(), get(), get(), get())
        }

        addSingletonFactory {
            DownloadBus()
        }

    }
}