import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.androidXBasic
import com.heyanle.buildsrc.easyPlayer
import com.heyanle.buildsrc.easyPlayerExo
import com.heyanle.buildsrc.exoplayer
import com.heyanle.buildsrc.exoplayerRtmp

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.heyanle.easy_bangumi_player"
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = com.heyanle.buildsrc.Android.minSdk
        targetSdk = com.heyanle.buildsrc.Android.targetSdk

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
    buildFeatures{
        viewBinding = true
    }
}


dependencies {
    androidXBasic()
    easyPlayerExo()
    easyPlayer()
}