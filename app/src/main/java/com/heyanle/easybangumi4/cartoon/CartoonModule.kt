package com.heyanle.easybangumi4.cartoon

import android.app.Application
import com.heyanle.easybangumi4.cartoon.repository.CartoonNetworkDataSource
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.cartoon.repository.db.AppDatabase
import com.heyanle.easybangumi4.cartoon.repository.db.CacheDatabase
import com.heyanle.easybangumi4.source.CartoonUpdateController
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
            AppDatabase.build(application)
        }

        addSingletonFactory {
            CacheDatabase.build(application)
        }

        addSingletonFactory {
            get<AppDatabase>().cartoonHistory
        }

        addSingletonFactory {
            get<AppDatabase>().cartoonStar
        }

        addSingletonFactory {
            get<AppDatabase>().cartoonTag
        }

        addSingletonFactory {
            get<AppDatabase>().searchHistory
        }

        addSingletonFactory {
            get<CacheDatabase>().cartoonInfo
        }



        addSingletonFactory {
            CartoonNetworkDataSource(get())
        }

        addSingletonFactory {
            CartoonRepository(get(), get(), get(), get())
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