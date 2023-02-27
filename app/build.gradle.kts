
import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.Config
import com.heyanle.buildsrc.accompanist
import com.heyanle.buildsrc.androidXBasic
import com.heyanle.buildsrc.androidXWebkit
import com.heyanle.buildsrc.appCenter
import com.heyanle.buildsrc.cling
import com.heyanle.buildsrc.coil
import com.heyanle.buildsrc.coilGif
import com.heyanle.buildsrc.commonsText
import com.heyanle.buildsrc.compose
import com.heyanle.buildsrc.easyPlayer
import com.heyanle.buildsrc.exoplayer
import com.heyanle.buildsrc.exoplayerRtmp
import com.heyanle.buildsrc.glide
import com.heyanle.buildsrc.gson
import com.heyanle.buildsrc.jsoup
import com.heyanle.buildsrc.junit
import com.heyanle.buildsrc.leakcanary
import com.heyanle.buildsrc.media
import com.heyanle.buildsrc.navigationCompose
import com.heyanle.buildsrc.okhttp3
import com.heyanle.buildsrc.okkv2
import com.heyanle.buildsrc.okkv2Compose
import com.heyanle.buildsrc.paging
import com.heyanle.buildsrc.pagingCompose
import com.heyanle.buildsrc.room
import com.heyanle.buildsrc.roomPaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.heyanle.easybangumi4"
    compileSdk = Android.compileSdk

    defaultConfig {
        applicationId = "com.heyanle.easybangumi4"
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas".toString()
            }
        }

        buildConfigField(
            "String",
            Config.APP_CENTER_SECRET,
            "\"${Config.getPrivateValue(Config.APP_CENTER_SECRET)}\""
        )
    }


    packagingOptions {
        resources.excludes.add("META-INF/beans.xml")
    }

    buildTypes {
        release {
            postprocessing {
                isRemoveUnusedCode = true
                isRemoveUnusedResources = true
                isObfuscate = false
                isOptimizeCode = true
                proguardFiles("proguard-rules.pro")
            }
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
    okkv2Compose()
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
    gson()
    jsoup()
    androidXWebkit()
    commonsText()
    cling()
    implementation(project(":easy-dlna"))
    implementation(project(":easy-crasher"))
    implementation(project(":easy-i18n"))
    implementation(project(":extension:extension-core"))
    implementation(project(":source:source-api"))
    implementation(project(":source:source-utils"))
    implementation(project(":extension:extension-api"))
}