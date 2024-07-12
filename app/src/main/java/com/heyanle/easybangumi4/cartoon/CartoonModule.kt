package com.heyanle.easybangumi4.cartoon

import android.app.Application
import com.heyanle.easybangumi4.cartoon.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.cartoon.download.req.CartoonDownloadReqController
import com.heyanle.easybangumi4.cartoon.download.runtime.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.cartoon.download.runtime.CartoonDownloadRuntimeFactory
import com.heyanle.easybangumi4.cartoon.download.step.BaseStep
import com.heyanle.easybangumi4.cartoon.download.step.CopyAndNfoStep
import com.heyanle.easybangumi4.cartoon.download.step.ParseStep
import com.heyanle.easybangumi4.cartoon.download.step.TransformerStep
import com.heyanle.easybangumi4.cartoon.local.CartoonLocalController
import com.heyanle.easybangumi4.cartoon.local.LocalCartoonPreference
import com.heyanle.easybangumi4.cartoon.repository.CartoonNetworkDataSource
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.cartoon.repository.db.CartoonDatabase
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.cartoon.tag.CartoonTagsController
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addPerKeyFactory
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

        // download

        addPerKeyFactory<BaseStep, String> {
            when(it) {
                ParseStep.NAME -> ParseStep
                TransformerStep.NAME -> TransformerStep
                CopyAndNfoStep.NAME -> CopyAndNfoStep
                else -> throw IllegalArgumentException("not found step: $it")
            }
        }

        addSingletonFactory {
            CartoonDownloadPreference(get())
        }

        addSingletonFactory {
            CartoonDownloadRuntimeFactory()
        }

        addSingletonFactory {
            CartoonDownloadDispatcher(get(), get(), get())
        }

        addSingletonFactory {
            CartoonDownloadReqController(application)
        }

        // local

        addSingletonFactory {
            CartoonLocalController(get())
        }

        addSingletonFactory {
            LocalCartoonPreference(get())
        }


        addSingletonFactory {
            CartoonLocalDownloadController(get(), get(), get())
        }
    }
}