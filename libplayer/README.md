播放器相关，独立代码层级
不允许依赖任何项目内代码（除了 logger），后续可能会单独抽离仓库

只负责处理视频帧播放相关并对外提供播放控制功能。播放器 UI 相关在业务层实现。
`:shared:player`

### api

KMP 模块，播放器相关的 API 接口，主要用于在 Common 中获取视频信息、播放状态等。

```kotlin
/**
 * 播放器桥接接口，用于在 Common 层中与播放器进行交互
 * 各平台需要自己实现
 * Created by heyanlin on 2025/5/27.
 */
interface PlayerBridge: AutoCloseable {

    interface Factory {
        fun create(): PlayerBridge
    }

    val impl: Any

    val playStateFlow: StateFlow<C.State>

    fun prepare(mediaItem: MediaItem)

    val playWhenReadyFlow: StateFlow<Boolean>
    fun setPlayWhenReady(playWhenReady: Boolean)

    val videoSizeFlow: StateFlow<VideoSize>

    // Playback control
    fun seekTo(positionMs: Long)

    // 不支持回调，业务自己缓存到 vm
    val positionMs: Long
    val bufferedPositionMs: Long
    val durationMs: Long

    // Renderer containers
    fun setScaleType(scaleType: C.RendererScaleType)
    val scaleTypeFlow: StateFlow<C.RendererScaleType>

    fun <A: Action> action(): A?
    
}
```

### exoplayer

AAR 模块，给 Android 平台使用，使用 ExoPlayer 播放器核心

```kotlin

```

### vlcj

JAR 模块，给 Desktop 平台使用，使用 VLCJ 封装的 VLC 播放器核心。

该模块不负责引入相关二进制文件，顶层业务需要自己引入。

具体到纯纯看番，使用 appResource 引入，可参考 `org.easybangumi.next.vlcj.AppResourceDirectoryProvider`

参考代码：

```kotlin
val url = "http://vjs.zencdn.net/v/oceans.mp4"
private val logger = logger("PlayerDebug")
@Composable
actual fun DebugScope.PlayerDebug() {

    val manager = koinInject<VlcjBridgeManager>()
    val tag = remember {
        "debug-player"
    }
    val frameState = rememberVlcjPlayerFrameState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState = lifecycle.currentStateAsState()
    val bridge = remember {
        manager.getOrCreateBridge(tag, null)
    }

    DisposableEffect(Unit) {
        onDispose {
            logger.info("PlayerDebug disposing bridge: $tag")
            // 销毁时释放资源
            frameState.unbindBridge()
            manager.release(tag)
        }
    }
    LaunchedEffect(Unit) {
        val mediaItem = MediaItem(uri = url)
        bridge.prepare(mediaItem)
        snapshotFlow {
            lifecycleState.value
        }.collect {
            logger.info("PlayerDebug lifecycle state: $it")
            when (it) {
                // 最小化
                Lifecycle.State.CREATED -> {
                    bridge.setPlayWhenReady(false)
                }
                // 没有焦点
                Lifecycle.State.STARTED -> {
                    // 创建时绑定桥接
                    frameState.bindBridge(bridge)
                }
                // 有焦点
                Lifecycle.State.RESUMED -> {
                    bridge.setPlayWhenReady(true)
                }

                Lifecycle.State.INITIALIZED -> {
                    // 接收不到，因为此时 Compose 还没有创建
                }
                Lifecycle.State.DESTROYED -> {
                    // 接收不到，因为这时 Compose 已经销毁了
                }
            }
        }
    }



    VlcjPlayerFrame(
        modifier = Modifier.fillMaxSize(),
        state = frameState,
    )


}
```
