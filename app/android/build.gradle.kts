import com.android.build.gradle.tasks.MergeSourceSetFolders
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

apply(from = "../build_common/property_loader.gradle.kts")

plugins {
    alias(builds.plugins.androidApplication)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.kotlinAndroid)
    alias(builds.plugins.ksp)
    id("AndroidAppResource")
    id("EasyLibBuild")
}


android {
    defaultConfig {
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "BANGUMI_APP_ID", "\"${extra["bangumiAppId"]}\"")
        buildConfigField("String", "BANGUMI_APP_SECRET", "\"${extra["bangumiAppSecret"]}\"")
        buildConfigField("String", "BANGUMI_APP_CALLBACK_URL", "\"${extra["bangumiAppCallbackUrl"]}\"")
    }
    packaging {
        resources.excludes.add("META-INF/beans.xml")
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles("proguard-rules.pro")
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles("proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }


}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}


dependencies {

    api(libs.androidx.appcompat)
    api(libs.androidx.activity.compose)
    api(libs.preference.ktx)

    api(libs.koin.core)
    api(libs.koin.android)

    api(libs.logback.android)


    api(projects.shared)

    api(projects.libplayer.libplayerExoplayer)
    api(libs.media3.exoplayer)


}


easyAndroidAssets {
    assetsDir = project.layout.projectDirectory.dir("../assets")
}

