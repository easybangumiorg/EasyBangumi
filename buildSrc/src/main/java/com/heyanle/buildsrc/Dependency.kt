package com.heyanle.buildsrc

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Created by HeYanLe on 2022/9/10 16:48.
 * https://github.com/heyanLE
 */

object Version {
    const val androidx_activity_ktx = "1.5.0"
    const val androidx_fragment_ktx = "1.5.3"
    const val androidx_core_ktx = "1.9.0"
    const val androidx_appcompat = "1.5.0"
    const val androidx_room = "2.4.2"
    const val google_material = "1.6.1"
    const val jsoup = "1.14.3"
    const val okhttp3 = "4.10.0"
    const val paging = "3.1.1"

    const val androidx_swipe_refresh_layout = "1.0.0"
    const val androidx_preference = "1.2.0"
    const val gson = "2.9.0"
    const val leakcanary = "2.7"
    const val kotlinx_coroutines = "1.6.0"
    const val glide = "4.12.0"
    const val okkv2 = "1.1.0"
    const val junit = "4.13.2"

    const val androidx_lifecycle_runtime_ktx = "2.5.1"

    const val exoplayer = "2.18.1"

    const val easy_player = "2.0"

    const val compose_bom = "2022.10.00"

    const val accompanist = "0.28.0"

    const val navigation_compose = "2.5.3"

    const val compose_runtime = "1.3.2"

    const val coil = "2.2.2"

    const val media3 = "1.0.0-beta03"

    const val media = "1.6.0"

    const val app_center = "4.4.5"
}

// AndroidX basic
const val androidXCoreKtx = "androidx.core:core-ktx:${Version.androidx_core_ktx}"
const val androidXAppCompat = "androidx.appcompat:appcompat:${Version.androidx_appcompat}"
const val googleMaterial = "com.google.android.material:material:${Version.google_material}"
const val androidXAndroidKtx = "androidx.activity:activity-ktx:${Version.androidx_activity_ktx}"
const val androidXFragmentKtx = "androidx.fragment:fragment-ktx:${Version.androidx_fragment_ktx}"
const val androidXLifecycleRuntimeKtx =
    "androidx.lifecycle:lifecycle-runtime-ktx:${Version.androidx_lifecycle_runtime_ktx}"
const val androidXTestExt = "androidx.test.ext:junit:1.1.3"
const val androidXTestEspresso = "androidx.test.espresso:espresso-core:3.4.0"
fun DependencyHandler.androidXBasic() {
    add(implementation, androidXCoreKtx)
    add(implementation, androidXAppCompat)
    add(implementation, googleMaterial)
    //add(implementation, androidXAndroidKtx)
    add(implementation, androidXFragmentKtx)
    add(implementation, androidXLifecycleRuntimeKtx)
    add(testImplementation, androidXTestExt)
    add(testImplementation, androidXTestEspresso)
}

// AndroidX room
const val androidXRoomRuntime = "androidx.room:room-runtime:${Version.androidx_room}"
const val androidXRoomKtx = "androidx.room:room-ktx:${Version.androidx_room}"
const val androidXRoomPaging = "androidx.room:room-paging:${Version.androidx_room}"
const val androidXRoomCompiler = "androidx.room:room-compiler:${Version.androidx_room}"
const val androidXRoomCommon = "androidx.room:room-common:${Version.androidx_room}"

fun DependencyHandler.room() {
    add(implementation, androidXRoomRuntime)
    add(implementation, androidXRoomKtx)
    add(kapt, androidXRoomCompiler)
}

fun DependencyHandler.roomPaging() {
    add(implementation, androidXRoomPaging)
}

fun DependencyHandler.roomCommon() {
    add(implementation, androidXRoomCommon)
}

const val junit = "junit:junit:${Version.junit}"
fun DependencyHandler.junit() {
    add(testImplementation, junit)
}

// jsoup
const val jsoup = "org.jsoup:jsoup:${Version.jsoup}"
fun DependencyHandler.jsoup() {
    add(implementation, jsoup)
}

// okhttp3
const val okhttp3 = "com.squareup.okhttp3:okhttp:${Version.okhttp3}"
fun DependencyHandler.okhttp3() {
    add(implementation, okhttp3)
}

// paging
const val paging = "androidx.paging:paging-runtime-ktx:${Version.paging}"
fun DependencyHandler.paging() {
    add(implementation, paging)
}

fun DependencyHandler.pagingCompose() {
    implementation("androidx.paging:paging-compose:1.0.0-alpha17")
}

const val androidXSwipeRefreshLayout =
    "androidx.swiperefreshlayout:swiperefreshlayout:${Version.androidx_swipe_refresh_layout}"

fun DependencyHandler.swipeRefreshLayout() {
    add(implementation, androidXSwipeRefreshLayout)
}

