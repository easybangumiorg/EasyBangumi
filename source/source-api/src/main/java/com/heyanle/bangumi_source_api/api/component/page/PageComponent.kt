package com.heyanle.bangumi_source_api.api.component.page

import androidx.annotation.Keep
import com.heyanle.bangumi_source_api.api.component.Component

/**
 * Created by HeYanLe on 2023/2/27 22:09.
 * https://github.com/heyanLE
 */
@Keep
interface PageComponent: Component {

    fun getPages(): List<SourcePage>

}