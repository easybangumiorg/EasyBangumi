# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 本体
-keep class com.heyanle.**{*;}
-keep interface com.heyanle.**{*;}

# 协程

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
-keep class kotlinx.coroutines.** {*;}
# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class com.tencent.mmkv.** {*;}

-keep class org.apache.commons.** {*;}

# okhttp
-keep class okhttp3.** {*;}

# jsoup
-keep class org.jsoup.** {*;}

# jsoup
-keep class com.google.gson.** {*;}

# aria
-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria.**{*;}
-keep class **$$DownloadListenerProxy{ *; }
-keep class **$$UploadListenerProxy{ *; }
-keep class **$$DownloadGroupListenerProxy{ *; }
-keep class **$$DGSubListenerProxy{ *; }
-keepclasseswithmembernames class * {
    @Download.* <methods>;
    @Upload.* <methods>;
    @DownloadGroup.* <methods>;
}

# m3u8 ffmepg lib
-keep class com.jeffmony.** {*;}

## androidx
#-keep class androidx.compose.** {*;}

-dontwarn javax.script.ScriptEngine
-dontwarn javax.script.ScriptEngineManager

-dontwarn javax.enterprise.context.ApplicationScoped
-dontwarn javax.enterprise.inject.Alternative
-dontwarn javax.inject.Inject
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

-dontwarn androidx.window.extensions.area.ExtensionWindowAreaPresentation
#-dontwarn androidx.window.extensions.WindowExtensions
#-dontwarn androidx.window.extensions.WindowExtensionsProvider
#-dontwarn androidx.window.extensions.area.ExtensionWindowAreaStatus
#-dontwarn androidx.window.extensions.area.WindowAreaComponent
#-dontwarn androidx.window.extensions.embedding.ActivityEmbeddingComponent
#-dontwarn androidx.window.extensions.embedding.ActivityRule$Builder
#-dontwarn androidx.window.extensions.embedding.ActivityRule
#-dontwarn androidx.window.extensions.embedding.ActivityStack
#-dontwarn androidx.window.extensions.embedding.EmbeddingRule
#-dontwarn androidx.window.extensions.embedding.SplitAttributes$Builder
#-dontwarn androidx.window.extensions.embedding.SplitAttributes$SplitType$ExpandContainersSplitType
#-dontwarn androidx.window.extensions.embedding.SplitAttributes$SplitType$HingeSplitType
#-dontwarn androidx.window.extensions.embedding.SplitAttributes$SplitType$RatioSplitType
#-dontwarn androidx.window.extensions.embedding.SplitAttributes$SplitType
#-dontwarn androidx.window.extensions.embedding.SplitAttributes
#-dontwarn androidx.window.extensions.embedding.SplitAttributesCalculatorParams
#-dontwarn androidx.window.extensions.embedding.SplitInfo
#-dontwarn androidx.window.extensions.embedding.SplitPairRule$Builder
#-dontwarn androidx.window.extensions.embedding.SplitPairRule
#-dontwarn androidx.window.extensions.embedding.SplitPlaceholderRule$Builder
#-dontwarn androidx.window.extensions.embedding.SplitPlaceholderRule
#-dontwarn androidx.window.extensions.layout.DisplayFeature
#-dontwarn androidx.window.extensions.layout.FoldingFeature
#-dontwarn androidx.window.extensions.layout.WindowLayoutComponent
#-dontwarn androidx.window.extensions.layout.WindowLayoutInfo
#-dontwarn androidx.window.sidecar.SidecarDeviceState
#-dontwarn androidx.window.sidecar.SidecarDisplayFeature
#-dontwarn androidx.window.sidecar.SidecarInterface$SidecarCallback
#-dontwarn androidx.window.sidecar.SidecarInterface
#-dontwarn androidx.window.sidecar.SidecarProvider
#-dontwarn androidx.window.sidecar.SidecarWindowLayoutInfo

