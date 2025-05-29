import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

object PlatformInformation {

    const val OS_MAC = "macos"
    const val OS_WINDOWS = "windows"
    const val OS_LINUX = "linux"

    const val ARCH_X64 = "x64"

    const val ARCH_ARM64 = "arm64"


    val hostOs: String by lazy {
        val osName = System.getProperty("os.name")
        when {
            "mac" in osName || "os x" in osName || "darwin" in osName -> OS_MAC
            "windows" in osName -> OS_WINDOWS
            "linux" in osName -> OS_LINUX
            else -> throw Error("Unknown OS $osName")
        }
    }

    val hostArch: String by lazy {
        when (val osArch = System.getProperty("os.arch").toDefaultLowerCase()) {
            "x86_64", "amd64" -> ARCH_X64
            "arm64", "aarch64" -> ARCH_ARM64
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