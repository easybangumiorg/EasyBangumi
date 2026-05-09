package org.easybangumi.next.shared.download.action

/**
 * 下载动作注册表
 * 平台特定模块注册各自的 DownloadAction 实现
 */
object DownloadActionRegistry {

    private val actions = mutableMapOf<String, DownloadAction>()

    /**
     * 注册下载动作
     */
    fun register(action: DownloadAction) {
        actions[action.name] = action
    }

    /**
     * 获取下载动作
     */
    fun get(name: String): DownloadAction? {
        return actions[name]
    }

    /**
     * 获取所有已注册的动作名称
     */
    fun registeredNames(): Set<String> {
        return actions.keys.toSet()
    }

    /**
     * 清空所有注册（主要用于测试）
     */
    fun clear() {
        actions.clear()
    }
}
