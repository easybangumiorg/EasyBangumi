抽象文件系统

```kotlin
// 抽象文件描述 （抽象路径）
data class UFD(
    val type: String,
    // 语义随着 type 不同有不同的诠释
    val uri: String,
){
    companion object {
        const val TYPE_OKIO = "okio"
        const val TYPE_JVM = "jvm"
        const val TYPE_ANDROID_UNI = "android_uni"
    }
}

```

* okio 使用 okio 进行读写，调用 uri.toPath 获取 path
* jvm 使用 java.io 进行读写，使用 File(uri) 获取 File
* android_uni 使用 Android UniFile 库进行读写，使用 UniFile.fromUri(context, Uri.parse(uri)) 获取

Okio: https://github.com/square/okio
UniFile: https://github.com/easybangumiorg/UniFile


对于业务内置路径（例如数据缓存目录），以上三种 type 都可使用，但在 Android 环境只能使用沙盒内目录。  
对于用户手动指定路径（例如下载目录指定），Android 环境只能使用 Android UniFile，并且需要通过 saf 授权获取目录唯一 uri。