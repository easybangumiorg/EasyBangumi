package com.heyanle.easybangumi4.plugin.source.json

import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.ComponentWrapper
import com.heyanle.easybangumi4.plugin.api.component.page.PageComponent
import com.heyanle.easybangumi4.plugin.api.component.page.SourcePage
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.withResult
import kotlinx.coroutines.Dispatchers

class JsonPageComponent(
    private val jsonSource: JsonSource,
    private val executor: JsonRuleExecutor,
) : ComponentWrapper(), PageComponent {

    init {
        innerSource = jsonSource
    }

    override fun getPages(): List<SourcePage> {
        return jsonSource.rule.pages.map { page ->
            val load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>> = { pageKey ->
                withResult(Dispatchers.IO) {
                    executor.loadList(page.list, pageKey)
                }
            }
            if (page.showCover) {
                SourcePage.SingleCartoonPage.WithCover(
                    label = page.label,
                    firstKey = { executor.firstPage(page.list) },
                    load = load,
                )
            } else {
                SourcePage.SingleCartoonPage.WithoutCover(
                    label = page.label,
                    firstKey = { executor.firstPage(page.list) },
                    load = load,
                )
            }
        }
    }
}
