package org.easybangumi.next.shared.download

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.easybangumi.next.shared.download.action.DownloadActionRegistry
import org.easybangumi.next.shared.download.model.DownloadInfo
import org.easybangumi.next.shared.download.model.DownloadReq
import org.easybangumi.next.shared.download.model.DownloadRuntime
import org.easybangumi.next.shared.download.model.DownloadState

/**
 * 下载任务调度器
 * 管理并发下载任务，驱动 Action 链执行
 */
class DownloadDispatcher(
    private val maxConcurrent: Int = 3,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val _runtimeMap = MutableStateFlow<Map<String, DownloadRuntime>>(emptyMap())
    val runtimeMap: StateFlow<Map<String, DownloadRuntime>> = _runtimeMap.asStateFlow()

    private val _downloadInfos = MutableStateFlow<List<DownloadInfo>>(emptyList())
    val downloadInfos: StateFlow<List<DownloadInfo>> = _downloadInfos.asStateFlow()

    var onComplete: suspend (DownloadReq) -> Unit = {}

    /**
     * 提交新的下载请求
     */
    fun submit(req: DownloadReq) {
        scope.launch {
            mutex.withLock {
                val runtime = DownloadRuntime(req)
                _runtimeMap.value = _runtimeMap.value + (req.uuid to runtime)
                updateInfos()
            }
            dispatch(req.uuid)
        }
    }

    /**
     * 暂停下载
     */
    fun pause(uuid: String) {
        scope.launch {
            val runtime = _runtimeMap.value[uuid] ?: return@launch
            val currentActionName = runtime.req.stepChain.getOrNull(runtime.currentStepIndex)
            if (currentActionName != null) {
                val action = DownloadActionRegistry.get(currentActionName)
                action?.pause(runtime)
            }
            updateInfos()
        }
    }

    /**
     * 恢复下载
     */
    fun resume(uuid: String) {
        scope.launch {
            val runtime = _runtimeMap.value[uuid] ?: return@launch
            val currentActionName = runtime.req.stepChain.getOrNull(runtime.currentStepIndex)
            if (currentActionName != null) {
                val action = DownloadActionRegistry.get(currentActionName)
                action?.resume(runtime)
            }
            updateInfos()
        }
    }

    /**
     * 取消下载
     */
    fun cancel(uuid: String) {
        scope.launch {
            val runtime = _runtimeMap.value[uuid] ?: return@launch
            runtime.markCanceled()

            // 清理所有 action
            for (actionName in runtime.req.stepChain) {
                val action = DownloadActionRegistry.get(actionName)
                action?.cancel(runtime)
            }

            mutex.withLock {
                _runtimeMap.value = _runtimeMap.value - uuid
                updateInfos()
            }
        }
    }

    /**
     * 调度执行
     */
    private suspend fun dispatch(uuid: String) {
        val runtime = _runtimeMap.value[uuid] ?: return

        while (runtime.currentStepIndex < runtime.req.stepChain.size) {
            if (runtime.isCanceled) return
            if (runtime.isPaused) return

            val actionName = runtime.req.stepChain[runtime.currentStepIndex]
            val action = DownloadActionRegistry.get(actionName)
                ?: throw IllegalStateException("Action not found: $actionName")

            runtime.state = DownloadState.DOING
            updateInfos()

            try {
                action.execute(runtime)

                // 检查执行结果
                when (runtime.state) {
                    DownloadState.STEP_COMPLETE -> {
                        // 当前步骤完成，进入下一步
                        runtime.currentStepIndex++
                        runtime.state = DownloadState.WAITING
                    }
                    DownloadState.PAUSED -> {
                        updateInfos()
                        return
                    }
                    DownloadState.ERROR -> {
                        updateInfos()
                        return
                    }
                    DownloadState.CANCEL -> {
                        return
                    }
                    else -> {
                        // 继续
                    }
                }
            } catch (e: Exception) {
                runtime.reportError(e)
                updateInfos()
                return
            }
        }

        // 全部步骤完成
        runtime.complete()
        updateInfos()

        // 清理
        for (actionName in runtime.req.stepChain) {
            val action = DownloadActionRegistry.get(actionName)
            action?.onTaskComplete(runtime)
        }

        // 通知完成
        onComplete(runtime.req)

        // 移除运行时
        mutex.withLock {
            _runtimeMap.value = _runtimeMap.value - uuid
            updateInfos()
        }
    }

    /**
     * 更新下载信息列表
     */
    private fun updateInfos() {
        val reqs = _downloadInfos.value.map { it.req }
        val map = _runtimeMap.value
        _downloadInfos.value = reqs.map { req ->
            DownloadInfo(req, map[req.uuid])
        } + map.values.filter { runtime ->
            reqs.none { it.uuid == runtime.req.uuid }
        }.map { runtime ->
            DownloadInfo(runtime.req, runtime)
        }
    }

    /**
     * 设置初始请求列表（从持久化加载）
     */
    fun setInitialRequests(requests: List<DownloadReq>) {
        scope.launch {
            mutex.withLock {
                _downloadInfos.value = requests.map { DownloadInfo(it, null) }
            }
        }
    }
}
