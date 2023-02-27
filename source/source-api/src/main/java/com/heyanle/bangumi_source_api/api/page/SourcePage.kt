package com.heyanle.bangumi_source_api.api.page

import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/27 21:29.
 * https://github.com/heyanLE
 */
sealed class SourcePage {

    abstract val label: String


    /**
     * 页面组，异步加载多个 单页面
     */
    class Group(
        override val label: String,
        val loadPage: suspend ()-> SourceResult<List<SingleCartoonPage>>
    ): SourcePage()

    /**
     * 新页面页面组，将会重新跳转到新的页面展示页面组
     */
    class GroupWithScreen(
        override val label: String,
        val loadPage: suspend ()-> SourceResult<List<SingleCartoonPage>>
    ): SourcePage()

    /**
     * 跳转新页面展示单个页面
     */
    class WithScreen(
        override val label: String,
        val loadPage: suspend ()-> SourceResult<SingleCartoonPage>
    ): SourcePage()

    /**
     * 单个页面
     */
    sealed class SingleCartoonPage: SourcePage() {

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



fun PageBuilderScope.singleCoverCartoonPage(
    label: String,
    firstKey: () -> Int,
    load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>,
){
    pages.add(SourcePage.SingleCartoonPage.WithCover(label, firstKey, load))
}

fun PageBuilderScope.singleTextCartoonPage(
    label: String,
    firstKey: () -> Int,
    load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>,
){
    pages.add(SourcePage.SingleCartoonPage.WithoutCover(label, firstKey, load))
}

fun PageBuilderScope.groupCartoonPage(
    label: String,
    loadPage: suspend ()-> SourceResult<List<SourcePage.SingleCartoonPage>>
){
    pages.add(SourcePage.Group(label, loadPage))
}

fun PageBuilderScope.groupWithScreenCartoonPage(
    label: String,
    loadPage: suspend ()-> SourceResult<List<SourcePage.SingleCartoonPage>>
){
    pages.add(SourcePage.Group(label, loadPage))
}

fun PageBuilderScope.singleWithScreenCartoonPage(
    label: String,
    loadPage: suspend ()-> SourceResult<SourcePage.SingleCartoonPage>
){
    pages.add(SourcePage.WithScreen(label, loadPage))
}

