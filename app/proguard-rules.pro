# EasyBangumi R8/ProGuard rules
#
# 策略说明（2025 性能优化）：
# 1. -dontoptimize 已移除：此前整体关闭了 R8 优化（无内联/无死代码消除），仅保留混淆。
# 2. 「com.heyanle.**」全量 keep 暂时保留：Rhino JS 扩展源通过 LiveConnect 反射调用
#    Java 方法（见 JSComponentBundle 放入 scope 的 helper 对象），且 Gson/Moshi 按字段名
#    解析 com.heyanle 下的实体类；第三方 JS 脚本可能 importClass 任意 app 类。
#    后续如需收窄，必须配合真机运行全部番源脚本回归验证。
# 3. okhttp3 / gson / jsoup / kotlinx.coroutines / kotlin-stdlib 自带 consumer rules，
#    此前的全量 keep 会连带关闭对这些库的优化，已移除（仅保留 ServiceLoader 等必要项）。
# 4. 文件此前存在三段完全重复的规则块，已合并去重。

# ---------------------------------------------------------------- 本体（Rhino/Gson 反射依赖，勿动）
-keep class com.heyanle.** {*;}
-keep interface com.heyanle.** {*;}

# ---------------------------------------------------------------- JS 引擎（Rhino LiveConnect 反射）
-keep class org.mozilla.javascript.** {*;}
-dontwarn javax.script.ScriptEngine
-dontwarn javax.script.ScriptEngineManager

# JS 番源脚本的 LiveConnect 反射面（实证：inner_source 的 js 直接调用
# okhttpHelper.client.newCall(...).execute()、Headers.entrySet()、Jsoup.parse(...)，
# 2025-08 真机 R8 实测删掉后报 EcmaError: Cannot find function newCall in okhttp3.z）
-keep class okhttp3.** {*;}
-keep interface okhttp3.** {*;}
-keep class org.jsoup.** {*;}
-keep interface org.jsoup.** {*;}

# Moshi KotlinJsonAdapterFactory 依赖 kotlin-reflect，其内部实现不可被优化
-keep class kotlin.reflect.jvm.internal.** { *; }

# ---------------------------------------------------------------- 第三方库
# bugly
-keep public class com.tencent.bugly.**{*;}
-dontwarn com.tencent.bugly.**

# cybergarage (UPnP)
-keep class org.cybergarage.**{*;}
-keep interface org.cybergarage.**{*;}

# mmkv
-keep class com.tencent.mmkv.** {*;}

# commons
-keep class org.apache.commons.**{*;}
-keep interface org.apache.commons.**{*;}

# aria 下载（注解处理器生成代理，反射回调）
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

# m3u8 ffmpeg lib
-keep class com.jeffmony.** {*;}

# slf4j
-keep class org.slf4j.impl.StaticLoggerBinder { *; }
-keep class org.slf4j.impl.StaticMDCBinder  { *; }
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder

# 历史遗留（lib_signal 已不在依赖中，规则保留以防回退）
-keep class com.pika.lib_signal.** {*;}
-keep interface com.pika.lib_signal.** {*;}

# ---------------------------------------------------------------- 序列化 / 系统契约
# Parcelable
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Serializable
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 枚举 values/valueOf
-keepclassmembers enum * {
  public static **[] values();
  public static ** valueOf(java.lang.String);
}

# 资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ---------------------------------------------------------------- Kotlin / 协程
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
# AFU 更新的 volatile 字段名不可被混淆
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ---------------------------------------------------------------- 通用 keepattributes
# 注：此前文件中 "Singature" 为拼写错误（应为 Signature），泛型签名实际未被保留
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*

# ---------------------------------------------------------------- -dontwarn（依赖库引用了非 Android 环境的类）
-dontwarn javax.inject.Qualifier
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
# Rhino 在 JVM 桌面环境的可选依赖（awt/swing），Android 上不存在
-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn javax.swing.**
