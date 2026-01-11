package org.easybangumi.next.source.inner.age

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
class AgePlayComponent: PlayComponent, BaseComponent() {

    private val logger = logger()

    private val ktorClient: HttpClient by inject()
    private val networkHelper: NetworkHelper by inject()
    private val prefHelper: PreferenceHelper by inject()
    private val webViewHelper: WebViewHelper by inject()


    override suspend fun getPlayLines(cartoonIndex: CartoonIndex): DataState<List<PlayerLine>> {
        return withResult {
            val host = prefHelper.ageHost()
            val html = ktorClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    this.host = host
                    path("detail", cartoonIndex.id)
                }
            }.bodyAsText()
            val doc = Ksoup.parse(html)
            val tabs =
                doc.select("body > div.body_content_wrapper.pb-2 > div > section > div > div.video_detail_right.ps-3.flex-grow-1 > div.video_detail_playlist_wrapper.pt-4 > ul > li ")
                    .iterator()
            val epRoot = doc.select("body > div.body_content_wrapper.pb-2 > div > section > div > div.video_detail_right.ps-3.flex-grow-1 > div.video_detail_playlist_wrapper.pt-4 > div.tab-content > div ul").iterator()
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

            val host = prefHelper.ageHost()
            val url = buildUrl {
                protocol = URLProtocol.HTTPS
                this.host = host
                path("play", cartoonIndex.id, playerLine.id, episode.id)
            }.toString()
            var m3u8Url: String? = null
            var mp4Url: String? = null
            webViewHelper.use {
                loadUrl(url, userAgent = networkHelper.defaultLinuxUA, interceptResRegex = null)
                waitingForPageLoaded()
                waitingForResourceLoaded(".*jx\\.ejtsyc\\.com.*", true, 1000)
                m3u8Url = waitingForResourceLoaded(".*index\\.m3u8", true, 2000L)
                mp4Url = waitingForResourceLoaded(".*\\.mp4", true, 2000L)
                delay(500L)
            } ?: throw DataStateException("获取播放信息失败，可能是网络异常或页面加载超时")
            val u = m3u8Url ?: mp4Url ?: ""
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