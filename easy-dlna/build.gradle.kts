import com.heyanle.buildsrc.cling
import com.heyanle.buildsrc.jetty
import com.heyanle.buildsrc.junit
import com.heyanle.buildsrc.servlet

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.heyanle.easy_dlna"
    compileSdk = com.heyanle.buildsrc.Android.compileSdk

    defaultConfig {
        minSdk = com.heyanle.buildsrc.Android.minSdk
        targetSdk = com.heyanle.buildsrc.Android.targetSdk

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
}

dependencies {
    cling()
    //jetty()
    //servlet()
    implementation(files("libs/javax.servlet-3.0.0.v201103241009.jar"))
    implementation(files("libs/jetty-client-8.1.9.v20130131.jar"))
    implementation(files("libs/jetty-continuation-8.1.9.v20130131.jar"))
    implementation(files("libs/jetty-http-8.1.9.v20130131.jar"))
    implementation(files("libs/jetty-io-8.1.9.v20130131.jar"))
    implementation(files("libs/jetty-security-8.1.9.v20130131.jar"))
    implementation(files("libs/jetty-server-8.1.9.v20130131.jar"))
    implementation(files("libs/jetty-servlet-8.1.9.v20130131.jar"))
    implementation(files("libs/jetty-util-8.1.9.v20130131.jar"))
}