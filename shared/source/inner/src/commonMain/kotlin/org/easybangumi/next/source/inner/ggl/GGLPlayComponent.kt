package org.easybangumi.next.source.inner.ggl

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.DataStateException
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.UrlUtils
import org.easybangumi.next.lib.utils.withResult
import org.easybangumi.next.shared.data.cartoon.CartoonPlayCover
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.play.IPlayComponent
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

    private val ktorClient: HttpClient by inject()
    private val networkHelper: NetworkHelper by inject()
    private val prefHelper: PreferenceHelper by inject()
    private val webViewHelper: WebViewHelper by inject()

    override fun getFirstKey(): String {
        return "0"
    }

    override suspend fun searchPlayCovers(
        param: IPlayComponent.PlayLineSearchParam,
        key: String
    ): DataState<PagingFrame<CartoonPlayCover>> {
        return withResult {
            val host = prefHelper.get("host", "https://anime.girigirilove.com")
            val html = ktorClient.get {
                url {
                    this.host = host
                    path("search", "${param.keyword ?: param.cartoonCover.name}----------${key}---" )
                }
            }.bodyAsText()
            val doc = Ksoup.parse(html)
            val list = arrayListOf<CartoonPlayCover>()

            doc.select("div div.public-list-box.search-box").forEach {
                val uu = it.child(1).child(0).attr("href")
                val id = uu.subSequence(1, uu.length - 1).toString()

                val coverStyle = it.select("div.cover")[0].attr("style")
                val coverPattern = Regex("""(?<=url\().*(?=\))""")
                var cover = coverPattern.find(coverStyle)?.value ?: ""
                if (cover.startsWith("//")) {
                    cover = "http:${cover}"
                }

                val title = it.select("div.thumb-content div.thumb-txt").first()?.text() ?: ""
                val b = CartoonPlayCover(
                    fromId = param.cartoonCover.id,
                    fromSourceKey = param.cartoonCover.source,
                    playId = id,
                    playSourceKey = source.key,
                    name = title,
                    coverUrl = UrlUtils.parse(host, cover),
                    intro = "",
                    webUrl = UrlUtils.parse(host, uu)
                )
                list.add(b)
            }
            (if (list.isEmpty()) null else (key.toIntOrNull()?:0) + 1).toString() to list
        }


    }


    override suspend fun getPlayLines(cartoonCover: CartoonPlayCover): DataState<List<PlayerLine>> {
        return withResult {
            val host = prefHelper.get("host", "https://anime.girigirilove.com")
            val html = ktorClient.get {
                url {
                    this.host = host
                    path(cartoonCover.playId)
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
        cartoonPlayCover: CartoonPlayCover,
        playerLine: PlayerLine,
        episode: Episode
    ): DataState<PlayInfo> {
        return withResult {
            val urlPath = "${
                if(cartoonPlayCover.playId.startsWith("GV"))
                    cartoonPlayCover.playId 
                else "GV${cartoonPlayCover.playId}"}-${playerLine.id}-${episode.id}"
            val host = prefHelper.get("host", "https://anime.girigirilove.com")
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