
import com.heyanle.buildsrc.Android
import com.heyanle.buildsrc.Config
import com.heyanle.buildsrc.Version
import com.heyanle.buildsrc.implementation
import com.heyanle.buildsrc.project

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
        kotlinCompilerExtensionVersion = "1.4.1"
    }

}



dependencies {


    implementation("com.github.bumptech.glide:glide:${Version.glide}")

    implementation("com.github.heyanLE.okkv2:okkv2-mmkv:${Version.okkv2}")

    implementation("com.squareup.okhttp3:okhttp:${Version.okhttp3}")
    implementation("com.squareup.okhttp3:logging-interceptor:${Version.okhttp3}")

    implementation("androidx.core:core-ktx:${Version.androidx_core_ktx}")
    implementation("androidx.appcompat:appcompat:${Version.androidx_appcompat}")
    implementation("com.google.android.material:material:${Version.google_material}")
    implementation("androidx.activity:activity-ktx:${Version.androidx_activity_ktx}")
    implementation("androidx.fragment:fragment-ktx:${Version.androidx_fragment_ktx}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Version.androidx_lifecycle_runtime_ktx}")

    implementation( "com.squareup.leakcanary:leakcanary-android:${Version.leakcanary}")

    implementation("androidx.paging:paging-runtime-ktx:${Version.paging}")
    implementation("androidx.paging:paging-compose:1.0.0-alpha18")
    implementation("com.github.heyanLE.EasyPlayer:eplayer-core:${Version.easy_player}")

    implementation(platform("androidx.compose:compose-bom:${Version.compose_bom}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")

    debugImplementation(platform("androidx.compose:compose-bom:${Version.compose_bom}"))
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.accompanist:accompanist-navigation-animation:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-pager:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-pager-indicators:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-swiperefresh:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-insets:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-insets-ui:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-flowlayout:${Version.accompanist}")

    implementation("androidx.navigation:navigation-compose:${Version.navigation_compose}")

    implementation("io.coil-kt:coil-compose:${Version.coil}")
    implementation("io.coil-kt:coil-gif:${Version.coil}")

    implementation("com.google.android.exoplayer:exoplayer:${Version.exoplayer}")
    implementation("com.google.android.exoplayer:extension-rtmp:${Version.exoplayer}")

    implementation("androidx.media:media:${Version.media}")

    implementation("com.github.heyanLE.EasyPlayer:eplayer-core:${Version.easy_player}")

    implementation("androidx.room:room-runtime:${Version.androidx_room}")
    implementation("androidx.room:room-ktx:${Version.androidx_room}")
    implementation("androidx.room:room-paging:${Version.androidx_room}")
    implementation("androidx.room:room-common:${Version.androidx_room}")

    kapt("androidx.room:room-compiler:${Version.androidx_room}")

    implementation("com.microsoft.appcenter:appcenter-analytics:${Version.app_center}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${Version.app_center}")
    implementation("com.microsoft.appcenter:appcenter-distribute:${Version.app_center}")

    implementation("com.google.code.gson:gson:${Version.gson}")

    implementation("org.jsoup:jsoup:${Version.jsoup}")

    implementation("androidx.webkit:webkit:${Version.androidx_webkit}")

    implementation("org.apache.commons:commons-text:${Version.commons_text}")

    implementation("org.fourthline.cling:cling-core:${Version.cling}")
    implementation("org.fourthline.cling:cling-support:${Version.cling}")

    implementation(project(":easy-dlna"))
    implementation(project(":easy-crasher"))
    implementation(project(":easy-i18n"))
    implementation(project(":extension:extension-core"))
    implementation(project(":source:source-api"))
    implementation(project(":source:source-utils"))
    implementation(project(":extension:extension-api"))
}