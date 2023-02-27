import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.SourceExtension
import com.heyanle.buildsrc.androidXWebkit
import com.heyanle.buildsrc.commonsText
import com.heyanle.buildsrc.gson
import com.heyanle.buildsrc.jsoup
import com.heyanle.buildsrc.kotlinx_coroutines
import com.heyanle.buildsrc.okhttp3
import com.heyanle.buildsrc.okhttp3logging
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.heyanle.lib_anim"
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
        targetSdk = Android.compileSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    publishingProps.load(project.rootProject.file("publishing/publishing.properties").inputStream())
}.onFailure {
    //it.printStackTrace()
}


afterEvaluate {
    publishing {
        publications {
            create("maven_public", MavenPublication::class) {
                groupId = "io.github.easybangumiorg"
                artifactId = "source-utils"
                version = SourceExtension.LIB_VERSION
                from(components.getByName("release"))

                pom {
                    name.set("EasyBangumi source utils")
                    description.set("SourceUtils for EasyBangumi")
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
                val releaseRepo = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotRepo = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = uri(snapshotRepo)
                credentials {
                    username = publishingProps.getProperty("credencial.username", System.getenv("OSSRH_USERNAME"))
                    password = publishingProps.getProperty("credencial.password", System.getenv("OSSRH_PASSWORD"))
                }
            }
            maven {
                name = "build"
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }

    }
    val keyId = publishingProps.getProperty("signing.keyId", System.getenv("SIGNING_KEY_ID"))
    val password = publishingProps.getProperty("signing.password", System.getenv("SIGNING_PASSWORD"))
    val secretKeyRingFile = publishingProps.getProperty("signing.secretKeyRingFile", "")

    //project.loadPropertyFromResources()
    if (keyId?.isNotEmpty() == true && password?.isNotEmpty() == true){
//        (project.properties as MutableMap<String, Any>).apply {
//            put("signing.keyId", keyId)
//            put("signing.password", password)
//            put("signing.secretKeyRingFile", secretKeyRingFile)
//        }

        val s = runCatching {
            if(secretKeyRingFile.isNotEmpty()){
                project.rootProject.file("publishing/"+secretKeyRingFile).readText()
            }else{
                throw IllegalAccessException()
            }
        }.getOrElse {
            System.getenv("SIGNING_SECRET_KEY")
        }
        //println(s)
        signing {
            useInMemoryPgpKeys(s, password)
            sign(publishing.publications.getByName("maven_public"))
        }
    }
}




dependencies {
    compileOnly(androidXWebkit)
    compileOnly(kotlinx_coroutines)
    compileOnly(jsoup)
    compileOnly(okhttp3)
    compileOnly(okhttp3logging)
    compileOnly(gson)
    compileOnly(commonsText)
}
