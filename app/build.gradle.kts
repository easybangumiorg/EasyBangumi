import com.heyanle.buildsrc.accompanist
import com.heyanle.buildsrc.androidXBasic
import com.heyanle.buildsrc.compose
import com.heyanle.buildsrc.easyPlayer
import com.heyanle.buildsrc.glide
import com.heyanle.buildsrc.junit
import com.heyanle.buildsrc.leakcanary
import com.heyanle.buildsrc.okhttp3
import com.heyanle.buildsrc.okkv2
import com.heyanle.buildsrc.paging
import com.heyanle.buildsrc.navigationCompose
import com.heyanle.buildsrc.swipeRefreshLayout
import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.coil
import com.heyanle.buildsrc.coilGif
import com.heyanle.buildsrc.media3Exo
import com.heyanle.buildsrc.pagingCompose

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
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
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),  "proguard-rules.pro")
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

    media3Exo()
    implementation(project(":easy-crasher"))
    implementation(project(":lib-anim"))
    implementation(project(":player-controller"))
}