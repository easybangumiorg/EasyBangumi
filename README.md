纯纯看番 KMP 项目，初步计划支持 Android, Desktop
## 代码结构

* 总体结构

```
[APP 框架] -> [Component 框架] -> [Utils 框架] -> [Base 框架]
```

* Android 

```
AppAndroid
    ↓
AppShared(androidMain & commonMain)
    ↓
[Component1 Component2 ...]
    ↓
Uills(utilsAndroid & utilsJvm)
    ↓
Base(only commonMain)
```

* Desktop

```
AppDesktop
    ↓
AppShared(desktopMain & commonMain)
    ↓
[Component1 Component2 ...]
    ↓
Uills(utilsJvm)
    ↓
Base(only commonMain)

```

### APP

APP 作为整个项目的顶层框架，其分为三个模块

* AndroidApp: app/android
* DesktopApp: app/desktop
* SharedApp: app/shared
* (IosApp 待支持)

#### AndroidAPP

纯安卓模块，其代码环境为 Android 环境。  
依赖 SharedAPP 模块。  
虽然理论上也有 JVM 环境但是不依赖 DesktopAPP 模块。  
如果要写 JVM 公共代码需要依赖后面的 Utils 框架。

#### DesktopAPP

纯 Kotlin 模块，其代码环境为 JVM 环境。  
依赖 SharedAPP 模块。

#### SharedApp

KotlinMultiplatform 模块。  
除了 iosMain 之外尽量不要添加其他平台的代码。  
相关代码写到 commonMain 里，为 KotlinCommon 环境。  
Compose 业务入口位于 SharedApp  

### Component

Component 为业务框架，根据业务需求自行添加 kotlinMultiplatform 模块。  
这里尽量不要添加纯净模块，保证每个模块都能被其他任何类型的模块一类。  
其内允许模块间互相依赖，但是不允许循环依赖。  

#### Component-Room

Room 模块，提供数据库支持。

### Utils

Utils 框架主要提供一系列工具，这里分为以下两个模块

* UtilsAndroid: Android 环境的纯净模块 （可以打包成 aar）
* UtilsJvm: JVM 环境的纯净模块（可以打包成 jar）

并且与 APP 框架不同的是这里的 UtilsAndroid 模块依赖 UtilsJvm 模块。  
后续如果需要支持 ios 可以添加 UtilsIos 模块和 UtilsCommon 模块。

### Base

Base 模块主要为了提供 Component 框架反向依赖 APP 框架的支持。  
一般为基础工具或者依赖 APP 框架的编译参数等，具体为接口位于 Base 但是实现位于 APP 框架。  
虽然为 kotlinMultiplatform 模块，但是规定只允许使用 commonMain 。  
接口注入直接使用 koin 注入。

```kotlin
val logger = koin.get<Logger>()
val logger = koin.getOrNull<Logger>()

// lazy
val logger by koin.inject<Logger>()
```

添加接口方式：
1. 直接在 com.heyanle.easy_bangumi_cm.base 包下添加接口
2. 在 BaseFactory 中添加新接口的工厂
3. 在 APP 框架中实现接口（只要添加能看到 APP 框架报错位置）

## 编译基建

## 业务基建

### 资源管理

### 日志

## 业务

### 插件化

### 播放器






