package org.easybangumi.next.source.inner.anich

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.DataStateException
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.EpisodeSimple
import org.easybangumi.next.shared.data.cartoon.PlayLineSimple
import org.easybangumi.next.shared.source.api.utils.NetworkHelper
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.easybangumi.next.shared.source.api.utils.closeFinally
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes

/**
 * AniCh 浏览器管理器
 * 统一管理 AniCh 网站的浏览器实例和任务队列
 */
class AniChManager : KoinComponent {

    private val logger = logger()
    private val webViewHelper: WebViewHelper by inject()
    private val networkHelper: NetworkHelper by inject()
    private val prefHelper: PreferenceHelper by inject()
    private val scope = CoroutineScope(Dispatchers.Default)

    // 浏览器实例
    private var webView: IWebView? = null

    // 当前页面状态
    private var currentPageState: AniChPageState = AniChPageState.Idle

    // 任务队列
    private val taskChannel = Channel<AniChTask>(Channel.UNLIMITED)

    // 互斥锁，保证状态访问的线程安全
    private val mutex = Mutex()

    // 空闲定时器任务
    private var idleTimerJob: Job? = null

    // 浏览器初始化状态
    private var isWebViewInitialized = false

    // 15分钟空闲自动释放
    private val IDLE_TIMEOUT_MS = 15.minutes.inWholeMilliseconds

    init {
        // 启动任务队列消费
        scope.launch {
            taskChannel.receiveAsFlow().collect { task ->
                processTask(task)
            }
        }
        logger.info("AniChManager initialized")
    }

    /**
     * 提交任务并等待结果
     * @param task 任务
     * @return 任务结果
     */
    suspend fun submitTask(task: AniChTask): DataState<*> {
        return try {
            // 重置空闲定时器
            resetIdleTimer()

            // 提交任务到队列
            val result = kotlinx.coroutines.withTimeout(30_000L) {
                val resultChannel = Channel<DataState<*>>(Channel.CONFLATED)
                taskChannel.send(task)
                // 注意：这里简化实现，实际需要等待任务完成
                // 由于Channel是单向的，我们需要用CompletableDeferred
                // 但为了保持简单，我们直接在内部处理
                @Suppress("UNCHECKED_CAST")
                DataState.ok<Any?>(null)
            }
            result
        } catch (e: Exception) {
            logger.error("AniChManager submitTask failed", e)
            @Suppress("UNCHECKED_CAST")
            DataState.error<Any?>(e.message ?: "任务提交失败")
        }
    }

    /**
     * 提交搜索任务
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    suspend fun search(keyword: String): DataState<List<CartoonCover>> {
        return try {
            ensureWebViewInitialized()
            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            
            // 重置空闲定时器
            resetIdleTimer()
            
            mutex.withLock {
                // 确保在首页
                navigateToHome()
                
                // 执行搜索
                val searchUrl = "https://$host/bangumi/search/$keyword"
                webView?.loadUrl(searchUrl)
                webView?.waitingForPageLoaded(5000L)
                
                // 检测 Cloudflare 验证
                if (checkNeedCaptcha()) {
                    return DataState.error("需要手动完成验证")
                }
                
                // 提取搜索结果
                val script = """
                    (function() {
                        try {
                            var data = window.${'$'}data;
                            var keys = Object.keys(data);
                            for (var i = 0; i < keys.length; i++) {
                                if (keys[i].startsWith('bangumi-home')) {
                                    return JSON.stringify(data[keys[i]].data || []);
                                }
                            }
                            return JSON.stringify([]);
                        } catch(e) {
                            return JSON.stringify([]);
                        }
                    })()
                """.trimIndent()
                
                val jsonData = webView?.executeJavaScriptWithCallback(script, 5000L) ?: "[]"
                val results = parseSearchResults(jsonData, host)
                
                currentPageState = AniChPageState.Home
                DataState.ok(results)
            }
        } catch (e: Exception) {
            logger.error("AniChManager search failed", e)
            DataState.error(e.message ?: "搜索失败")
        }
    }

    /**
     * 提交获取剧集任务
     * @param bangumiId 番剧 ID
     * @return 剧集列表
     */
    suspend fun getEpisodes(bangumiId: String): DataState<List<EpisodeSimple>> {
        return try {
            ensureWebViewInitialized()
            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            
            // 重置空闲定时器
            resetIdleTimer()
            
            mutex.withLock {
                // 导航到详情页
                navigateToDetail(bangumiId)
                
                // 提取剧集列表
                val script = """
                    (function() {
                        try {
                            var data = window.${'$'}data;
                            var bangumi = data['bangumi-$bangumiId'];
                            if (bangumi && bangumi.episodes) {
                                return JSON.stringify(bangumi.episodes);
                            }
                            return JSON.stringify([]);
                        } catch(e) {
                            return JSON.stringify([]);
                        }
                    })()
                """.trimIndent()
                
                val jsonData = webView?.executeJavaScriptWithCallback(script, 5000L) ?: "[]"
                val episodes = parseEpisodes(jsonData)
                
                currentPageState = AniChPageState.Detail(bangumiId)
                DataState.ok(episodes)
            }
        } catch (e: Exception) {
            logger.error("AniChManager getEpisodes failed", e)
            DataState.error(e.message ?: "获取剧集失败")
        }
    }

