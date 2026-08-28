# 本地性能版

`performance` 是用于本地性能验证的 release 等价构建。它保留 R8 压缩与优化，关闭调试能力，并使用本地 debug keystore 签名，因此不依赖发布 CI 即可构建和安装。

## 与其他版本的区别

| 项目 | 性能版 |
| --- | --- |
| Gradle 变体 | `performance` |
| 包名 | `com.heyanle.easybangumi4.performance` |
| 软件名 | `纯纯看番 性能版` |
| 版本名后缀 | `-performance` |
| 签名 | 本地 debug keystore |
| R8 | 开启，与 release 一致 |
| `debuggable` | 关闭 |
| shell profiling | 开启，可供 adb、Perfetto 和 Macrobenchmark 采样 |
| 崩溃收集 | 关闭 |

性能版有独立的橙色测速图标，可以与正式版和 Debug 版同时安装。它只用于本地验证，不应上传或用于正式发布。

## 构建与安装

```shell
./gradlew :app:assemblePerformance
```

APK 输出到：

```text
app/build/outputs/apk/performance/app-performance.apk
```

连接测试设备后可直接安装：

```shell
./gradlew :app:installPerformance
```

也可以确认设备上的变体身份：

```shell
adb shell dumpsys package com.heyanle.easybangumi4.performance
```

## 测量约定

- 性能结论只使用 `performance`，不要使用 Debug 包。
- 冷启动测试前先强制停止应用；滚动测试使用相同账号、数据和图片缓存状态。
- 每条用户路径至少重复 10 次，记录中位数以及 P95/P99，而不是只看单次结果。
- 对比变更前后时使用同一台设备、相同刷新率与温度状态。
