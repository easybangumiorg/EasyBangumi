## 纯纯看番番剧源

源本身的 API 请看二次开发文档（开发中）    
本文档主要介绍纯纯看番提供给源的几个工具  



### NetworkHelper

须知： 所有涉及到网络请求的部分，不管是用 Okhttp 还是 WebView ，都公用一套 CookieManager，会自动存取，当然也可以手动存取 
使用 `networkHelper` 获取实例  

请求可以直接使用 Request.kt 中的方法

- cookieManager 全局 Cookie 管理器
- defaultLinuxUA 和 defaultUA 分别是手机自带的 UA 和 linux 系统的 UA，如果不指定则使用手机自带啊
- cloudflareClient 支持静默 Cloudflare 处理的 Client，处理后的 Cookie 全局共享
- cloudflareUserClient 支持显式 Cloudflare 处理（打开一个新页面显式浏览器，可以让用户做人机检测）处理的 Client
- client 普通 Client

### StringHelper

使用 `stringHelper` 获取实例  

- `fun moeSnackBar(string: String)` 以 snackBar 展示文字，这里是 纯纯看番自定义样式，顶部通知
- `fun toast(string: String)` 以 Toast 展示文字

### FileHelper

管理每个 Key 的文件  
使用 `fileHelper` 获取实例  

- `fun getFile(parserKey: String, fileName: String)` 根据 fileName 获取文件，这里不会创建文件，需要自行创建并写入，不保证文件存在，每个 源同时只允许存在 10 个文件，超出则使用 LRU 算法删除最旧文件  
- `fun File.getUri(): String`  返回 uri，可以在 getPlayMsg 接口返回，exoPlayer 支持播放文件

### WebViewUserHelper  

打开一个界面显示网页  
使用 `webViewUserHelper` 获取实例  

其中创建 webview 需要的上下文可以从 AppHelper 获取

```kotlin
/**
 * 启动一个界面显示网页
 * webView： 要显示的 WebView 这里需要自己创建并配置
 * check：判断网页是否加载完成（每秒会执行一次，返回 true 则会直接关闭页面）
 * onStop：当页面关闭时的回调
 */
fun start(
        webView: WebView,
        check: (WebView) -> Boolean,
        onStop: (WebView) -> Unit,
    )
```

### WebViewHelper

使用后台 webview 加载网页  
使用 `webViewHelper` 获取实例  

```kotlin
/**
     * 获取已渲染网页源码
     *
     * @param callBackRegex 回调正则。在检测到特定请求时返回结果。默认为空则在页面加载完成后自动回调（因为ajax等因素可能得到的源码不完整，另外注意超时）
     * @param actionJs 在页面加载完成后执行的js代码，可用于主动加载资源，如让视频加载出来以拦截
     * @param timeOut 加载超时。当超过超时时间后还没返回数据则会直接返回当前源码
     */
    suspend fun getRenderedHtmlCode(
        url: String,
        callBackRegex: String = "",
        encoding: String = "utf-8",
        userAgentString: String? = null,
        header: Map<String, String>? = null,
        actionJs: String? = null,
        timeOut: Long = 8000L
    ): String

    /**
     * 拦截资源
     *
     * @param regex 回调正则，在检测到特定请求时返回结果
     * @param actionJs 在页面加载完成后执行的js代码，可用于主动加载资源，如让视频加载出来以拦截
     * @param timeOut 加载超时。当超过超时时间后还没返回数据则会直接返回当前源码
     */
    suspend fun interceptResource(
        url: String,
        regex: String,
        userAgentString: String? = null,
        header: Map<String, String>? = null,
        actionJs: String? = null,
        timeOut: Long = 8000L
    ): String

    /**
     * 拦截Blob数据
     *
     * @param regex 回调正则，在检测到特定Blob数据符合时返回结果
     * @param actionJs 在页面加载完成后执行的js代码，可用于主动加载资源，如让视频加载出来以拦截
     * @param timeOut 加载超时。当超过超时时间后还没返回数据则会直接返回当前源码
     */
    suspend fun interceptBlob(
        url: String,
        regex: String,
        userAgentString: String? = null,
        header: Map<String, String>? = null,
        actionJs: String? = null,
        timeOut: Long = 8000L
    ): String

```

