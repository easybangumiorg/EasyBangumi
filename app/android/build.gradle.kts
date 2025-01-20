plugins {
    alias(builds.plugins.androidApplication)
    alias(builds.plugins.kotlinCompose)
    alias(builds.plugins.kotlinAndroid)
    alias(builds.plugins.ksp)
}

group = AppConfig.namespace
version = AppConfig.versionName

android {
    namespace =  AppConfig.namespace
    compileSdk = 34

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    kotlinOptions {
        jvmTarget = "17"
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


    implementation(projects.app.shared)
    implementation(projects.app.shared.utils)
    implementation(projects.inject)

    implementation(projects.javascript)



}