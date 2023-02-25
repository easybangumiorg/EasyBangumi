package com.heyanle.bangumi_source_api.api2.component

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.component.page.ListPage
import com.heyanle.bangumi_source_api.api2.component.page.ListPageGroup
import com.heyanle.bangumi_source_api.api2.component.page.ListPageGroupTab
import com.heyanle.bangumi_source_api.api2.component.search.SearchComponent
import com.heyanle.bangumi_source_api.api2.entity.CartoonCover


/**
 * Created by HeYanLe on 2023/2/25 14:30.
 * https://github.com/heyanLE
 */
interface Component {

    val source: Source

}

class ComponentBuilderScope(
    val source: Source,
) {
    val components: ArrayList<Component> = arrayListOf()

    fun search(
        firstKey: Int,
        firstKeyFactory: () -> Int = { firstKey },
        search: suspend (
            keyword: String,
            pageKey: Int
        ) -> Source.SourceResult<Pair<Int?, List<CartoonCover>>>
    ) {
        this.components.add(SearchComponent(source, firstKeyFactory, search))
    }

    fun listPage(
        label: String,
        newScreen: Boolean = false,
        firstKey: Int,
        firstKeyFactory: () -> Int = { firstKey },
        getCartoons: suspend (
            pageKey: Int
        ) -> Source.SourceResult<Pair<Int?, List<CartoonCover>>>
    ) {
        this.components.add(ListPage(label, source, newScreen, firstKeyFactory, getCartoons))
    }

    fun listPageGroup(
        label: String,
        listPage: suspend () -> Source.SourceResult<List<ListPage>>,
    ) {
        this.components.add(ListPageGroup(label, source, false, listPage))
    }

    fun listPageGroupTab(
        label: String,
        newScreen: Boolean = true,
        listPage: suspend () -> Source.SourceResult<List<ListPage>>,
    ) {
        this.components.add(ListPageGroupTab(label, source, newScreen, listPage))
    }


}