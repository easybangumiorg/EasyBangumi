js 加载引擎为独立代码层级  
不允许依赖任何项目内代码，后续可能会单独抽离仓库

### Rhino

纯 Java 层实现的引擎，将 JavaScript 代码翻译成字节码添加到虚拟机中。
因为本质上也是在虚拟机中运行，因此数据交互性能较高，不涉及任何数据拷贝。

而资源嗅探过程需要频繁和 Java 层交互，因此暂时先选用 Rhino 引擎，后续需要支持 ios 在考虑其他引擎。



#### 初始化

使用需要外部注入 RhinoService，目前有直接注册和服务发现两种方式。

```kotlin
interface RhinoService {
    fun getSingletonDispatcher(): CoroutineDispatcher
    // 使用 slf4j Logger
    fun getLogger(tag: String): Logger
}
```

* 直接注册：在使用前调用 `RhinoService.init(impl)` 注册
* 服务发现：在 `resources/META-INF/services/org.easybangumi.next.rhino.RhinoService` 文件中添加实现类的全限定名

具体到纯纯看番，使用服务发现。具体方式可参考 `:Shared:Plugin` 模块，

#### RhinoRuntime

RhinoRuntime 为 Rhino 引擎运行时的封装，一个 RhinoRuntime 对应一个 CoroutineDispatcher，这里需要保证该 Dispatcher 为单线程模型。

一个 RhinoRuntime 可以运行多个 RhinoScope

使用引用计数法，当 RhinoScope 计数为 0 时自动释放资源。


#### RhinoScope

RhinoScope 为 Rhino 引擎的作用域，一个 RhinoScope 对应一个 JavaScript 作用域。

需要传入 RhinoRuntime 使用。

```kotlin
val rhinoRuntime = RhinoRuntime()
val rs = RhinoScope(rhinoRuntime)
rs.init()
val res1 = rs.runWithScope { ctx, scriptable ->
    // 直接执行代码，注意，因为 Rhino 引擎直接操作的虚拟机
    // 因此返回值可能直接是对象而不是封装
    ctx.evaluateString(
        scriptable,
        "{{JavaScript code here}}",
        "{{ScriptName for logging}}",
        1, 0
    ).jsUnwrap<Any?>()
}

val res2 = rs.runWithScope { ctx, scriptable ->
    // 直接执行输入流
    if (param.ufd != null) {
        val file = UniFileFactory.fromUFDOrThrow(param.ufd)
        ctx.evaluateReader(
            scriptable,
            // file reader
            file.openSource().buffer().inputStream().reader(),
            "Rhino ${param.ufd} Source(${source.manifest.id})",
            1, 0
        ).jsUnwrap<Any?>()
    }
}

// 寻找钩子函数
val rf = rs.findFunction("functionName")
if (rf != null) {
    // 函数调用
    val result = rs.callFunction(rf, arrayOf("arg1", "arg2")).jsUnwrap<Any>()
} else {
    // 函数不存在
}
rs.release() // 释放资源
```

