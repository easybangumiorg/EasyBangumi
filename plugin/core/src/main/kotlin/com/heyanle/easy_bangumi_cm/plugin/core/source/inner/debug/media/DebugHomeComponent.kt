package com.heyanle.easy_bangumi_cm.plugin.core.source.inner.debug.media

import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.base.withResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentWrapper
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.repository.cartoon.CartoonCover

/**
 * Created by heyanlin on 2024/12/9.
 */
class DebugHomeComponent: ComponentWrapper(), HomeComponent {

    override suspend fun home(): SourceResult<List<HomePage>> {
        return withResult {
            listOf(
                singleHomePageWithCover(),
                groupHomePage()
            )
        }
    }

    private fun singleHomePageWithCover(): HomePage.SingleCartoonPage {
        return HomePage.SingleCartoonPage.WithCover(
            label = "调试单首页",
            firstKey = {1},
            load = {
                withResult {
                    when (it) {
                        1 -> Pair(2, newCartoonCover(1, 10))
                        2 -> Pair(3, newCartoonCover(11, 10))
                        else -> Pair(null, listOf())
                    }

                }
            }
        )
    }

    private fun groupHomePage(): HomePage {
        return HomePage.Group(
            label = "调试组首页",
            loadPage = {
                withResult {
                    listOf(
                        singleHomePageWithCover(),
                        singleHomePageWithCover()
                    )
                }
            }
        )
    }

    private fun newCartoonCover(start: Int, count: Int): List<CartoonCover> {
        val list = mutableListOf<CartoonCover>()
        for (i in 0 until count) {
            list.add(CartoonCover("https://www.baidu.com", source.source, "test ${start + i}", "test", "test"))
        }
        return list
    }
}