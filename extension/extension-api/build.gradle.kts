import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.*

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
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

afterEvaluate {
    publishing {
        publications {
            create("maven_public", MavenPublication::class) {
                groupId = "com.heyanle"
                artifactId = "extension-api"
                version = Extension.LIB_VERSION_NAME
                from(components.getByName("release"))
            }
        }
    }
}

dependencies {
    api(okhttp3)
    api(jsoup)
    api(project(":source-api"))
    api(project(":source-utils"))
}