val localPropertiesFile = project.rootProject.file("local.properties")
val localProperties = java.util.Properties().apply {
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun findProperty(key: String): String {
    return System.getenv(key)
        ?: localProperties.getProperty(key)
        ?: ""
}

val KEY_BANGUMI_APP_ID = "bangumi.app.id"
val KEY_BANGUMI_APP_SECRET = "bangumi.app.secret"
val KEY_BANGUMI_APP_CALLBACK_URL = "bangumi.app.callback.url"

extra["bangumiAppId"] = findProperty(KEY_BANGUMI_APP_ID)
extra["bangumiAppSecret"] = findProperty(KEY_BANGUMI_APP_SECRET)
extra["bangumiAppCallbackUrl"] = findProperty(KEY_BANGUMI_APP_CALLBACK_URL)



