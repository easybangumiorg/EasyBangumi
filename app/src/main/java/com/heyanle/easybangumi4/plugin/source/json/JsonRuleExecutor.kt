package com.heyanle.easybangumi4.plugin.source.json

import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedWebViewCheckBusinessException
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoWebViewCheckParam
import com.heyanle.easybangumi4.plugin.api.component.SearchNeedWebViewCheckBusinessException
import com.heyanle.easybangumi4.plugin.api.component.SearchWebViewCheckParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCoverImpl
import com.heyanle.easybangumi4.plugin.api.entity.CartoonImpl
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.plugin.api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.RenderHelper
import okhttp3.Request
import okhttp3.FormBody
import org.jsoup.nodes.Element
import java.net.URI
import java.net.URLEncoder
import java.util.Locale

class JsonRuleExecutor(
    private val source: JsonSource,
    private val networkHelper: NetworkHelper,
    private val okhttpHelper: OkhttpHelper,
    private val renderHelper: RenderHelper,
    private val fetcher: (suspend (String) -> String)? = null,
) {
    private val rule: JsonSourceRule = source.rule

    fun firstPage(listRule: ListRule): Int = listRule.firstPage

    suspend fun loadList(
        listRule: ListRule,
        page: Int,
        keyword: String? = null,
        verificationResult: VerificationResult? = null,
    ): Pair<Int?, List<CartoonCover>> {
        val url = buildUrl(listRule.url, mapOf("page" to page.toString(), "keyword" to keyword.orEmpty()))
        val document = XPathUtils.parse(fetch(url, CaptchaContext.Search(page, keyword.orEmpty()), verificationResult), url)
        val items = document.selectBy(listRule.item)
        val covers = items.mapNotNull { element ->
            runCatching { parseCover(element, listRule.fields, url) }.getOrNull()
        }
        val next = listRule.nextPage?.let { next ->
            next.selector?.let { selector ->
                document.extract(selector)?.toIntOrNull()
            } ?: next.offset?.let { page + it }
        }
        return next to covers
    }

    suspend fun loadDetail(summary: CartoonSummary): Pair<Cartoon, List<PlayLine>> {
        val detailRule = rule.detail ?: throw ParserException("json detail rule is missing")
        val url = buildUrl(detailRule.url, mapOf("id" to summary.id, "url" to summary.id))
        val document = XPathUtils.parse(fetch(url, CaptchaContext.Search(0, summary.id)), url)
        val cartoon = parseCartoon(document, detailRule.fields, summary, url)
        val playLines = parsePlayLines(document, detailRule.playLines)
        return cartoon to playLines
    }

    suspend fun loadPlay(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        verificationResult: VerificationResult? = null,
    ): PlayerInfo {
        val playRule = rule.play ?: throw ParserException("json play rule is missing")
        val pageUrl = buildUrl(
            playRule.url,
            mapOf(
                "id" to summary.id,
                "lineId" to playLine.id,
                "episodeId" to episode.id,
                "url" to episode.id,
            )
        )
        val direct = playRule.direct?.let { selector ->
            val document = XPathUtils.parse(fetch(pageUrl, CaptchaContext.Play(summary, playLine, episode), verificationResult), pageUrl)
            document.extract(selector)?.let { normalizeUrl(it, pageUrl) }
        }
        val videoUrl = direct ?: if (playRule.renderVideo) {
            renderHelper.renderVideo(
                RenderHelper.VideoStrategy(
                    url = pageUrl,
                    userAgentString = rule.site.userAgent ?: networkHelper.defaultLinuxUA,
                    header = rule.site.headers,
                    actionJs = playRule.actionJs,
                    timeOut = playRule.timeout,
                    useLegacyParser = playRule.useLegacyParser,
                )
            ).url
        } else {
            pageUrl
        }
        if (videoUrl.isBlank()) throw ParserException("json play url parse failed")
        return PlayerInfo(
            decodeType = if (videoUrl.substringBefore("?").lowercase(Locale.ROOT).endsWith(".m3u8")) {
                PlayerInfo.DECODE_TYPE_HLS
            } else {
                PlayerInfo.DECODE_TYPE_OTHER
            },
            uri = videoUrl,
        ).apply {
            header = rule.site.headers.takeIf { it.isNotEmpty() }
        }
    }

    private fun parseCover(element: Element, fields: CoverFieldRule, baseUrl: String): CartoonCover {
        val url = normalizeUrl(required(element.extract(fields.url), "cover.url"), baseUrl)
        val id = element.extract(fields.id)
            ?.takeIf { it.isNotBlank() }
            ?.let { normalizeId(it, baseUrl) }
            ?: url
        return CartoonCoverImpl(
            id = id,
            source = source.key,
            url = url,
            title = required(element.extract(fields.title), "cover.title"),
            coverUrl = element.extract(fields.cover)?.let { normalizeUrl(it, baseUrl) },
            intro = element.extract(fields.intro),
        )
    }

    private fun parseCartoon(element: Element, fields: CartoonFieldRule, summary: CartoonSummary, baseUrl: String): Cartoon {
        val id = element.extract(fields.id) ?: summary.id
        val url = element.extract(fields.url)?.let { normalizeUrl(it, baseUrl) } ?: baseUrl
        return CartoonImpl(
            id = id,
            source = source.key,
            url = url,
            title = element.extract(fields.title) ?: id,
            coverUrl = element.extract(fields.cover)?.let { normalizeUrl(it, baseUrl) },
            intro = element.extract(fields.intro),
            description = element.extract(fields.description),
            genre = element.extract(fields.genre),
            status = parseStatus(element.extract(fields.status)),
            updateStrategy = fields.updateStrategy,
        )
    }

    private fun parsePlayLines(element: Element, rule: PlayLineRule): List<PlayLine> {
        val lines = element.selectBy(rule.line)
        if (lines.isEmpty()) return emptyList()
        return lines.mapIndexed { lineIndex, lineElement ->
            val episodes = lineElement.selectBy(rule.episode).mapIndexed { episodeIndex, episodeElement ->
                val episodeUrl = episodeElement.extract(rule.episodeUrl)?.let { normalizeUrl(it, sourceBaseUrl(element)) }
                Episode(
                    id = episodeElement.extract(rule.episodeId) ?: episodeUrl ?: episodeIndex.toString(),
                    label = episodeElement.extract(rule.episodeLabel) ?: episodeElement.text().ifBlank { "Episode ${episodeIndex + 1}" },
                    order = episodeIndex,
                )
            }
            PlayLine(
                id = lineElement.extract(rule.lineId) ?: lineIndex.toString(),
                label = lineElement.extract(rule.lineLabel) ?: lineElement.ownText().ifBlank { "Line ${lineIndex + 1}" },
                episode = ArrayList(episodes),
            )
        }
    }

    private fun sourceBaseUrl(element: Element): String {
        return element.baseUri().takeIf { it.isNotBlank() } ?: rule.site.baseUrl
    }

    private suspend fun fetch(
        url: String,
        captchaContext: CaptchaContext,
        verificationResult: VerificationResult? = null,
    ): String {
        fetcher?.let {
            val requestUrl = buildRequest(url, verificationResult).url.toString()
            val html = it(requestUrl)
            checkCaptcha(html, requestUrl, captchaContext)
            return html
        }
        val request = buildRequest(url, verificationResult)
        val html = okhttpHelper.cloudflareWebViewClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ParserException("json request failed: ${response.code}")
            response.body?.string().orEmpty()
        }
        checkCaptcha(html, url, captchaContext)
        return html
    }

    private fun buildRequest(url: String, verificationResult: VerificationResult? = null): Request {
        val captcha = rule.captcha
        val input = (verificationResult as? VerificationResult.ImageCaptcha)?.input
        val targetUrl = if (!input.isNullOrBlank() && captcha != null && captcha.method.equals("GET", ignoreCase = true)) {
            appendQuery(buildUrl(captcha.url.ifBlank { url }, emptyMap()), captcha.inputName, input)
        } else {
            url
        }
        return Request.Builder()
            .url(targetUrl)
            .apply {
                val ua = rule.site.userAgent ?: networkHelper.defaultLinuxUA
                if (ua.isNotBlank()) header("User-Agent", ua)
                rule.site.headers.forEach { (key, value) -> header(key, value) }
                if (!input.isNullOrBlank() && captcha != null && captcha.method.equals("POST", ignoreCase = true)) {
                    post(FormBody.Builder().add(captcha.inputName, input).build())
                }
            }
            .build()
    }

    private fun checkCaptcha(html: String, url: String, captchaContext: CaptchaContext) {
        val captcha = rule.captcha ?: return
        val document = XPathUtils.parse(html, url)
        if (document.selectBy(captcha.detect).isEmpty()) return
        val image = required(document.extract(captcha.image)?.let { normalizeUrl(it, url) }, "captcha.image")
        val verificationParam = VerificationParam.ImageCaptcha(
            image = image,
            text = captcha.text,
            title = captcha.title,
            hint = captcha.hint,
        )
        val exception = when (captchaContext) {
            is CaptchaContext.Search -> SearchNeedWebViewCheckBusinessException(
                param = SearchWebViewCheckParam(
                    key = captchaContext.page,
                    keyword = captchaContext.keyword,
                    source = source.key,
                    iWebProxy = EmptyWebProxy,
                ),
                verificationParam = verificationParam,
            )
            is CaptchaContext.Play -> PlayInfoNeedWebViewCheckBusinessException(
                param = PlayInfoWebViewCheckParam(
                    summary = captchaContext.summary,
                    playLine = captchaContext.playLine,
                    episode = captchaContext.episode,
                    iWebProxy = EmptyWebProxy,
                ),
                verificationParam = verificationParam,
            )
        }
        throw ParserException(
            message = "need captcha",
            exception = exception,
        )
    }

    private fun buildUrl(template: String, values: Map<String, String>): String {
        var result = template
        values.forEach { (key, value) ->
            result = result.replace("{$key}", value)
            result = result.replace("{${key}:url}", URLEncoder.encode(value, "UTF-8"))
        }
        return normalizeUrl(result, rule.site.baseUrl)
    }

    private fun normalizeUrl(raw: String, baseUrl: String): String {
        val value = raw.trim()
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        if (value.startsWith("//")) return "https:$value"
        return runCatching {
            URI(baseUrl).resolve(value).toString()
        }.getOrElse { value }
    }

    private fun appendQuery(url: String, name: String, value: String): String {
        val encoded = URLEncoder.encode(value, "UTF-8")
        val separator = if (url.contains("?")) "&" else "?"
        return "$url$separator$name=$encoded"
    }

    private fun normalizeId(raw: String, baseUrl: String): String {
        val value = raw.trim()
        return if (
            value.startsWith("http://") ||
            value.startsWith("https://") ||
            value.startsWith("//") ||
            value.startsWith("/")
        ) {
            normalizeUrl(value, baseUrl)
        } else {
            value
        }
    }

    private fun required(value: String?, name: String): String {
        return value?.takeIf { it.isNotBlank() } ?: throw ParserException("json field $name is empty")
    }

    private fun parseStatus(value: String?): Int {
        return when (value?.trim()?.lowercase(Locale.ROOT)) {
            "1", "ongoing", "连载", "连载中" -> Cartoon.STATUS_ONGOING
            "2", "completed", "完结", "已完结" -> Cartoon.STATUS_COMPLETED
            else -> Cartoon.STATUS_UNKNOWN
        }
    }
}