const val androidXPreference = "androidx.preference:preference-ktx:${Version.androidx_preference}"
fun DependencyHandler.preference() {
    add(implementation, androidXPreference)
}

const val gson = "com.google.code.gson:gson:${Version.gson}"
fun DependencyHandler.gson() {
    add(implementation, gson)
}

const val leakcanary = "com.squareup.leakcanary:leakcanary-android:${Version.leakcanary}"
fun DependencyHandler.leakcanary() {
    add(debugImplementation, leakcanary)
}

const val kotlinx_coroutines =
    "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.kotlinx_coroutines}"

fun DependencyHandler.coroutines() {
    add(implementation, kotlinx_coroutines)
}

const val glide = "com.github.bumptech.glide:glide:${Version.glide}"
fun DependencyHandler.glide() {
    add(implementation, glide)
}

const val okkv2 = "com.github.heyanLE.okkv2:okkv2-mmkv:${Version.okkv2}"
fun DependencyHandler.okkv2() {
    add(implementation, okkv2)
}

const val exoplayer = "com.google.android.exoplayer:exoplayer:${Version.exoplayer}"
fun DependencyHandler.exoplayer() {
    add(implementation, exoplayer)
}

const val exoplayer_rtmp = "com.google.android.exoplayer:extension-rtmp:${Version.exoplayer}"
fun DependencyHandler.exoplayerRtmp() {
    add(implementation, exoplayer_rtmp)
}

const val easy_player = "com.github.heyanLE.EasyPlayer:eplayer-core:${Version.easy_player}"
fun DependencyHandler.easyPlayer() {
    add(implementation, easy_player)
}

fun DependencyHandler.easyPlayerExo() {
    implementation("com.github.heyanLE.EasyPlayer:eplayer-exo:${Version.easy_player}")
}

const val composeBom = "androidx.compose:compose-bom:${Version.compose_bom}"
const val composeUI = "androidx.compose.ui:ui"
const val composeUIGraphics = "androidx.compose.ui:ui-graphics"
const val composeUIToolingPreview = "androidx.compose.ui:ui-tooling-preview"
const val composeMaterial3 = "androidx.compose.material3:material3"

const val composeUITestJunit4 = "androidx.compose.ui:ui-test-junit4"
const val composeUITooling = "androidx.compose.ui:ui-tooling"
const val composeUITestManifest = "androidx.compose.ui:ui-test-manifest"
fun DependencyHandler.compose() {
    add(implementation, platform(composeBom))
    add(implementation, composeUI)
    add(implementation, composeUIGraphics)
    add(implementation, composeUIToolingPreview)
    add(implementation, composeMaterial3)

    add(androidTestImplementation, platform(composeBom))
    add(androidTestImplementation, composeUITestJunit4)
    add(debugImplementation, composeUITooling)
    add(debugImplementation, composeUITestManifest)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.compose.runtime:runtime-livedata:${Version.compose_runtime}")
}

fun DependencyHandler.accompanist() {
    implementation("com.google.accompanist:accompanist-navigation-animation:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-pager:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-pager-indicators:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-swiperefresh:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-insets:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-insets-ui:${Version.accompanist}")
    implementation("com.google.accompanist:accompanist-flowlayout:${Version.accompanist}")
}

fun DependencyHandler.navigationCompose() {
    implementation("androidx.navigation:navigation-compose:${Version.navigation_compose}")
}

fun DependencyHandler.coil() {
    implementation("io.coil-kt:coil-compose:${Version.coil}")
}

fun DependencyHandler.coilGif() {
    implementation("io.coil-kt:coil-gif:${Version.coil}")
}

fun DependencyHandler.media() {
    implementation("androidx.media:media:${Version.media}")
}

fun DependencyHandler.media3Exo() {
    // For media playback using ExoPlayer
    implementation("androidx.media3:media3-exoplayer:${Version.media3}")

    // For DASH playback support with ExoPlayer
    implementation("androidx.media3:media3-exoplayer-dash:${Version.media3}")
    // For HLS playback support with ExoPlayer
    implementation("androidx.media3:media3-exoplayer-hls:${Version.media3}")
    // For RTSP playback support with ExoPlayer
    implementation("androidx.media3:media3-exoplayer-rtsp:${Version.media3}")
    implementation("androidx.media3:media3-datasource-rtmp:${Version.media3}")
    // For building media playback UIs
    implementation("androidx.media3:media3-ui:${Version.media3}")
}

fun DependencyHandler.appCenter() {
    implementation("com.microsoft.appcenter:appcenter-analytics:${Version.app_center}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${Version.app_center}")
    implementation("com.microsoft.appcenter:appcenter-distribute:${Version.app_center}")
}
