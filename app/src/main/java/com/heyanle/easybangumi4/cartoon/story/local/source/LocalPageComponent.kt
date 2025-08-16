package com.heyanle.easybangumi4.cartoon.story.local.source

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.easybangumi4.plugin.source.SourceException
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlin.getValue

/**
 * Created by heyanle on 2025/8/16
 * https://github.com/heyanLE
 */
class LocalPageComponent: ComponentWrapper(), PageComponent {

    val cartoonStoryController: CartoonStoryController by Inject.injectLazy()

    override fun getPages(): List<SourcePage> {
        return PageComponent.NonLabelSinglePage(
            SourcePage.SingleCartoonPage.WithCover(
                label = "",
                firstKey = {1},
                load = {
                    val result = cartoonStoryController.storyItemList.firstOrNull { it !is DataResult.Loading }
                    if (result is DataResult.Ok) {
                        return@WithCover SourceResult.Complete(null to result.data.map {
                            it.cartoonLocalItem.cartoonCover
                        })

                    } else if (result is DataResult.Error) {
                        return@WithCover SourceResult.Error<Pair<Int?, List<CartoonCover>>>(result.throwable ?: SourceException(result.errorMsg))
                    } else {
                        throw IllegalStateException("unexpected result type: $result")
                    }

                }
            )
        )
    }
}