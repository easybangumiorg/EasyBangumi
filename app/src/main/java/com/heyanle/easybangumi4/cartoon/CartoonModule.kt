package com.heyanle.easybangumi4.cartoon

import android.app.Application
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by HeYanLe on 2023/8/13 17:29.
 * https://github.com/heyanLE
 */
class CartoonModule(
    private val application: Application
) : InjektModule {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            CartoonNetworkDataSource(get())
        }

        addScopedFactory {
            CartoonRepository(get(), get(), get(), get())
        }
    }
}