package com.heyanle.easy_bangumi_cm.plugin.api.component.media.home

import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover


/**
 * 一级 tab，二级 tab，首页内容
 * Created by HeYanLe on 2024/12/8 21:59.
 * https://github.com/heyanLE
 */

// 首页展示内容
sealed class HomeContent {

    // 单页面，没有 title
    class Single(
        val single: HomePage
    ): HomeContent()

    // 带一级 Tab，一级 tab 不允许异步
    // label to HomePage
    class Multiple(
        val pageList: List<Pair<String, HomePage>>
    ): HomeContent()

}

sealed class HomePage {

    class Single (
        val load: suspend () -> SourceResult<CartoonPage>
    ): HomePage()

    // 二级 tab 支持异步加载
    class Group (
        val load: suspend () -> SourceResult<List<Pair<String, CartoonPage>>>
    ): HomePage()

}

sealed class CartoonPage {
    // 获取首页 key
    abstract var firstKey: () -> Int

    // 加载某一页数据
    // 返回下一页的 key 和数据
    abstract var load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>

    /**
     * 带有番剧缩略图
     * 将会展示 CartoonCover 里的 coverUrl 和 title
     */
    class WithCover(
        override var firstKey: () -> Int,
        override var load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>,
    ): CartoonPage()

    /**
     * 不带缩略图
     * 将会展示 CartoonCover 里的 intro（如果有）和 title
     */
    class WithoutCover(
        override var firstKey: () -> Int,
        override var load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>,
    ): CartoonPage()
}
