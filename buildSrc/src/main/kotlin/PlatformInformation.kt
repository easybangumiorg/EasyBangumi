import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

// 暂不考虑交叉编译，编译环境直接获取平台信息
object PlatformInformation {

    const val OS_MAC = "macos"
    const val OS_WINDOWS = "windows"
    const val OS_LINUX = "linux"

    const val ARCH_X64 = "x64"

    const val ARCH_ARM64 = "arm64"


    val hostOs: String by lazy {
        val osName = System.getProperty("os.name").toDefaultLowerCase()
        when {
            "mac" in osName || "os x" in osName || "darwin" in osName -> OS_MAC
            "windows" in osName -> OS_WINDOWS
            "linux" in osName -> OS_LINUX
            else -> throw Error("Unknown OS $osName")
        }
    }

    val hostArch: String by lazy {
        val osArch = System.getProperty("os.arch").toDefaultLowerCase()
        when  {
            "x86_64" in osArch || "amd64" in osArch -> ARCH_X64
            "arm64" in osArch || "aarch64" in osArch -> ARCH_ARM64
            else -> throw Error("Unknown arch $osArch")
        }
    }

    val platformName: String
        get() = "Desktop ${hostOs} (${hostArch})"

    val isAndroid: Boolean
        get() = false

    val isIos: Boolean
        get() = false

    val isDesktop: Boolean
        get() = true
}