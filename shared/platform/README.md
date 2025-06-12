依赖倒置，顶层模块需要向 koin 注入实现，便于给 Shared 业务使用

```kotlin

// for common
expect interface Platform {
    val platformType: PlatformType
    val platformName: String
    val isDebug: Boolean
    val versionCode: Int
    val versionName: String
}

// for android
actual interface Platform {
    actual val platformType: PlatformType
    actual val platformName: String
    actual val isDebug: Boolean
    actual val versionCode: Int
    actual val versionName: String
    val sdkCode : Int
}

// for desktop
actual interface Platform {
    actual val platformName: String
    actual val platformType: PlatformType
    actual val isDebug: Boolean
    actual val versionCode: Int
    actual val versionName: String
    val hostOs: DesktopHostOs
    val hostArch: DesktopHostArch
}

enum class DesktopHostOs(name: String) {
    MacOS("macos"),
    Windows("windows"),
    Linux("linux")
}

enum class DesktopHostArch(name: String) {
    X64("x64"),
    ARM64("arm64")
}


```