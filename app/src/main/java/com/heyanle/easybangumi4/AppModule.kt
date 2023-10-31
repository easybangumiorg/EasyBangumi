package com.heyanle.easybangumi4

import android.app.Application
import com.google.gson.Gson
import com.heyanle.easybangumi4.source.SourceController
import com.heyanle.easybangumi4.theme.EasyThemeController
import com.heyanle.easybangumi4.source.CartoonUpdateController
import com.heyanle.easybangumi4.utils.MoshiArrayListJsonAdapter
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Created by heyanlin on 2023/10/30.
 */

class RootModule(
    private val application: Application,
): InjektModule {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            Gson()
        }
        addSingletonFactory {
            Moshi.Builder()
                .add(MoshiArrayListJsonAdapter.FACTORY)
                .addLast(KotlinJsonAdapterFactory())
                .build()
        }
    }
}

class ControllerModule(
    private val application: Application
) : InjektModule {

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            application
        }
        addSingletonFactory {
            EasyThemeController(get())
        }

        addSingletonFactory {
            SourceController(get(), get(), get())
        }
        addSingletonFactory {
            CartoonUpdateController(get(), get())
        }
        addSingletonFactory {
            CartoonPlayingControllerOld(
                get(), get(), get(), get(), get()
            )
        }
    }
}