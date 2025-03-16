package com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.DebugInnerSource
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover
import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.base.withResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentWrapper
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.CartoonPage
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeContent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.utils.StringHelper
import kotlinx.coroutines.delay

/**
 * Created by heyanlin on 2025/2/6.
 */
class DebugHomeComponent(
    private val debugInnerSource: DebugInnerSource,
    private val stringHelper: StringHelper,
): HomeComponent, ComponentWrapper() {

    override suspend fun home(): SourceResult<HomeContent> {
        return withResult(CoroutineProvider.io) {
            delay(5000)
            HomeContent.Multiple(
                pageList = listOf(
                    "debug 1" to HomePage.Group(
                        load = {
                            withResult (CoroutineProvider.io){
                                delay(5000)
                                listOf(
                                    "page 1" to pageWithCover(),
                                    "page 2" to pageWithoutCover("page 2"),
                                )
                            }
                        }
                    ),
                    "debug 2" to HomePage.Group(
                        load = {
                            withResult (CoroutineProvider.io){
                                delay(5000)
                                listOf(
                                    "page3" to pageWithCover(),
                                    "page 4" to pageWithoutCover("page 4"),
                                )
                            }
                        }
                    ),
                )
            )

        }
    }

    private fun pageWithCover(
    ): CartoonPage.WithCover {
        return CartoonPage.WithCover(
            firstKey = {
                1
            },
            load =  { key ->
                withResult(CoroutineProvider.io) {
                    delay(5000)
                    if (key > 10) {
                        return@withResult Pair(null, listOf())
                    }
                    val res = MutableList(10) {
                        CartoonCover(
                            id = "test ${key * 10 + it}",
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
    ): CartoonPage.WithoutCover {
        return CartoonPage.WithoutCover(
            firstKey = {
                1
            },
            load =  { key ->
                withResult(CoroutineProvider.io) {
                    delay(5000)
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