package com.heyanle.easybangumi4.cartoon

import android.app.Application
import com.heyanle.easybangumi4.source.CartoonUpdateController
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonPlayingController
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by heyanlin on 2023/10/30.
 */
class CartoonModule(
    private val application: Application
) : InjektModule {

    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            CartoonUpdateController(get(), get())
        }
        addSingletonFactory {
            CartoonPlayingController(
                get(), get(), get(), get(), get()
            )
        }
    }
}