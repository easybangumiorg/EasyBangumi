import com.heyanle.buildsrc.Android

plugins {
    alias(build.plugins.android.library)
    alias(build.plugins.kotlin.android)
}

android {
    namespace = "com.heyanle.easy_crasher"
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
        targetSdk = Android.compileSdk

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":lib_upnp"))


}