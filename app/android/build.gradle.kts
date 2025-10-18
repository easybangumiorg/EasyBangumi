import com.android.build.gradle.tasks.MergeSourceSetFolders

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

    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
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

