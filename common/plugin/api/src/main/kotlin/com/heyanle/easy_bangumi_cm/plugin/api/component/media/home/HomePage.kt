package com.heyanle.easy_bangumi_cm.plugin.api.component.media.home

import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover


/**
 * Created by HeYanLe on 2024/12/8 21:59.
 * https://github.com/heyanLE
 */

// 首页展示内容
sealed class HomeContent {

    // 单页面，没有 title
    class SinglePage(
        val singlePage: HomePage.SingleCartoonPage
    )

    // 带一级 Tab
    class MultiplePage(
        val pageList: List<HomePage>
    )

}



sealed class HomePage {

    abstract val label: String

    /**
     * 页面组，异步加载多个
     */
    class Group(
        override val label: String,
        val loadPage: suspend ()-> SourceResult<List<SingleCartoonPage>>,
    ): HomePage()


    /**
     * 单个页面
     */
    sealed class SingleCartoonPage: HomePage() {

        // 获取首页 key
        abstract var firstKey: () -> Int

        // 加载某一页数据
        abstract var load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>

        /**
         * 带有番剧缩略图
         * 将会展示 CartoonCover 里的 coverUrl 和 title
         */
        class WithCover(
            override var label: String,
            override var firstKey: () -> Int,
            override var load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>,
        ): SingleCartoonPage()

        /**
         * 不带缩略图
         * 将会展示 CartoonCover 里的 intro（如果有）和 title
         */
        class WithoutCover(
            override var label: String,
            override var firstKey: () -> Int,
            override var load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>,
        ): SingleCartoonPage()

    }

}