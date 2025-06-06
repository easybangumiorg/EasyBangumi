import com.android.build.gradle.tasks.MergeSourceSetFolders

plugins {
    alias(builds.plugins.androidApplication)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.kotlinAndroid)
    alias(builds.plugins.ksp)
    id("AndroidAppResource")
}

group = AppConfig.namespace
version = AppConfig.versionName

android {
    namespace =  AppConfig.namespace
    compileSdk = 35

    defaultConfig {

        applicationId = AppConfig.namespace
        minSdk = 21
        targetSdk = 34
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName

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
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
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

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.preference.ktx)

    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.logback.android)


    implementation(projects.shared)


}


easyAndroidAssets {
    assetsDir = project.layout.projectDirectory.dir("../assets")
}