package com.heyanle.bangumi_source_api.api.component.page

import androidx.annotation.Keep
import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/27 21:29.
 * https://github.com/heyanLE
 */
@Keep
sealed class SourcePage {

    abstract val label: String
    abstract val newScreen: Boolean


    /**
     * 页面组，异步加载多个
     */
    @Keep
    class Group(
        override val label: String,
        override val newScreen: Boolean,
        val loadPage: suspend ()-> SourceResult<List<SingleCartoonPage>>,
    ): SourcePage()

    /**
     * 异步单页面
     */
    class SingleAsyncPage(
        override val label: String,
        override val newScreen: Boolean,
        val load: suspend () -> SourceResult<SingleCartoonPage>
    ): SourcePage()



    /**
     * 单个页面
     */
    @Keep
    sealed class SingleCartoonPage: SourcePage() {

        override val newScreen: Boolean
            get() = false

        // 获取首页 key
        abstract var firstKey: () -> Int

        // 加载某一页数据
        abstract var load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>

        /**
         * 带有番剧缩略图
         * 将会展示 CartoonCover 里的 coverUrl 和 title
         */
        @Keep
        class WithCover(
            override var label: String,
            override var firstKey: () -> Int,
            override var load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>,
        ): SingleCartoonPage()

        /**
         * 不带缩略图
         * 将会展示 CartoonCover 里的 intro（如果有）和 title
         */
        @Keep
        class WithoutCover(
            override var label: String,
            override var firstKey: () -> Int,
            override var load: suspend (Int) -> SourceResult<Pair<Int?, List<CartoonCover>>>,
        ): SingleCartoonPage()

    }

}

