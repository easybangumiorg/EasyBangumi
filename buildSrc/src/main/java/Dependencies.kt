
/**
 * Created by HeYanLe on 2022/6/5 15:27.
 * https://github.com/heyanLE
 */
object Dependencies {
    object Version {
        const val room = "2.4.2"
        const val jsoup = "1.14.3"
        const val okhttp3 = "5.0.0-alpha.2"
        const val glide = "4.12.0"
        const val okkv2 = "1.1.0"
        const val android_core = "1.8.0"
        const val material = "1.6.1"
        const val kotlinx_coroutines = "1.6.0"
        const val appcompat = "1.4.2"
        const val gson = "2.9.0"
        const val leakcanary = "2.7"
        const val jzvd = "7.7.0"
        const val paging = "3.1.1"
        const val exoplayer = "2.17.1"
        const val flexbox = "3.0.0"
        const val swipe_refresh_layout = "1.0.0"
        const val preference = "1.2.0"
    }

    val project = listOf<String>(
    )

    val classpath = listOf<String>(

    )

    val implementation = listOf<String>(
        "androidx.preference:preference-ktx:${Version.preference}",
        "androidx.swiperefreshlayout:swiperefreshlayout:${Version.swipe_refresh_layout}",
        "com.google.android.flexbox:flexbox:${Version.flexbox}",
        "com.google.android.exoplayer:exoplayer:${Version.exoplayer}",
        "androidx.paging:paging-runtime-ktx:${Version.paging}",
        "androidx.core:core-ktx:${Version.android_core}",
        "cn.jzvd:jiaozivideoplayer:${Version.jzvd}",
        "androidx.room:room-runtime:${Version.room}",
        "androidx.room:room-ktx:${Version.room}",
        "androidx.room:room-paging:${Version.room}",
        "org.jsoup:jsoup:${Version.jsoup}",
        "com.squareup.okhttp3:okhttp:${Version.okhttp3}",
        "com.google.android.material:material:${Version.material}",
        "androidx.appcompat:appcompat:${Version.appcompat}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.kotlinx_coroutines}",
        "com.github.bumptech.glide:glide:${Version.glide}",
        "com.google.code.gson:gson:${Version.gson}",
        "com.github.heyanLE.okkv2:okkv2-mmkv:${Version.okkv2}",
    )

    val ksp = listOf<String>(

    )

    val kapt = listOf<String>(
        "androidx.room:room-compiler:${Version.room}"
    )

    val testImplementation = listOf<String>(
        "junit:junit:4.13.2"
    )

    val androidTestImplementation = listOf<String>(
        "androidx.test.ext:junit:1.1.3",
        "androidx.test.espresso:espresso-core:3.4.0"
    )

    val debugImplementation = listOf<String>(
        "com.squareup.leakcanary:leakcanary-android:${Version.leakcanary}",
    )


}