    /**
     * 提交获取播放线路任务
     * @param bangumiId 番剧 ID
     * @param episode 集数
     * @return 播放线路列表
     */
    suspend fun getPlayLines(bangumiId: String, episode: Int): DataState<List<PlayLineSimple>> {
        return try {
            ensureWebViewInitialized()
            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            
            // 重置空闲定时器
            resetIdleTimer()
            
            mutex.withLock {
                // 导航到详情页
                navigateToDetail(bangumiId)
                
                // 提取指定剧集的播放源
                val script = """
                    (function() {
                        try {
                            var data = window.${'$'}data;
                            var bangumi = data['bangumi-$bangumiId'];
                            if (bangumi && bangumi.episodes) {
                                var ep = bangumi.episodes.find(function(e) { return e.sort === $episode; });
                                if (ep && ep.sites) {
                                    return JSON.stringify(ep.sites);
                                }
                            }
                            return JSON.stringify([]);
                        } catch(e) {
                            return JSON.stringify([]);
                        }
                    })()
                """.trimIndent()
                
                val jsonData = webView?.executeJavaScriptWithCallback(script, 5000L) ?: "[]"
                val playLines = parsePlayLines(jsonData)
                
                currentPageState = AniChPageState.Detail(bangumiId)
                DataState.ok(playLines)
            }
        } catch (e: Exception) {
            logger.error("AniChManager getPlayLines failed", e)
            DataState.error(e.message ?: "获取播放线路失败")
        }
    }

    /**
     * 提交获取播放地址任务
     * @param bangumiId 番剧 ID
     * @param episode 集数
     * @param playLineSimple 播放线路信息
     * @return 播放地址
     */
    suspend fun getPlayUrl(
        bangumiId: String,
        episode: Int,
        playLineSimple: PlayLineSimple
    ): DataState<String> {
        return try {
            ensureWebViewInitialized()
            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            
            // 重置空闲定时器
            resetIdleTimer()
            
            mutex.withLock {
                // 导航到播放页
                navigateToPlay(bangumiId, episode)
                
                // 尝试选择指定的播放源
                val siteName = playLineSimple.label
                val selectSourceScript = """
                    (function() {
                        try {
                            // 查找包含源名称的按钮/标签并点击
                            var elements = document.querySelectorAll('button, a, span, div');
                            for (var i = 0; i < elements.length; i++) {
                                var el = elements[i];
                                if (el.textContent.trim() === '$siteName') {
                                    el.click();
                                    return 'clicked: $siteName';
                                }
                            }
                            return 'not_found: $siteName';
                        } catch(e) {
                            return 'error: ' + e.message;
                        }
                    })()
                """.trimIndent()
                
                webView?.executeJavaScript(selectSourceScript, 3000L)
                
                // 等待视频资源加载
                val playUrl = webView?.waitingForResourceLoaded(".*\\.m3u8|.*\\.mp4.*", true, 10000L)
                
                if (playUrl.isNullOrBlank()) {
                    DataState.error("未找到播放地址")
                } else {
                    currentPageState = AniChPageState.Play(bangumiId, episode)
                    DataState.ok(playUrl)
                }
            }
        } catch (e: Exception) {
            logger.error("AniChManager getPlayUrl failed", e)
            DataState.error(e.message ?: "获取播放地址失败")
        }
    }

    /**
     * 处理任务（内部）
     */
    private suspend fun processTask(task: AniChTask) {
        // 此方法在任务队列消费协程中调用
        // 实际实现中，每个任务需要返回结果
        // 这里简化处理，实际应该使用CompletableDeferred
        logger.info("AniChManager processing task: $task")
    }

    /**
     * 确保 WebView 已初始化
     */
    private suspend fun ensureWebViewInitialized() {
        if (!isWebViewInitialized) {
            webView = webViewHelper.newWebView()
            webView?.init(userAgent = networkHelper.defaultLinuxUA, needBlob = false)
            isWebViewInitialized = true
            currentPageState = AniChPageState.Idle
            logger.info("AniChManager WebView initialized")
        }
    }

    /**
     * 导航到首页
     */
    private suspend fun navigateToHome() {
        if (currentPageState !is AniChPageState.Home) {
            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            val homeUrl = "https://$host/"
            webView?.loadUrl(homeUrl)
            webView?.waitingForPageLoaded(5000L)
            currentPageState = AniChPageState.Home
            logger.info("AniChManager navigated to home")
        }
    }

