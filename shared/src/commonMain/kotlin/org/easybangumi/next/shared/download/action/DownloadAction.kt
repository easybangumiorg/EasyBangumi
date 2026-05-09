package org.easybangumi.next.shared.download.action

import org.easybangumi.next.shared.download.model.DownloadRuntime

/**
 * 可插拔的下载动作接口
 * 不同平台可以注册不同的实现
 */
interface DownloadAction {

    /**
     * 动作名称，用于下载链配置
     */
    val name: String

    /**
     * 是否异步动作
     * 异步动作 push 后立即返回，通过回调通知完成
     * 同步动作 push 后阻塞直到完成
     */
    fun isAsync(): Boolean

    /**
     * 是否可以从上次中断处恢复
     */
    suspend fun canResume(req: org.easybangumi.next.shared.download.model.DownloadReq): Boolean

    /**
     * 执行下载动作
     */
    suspend fun execute(runtime: DownloadRuntime)

    /**
     * 暂停（仅异步 action 支持）
     * @return true 表示成功暂停
     */
    suspend fun pause(runtime: DownloadRuntime): Boolean

    /**
     * 恢复
     * @return true 表示成功恢复
     */
    suspend fun resume(runtime: DownloadRuntime): Boolean

    /**
     * 取消
     */
    suspend fun cancel(runtime: DownloadRuntime)

    /**
     * 任务完成后的清理
     */
    suspend fun onTaskComplete(runtime: DownloadRuntime)
}
