val localPropertiesFile = project.rootProject.file("local.properties")
val localProperties = java.util.Properties().apply {
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}
// 优先级：环境变量 > local.properties > gradle.properties

fun findProperty(key: String): String {
    return System.getenv(key)
        ?: localProperties.getProperty(key)
        ?: project.findProperty(key)?.toString()
        ?: ""
}

extra["bangumiAppId"] = findProperty("bangumi.app.id")
extra["bangumiAppSecret"] = findProperty("bangumi.app.secret")
extra["bangumiAppCallbackUrl"] = findProperty("bangumi.app.callback.url")
extra["namespace"] = project.findProperty("easy.build.namespace").toString()
extra["applicationId"] = project.findProperty("easy.build.applicationId").toString()
extra["versionCode"] = project.findProperty("easy.build.versionCode").toString().toInt()
extra["versionName"] = project.findProperty("easy.build.versionName").toString()




