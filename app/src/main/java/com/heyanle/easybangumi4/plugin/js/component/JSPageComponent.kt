package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.entity.MainTab
import com.heyanle.easybangumi4.plugin.js.entity.NonLabelMainTab
import com.heyanle.easybangumi4.plugin.js.entity.SubTab
import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.js.runtime.JSScopeException
import com.heyanle.easybangumi4.plugin.js.utils.JSFunction
import com.heyanle.easybangumi4.plugin.js.utils.jsUnwrap
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.source_api.ParserException
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.withResult
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import java.util.ArrayList

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JSPageComponent(
    private val jsScope: JSScope,
    private val getMainTabs: JSFunction,
    private val getSubTabs: JSFunction,
    private val getContent: JSFunction,
) : ComponentWrapper(), PageComponent, JSBaseComponent {


    companion object {


        const val FUNCTION_NAME_GET_MAIN_TABS = "PageComponent_getMainTabs"
        const val FUNCTION_NAME_GET_SUB_TABS = "PageComponent_getSubTabs"
        const val FUNCTION_NAME_GET_CONTENT = "PageComponent_getContent"

        suspend fun of(jsScope: JSScope): JSPageComponent? {
            return jsScope.runWithScope { _, scriptable ->
                val getMainTabs =
                    scriptable.get(FUNCTION_NAME_GET_MAIN_TABS, scriptable) as? JSFunction
                val getSubTabs = scriptable.get(FUNCTION_NAME_GET_SUB_TABS, scriptable) as? JSFunction
                val getContent = scriptable.get(FUNCTION_NAME_GET_CONTENT, scriptable) as? JSFunction
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

    private var webProxyManager: WebProxyManager? = null

    override fun setWebProxyManager(webProxyManager: WebProxyManager) {
        this.webProxyManager = webProxyManager
    }


    override suspend fun init() {
        // 历史遗留问题导致 getPages 不是 suspend 方法，业务也没有做加载态直接同步加载
        // 这里 getMainTab 的操作只能前置到 init
        // 这里 5s 超时尽量保证 getMainTab 不做延时操作
        try {
            jsScope.requestRunWithScope (
                5000,
            ) { context, scriptable ->
                val result = arrayListOf<MainTab>()
                (getMainTabs.call(
                    context, scriptable, scriptable, arrayOf()
                )?.apply {
                    this.logi("JSPageComponent")
                }.jsUnwrap() as? ArrayList<*>)?.let {
                    if (it is NonLabelMainTab) {
                        result.add(MainTab("", it.type))
                    } else {
                        it.forEach {
                            if (it is MainTab) {
                                result.add(it)
                            }
                        }
                    }
                }
                mainTabList.clear()
                mainTabList.addAll(result)
            }
        } catch (e: TimeoutCancellationException) {
            e.printStackTrace()
            throw JSScopeException("${FUNCTION_NAME_GET_MAIN_TABS} 方法需要同步返回，异步 tab 需要设定 NonLabelMainTab 后在 subTab 处理")
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
        return mainTabList.map { mainTab2SourcePage(it) }.apply {
            this.logi("JSPageComponent")
            webProxyManager?.close()
        }
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
                            ).jsUnwrap() as? ArrayList<*>) ?: arrayListOf<Any>()).filterIsInstance<SubTab>()
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
                    val source = (getContent.call(
                        context,
                        scriptable,
                        scriptable,
                        arrayOf(
                            mainTab, subTab, key
                        )
                    ))
                    val jsSource = source.jsUnwrap()
                    val res = jsSource as? Pair<*, *>
                    if (res == null) {
                        throw ParserException("js parse error")
                    }
                    val nextKey = res.first?.toString()?.toDoubleOrNull()?.toInt()
                    val data = res.second as? java.util.ArrayList<*> ?: throw ParserException("js parse error")
                    if (data.isNotEmpty() && data.first() !is CartoonCover) {
                        webProxyManager?.close()
                        throw ParserException("js parse error")
                    }
                    webProxyManager?.close()
                    return@requestRunWithScope nextKey to data.filterIsInstance<CartoonCover>().apply {
                        this.forEach {
//                            it.coverUrl.logi("JsPageComponent")
                        }
                    }
                }
            }
        }
    }