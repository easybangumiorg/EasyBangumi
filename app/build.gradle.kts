import com.heyanle.buildsrc.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.heyanle.easybangumi"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation(project(":lib-anim"))
    implementation(project(":easy-crasher"))
    implementation(project(":easy-lightdark"))
    implementation(project(":easy-media"))
    implementation(project(":easy-view"))
    glide()
    okkv2()
    okhttp3()
    androidXBasic()
    leakcanary()
    paging()
    junit()
    swipeRefreshLayout()

}