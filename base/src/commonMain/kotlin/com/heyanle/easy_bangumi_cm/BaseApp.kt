package com.heyanle.easy_bangumi_cm

import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Created by heyanlin on 2024/12/3.
 */
object BaseApp {

    fun init(baseFactory: BaseFactory) {
        startKoin {
            modules(
                module {
                    single(definition = baseFactory.makePathProvider)
                    single(definition = baseFactory.makeCoroutineProvider)
                    single(definition = baseFactory.makeLogger)
                    single(definition = baseFactory.makePreferenceStore)
                    single(definition = baseFactory.makePlatform)
                }
            )
        }
    }

}