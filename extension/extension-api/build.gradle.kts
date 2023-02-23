import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.*
import org.jetbrains.kotlin.gradle.utils.loadPropertyFromResources
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.load.kotlin.signatures

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.heyanle.extension_api"
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val publishingProps = Properties()
runCatching {
    publishingProps.load(project.rootProject.file("extension/extension-api/publishing/publishing.properties").inputStream())
}.onFailure {
    it.printStackTrace()
}


afterEvaluate {
    publishing {
        publications {
            create("maven_public", MavenPublication::class) {
                groupId = "io.github.easybangumiorg"
                artifactId = "extension-api"
                version = Extension.LIB_VERSION_NAME + "-SNAPSHOT"
                from(components.getByName("release"))

                pom {
                    name.set("EasyBangumi extension api")
                    description.set("ExtensionApi for EasyBangumi")
                    url.set("https://github.com/easybangumiorg/EasyBangumi.git")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("Heyanle")
                            name.set("Heyanle")
                            url.set("https://heyanle.com")
                        }
                    }

                    scm {
                        url.set("https://github.com/easybangumiorg/EasyBangumi.git")
                    }
                }

            }
        }

        repositories {
            maven {
                // change to point to your repo
                val snapshotRepo = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = uri(snapshotRepo)
                credentials {
                    username = publishingProps.getProperty("credencial.username", "")
                    password = publishingProps.getProperty("credencial.password", "")
                }
            }
            maven {
                name = "build"
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }

    }
    val keyId = publishingProps.getProperty("signing.keyId", "")
    val password = publishingProps.getProperty("signing.password", "")
    val secretKeyRingFile = publishingProps.getProperty("signing.secretKeyRingFile", "")

    //project.loadPropertyFromResources()
    if (keyId?.isNotEmpty() == true && password?.isNotEmpty() == true && secretKeyRingFile?.isNotEmpty() == true){
//        (project.properties as MutableMap<String, Any>).apply {
//            put("signing.keyId", keyId)
//            put("signing.password", password)
//            put("signing.secretKeyRingFile", secretKeyRingFile)
//        }

        val s = file(secretKeyRingFile).readText()
        //println(s)
        signing {
            useInMemoryPgpKeys(s, password)
            sign(publishing.publications.getByName("maven_public"))
        }
    }
}


dependencies {
    api(okhttp3)
    api(jsoup)
    api(project(":source-api"))
    api(project(":source-utils"))
}