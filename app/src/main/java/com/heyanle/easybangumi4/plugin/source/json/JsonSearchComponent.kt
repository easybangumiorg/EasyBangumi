package com.heyanle.easybangumi4.plugin.source.json

import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.ComponentWrapper
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.withResult
import kotlinx.coroutines.Dispatchers

class JsonSearchComponent(
    private val jsonSource: JsonSource,
    private val executor: JsonRuleExecutor,
) : ComponentWrapper(), SearchComponent {

    init {
        innerSource = jsonSource
    }

    private val searchRule: ListRule
        get() = jsonSource.rule.search ?: throw ParserException("json search rule is missing")

    override fun getFirstSearchKey(keyword: String): Int {
        return searchRule.firstPage
    }

    override suspend fun search(pageKey: Int, keyword: String): SourceResult<Pair<Int?, List<CartoonCover>>> {
        return withResult(Dispatchers.IO) {
            executor.loadList(searchRule, pageKey, keyword)
        }
    }

    override suspend fun search(
        pageKey: Int,
        keyword: String,
        verificationResult: VerificationResult,
    ): SourceResult<Pair<Int?, List<CartoonCover>>> {
        return withResult(Dispatchers.IO) {
            executor.loadList(searchRule, pageKey, keyword, verificationResult)
        }
    }
}
