package com.heyanle.easybangumi4.cartoon

import android.app.Application
import com.heyanle.easybangumi4.cartoon.repository.CartoonNetworkDataSource
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.cartoon.repository.db.CartoonDatabase
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.cartoon.tag.CartoonTagsController
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
class CartoonModule(
    private val application: Application
) : InjektModule {

    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            CartoonDatabase.build(application)
        }

        addSingletonFactory {
            get<CartoonDatabase>().cartoonInfo
        }

        addSingletonFactory {
            get<CartoonDatabase>().cartoonTag
        }

        addSingletonFactory {
            get<CartoonDatabase>().searchHistory
        }

        addSingletonFactory {
            CartoonNetworkDataSource(get())
        }

        addSingletonFactory {
            CartoonRepository(get(), get(), get(), get())
        }

        addSingletonFactory {
            CartoonStarController(get(), get())
        }

        addSingletonFactory {
            CartoonTagsController(get(), get())
        }
    }
}