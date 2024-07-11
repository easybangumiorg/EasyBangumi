package com.heyanle.easybangumi4.cartoon

import android.app.Application
import com.heyanle.easybangumi4.cartoon.repository.CartoonNetworkDataSource
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.cartoon.repository.db.CartoonDatabase
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.cartoon.tag.CartoonTagsController
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get

/**
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
class CartoonModule(
    private val application: Application
) : InjectModule {

    override fun InjectScope.registerInjectables() {
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

//        addSingletonFactory {
//            CartoonStarControllerOld(get(), get())
//        }

        addSingletonFactory {
            CartoonStarController(get(), get(), get())
        }

        addSingletonFactory {
            CartoonTagsController(get(), get())
        }

        addSingletonFactory {
            CartoonUpdateController(get(), get())
        }
    }
}