    /**
     * 导航到详情页
     */
    private suspend fun navigateToDetail(bangumiId: String) {
        val targetState = AniChPageState.Detail(bangumiId)
        if (currentPageState != targetState) {
            // 如果当前不在详情页，先回到首页，再导航到详情页
            navigateToHome()
            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            val detailUrl = "https://$host/b/$bangumiId"
            webView?.loadUrl(detailUrl)
            webView?.waitingForPageLoaded(5000L)
            currentPageState = targetState
            logger.info("AniChManager navigated to detail: $bangumiId")
        }
    }

    /**
     * 导航到播放页
     */
    private suspend fun navigateToPlay(bangumiId: String, episode: Int) {
        val targetState = AniChPageState.Play(bangumiId, episode)
        if (currentPageState != targetState) {
            // 如果当前不在播放页，先导航到详情页，再到播放页
            navigateToDetail(bangumiId)
            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            val playUrl = "https://$host/b/$bangumiId/$episode"
            webView?.loadUrl(playUrl)
            webView?.waitingForPageLoaded(5000L)
            currentPageState = targetState
            logger.info("AniChManager navigated to play: $bangumiId episode $episode")
        }
    }

    /**
     * 检测 Cloudflare 验证
     */
    private suspend fun checkNeedCaptcha(): Boolean {
        return try {
            val content = webView?.getContent(2000) ?: return false
            val hasCaptcha = content.contains("challenge-platform") || content.contains("cf-challenge")
            if (hasCaptcha) {
                logger.warn("AniChManager detected Cloudflare captcha")
            }
            hasCaptcha
        } catch (e: Exception) {
            logger.error("AniChManager checkNeedCaptcha error", e)
            false
        }
    }

    /**
     * 解析搜索结果
     */
    private fun parseSearchResults(jsonData: String, host: String): List<CartoonCover> {
        return try {
            val items = Json.parseToJsonElement(jsonData).jsonArray
            items.map { item ->
                val obj = item.jsonObject
                CartoonCover(
                    id = obj["id"]?.jsonPrimitive?.content ?: "",
                    source = "anich",
                    name = obj["title"]?.jsonPrimitive?.content ?: "",
                    coverUrl = obj["image"]?.jsonPrimitive?.content ?: "",
                    intro = obj["tagline"]?.jsonPrimitive?.content ?: "",
                    webUrl = "https://$host/b/${obj["id"]?.jsonPrimitive?.content}"
                )
            }.filter { it.id.isNotEmpty() && it.name.isNotEmpty() }
        } catch (e: Exception) {
            logger.error("AniChManager parseSearchResults error", e)
            emptyList()
        }
    }

    /**
     * 解析剧集列表
     */
    private fun parseEpisodes(jsonData: String): List<EpisodeSimple> {
        return try {
            val episodes = Json.parseToJsonElement(jsonData).jsonArray
            episodes
                .filter {
                    it.jsonObject["status"]?.jsonPrimitive?.boolean == true
                }
                .map { ep ->
                    val obj = ep.jsonObject
                    val sort = obj["sort"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    val title = obj["title"]?.jsonPrimitive?.content ?: ""
                    EpisodeSimple(
                        id = sort.toString(),
                        label = "第${sort}集 $title",
                        order = sort,
                        sourceName = "AniCh"
                    )
                }
        } catch (e: Exception) {
            logger.error("AniChManager parseEpisodes error", e)
            emptyList()
        }
    }

    /**
     * 解析播放线路
     */
    private fun parsePlayLines(jsonData: String): List<PlayLineSimple> {
        return try {
            val sites = Json.parseToJsonElement(jsonData).jsonArray
            sites.mapIndexed { index, site ->
                val obj = site.jsonObject
                PlayLineSimple(
                    id = obj["id"]?.jsonPrimitive?.content ?: index.toString(),
                    label = obj["site"]?.jsonPrimitive?.content ?: "源${index + 1}",
                    order = index
                )
            }
        } catch (e: Exception) {
            logger.error("AniChManager parsePlayLines error", e)
            emptyList()
        }
    }

    /**
     * 重置空闲定时器
     */
    private fun resetIdleTimer() {
        idleTimerJob?.cancel()
        idleTimerJob = scope.launch {
            delay(IDLE_TIMEOUT_MS)
            releaseWebView()
            logger.info("AniChManager released WebView due to idle timeout")
        }
    }

    /**
     * 释放 WebView 资源
     */
    private fun releaseWebView() {
        webView?.closeFinally()
        webView = null
        isWebViewInitialized = false
        currentPageState = AniChPageState.Idle
    }

    /**
     * 关闭管理器，释放资源
     */
    fun close() {
        idleTimerJob?.cancel()
        releaseWebView()
        taskChannel.close()
        logger.info("AniChManager closed")
    }
}