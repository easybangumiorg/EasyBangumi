package com.heyanle.easybangumi4.cartoon

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.cartoon.story.download.req.CartoonDownloadReqController
import com.heyanle.easybangumi4.cartoon.story.local.CartoonLocalController
import com.heyanle.easybangumi4.cartoon.story.local.LocalCartoonPreference
import com.heyanle.easybangumi4.cartoon.repository.CartoonNetworkDataSource
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.cartoon.repository.db.CartoonDatabase
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.cartoon.star.CartoonTagsController
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryControllerImpl
import com.heyanle.easybangumi4.cartoon.story.download.action.AriaAction
import com.heyanle.easybangumi4.cartoon.story.download.action.BaseAction
import com.heyanle.easybangumi4.cartoon.story.download.action.CopyAndNfoAction
import com.heyanle.easybangumi4.cartoon.story.download.action.ParseAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TranscodeAction
import com.heyanle.easybangumi4.cartoon.story.download.action.TransformerAction
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadDispatcher
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addAlias
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

    @OptIn(UnstableApi::class)
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


        addPerKeyFactory<BaseAction, String> {
            when(it) {
                ParseAction.NAME -> ParseAction(get())
                AriaAction.NAME -> AriaAction(application, get())
                TranscodeAction.NAME -> TranscodeAction(application)
                TransformerAction.NAME -> TransformerAction(get(), get())
                CopyAndNfoAction.NAME -> CopyAndNfoAction()
                else -> throw IllegalArgumentException("not found action: $it")
            }
        }

        addSingletonFactory {
            CartoonDownloadDispatcher(get(), get())
        }


        addSingletonFactory {
            CartoonDownloadPreference(get())
        }


        addSingletonFactory {
            CartoonDownloadReqController(get())
        }

        // local

        addSingletonFactory {
            CartoonLocalController(get())
        }

        addSingletonFactory {
            LocalCartoonPreference(get(), get(), get())
        }



        addSingletonFactory {
            CartoonStoryControllerImpl(get(), get(), get())
        }

        addAlias<CartoonStoryControllerImpl, CartoonStoryController>()
    }
}