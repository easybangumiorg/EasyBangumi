 生成 kotlin Multiplatform 可用的 BuildConfig 文件

```kotlin
// build.gradle.kts

plugins {
    id("EasyConfig")
}

easyBuildConfig {
    packageName.set(AppConfig.namespace)
    buildConfigFileName.set("EasyConfig")
    sourceDir.set(kotlin.sourceSets.findByName("commonMain")!!.kotlin)
    configProperties {
        "NAMESPACE" with AppConfig.namespace
        "VERSION_CODE" with AppConfig.versionCode
        "VERSION_NAME" with AppConfig.versionName
    }
}
```

生成类

```java
package org.easybangumi.next;

public final class BuildConfig {
  public static final boolean DEBUG = Boolean.parseBoolean("true");
  public static final String APPLICATION_ID = "org.easybangumi.next";
  public static final String BUILD_TYPE = "debug";
  public static final int VERSION_CODE = 1;
  public static final String VERSION_NAME = "1.0.0";
}

```