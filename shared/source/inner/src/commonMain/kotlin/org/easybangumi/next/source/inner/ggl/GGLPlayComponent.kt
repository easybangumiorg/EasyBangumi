package org.easybangumi.next.source.inner.ggl

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.DataStateException
import org.easybangumi.next.lib.utils.withResult
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.utils.NetworkHelper
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.koin.core.component.inject

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class GGLPlayComponent: PlayComponent, BaseComponent() {

    private val logger = logger()

    private val ktorClient: HttpClient by inject()
    private val networkHelper: NetworkHelper by inject()
    private val prefHelper: PreferenceHelper by inject()
    private val webViewHelper: WebViewHelper by inject()


    override suspend fun getPlayLines(cartoonIndex: CartoonIndex): DataState<List<PlayerLine>> {
        return withResult {
            val host = prefHelper.get("host", "anime.girigirilove.com")
            val html = ktorClient.get {
                url {
                    this.host = host
                    path(cartoonIndex.id)
                }
            }.bodyAsText()
            val doc = Ksoup.parse(html)
            val tabs =
                doc.select("div.anthology.wow div.anthology-tab div.swiper-wrapper a.swiper-slide")
                    .iterator()
            val epRoot = doc.select("div.anthology-list-box div ul.anthology-list-play").iterator()
            val playLines = arrayListOf<PlayerLine>()
            var ii = 1
            while (tabs.hasNext() && epRoot.hasNext()) {
                val tab = tabs.next()
                val ul = epRoot.next()

                val es = arrayListOf<Episode>()
                ul.children().forEachIndexed { index, element ->
                    es.add(
                        Episode(
                            id = (index + 1).toString(),
                            label = element?.text() ?: "",
                            order = index
                        )
                    )
                }

                playLines.add(
                    PlayerLine(
                        id = ii.toString(),
                        label = tab.text(),
                        episodeList = es,
                    )
                )
                ii++
            }
            playLines
        }
    }

    override suspend fun getPlayInfo(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode
    ): DataState<PlayInfo> {
        return withResult {
            val urlPath = "${
                if(cartoonIndex.id.startsWith("GV"))
                    cartoonIndex.id
                else "GV${cartoonIndex.id}"}-${playerLine.id}-${episode.id}"
            val host = prefHelper.get("host", "anime.girigirilove.com")
            val url = buildUrl {
                this.host = host
                path("play", urlPath)
            }.toString()
            val html = webViewHelper.use {
                loadUrl(url, userAgent = networkHelper.defaultLinuxUA)
                waitingForResourceLoaded(".*play.*", true, 2000L)
                delay(500L)
                getContent(2000L)
            } ?: throw DataStateException("获取播放信息失败，可能是网络异常或页面加载超时")
            val doc = Ksoup.parse(html ?: "")
            val src = doc.select("tbody td iframe").first()?.attr("src")?:""
            val u = src.split("?").last().split("&").find {
                it.startsWith("url=")
            }?.let {
                it.subSequence(4, it.length)
            }?.toString() ?:""
            if(u.isEmpty()){
                throw DataStateException("url 解析失败")
            }
            PlayInfo(
                type = if(u.endsWith("m3u8")) PlayInfo.TYPE_HLS else PlayInfo.TYPE_NORMAL,
                url = u
            )
        }
    }
}