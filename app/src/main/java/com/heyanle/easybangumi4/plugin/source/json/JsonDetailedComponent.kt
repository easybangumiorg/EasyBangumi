package com.heyanle.easybangumi4.plugin.source.json

import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.ComponentWrapper
import com.heyanle.easybangumi4.plugin.api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.withResult
import kotlinx.coroutines.Dispatchers

class JsonDetailedComponent(
    private val jsonSource: JsonSource,
    private val executor: JsonRuleExecutor,
) : ComponentWrapper(), DetailedComponent {

    init {
        innerSource = jsonSource
    }

    override suspend fun getDetailed(summary: CartoonSummary): SourceResult<Cartoon> {
        return getAll(summary).map { it.first }
    }

    override suspend fun getPlayLine(summary: CartoonSummary): SourceResult<List<PlayLine>> {
        return getAll(summary).map { it.second }
    }

    override suspend fun getAll(summary: CartoonSummary): SourceResult<Pair<Cartoon, List<PlayLine>>> {
        return withResult(Dispatchers.IO) {
            executor.loadDetail(summary)
        }
    }

    private fun <T, R> SourceResult<T>.map(transform: (T) -> R): SourceResult<R> {
        return when (this) {
            is SourceResult.Complete -> SourceResult.Complete(transform(data))
            is SourceResult.Error -> SourceResult.Error(throwable, isParserError)
        }
    }
}
