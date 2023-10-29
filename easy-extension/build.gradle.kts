plugins {
    alias(build.plugins.android.library)
    alias(build.plugins.kotlin.android)
}

android {
    namespace = "com.heyanle.extension_load"
    compileSdk = com.heyanle.buildsrc.Android.compileSdk

    defaultConfig {
        minSdk = com.heyanle.buildsrc.Android.minSdk
        targetSdk = com.heyanle.buildsrc.Android.targetSdk
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(androidx.appcompat)
    implementation(extension.source.utils)
    implementation(extension.source.api)
    implementation(extension.extension.api)
    implementation(project(":easy-i18n"))

}