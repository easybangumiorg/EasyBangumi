package com.heyanle.easybangumi4.getter

import android.app.Application
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by heyanlin on 2023/10/30.
 */
class GetterModule(
    private val application: Application
) : InjektModule {

    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            CartoonInfoGetter(get())
        }
        addSingletonFactory {
            DownloadItemGetter(get())
        }
        addSingletonFactory {
            ExtensionGetter(get())
        }
        addSingletonFactory {
            SourceStateGetter(get())
        }
        addSingletonFactory {
            LocalCartoonGetter(get())
        }
    }
}