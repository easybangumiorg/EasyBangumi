package com.heyanle.easybangumi4

import com.heyanle.easybangumi4.plugin.js.component.JSPageComponent
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntime
import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperV2Impl
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.mozilla.javascript.Function

/**
 * Created by heyanle on 2024/6/10.
 * https://github.com/heyanLE
 */
object TestMain {

    const val TEST_JS = """
// @key cyc
// @label 次元城动漫
// @icon 
// @library 
// @version 
// @version_code
// end

function PageComponent_getMainTabs(){
    return ["首页", "新番", "推荐"];
}

function PageComponent_getSubTabs(mainTab){
    if (mainTab === "首页") {
        return ["动漫", "番剧", "资讯"];
    } else if (mainTab === "新番") {
        return ["全部", "连载", "完结"];
    } else if (mainTab === "推荐") {
        return ["热门", "推荐", "热榜"];
    }
}
function PageComponent_initPageKey(mainTab, subTab){
    return 1;
}

function PageComponent_getContent(mainTab, subTab, pageKey){
    if (mainTab == "首页") {
        return [
            new CartoonImpl(
                "id",
                "cyc",
                "https://www.cycdm.com/",
                "我的青春恋爱物语",
                "热血, 爱情",
                "https://www.cycanime.com/upload/site/20240319-1/23324d2df3da86834984e523293636cf.png",
                "介绍",
                "介绍",
                0,
                false,
                0,
            )
            
        ];
    }
    return [];
}

function getCartoonPageDocById(id: String) {
    return Jsoup.connect("https://www.cycdm.com//bangumi/"+id+".html").userAgent(userAgent).get()
}


    """


    fun main(){
        val sourceController: SourceController by Inject.injectLazy()
        sourceController.scope.launch {
            "test".logi("TestMain")
        }
    }

}