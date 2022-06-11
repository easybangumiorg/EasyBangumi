/**
 * Created by HeYanLe on 2022/6/5 15:24.
 * https://github.com/heyanLE
 */
object AndroidConfig {
    const val compileSdk = 32
    const val buildToolsVersion = "32.1.0-rc1"
    val defaultConfig = DefaultConfig()

    class DefaultConfig {
        val minSdk = 21
        val targetSdk = 32
        val versionCode = 21
        val versionName = "2.0.1"
    }
}