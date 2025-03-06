package com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.DebugInnerSource
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover
import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.base.withResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentWrapper
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeContent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.utils.StringHelper

/**
 * Created by heyanlin on 2025/2/6.
 */
class DebugHomeComponent(
    private val debugInnerSource: DebugInnerSource,
    private val stringHelper: StringHelper,
): HomeComponent, ComponentWrapper() {

    override suspend fun home(): SourceResult<HomeContent> {
        return withResult(CoroutineProvider.io) {
            HomeContent.MultiplePage(
                pageList = listOf(
                    HomePage.Group(
                        "debug 1",
                        loadPage = {
                            withResult (CoroutineProvider.io){
                                listOf(
                                    pageWithCover("page 1"),
                                    pageWithoutCover("page 2"),
                                )
                            }
                        }
                    ),
                    HomePage.Group(
                        "debug 2",
                        loadPage = {
                            withResult (CoroutineProvider.io){
                                listOf(
                                    pageWithCover("page 3"),
                                    pageWithoutCover("page 4"),
                                )
                            }
                        }
                    ),
                )
            )

        }
    }

    private fun pageWithCover(
        label: String,
    ): HomePage.SingleCartoonPage.WithCover {
        return HomePage.SingleCartoonPage.WithCover(
            label = label,
            firstKey = {
                1
            },
            load =  { key ->
                withResult(CoroutineProvider.io) {
                    if (key > 10) {
                        return@withResult Pair(null, listOf())
                    }
                    val res = MutableList(10) {
                        CartoonCover(
                            id = "test ${label} ${key * 10 + it}",
                            mediaSource = debugInnerSource.id,

                            name = "test ${key * 10 + it}",
                            coverUrl = "https://img.cycimg.me/r/800/pic/cover/l/77/c3/454684_ZH5tU.jpg",
                            intro = "",
                            detailedUrl = "",
                        )
                    }
                    Pair(key + 1, res)
                }
            }
        )
    }

    private fun pageWithoutCover(
        label: String,
    ): HomePage.SingleCartoonPage.WithoutCover {
        return HomePage.SingleCartoonPage.WithoutCover(
            label = label,
            firstKey = {
                1
            },
            load =  { key ->
                withResult(CoroutineProvider.io) {
                    if (key > 10) {
                        return@withResult Pair(null, listOf())
                    }
                    val res = MutableList(10) {
                        CartoonCover(
                            id = "test ${label} ${key * 10 + it}",
                            mediaSource = debugInnerSource.id,

                            name = "test ${key * 10 + it}",
                            coverUrl = "",
                            intro = "Mujica 星期四",
                            detailedUrl = "",
                        )
                    }
                    Pair(key + 1, res)
                }
            }
        )
    }

}