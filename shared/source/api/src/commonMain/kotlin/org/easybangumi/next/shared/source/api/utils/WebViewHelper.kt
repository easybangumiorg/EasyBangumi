package org.easybangumi.next.shared.source.api.utils


/**
 * Created by HeYanLe on 2024/12/8 23:03.
 * https://github.com/heyanLE
 */

interface WebViewHelper {

    class Rule(
        // 触发器
        val trigger: Trigger,
        // 执行动作
        val action: List<Action>,
    )

    open class Trigger {
        data class LoadEnd(
            val delay: Long = 0L
        ) : Trigger()

        data class LoadStart(
            val delay: Long
        ) : Trigger()

        data class Request(
            val matchRegex: String,
            val saveTag: String? = null,
            val delay: Long = 0L
        ) : Trigger()

        data class BlobRequest(
            val matchRegex: String,
            val saveTag: String? = null,
            val delay: Long = 0L
        ) : Trigger()
    }


    open class Action {
        data class Dealy(val delay: Long) : Action()
        data class ExecuteJSAction(val javaScript: String) : Action()
        data class GetHtmlContent(val key: String) : Action()
        data class SaveRequestMatch(val key: String) : Action()
        data class StopAll(val delay: Long): Action()
    }


    data class RenderedStrategy(
        // 网址
        val url: String,
        // 拦截资源加载，可加快速度
        val needInterceptResource: Boolean = true,
        val interceptResourceRegex: String? = null,
        // UA
        val userAgentString: String? = null,
        // 请求头
        val header: Map<String, String>? = null,
        val rule: List<Rule> = emptyList(),
        // 加载超时。当超过超时时间后还没返回数据则会直接返回当前源码
        val timeOut: Long = 8000L,
    ) {
        val interceptResRegex: Regex? by lazy {
            Regex(interceptResourceRegex ?: return@lazy null)
        }
    }

    data class RenderedResult(
        val strategy: RenderedStrategy,
        val errMsg: String?,
        // 根据保存 tag 获取内容
        val bundle: Map<String, String>,
    )

    suspend fun renderedHtml(strategy: RenderedStrategy): RenderedResult

}