
import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.Config

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

    val androidx_activity_ktx = "1.5.0"
    val androidx_fragment_ktx = "1.5.3"
    val androidx_core_ktx = "1.9.0"
    val androidx_appcompat = "1.5.0"
    val androidx_room = "2.4.2"
    val google_material = "1.6.1"
    val jsoup = "1.14.3"
    val okhttp3 = "4.10.0"
    val paging = "3.1.1"
    val gson = "2.9.0"
    val leakcanary = "2.7"
    val glide = "4.12.0"
    val okkv2 = "1.2.4"
    val androidx_lifecycle_runtime_ktx = "2.5.1"
    val exoplayer = "2.18.1"
    val easy_player = "2.0"
    val compose = "1.0.1"
    val accompanist = "0.28.0"
    val navigation_compose = "2.5.3"
    val compose_runtime = "1.3.2"
    val coil = "2.2.2"
    val media = "1.6.0"
    val app_center = "4.4.5"
    val androidx_webkit = "1.5.0"
    val commons_text = "1.10.0"
    val cling = "2.1.2"
    
    implementation("com.github.bumptech.glide:glide:${glide}")
    implementation("com.github.heyanLE.okkv2:okkv2-mmkv:${okkv2}")
    implementation("com.squareup.okhttp3:okhttp:${okhttp3}")
    implementation("com.squareup.okhttp3:logging-interceptor:${okhttp3}")

    implementation("androidx.core:core-ktx:${androidx_core_ktx}")
    implementation("androidx.appcompat:appcompat:${androidx_appcompat}")
    implementation("com.google.android.material:material:${google_material}")
    implementation("androidx.activity:activity-ktx:${androidx_activity_ktx}")
    implementation("androidx.fragment:fragment-ktx:${androidx_fragment_ktx}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${androidx_lifecycle_runtime_ktx}")

    implementation( "com.squareup.leakcanary:leakcanary-android:${leakcanary}")

    implementation("androidx.paging:paging-runtime-ktx:${paging}")
    implementation("androidx.paging:paging-compose:1.0.0-alpha18")
    implementation("com.github.heyanLE.EasyPlayer:eplayer-core:${easy_player}")

    implementation("androidx.compose.ui:ui:${compose}")
    implementation("androidx.compose.ui:ui-graphics:${compose}")
    implementation("androidx.compose.ui:ui-tooling-preview:${compose}")
    implementation("androidx.compose.material3:material3:${compose}")
    implementation("androidx.compose.material:material-icons-core:${compose}")
    implementation("androidx.compose.material:material-icons-extended:${compose}")

    debugImplementation("androidx.compose.ui:ui-tooling:${compose}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${compose}")

    implementation("androidx.compose.runtime:runtime-livedata:${compose_runtime}")

    implementation("com.google.accompanist:accompanist-navigation-animation:${accompanist}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${accompanist}")
    implementation("com.google.accompanist:accompanist-pager:${accompanist}")
    implementation("com.google.accompanist:accompanist-pager-indicators:${accompanist}")
    implementation("com.google.accompanist:accompanist-swiperefresh:${accompanist}")
    implementation("com.google.accompanist:accompanist-insets:${accompanist}")
    implementation("com.google.accompanist:accompanist-insets-ui:${accompanist}")
    implementation("com.google.accompanist:accompanist-flowlayout:${accompanist}")

    implementation("androidx.navigation:navigation-compose:${navigation_compose}")

    implementation("io.coil-kt:coil-compose:${coil}")
    implementation("io.coil-kt:coil-gif:${coil}")

    implementation("com.google.android.exoplayer:exoplayer:${exoplayer}")
    implementation("com.google.android.exoplayer:extension-rtmp:${exoplayer}")

    implementation("androidx.media:media:${media}")

    implementation("com.github.heyanLE.EasyPlayer:eplayer-core:${easy_player}")

    implementation("androidx.room:room-runtime:${androidx_room}")
    implementation("androidx.room:room-ktx:${androidx_room}")
    implementation("androidx.room:room-paging:${androidx_room}")
    implementation("androidx.room:room-common:${androidx_room}")

    kapt("androidx.room:room-compiler:${androidx_room}")

    implementation("com.microsoft.appcenter:appcenter-analytics:${app_center}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${app_center}")
    implementation("com.microsoft.appcenter:appcenter-distribute:${app_center}")

    implementation("com.google.code.gson:gson:${gson}")

    implementation("org.jsoup:jsoup:${jsoup}")

    implementation("androidx.webkit:webkit:${androidx_webkit}")

    implementation("org.apache.commons:commons-text:${commons_text}")

    implementation("org.fourthline.cling:cling-core:${cling}")
    implementation("org.fourthline.cling:cling-support:${cling}")
    implementation(project(":easy-crasher"))
    implementation(project(":easy-dlna"))
    implementation(project(":easy-i18n"))
}