private sealed class CaptchaContext {
    data class Search(
        val page: Int,
        val keyword: String,
    ) : CaptchaContext()

    data class Play(
        val summary: CartoonSummary,
        val playLine: PlayLine,
        val episode: Episode,
    ) : CaptchaContext()
}

private object EmptyWebProxy : com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy {
    override suspend fun href(url: String, cleanLoaded: Boolean) = Unit
    override suspend fun loadUrl(
        url: String,
        userAgent: String?,
        headers: Map<String, String>,
        interceptResRegex: String?,
        needBlob: Boolean,
    ) = Unit
    override suspend fun waitingForPageLoaded(timeout: Long): Boolean = false
    override suspend fun waitingForResourceLoaded(urlRegex: String, sticky: Boolean, timeout: Long): String? = null
    override suspend fun waitingForBlobText(
        urlRegex: String?,
        textRegex: String?,
        sticky: Boolean,
        timeout: Long,
    ): Pair<String?, String?>? = null
    override suspend fun getContent(timeout: Long): String? = null
    override suspend fun getContentWithIframe(timeout: Long): String? = null
    override suspend fun executeJavaScript(script: String, delay: Long): String? = null
    override fun addToWindow(show: Boolean) = Unit
    override fun getWebView(): android.webkit.WebView? = null
    override fun close() = Unit
}
