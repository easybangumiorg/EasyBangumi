package com.heyanle.easybangumi4.case

import android.app.Application
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by heyanlin on 2023/10/30.
 */
class CaseModule(
    private val application: Application
) : InjektModule {

    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            CartoonInfoCase(get())
        }
        addSingletonFactory {
            CartoonDownloadCase(get(), get())
        }
        addSingletonFactory {
            ExtensionCase(get())
        }
        addSingletonFactory {
            SourceStateCase(get())
        }
        addSingletonFactory {
            LocalCartoonCase(get())
        }
    }
}