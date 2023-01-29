import com.heyanle.buildsrc.*
import org.gradle.kotlin.dsl.project

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.heyanle.easybangumi"
    compileSdk = Android.compileSdk

    defaultConfig {
        applicationId = "com.heyanle.easybangumi"
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }


        buildConfigField("String",Config.APP_CENTER_SECRET, "\"${Config.getPrivateValue(Config.APP_CENTER_SECRET)}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }

}

dependencies {
    glide()
    okkv2()
    okhttp3()
    androidXBasic()
    leakcanary()
    paging()
    pagingCompose()
    junit()
    easyPlayer()
    compose()
    accompanist()
    navigationCompose()
    coil()
    coilGif()
    exoplayer()
    exoplayerRtmp()
    media()
    easyPlayer()
    room()
    roomPaging()
    appCenter()
    implementation(project(":easy-crasher"))
    implementation(project(":lib-anim"))
}