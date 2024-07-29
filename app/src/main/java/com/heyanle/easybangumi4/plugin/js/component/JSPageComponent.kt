package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.entity.MainTab
import com.heyanle.easybangumi4.plugin.js.entity.SubTab
import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.source_api.ParserException
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import org.mozilla.javascript.Function
import java.util.ArrayList
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.suspendCoroutine

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JSPageComponent(
    private val jsScope: JSScope,
    private val getMainTabs: Function,
    private val getSubTabs: Function,
    private val getContent: Function,
) : ComponentWrapper(), PageComponent, JSBaseComponent {


    companion object {


        const val FUNCTION_NAME_GET_MAIN_TABS = "PageComponent_getMainTabs"
        const val FUNCTION_NAME_GET_SUB_TABS = "PageComponent_getSubTabs"
        const val FUNCTION_NAME_GET_CONTENT = "PageComponent_getContent"

        suspend fun of(jsScope: JSScope): JSPageComponent? {
            return jsScope.runWithScope { _, scriptable ->
                val getMainTabs =
                    scriptable.get(FUNCTION_NAME_GET_MAIN_TABS, scriptable) as? Function
                val getSubTabs = scriptable.get(FUNCTION_NAME_GET_SUB_TABS, scriptable) as? Function
                val getContent = scriptable.get(FUNCTION_NAME_GET_CONTENT, scriptable) as? Function
                if (getMainTabs == null || getSubTabs == null || getContent == null) {
                    return@runWithScope null
                }
                return@runWithScope JSPageComponent(
                    jsScope,
                    getMainTabs,
                    getSubTabs,
                    getContent
                )
            }
        }
    }

    @Volatile
    private var mainTabList = arrayListOf<MainTab>()

    override suspend fun init() {
        jsScope.requestRunWithScope { context, scriptable ->
            val result = arrayListOf<MainTab>()
            (getMainTabs.call(
                context, scriptable, scriptable, arrayOf()
            ) as? ArrayList<*>)?.forEach {
                if (it is MainTab) {
                    result.add(it)
                }

            }
            mainTabList.clear()
            mainTabList.addAll(result)
        }
    }

    override fun getPages(): List<SourcePage> {
        if (mainTabList.size == 1) {
            val f = mainTabList.first()
            if (f.label.isEmpty()) {
                return PageComponent.NonLabelSinglePage(
                    mainTab2SourcePage(f)
                )
            }
        }
        return mainTabList.map { mainTab2SourcePage(it) }
    }

    private fun mainTab2SourcePage(mainTab: MainTab) : SourcePage{
        return if (mainTab.type == MainTab.MAIN_TAB_GROUP) {
            SourcePage.Group(
                label = mainTab.label,
                newScreen = false,
                loadPage = suspend {
                    withResult(Dispatchers.IO) {
                        jsScope.runWithScope { context, scriptable ->
                            ((getSubTabs.call(
                                context, scriptable, scriptable,
                                arrayOf(mainTab)
                            ) as? ArrayList<*>) ?: arrayListOf<Any>()).filterIsInstance<SubTab>()
                                .map {
                                    subTab2SourcePage(mainTab, it)
                                }

                        } ?: emptyList()
                    }
                }
            )
        } else {
            if (mainTab.type == MainTab.MAIN_TAB_WITH_COVER) {
                SourcePage.SingleCartoonPage.WithCover(
                    label = mainTab.label,
                    firstKey = { 0 },
                    load = {
                        load(mainTab, null, it)
                    }
                )
            } else {
                SourcePage.SingleCartoonPage.WithoutCover(
                    label = mainTab.label,
                    firstKey = { 0 },
                    load = {
                        load(mainTab, null, it)
                    }
                )
            }
        }
    }

        private fun subTab2SourcePage(mainTab: MainTab, subTab: SubTab): SourcePage.SingleCartoonPage {
            return if (subTab.isCover)
                SourcePage.SingleCartoonPage.WithCover(
                    label = subTab.label,
                    firstKey = { 0 },
                    load = {
                        load(mainTab, subTab, it)
                    }
                )
            else
                SourcePage.SingleCartoonPage.WithoutCover(
                    label = subTab.label,
                    firstKey = { 0 },
                    load = {
                        load(mainTab, subTab, it)
                    }
                )
        }

        private suspend fun load(mainTab: MainTab, subTab: SubTab?, key: Int): SourceResult<Pair<Int?, List<CartoonCover>>> {
            return withResult(Dispatchers.IO) {
                jsScope.requestRunWithScope { context, scriptable ->
                    val res = (getContent.call(
                        context,
                        scriptable,
                        scriptable,
                        arrayOf(
                            mainTab, subTab, key
                        )
                    ) as? Pair<*, *>)
                    if (res == null) {
                        throw ParserException("js parse error")
                    }
                    val nextKey = res.first as? Int?
                    val data = res.second as? java.util.ArrayList<*> ?: throw ParserException("js parse error")
                    if (data.isNotEmpty() && data.first() !is CartoonCover) {
                        throw ParserException("js parse error")
                    }
                    return@requestRunWithScope nextKey to data.filterIsInstance<CartoonCover>()
                }
            }
        }
    }