package com.heyanle.easybangumi4.plugin.source.json

import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.ComponentWrapper
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.plugin.api.withResult
import kotlinx.coroutines.Dispatchers

class JsonPlayComponent(
    private val jsonSource: JsonSource,
    private val executor: JsonRuleExecutor,
) : ComponentWrapper(), PlayComponent {

    init {
        innerSource = jsonSource
    }

    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        canCache: Boolean,
    ): SourceResult<PlayerInfo> {
        return withResult(Dispatchers.IO) {
            executor.loadPlay(summary, playLine, episode)
        }
    }

    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        verificationResult: VerificationResult,
        canCache: Boolean,
    ): SourceResult<PlayerInfo> {
        return withResult(Dispatchers.IO) {
            executor.loadPlay(summary, playLine, episode, verificationResult)
        }
    }
}
