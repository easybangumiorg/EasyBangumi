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
    implementation(project(":easy-crasher"))
    implementation(project(":source-core"))
    implementation(project(":source-api"))
    implementation(project(":easy-dlna"))
    implementation(project(":easy-i18n"))
    implementation(project(":extension:extension-api"))
    implementation(project(":extension:extension-load"))
}